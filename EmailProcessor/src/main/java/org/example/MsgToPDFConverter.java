package org.example;

import com.lowagie.text.DocumentException;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.w3c.tidy.Tidy;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;

public class MsgToPDFConverter {

    public static void main(String[] args) throws Exception {
        String msgFilePath = "path_to_your_msg_file.msg"; // Hardcoded input file path
        String emlFilePath = msgFilePath + ".eml";
        String pdfFilePath = msgFilePath + ".pdf";

        // Convert .msg to .eml
        convertMsgToEml(msgFilePath, emlFilePath);

        // Convert .eml to .html, clean it, and then convert to .pdf
        convertEmlToPdf(emlFilePath, pdfFilePath);
    }

    public static void convertMsgToEml(String msgFilePath, String emlFilePath) throws Exception {
        File msgFile = new File(msgFilePath);
        MsgConvert inst = new MsgConvert(msgFile);
        MimeMessage mimeMessage = inst.toEmailMime();

        try (FileOutputStream fos = new FileOutputStream(emlFilePath)) {
            mimeMessage.writeTo(fos);
        }
    }

    public static void convertEmlToPdf(String emlFilePath, String pdfFilePath) throws Exception {
        // Read .eml file and save as .html
        File emlFile = new File(emlFilePath);
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props, null);
        MimeMessage mimeMessage = new MimeMessage(session, new FileInputStream(emlFile));

        String htmlFilePath = emlFilePath + ".html";
        saveMimeMessageAsHtml(mimeMessage, htmlFilePath);

        // Clean the HTML and convert to XHTML
        String xhtmlFilePath = htmlFilePath.replace(".html", ".xhtml");
        cleanHtml(htmlFilePath, xhtmlFilePath);

        // Convert XHTML to PDF
        createPdf(xhtmlFilePath, pdfFilePath);
    }

    public static void saveMimeMessageAsHtml(MimeMessage mimeMessage, String filename) throws Exception {
        try (FileOutputStream os = new FileOutputStream(filename)) {
            mimeMessage.writeTo(os);
        }
    }

    public static void cleanHtml(String inputHtmlPath, String outputXhtmlPath) {
        try (InputStream in = new FileInputStream(inputHtmlPath);
             OutputStream out = new FileOutputStream(outputXhtmlPath)) {

            Tidy tidy = new Tidy();
            tidy.setQuiet(false);
            tidy.setShowWarnings(true);
            tidy.setShowErrors(0);
            tidy.setMakeClean(true);
            tidy.setForceOutput(true);
            tidy.parseDOM(in, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createPdf(String xhtmlFilePath, String pdfFilePath) throws IOException {
        try (OutputStream os = new FileOutputStream(pdfFilePath);
             PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700); // Set the position where you want to start rendering
                contentStream.showText("Hello, PDFBox!");
                contentStream.endText();
            }

            document.save(os);
        }
    }

    // MsgConvert class and related methods

    static class MsgConvert {
        private final File msgFile;
        private MimeMessage mimeMessage;
        private boolean bogusFrom;
        private boolean bogusTo;
        private boolean sending;
        private HtmlEmail mimeBuilder;
        private InternetHeaders parsedHeaders;
        private String forwardFrom;
        private String forwardTo;
        private final List<Map.Entry<String, String>> headers = new ArrayList<>();
        private static final String BOUNCE_ADDRESS = "bounce@domain.com";
        private static final Set<String> SKIPHEADERS = new HashSet<>(Arrays.asList("HEADER1", "HEADER2"));
        private static final String GATE1 = "gate1.domain.com";
        private static final String DEFAULT_DOMAIN = "domain.com";

        public MsgConvert(File msgFile) throws MessagingException, IOException {
            this.msgFile = msgFile;
            Session session = Session.getDefaultInstance(new Properties());
            this.mimeMessage = new MimeMessage(session, new FileInputStream(msgFile));
        }

        private void addHeaderField(String name, String value) {
            if (value != null && !value.isEmpty()) {
                headers.add(new AbstractMap.SimpleEntry<>(name, value));
            }
        }

        private void copyHeaderData(MimeMessage mimeMessage) throws MessagingException {
            if (bogusFrom) {
                mimeMessage.removeHeader("From");
            }
            if (bogusTo) {
                mimeMessage.removeHeader("To");
            }
            List<Header> reverseHeaders = new ArrayList<>();
            if (parsedHeaders != null) {
                if (sending) {
                    parsedHeaders.setHeader("Return-Path", BOUNCE_ADDRESS);
                }
                Enumeration<?> enu = parsedHeaders.getAllHeaders();
                while (enu.hasMoreElements()) {
                    Header header = (Header) enu.nextElement();
                    String name = header.getName();
                    String value = header.getValue();
                    if ("Date".equalsIgnoreCase(name)) {
                        mimeMessage.setHeader("Date", value);
                    } else if ("Received".equalsIgnoreCase(name) || "Return-Path".equalsIgnoreCase(name)) {
                        reverseHeaders.add(header);
                    } else if (!SKIPHEADERS.contains(name.toUpperCase())) {
                        addHeaderField(name, value);
                    }
                }
            }
            for (Map.Entry<String, String> entry : headers) {
                mimeMessage.addHeader(entry.getKey(), entry.getValue());
            }
            for (int i = reverseHeaders.size() - 1; i >= 0; i--) {
                Header h = reverseHeaders.get(i);
                mimeMessage.addHeader(h.getName(), h.getValue());
            }
        }

        private void parseHeaders() throws MessagingException {
            String sHeaders = mimeMessage.getHeader("ALL", null);
            if (sHeaders == null) {
                return;
            }
            parsedHeaders = new InternetHeaders(new ByteArrayInputStream(sHeaders.getBytes()));
        }

        private void prepareBuilder() throws EmailException {
            mimeBuilder = new HtmlEmail();
        }

        public MimeMessage toEmailMime() throws MessagingException, IOException, EmailException {
            parseHeaders();
            prepareBuilder();
            copyHeaderData(mimeMessage);
            return mimeMessage;
        }
    }
}