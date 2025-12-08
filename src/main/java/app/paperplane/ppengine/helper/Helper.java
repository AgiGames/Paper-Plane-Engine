package app.paperplane.ppengine.helper;

// helper class for various helper functions that we will beed
public class Helper {

    // removes existing extension and adds .pdf file extension to file path
    public static String addPDFFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.'); // find the index of the last dot
        return fileName.substring(0, lastDotIndex) + ".pdf";
    }

    // got tired of writing System.out.println all the time (im a python programmer)
    public static void print(Object obj) {
        System.out.println(obj);
    }

    public static float mmToPoints(float mm) {
        return mm * 72f / 25.4f;
    }
}