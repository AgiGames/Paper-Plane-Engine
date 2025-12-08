package app.paperplane.ppengine;

import app.paperplane.ppengine.records.PageSize;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
import java.io.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/ppe")

public class PPEngineController {

    private static final String BASE_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/print")
    public ResponseEntity<String> handleFilePrint(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("color") boolean color,
                                                  @RequestParam("width-mm") float widthMM,
                                                  @RequestParam("height-mm") float heightMM) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Print failed: File is empty.");
        }

        File savedFile = new File(BASE_DIR + System.currentTimeMillis() + "_" + file.getOriginalFilename());
        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Print failed: " + e.getMessage());
        }

        Main.printEngine.printFile(savedFile.getAbsolutePath(), color, new PageSize(widthMM, heightMM));

        return ResponseEntity.ok("File sent for print successfully: " + savedFile.getAbsolutePath());
    }

    @PostMapping("/get-printers")
    public ResponseEntity<String[]> getPrinters() {
        return ResponseEntity.ok(Main.printEngine.printerDescriptions);
    }

}