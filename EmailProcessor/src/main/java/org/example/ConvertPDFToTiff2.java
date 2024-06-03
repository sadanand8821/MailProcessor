package org.example;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import java.io.IOException;
import java.io.File;


public class ConvertPDFToTiff2 {
    public static void convertToTiffs(String inputPdf, String outputDir) {
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs(); // Create the output directory if it does not exist
        }

        try (PDDocument document = PDDocument.load(new File(inputPdf))) {
            int pageCount = document.getNumberOfPages();
            ConvertCmd cmd = new ConvertCmd();
            cmd.setSearchPath("/opt/homebrew/bin");


            for (int i = 0; i < pageCount; i++) {
                IMOperation op = new IMOperation();
                op.addImage(inputPdf + "[" + i + "]");

                op.density(300);
                op.compress("Group4");
                op.type("Bilevel");
                op.monochrome();
                String outputFilePath = String.format("%s/page_%d.tif", outputDir, i + 1);

                op.addImage(outputFilePath);

                System.out.println("Executing command: " + op); // Print the command to be executed
                cmd.run(op); // Execute the command
            }
        } catch (Exception e) {
            System.out.println("Error during PDF conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void convertImageToTiff(String inputImage, String outputDir) {
        ConvertCmd cmd = new ConvertCmd();
        cmd.setSearchPath("/opt/homebrew/bin"); // Path where ImageMagick binaries are installed

        IMOperation op = new IMOperation();
        op.addImage(inputImage);

        op.density(300);
        op.compress("Group4"); // CCITT Group 4 fax compression, ideal for B&W images
        op.type("Bilevel"); // Ensure the image is black and white
        op.monochrome(); // Additional flag to enforce monochrome image

        String outputFilePath = String.format("%s%s%s.tif", outputDir, File.separator, getFileNameWithoutExtension(inputImage));
        System.out.println(outputFilePath);
        op.addImage(outputFilePath);

        try {
            System.out.println("Executing command: " + op); // Print the command to be executed
            cmd.run(op); // Execute the command
        } catch (Exception e) {
            System.err.println("Error during image conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private static String getFileNameWithoutExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        convertImageToTiff("/Users/sadanandsingh/Downloads/8x10_final.png", "");
        //convertToTiffs("/Users/sadanandsingh/Downloads/Sample-PPT-File-1000kb.pdf", "/Users/sadanandsingh/Downloads/output");
    }
}