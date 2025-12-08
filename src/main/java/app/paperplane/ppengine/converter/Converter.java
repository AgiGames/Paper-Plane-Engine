package app.paperplane.ppengine.converter;

import app.paperplane.ppengine.records.PageSize;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import app.paperplane.ppengine.helper.Helper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import java.io.*;

// class that converts a given file to another file
// eg: image -> PDF, docx -> PDF
public class Converter {

    // all extensions that doc files usually have and also convertible by this class
    private static final String[] wordDocExtensions = {".doc", ".docx", ".dot", ".dotx", ".docm", ".dotm"};

    // all extensions that image files usually have and also convertible by this class
    private static final String[] imageExtensions = {".png", ".jpg", ".jpeg"};

    // checks if a given file-path points to a document file
    public static boolean isWordDoc(String filePath) {
        boolean isWordDoc = false;
        for (String extension: wordDocExtensions) {
            if (filePath.endsWith(extension))
                return true;
        }
        return false;
    }

    // checks if a given file-path points to an image file
    public static boolean isImage(String filePath) {
        for (String extension: imageExtensions) {
            if (filePath.endsWith(extension))
                return true;
        }
        return false;
    }

    // converts a given input document to a pdf file
    // param:
    // inputDocPath: path of the document file we will be converting to pdf
    // outputPdfPath: path of the pdf file where the pdf file will be written
    public static void convertWordToPDF(String inputDocPath, String outputPdfPath) {
        // open up a file input stream for the input doc to read from it
        try (FileInputStream in = new FileInputStream(inputDocPath)) {
            // feed that input stream to a document instance
            XWPFDocument document = new XWPFDocument(in);

            // create our pdf file to which we will be writing our content into
            File pdfFile = new File(outputPdfPath);

            // open an output stream to our created pdf file
            try (FileOutputStream out = new FileOutputStream(pdfFile)) {
                // convert the document to a pdf and write the pdf contents into our pdf file
                PdfConverter.getInstance().convert(document, out, null);
            }
            catch (Exception e) {
                System.out.println("File output stream interrupted.");
            }
        }
        catch (Exception e) {
            System.out.println("File input stream interrupted.");
        }
    }

    // converts a given input image to a pdf file
    // param:
    // inputImagePath: path of the image file we will be converting to pdf
    // outputPdfPath: path of the pdf file where the pdf file will be written
    public static void convertImageToPDF(String inputImagePath, String outputPdfPath, PageSize pageSize) {
        // make an instance of a new pdf document
        try (PDDocument document = new PDDocument()) {
            // make an instance of a pdf image, and store the image in the document
            PDImageXObject image = PDImageXObject.createFromFile(inputImagePath, document);

            // create a new page with desired size and put in the pdf file
            PDPage page = new PDPage(new PDRectangle(
                    Helper.mmToPoints(pageSize.width()),
                    Helper.mmToPoints(pageSize.height())
                            )
                    );
            document.addPage(page);

            // open up an output stream to write our image into the newly created page in the document
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                // scale the image so it fits in the page
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                float imgWidth = image.getWidth();
                float imgHeight = image.getHeight();

                float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);

                float drawWidth = imgWidth * scale;
                float drawHeight = imgHeight * scale;

                // center the image
                float x = (pageWidth - drawWidth) / 2;
                float y = (pageHeight - drawHeight) / 2;

                // draw the image
                content.drawImage(image, x, y, drawWidth, drawHeight);
            }
            // save the pdf
            document.save(outputPdfPath);

        } catch (Exception e) {
            System.out.println("Image to PDF conversion failed.");
        }
    }

    // call the necessary convert function to convert given file to pdf
    public static String convertFileToPDF(String inputFilePath, PageSize pageSize) {
        String outputFilePath = Helper.addPDFFileExtension(inputFilePath);
        if (inputFilePath.endsWith(".pdf")) {
            return inputFilePath;
        }
        if (isImage(inputFilePath)) {
            convertImageToPDF(inputFilePath, outputFilePath, pageSize);
        }
        if (isWordDoc(inputFilePath)) {
            convertWordToPDF(inputFilePath, outputFilePath);
        }
        return outputFilePath;
    }

}