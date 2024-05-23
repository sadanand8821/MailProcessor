package org.example;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ooo.connector.BootstrapSocketConnector;

public class DocumentConverter {

    public static void convertToPDF(String oooExeFolder, String inputFile, String outputFile) throws Exception {
        // Initialize the UNO Component Context
        XComponentContext xContext = BootstrapSocketConnector.bootstrap(oooExeFolder);
        XComponentLoader loader = UnoRuntime.queryInterface(XComponentLoader.class, xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xContext));

        // Set properties for loading the document
        PropertyValue[] loadProps = new PropertyValue[1];
        loadProps[0] = new PropertyValue();
        loadProps[0].Name = "Hidden";
        loadProps[0].Value = true;
        // Load the document
        XComponent xComp = loader.loadComponentFromURL("file:///" + inputFile.replace("\\", "/"), "_blank", 0, loadProps);

        // Set properties for storing the document in PDF format
        PropertyValue[] storeProps = new PropertyValue[2];
        storeProps[0] = new PropertyValue();
        storeProps[0].Name = "FilterName";
        storeProps[0].Value = "writer_pdf_Export";
        storeProps[1] = new PropertyValue();
        storeProps[1].Name = "Overwrite";
        storeProps[1].Value = true;

        // Save the document as PDF
        XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xComp);
        xStorable.storeToURL("file:///" + outputFile.replace("\\", "/"), storeProps);

        System.out.println("Document converted successfully to PDF: " + outputFile);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar DocumentConverter.jar <LibreOfficeProgramFolder> <inputFile> <outputFile>");
        }
        try {
            convertToPDF("Enter Your sOffice Executable Location Here", "Input File ", "Output File should end in .pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
