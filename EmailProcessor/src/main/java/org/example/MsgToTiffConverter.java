package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;
import javax.imageio.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MsgToTiffConverter {

    public static void main(String[] args) throws Exception {
        String msgFilePath = "path_to_your_msg_file.msg"; // Hardcoded input file path
        String tiffFilePath = "output_path.tiff";

        convertMsgToTiff(msgFilePath, tiffFilePath);
    }

    public static void convertMsgToTiff(String msgFilePath, String tiffFilePath) throws Exception {
        // Extract email content and attachments
        MAPIMessage msg = new MAPIMessage(msgFilePath);
        String emailContent = msg.getHtmlBody();  // Changed from getTextBody to getHtmlBody to preserve HTML formatting

        // Clean up the email HTML content
        Document document = Jsoup.parse(emailContent);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        emailContent = document.html().replace("&nbsp;", "&#160;");

        // Log the cleaned HTML content for analysis
        logHtmlContent(emailContent);

        // Convert email content to an image with proper formatting
        BufferedImage emailImage = renderHtmlToImage(emailContent);

        List<AttachmentChunks> attachments = List.of(msg.getAttachmentFiles());

        // Convert attachments to images in grayscale
        List<BufferedImage> attachmentImages = new ArrayList<>();
        for (AttachmentChunks attachment : attachments) {
            String filename = attachment.getAttachLongFileName().getValue();
            if (filename != null) {
                InputStream attachmentStream = new ByteArrayInputStream(attachment.getAttachData().getValue());
                if (filename.endsWith(".pdf")) {
                    attachmentImages.addAll(convertPdfToGrayImages(attachmentStream));
                } else if (filename.endsWith(".doc")) {
                    attachmentImages.addAll(convertDocToGrayImages(attachmentStream));
                } else if (filename.endsWith(".docx")) {
                    attachmentImages.addAll(convertDocxToGrayImages(attachmentStream));
                } else if (filename.endsWith(".htm") || filename.endsWith(".html")) {
                    attachmentImages.addAll(convertHtmlToGrayImages(attachmentStream));
                }
            }
        }

        // Combine images into a single multi-page TIFF
        List<BufferedImage> allImages = new ArrayList<>();
        allImages.add(emailImage);
        allImages.addAll(attachmentImages);

        saveAsMultiPageTiff(allImages, tiffFilePath);
    }

    private static List<BufferedImage> convertHtmlToGrayImages(InputStream htmlStream) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        // Read the HTML content from the InputStream
        Document document = Jsoup.parse(htmlStream, "UTF-8", "");

        // Clean up and ensure proper closing of tags
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);

        // Replace problematic entities with their numeric equivalents
        String htmlContent = document.html().replace("&nbsp;", "&#160;");

        // Log the cleaned HTML content for analysis
        logHtmlContent(htmlContent);

        // Render the HTML content to an image
        BufferedImage image = renderHtmlToImage(htmlContent);
        images.add(image);

        return images;
    }

    private static void logHtmlContent(String htmlContent) {
        try (PrintWriter out = new PrintWriter("htmlContent.log")) {
            out.println(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage renderHtmlToImage(String htmlContent) {
        // Create a temporary file to store the HTML content
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempHtml", ".html");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(htmlContent);
            writer.close();

            // Log the HTML content before rendering
            logHtmlContent(htmlContent);

            // Render the HTML content to an image
            Java2DRenderer renderer = new Java2DRenderer(tempFile, 800, 1000);
            BufferedImage img = renderer.getImage();

            // Convert the image to grayscale
            BufferedImage grayImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayImage.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();

            return grayImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static List<BufferedImage> convertPdfToGrayImages(InputStream pdfStream) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        PDDocument document = PDDocument.load(pdfStream);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.GRAY);
            images.add(image);
        }
        document.close();
        return images;
    }

    private static List<BufferedImage> convertDocToGrayImages(InputStream docStream) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        byte[] docBytes = inputStreamToByteArray(docStream);

        // Log byte array size
        System.out.println("Byte array size: " + docBytes.length);

        // Print a portion of the byte array as a string to verify content
        String contentSnippet = new String(docBytes, 0, Math.min(docBytes.length, 1000));
        System.out.println("Input stream content snippet: " + contentSnippet);

        // Directly render the content snippet to an image
        if (!contentSnippet.trim().isEmpty()) {
            images.add(renderTextToImage(contentSnippet));
        } else {
            System.out.println("Content snippet is empty.");
        }

        return images;
    }

    private static List<BufferedImage> convertDocxToGrayImages(InputStream docxStream) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(docxStream)) {
            StringBuilder textBuilder = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> textBuilder.append(paragraph.getText()).append("\n"));

            // Render the entire document content to an image
            images.add(renderTextToImage(textBuilder.toString()));
        } catch (Exception e) {
            System.out.println("Error processing DOCX file: " + e.getMessage());
        }
        return images;
    }

    private static int findWrapPosition(String line, FontMetrics fm, int maxLineWidth) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            if (fm.stringWidth(line.substring(0, i)) > maxLineWidth) {
                return i - 1;
            }
        }
        return len;
    }

    private static BufferedImage renderTextToImage(String text) {
        int width = 800;
        int height = 1000;
        int padding = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setPaint(Color.BLACK);
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int y = padding + lineHeight;
        int maxLineWidth = width - 2 * padding;

        for (String line : text.split("\n")) {
            while (line.length() > 0) {
                int len = fm.stringWidth(line) <= maxLineWidth ? line.length() : findWrapPosition(line, fm, maxLineWidth);
                g2d.drawString(line.substring(0, len), padding, y);
                line = line.substring(len).trim();
                y += lineHeight;
            }
        }

        g2d.dispose();
        return image;
    }

    private static IIOMetadata getMetadata(ImageWriter writer, BufferedImage image) throws IOException {
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(image.getType());

        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
            return metadata;
        }
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
        IIOMetadataNode compression = new IIOMetadataNode("Compression");
        IIOMetadataNode compressionTypeName = new IIOMetadataNode("CompressionTypeName");
        compressionTypeName.setAttribute("value", "LZW");
        compression.appendChild(compressionTypeName);
        root.appendChild(compression);

        metadata.setFromTree(metaFormatName, root);

        return metadata;
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static void saveAsMultiPageTiff(List<BufferedImage> images, String tiffFilePath) throws IOException {
        ImageWriter writer = null;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("tiff");
        if (iter.hasNext()) {
            writer = iter.next();
        }
        ImageOutputStream ios = ImageIO.createImageOutputStream(new File(tiffFilePath));
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType("LZW");  // Use LZW compression

        writer.prepareWriteSequence(null);

        for (BufferedImage image : images) {
            writer.writeToSequence(new IIOImage(image, null, null), param);
        }
        writer.endWriteSequence();
        ios.close();
    }
}
