import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.binary.XSSFBParser;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFBSharedStringsTable;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.InputStream;

public class XLSBParserExample {
    public static void main(String[] args) {
        try (InputStream is = new FileInputStream("path/to/your/file.xlsb")) {
            OPCPackage pkg = OPCPackage.open(is);
            XSSFBReader reader = new XSSFBReader(pkg);

            XSSFBStylesTable styles = reader.getXSSFBStylesTable();
            XSSFBSharedStringsTable sst = reader.getSharedStringsTable();

            for (InputStream sheetStream : reader.getSheetsData()) {
                printSheet(styles, sst, sheetStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printSheet(XSSFBStylesTable styles, XSSFBSharedStringsTable sst, InputStream sheetStream) {
        try {
            XSSFBParser parser = new XSSFBParser(styles, sst, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
                    if ("c".equals(qName)) {
                        String cellReference = attributes.getValue("r");
                        String cellValue = attributes.getValue("v");
                        System.out.println(cellReference + " - " + cellValue);
                    }
                }
            });

            parser.parse(sheetStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
