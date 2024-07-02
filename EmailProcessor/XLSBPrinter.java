package org.example.files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class XLSBPrinter {
    public static void main(String[] args) {
        Path filePath = Paths.get("/Users/sadanandsingh/Downloads/NumberFormatCondition.xlsb");  // Set your fixed file path here
        String worksheetName = "";  // Optional: specify worksheet name if needed
        boolean hasHeaders = true;  // Change this if your file does not have headers
        try {
            RowReader rowReader = new RowReader();
            XLSBStreamingTableReader reader = new XLSBStreamingTableReader(filePath, worksheetName, rowReader, hasHeaders);
            reader.parse();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}