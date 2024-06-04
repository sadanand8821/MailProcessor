package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;

import javax.imageio.*;
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
        String emailContent = msg.getTextBody();
        List<AttachmentChunks> attachments = List.of(msg.getAttachmentFiles());

        // Convert email content to an image with proper formatting
        BufferedImage emailImage = renderTextToImage(emailContent);

        // Convert attachments to images in grayscale
        List<BufferedImage> attachmentImages = new ArrayList<>();
        for (AttachmentChunks attachment : attachments) {
            String filename = attachment.getAttachLongFileName().getValue();
            if (filename != null && filename.endsWith(".pdf")) {
                InputStream attachmentStream = new ByteArrayInputStream(attachment.getAttachData().getValue());
                attachmentImages.addAll(convertPdfToGrayImages(attachmentStream));
            }
        }

        // Combine images into a single multi-page TIFF
        List<BufferedImage> allImages = new ArrayList<>();
        allImages.add(emailImage);
        allImages.addAll(attachmentImages);

        saveAsMultiPageTiff(allImages, tiffFilePath);
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

    private static int findWrapPosition(String line, FontMetrics fm, int maxLineWidth) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            if (fm.stringWidth(line.substring(0, i)) > maxLineWidth) {
                return i - 1;
            }
        }
        return len;
    }

    private static List<BufferedImage> convertPdfToGrayImages(InputStream pdfStream) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        PDDocument document = PDDocument.load(pdfStream);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 600, ImageType.GRAY);  // Increased DPI
            BufferedImage cleanedImage = cleanImage(image);
            images.add(cleanedImage);
        }
        document.close();
        return images;
    }

    private static BufferedImage cleanImage(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;
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
            writer.writeToSequence(new IIOImage(image, null, getMetadata(writer, image)), param);
        }
        writer.endWriteSequence();
        ios.close();
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
}
