package app.paperplane.ppengine;

import app.paperplane.ppengine.converter.Converter;
import app.paperplane.ppengine.records.PageSize;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.pdmodel.PDDocument;
import javax.print.attribute.standard.*;
import java.awt.print.PrinterJob;
import java.util.HashMap;
import java.util.HashSet;
import javax.print.*;
import java.util.Map;
import java.util.Set;
import java.io.File;

// main print engine class, all the shit happens from here
public class PPEngine {

    // a list of strings that stores description of all printers
    public String[] printerDescriptions;

    // maps page size to printers capable of printing that page size
    private Map<PageSize, Set<PrintService>> pageSizeToPrintServices = new HashMap<>(0);

    // stores which printers can print in color and which cannot
    private Map<Boolean, Set<PrintService>> colorToPrintServices = new HashMap<>();

    // maps name of a printer to it's print service instance in java
    private Map<String, PrintService> nameToPrintService = new HashMap<>();

    // set of printers which we need not consider when printing a file
    private Set<PrintService> excludedPrinters = new HashSet<>();

    public PPEngine() {
        // read the connected printers and store them into maps / sets as required
        initializePrinters();
    }

    // given names of printers, adds the corresponding printer services into the exclude printers set
    void excludePrinters(String[] excludedPrinterNames, boolean clearExcludeSet) {
        // if we need to work with an empty excluded printers set, we clear the set before adding stuff into it
        if (clearExcludeSet) {
            excludedPrinters.clear();
        }

        for (String excludedPrinterName: excludedPrinterNames) {
            PrintService excludedPrinterService = nameToPrintService.get(excludedPrinterName);
            excludedPrinters.add(excludedPrinterService);
        }
    }

    // gets a print service which has the minimum jobs from the given set
    public PrintService getPrintServiceWithMinJobs(Set<PrintService> printers) {

        // initialize the minJobs so far to some large ass value
        int minJobs = Integer.MAX_VALUE;

        // so far we have not seen any print services with any number of jobs so its just null for now
        PrintService minJobsPs = null;

        // loop through the printers in the set
        for (PrintService ps: printers) {
            // get the number of jobs in the printer
            QueuedJobCount count = ps.getAttribute(QueuedJobCount.class);

            // if the count is null, and if no print service has been chosen yet, might as well choose this one
            // as a worst case scenario
            if (count == null) {
                if (minJobsPs == null) {
                    minJobsPs = ps;
                }
            }

            // if the number of jobs in the current printer is lower than the number of jobs of the
            // currently chosen print service
            // choose the current printer
            else if (count.getValue() < minJobs){
                minJobs = count.getValue();
                minJobsPs = ps;
            }
        }

        // if somehow no printer was chosen
        // throw an error (might change in the future idk)
        if (minJobsPs == null) {
            throw new RuntimeException("No suitable printers.");
        }

        // return the printer that was chosen
        return minJobsPs;
    }

    // prints a given pdf file, with the chosen printer and desired attributes
    private void printPDF(File pdfFile, PrintService printService, PrintRequestAttributeSet attrs) {
        // open the pdf file
        try (PDDocument document = PDDocument.load(pdfFile)) {
            // make a job instance
            PrinterJob job = PrinterJob.getPrinterJob();

            // do some shit idk what this does
            job.setPageable(new PDFPageable(document));
            job.setPrintService(printService);

            // print the file using the desired attributes
            job.print(attrs);

        } catch (Exception e) {
            System.err.println("Loading PDF file interrupted.");
        }
    }

    // returns an attribute set that you need for printing based on some desired params
    public PrintRequestAttributeSet getPrintRequestAttributeSet(boolean color, PageSize pageSize) {
        // if the printing needs to be in color, add color attribute to set
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        if (color) {
            attrs.add(Chromaticity.COLOR);
        } else {
            attrs.add(Chromaticity.MONOCHROME);
        }

        // add size of the pages used in printing to the attribute set
        attrs.add(pageSize.toMediaSizeName());

        return attrs;
    }

    // function that prints the file desired attributes
    public void printFile(String filePath, boolean color, PageSize pageSize) {
        // retrieve all suitable printers for the input params
        Set<PrintService> suitablePrinters = new HashSet<>(colorToPrintServices.getOrDefault(color, Set.of()));
        suitablePrinters.retainAll(pageSizeToPrintServices.getOrDefault(pageSize, Set.of()));
        suitablePrinters.removeAll(excludedPrinters);

        // convert our input file to a pdf
        String pdfFilePath = Converter.convertFileToPDF(filePath, pageSize);

        // get the printer with the lowest jobs currently
        PrintService minJobsPs = getPrintServiceWithMinJobs(suitablePrinters);

        // get the attribute set for our desired attributes
        PrintRequestAttributeSet attrs = getPrintRequestAttributeSet(color, pageSize);

        // create an instance of the pdf file in memory
        File pdfFile = new File(pdfFilePath);
        System.out.println("Printing on printer " + minJobsPs.getName() + " with " + minJobsPs.getAttribute(QueuedJobCount.class) + " jobs left.");

        // print the fuckin' pdf file hell yeah
        printPDF(pdfFile, minJobsPs, attrs);
    }

    // what this does was explained in the constructor... go look up u lazy goose
    private void initializePrinters() {
        // get all the printers currently connected to whichever unfortunate laptop / computer
        // that is running this program (hopefully my manager doesn't see this comment lol)
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        int numServices = printServices.length;

        // initialize the printer descriptions array
        printerDescriptions = new String[numServices];

        // loop through da services
        for (int i = 0; i < numServices; i++) {
            PrintService ps = printServices[i];

            // make a new string builder because we will be building the description, which is a string 🤯
            StringBuilder sb = new StringBuilder();

            // map the name of the current printer to the current printer
            nameToPrintService.put(ps.getName(), ps);

            // append the name of the printer into the description
            sb.append(ps.getName());

            // see if the current printer supports color
            ColorSupported colorSupported = ps.getAttribute(ColorSupported.class);
            // add that shit into the description also
            sb.append("\n\tColor Supported: ");
            sb.append(colorSupported == ColorSupported.SUPPORTED);

            // if color is supported store that this printer supports color by storing it in the map
            if (colorSupported == ColorSupported.SUPPORTED) {
                colorToPrintServices
                        .computeIfAbsent(true, k -> new HashSet<>())
                        .add(ps);
            }
            // well... i think all printers support printing in black and white... regardless of if they
            // support color or not, so add all printers into the map by mentioning they don't print in color
            colorToPrintServices
                    .computeIfAbsent(false, k -> new HashSet<>())
                    .add(ps);

            // we will be adding sizes of paper the printer can print to the description, yay!
            sb.append("\n\tPaper sizes:");

            // get all media supported by the curent printer
            Object vals = ps.getSupportedAttributeValues(Media.class, null, null);

            // loop through each supported media
            if (vals instanceof Media[] media) {
                for (Media m : media) {
                    // we only care about objects that instantiate MediaSizeName class
                    // that implements Media class
                    if (m instanceof MediaSizeName msn) {
                        // get size of media
                        MediaSize size = MediaSize.getMediaSizeForName(msn);

                        if (size != null) {
                            // convert size into millimeters (inches users punching air rn)
                            float w = size.getX(MediaSize.MM);
                            float h = size.getY(MediaSize.MM);

                            // add the print service to the set that's mapped to from the size of the media...
                            // hope that made sense :(
                            pageSizeToPrintServices
                                    .computeIfAbsent(new PageSize(w, h), k -> new HashSet<>())
                                    .add(ps);

                            // add the media name and its size to the description
                            sb.append("\n\t\t")
                                    .append(msn.toString().trim())
                                    .append(" (")
                                    .append(w).append("mm x ")
                                    .append(h).append("mm),");
                        }
                    }
                }
            }

            // store the description of the current printer in the descriptions array
            printerDescriptions[i] = sb.toString();
        }
    }

    // just an override for working with the print function... nothing much
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String printerDescription: printerDescriptions) {
            sb.append(printerDescription);
            sb.append("\n\n");
        }

        return sb.toString();
    }

}