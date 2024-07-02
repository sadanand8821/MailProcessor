package org.example.files;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class XLSBStreamingTableReader extends AbstractWorkbookTableReader {

    private static final Logger LOG = LoggerFactory.getLogger(XLSBStreamingTableReader.class);
    final RowReader rowReader;
    final OPCPackage opcPackage;
    final String worksheetName;
    final XSSFBReader xssfReader;
    InputStream sheetInputStream;

    public XLSBStreamingTableReader(Path path, String worksheetName, RowReader rowReader, boolean hasHeaders)
            throws TableReaderException {
        super(hasHeaders);
        try {
            this.opcPackage = OPCPackage.open(path.toFile(), PackageAccess.READ);
            this.xssfReader = new XSSFBReader(opcPackage);
        } catch (IOException | OpenXML4JException e) {
            throw new TableReaderException("Bad XML", e);
        }
        this.worksheetName = worksheetName;
        this.rowReader = rowReader;
    }

    public void parse() throws TableReaderException {
        XSSFBSharedStringsTable sharedStringsTable;
        XSSFBStylesTable stylesTable;
        try {
            sharedStringsTable = new XSSFBSharedStringsTable(opcPackage);
            stylesTable = xssfReader.getXSSFBStylesTable();
        } catch (IOException | SAXException e) {
            throw new TableReaderException("Bad styles table", e);
        }
        setSheetInputStream(worksheetName, xssfReader);

        try {
            DataFormatter formatter = new DataFormatter();
            XSSFBSheetHandler xssfbSheetHandler = new XSSFBSheetHandler(sheetInputStream, stylesTable, null, sharedStringsTable, new RowWrapper(rowReader), formatter, false);
            xssfbSheetHandler.parse();
        } catch (PleaseStopReadingException e) {
            //ok, we'll stop reading
        } catch (Exception e) {
            throw new TableReaderException("Problem parsing sheet", e);
        }
    }

    private void setSheetInputStream(String worksheetName, XSSFReader xssfReader)
            throws TableReaderException {
        int index = 0;
        XSSFReader.SheetIterator iter;
        try {
            iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        } catch (IOException | InvalidFormatException e) {
            throw new TableReaderException("Bad xml", e);
        }

        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String sheetName = iter.getSheetName();
            if (index == 0 && StringUtils.isBlank(worksheetName)) {
                sheetInputStream = new BufferedInputStream(stream);
                break;
            } else if (worksheetName.equals(sheetName)) {
                sheetInputStream = new BufferedInputStream(stream);
                break;
            }
            index++;
        }

        if (worksheetName == null && index > 1) {
            LOG.warn("No work sheet name and more than one worksheet. " +
                    "I'm defaulting to trying to read the first worksheet");
        }
        if (sheetInputStream == null) {
            List<String> worksheets = getSheets();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String w : worksheets) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(w);
            }
            throw new TableReaderException("I'm sorry, but I couldn't find a worksheet named \"" +
                    worksheetName + "\", but I did see: " + sb.toString());
        }
    }

    public void close() throws Exception {
        if (sheetInputStream != null) {
            sheetInputStream.close();
        }
        if (opcPackage != null) {
            opcPackage.close();
        }
    }

    public List<String> getSheets() throws TableReaderException {
        XSSFReader.SheetIterator iter;
        try {
            iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        } catch (IOException | InvalidFormatException e) {
            throw new TableReaderException("Bad XML", e);
        }

        List<String> worksheets = new ArrayList<>();
        while (iter.hasNext()) {
            iter.next();
            worksheets.add(iter.getSheetName());
        }

        return worksheets;
    }

    private class RowWrapper implements XSSFSheetXMLHandler.SheetContentsHandler {

        final RowReader rowReader;
        final Map<String, String> buffer = new TreeMap<>();
        int rowsProcessed = 0;
        List<String> headers = new ArrayList<>();
        boolean collectHeaders = false;

        public RowWrapper(RowReader reader) {
            this.rowReader = reader;
        }

        @Override
        public void startRow(int i) {
            if (rowsProcessed == 0 && getHasHeaders()) {
                collectHeaders = true;
            }
            //for now we don't care about empty rows
        }

        @Override
        public void endRow(int rowIndex) {
            if (rowsProcessed++ == 0) {
                collectHeaders = false;
                if (getHasHeaders()) {
                    List<String> copy = new ArrayList<>(headers);
                    rowReader.setHeaders(Collections.unmodifiableList(copy));
                    printHeaders();
                    return;
                } else {
                    for (int i = 0; i < buffer.size(); i++) {
                        headers.add(getNonHeaderLabel(i));
                    }
                    rowReader.setHeaders(headers);
                }
            }

            try {
                boolean keepGoing = rowReader.process(buffer);
                if (!keepGoing) {
                    throw new PleaseStopReadingException();
                }
                printRow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buffer.clear();
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment xssfComment) {
            CellReference cr = new CellReference(cellReference);
            if (collectHeaders) {
                for (int i = headers.size(); i < cr.getCol(); i++) {
                    headers.add(getNonHeaderLabel(i));
                }
                headers.add(formattedValue);
                return;
            }

            if (cr.getCol() < headers.size()) {
                String header = headers.get(cr.getCol());
                buffer.put(header, formattedValue);
            } else {
                LOG.warn("Column with index (" + cr.getCol() + ") is not within header range.  I'm skipping it");
            }
        }

        @Override
        public void headerFooter(String s, boolean b, String s1) {
            // Handle header and footer if necessary
        }

        private void printHeaders() {
            for (String header : headers) {
                System.out.print(header + "\t");
            }
            System.out.println();
        }

        private void printRow() {
            for (String header : headers) {
                String value = buffer.get(header);
                System.out.print((value != null ? value : "") + "\t");
            }
            System.out.println();
        }

        public List<String> headers() {
            return headers;
        }
    }

    private class PleaseStopReadingException extends RuntimeException {

    }
}

class RowReader {
    private List<String> headers;

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public boolean process(Map<String, String> row) throws IOException {
        // Implement your row processing logic here.
        // Print the row for demonstration
        row.forEach((key, value) -> System.out.print(key + ": " + value + "\t"));
        System.out.println();
        // Return false if you want to stop reading the file early.
        return true;
    }
}

abstract class AbstractWorkbookTableReader {
    private final boolean hasHeaders;

    public AbstractWorkbookTableReader(boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
    }

    public boolean getHasHeaders() {
        return hasHeaders;
    }

    protected String getNonHeaderLabel(int index) {
        return "Column " + (index + 1);
    }
}

class TableReaderException extends Exception {
    public TableReaderException(String message) {
        super(message);
    }

    public TableReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}