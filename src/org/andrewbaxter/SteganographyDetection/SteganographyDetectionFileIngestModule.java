/*
 * MIT License
 * 
 * Copyright (c) 2024 Andrew Baxter
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.andrewbaxter.SteganographyDetection;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.IngestModule.ProcessResult;
import org.sleuthkit.autopsy.ingest.IngestModule.IngestModuleException;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.TagsManager;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.Blackboard.BlackboardException;
import org.sleuthkit.datamodel.TagName;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities; // Note: In newer versions, this might be ChartUtils
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import java.io.BufferedReader;



public class SteganographyDetectionFileIngestModule implements FileIngestModule {

    private final Logger logger = IngestServices.getInstance().getLogger("SteganographyDetection");
    private int totalFilesProcessed = 0;
    private int suspectedFilesCount = 0;
    private List<SuspectedFile> suspectedFiles = new ArrayList<>();  // List to hold suspected files

    @Override
    public void startUp(IngestJobContext ijc) throws IngestModuleException {
        logger.log(Level.INFO, "SteganographyDetectionFileIngestModule starting up");
    }

    @Override
    public void shutDown() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String pieChartFileName = "pieChart_" + timestamp + ".png";
        String pieChartImagePath = Case.getCurrentCase().getExportDirectory() + File.separator + pieChartFileName;
        String reportFileName = "SteganographyDetectionReport_" + timestamp + ".html";
        String reportPath = Case.getCurrentCase().getExportDirectory() + File.separator + reportFileName;

        try {
            generatePieChart(pieChartImagePath);
            generateReport(reportPath, pieChartFileName);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        logger.log(Level.INFO, "SteganographyDetectionFileIngestModule shutting down");
    }

@Override
public ProcessResult process(AbstractFile file) {
    List<String> supportedExtensions = Arrays.asList(".png", ".jpg", ".jpeg", ".dng", ".pgm", ".bmp", ".gif");
    String fileName = file.getName().toLowerCase();
    boolean isSupportedType = false;

    for (String extension : supportedExtensions) {
        if (fileName.endsWith(extension)) {
            isSupportedType = true;
            break;
        }
    }

    if (isSupportedType) {
        boolean isSuspected = detectSteganographyUsingSVC(file);
        totalFilesProcessed++;  // Increment for every file processed

        if (isSuspected) {
            suspectedFilesCount++;  // Increment if steganography is suspected
            suspectedFiles.add(new SuspectedFile(file.getLocalAbsPath(), "Detection Method: SVM"));
            sendIngestMessage(file, isSuspected); // Send detailed message if steganography is detected
            try {
                tagFile(file, "Suspected Steganography"); // Tag the file as suspected steganography
                addArtifactToBlackboard(file); // Add a comment to the blackboard
            } catch (TagsManager.TagNameAlreadyExistsException e) {
                logger.log(Level.SEVERE, "Error tagging file", e);
            }
        }
    }

    return ProcessResult.OK;
}



    private void addArtifactToBlackboard(AbstractFile file) {
        try {
            BlackboardArtifact artifact = file.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT);
            BlackboardAttribute methodAttribute = new BlackboardAttribute(
                BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
                "SteganographyDetection",
                "Detection Method: SVC Model"
            );
            artifact.addAttribute(methodAttribute);
            Case.getCurrentCaseThrows().getSleuthkitCase().getBlackboard().postArtifact(artifact, "SteganographyDetection");
        } catch (TskCoreException | NoCurrentCaseException | BlackboardException e) {
            logger.log(Level.SEVERE, "Error adding artifact to the blackboard", e);
        }
    }

    private void tagFile(AbstractFile file, String tagName) throws TagsManager.TagNameAlreadyExistsException {
        try {
            Case currentCase = Case.getCurrentCaseThrows();
            TagsManager tagsManager = currentCase.getServices().getTagsManager();
            TagName tag = null;
            List<TagName> existingTags = tagsManager.getAllTagNames();
            for (TagName existingTag : existingTags) {
                if (existingTag.getDisplayName().equalsIgnoreCase(tagName)) {
                    tag = existingTag;
                    break;
                }
            }
            if (tag == null) {
                tag = tagsManager.addTagName(tagName, "Suspected steganography", TagName.HTML_COLOR.RED);
            }
            tagsManager.addContentTag(file, tag, "Steganography detection plugin flagged this file.");
        } catch (TskCoreException | NoCurrentCaseException e) {
            logger.log(Level.SEVERE, "Error tagging file", e);
        }
    }




    private void generateReport(String reportPath, String pieChartFileName) {
    File reportFile = new File(reportPath);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
        writer.write("<!DOCTYPE html><html><head><title>Steganography Detection Report</title>");
        // Adding some basic CSS
        writer.write("<style>");
        writer.write("body { font-family: Arial, sans-serif; margin: 40px; }");
        writer.write("h1, h2 { color: navy; }");
        writer.write("p { font-size: 16px; }");
        writer.write("ul { background-color: #f8f8f8; padding: 20px; }");
        writer.write("li { margin: 10px 0; }");
        writer.write("img { display: block; margin-top: 20px; max-width: 100%; height: auto; border: 1px solid #ccc; padding: 5px; }");
        writer.write("</style>");
        writer.write("</head><body>");
        writer.write("<h1>Steganography Detection Report</h1>");
        writer.write("<p>Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</p>");
        writer.write("<h2>Summary</h2>");
        writer.write("<p>Total files processed: " + totalFilesProcessed + "</p>");
        writer.write("<p>Files suspected of containing steganography: " + suspectedFilesCount + "</p>");
        writer.write("<img src='" + pieChartFileName + "' alt='Pie Chart'/>");
        writer.write("<h2>File name detected:</h2><ul>");
        for (SuspectedFile file : suspectedFiles) {
            writer.write("<li>" + file.getFilePath() + " - Method: " + file.getDetectionMethod() + "</li>");
        }
        writer.write("</ul>");
        writer.write("</body></html>");
    } catch (IOException e) {
        logger.log(Level.SEVERE, "Error generating HTML report", e);
    }
}



    private void generatePieChart(String imagePath) throws IOException {
        PieDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createPieChart(
            "Steganography Detection Summary",
            dataset,
            true,
            true,
            false);
        ChartUtilities.saveChartAsPNG(new File(imagePath), chart, 500, 300);
    }

    private PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Files with Steganography", suspectedFilesCount);
        dataset.setValue("Other Files", totalFilesProcessed - suspectedFilesCount);
        return dataset;
    }

    private boolean detectSteganographyUsingSVC(AbstractFile file) {
        boolean stegoDetected = false;
        String pythonScriptPath = getPythonScriptPath();
        if (pythonScriptPath == null) {
            logger.log(Level.SEVERE, "Python script path is incorrect or script does not exist.");
            return false;
        }

        String filePath = file.getLocalAbsPath();
        if (filePath == null || filePath.isEmpty()) {
            logger.log(Level.SEVERE, "File path is null or empty for file: " + file.getName());
            return false;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, filePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Steganography Detected: Yes")) {
                        stegoDetected = true;
                    }
                    logger.log(Level.INFO, line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.log(Level.WARNING, "Python script exited with code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error running steganography detection script", e);
        }
        return stegoDetected;
    }

//    private String getPythonScriptPath() {
//        String currentDir = System.getProperty("user.dir");
//        String scriptRelativePath = "src/org/andrewbaxter/SteganographyDetection/scripts/autopsySVC.py";
//        String scriptPath = currentDir + File.separator + scriptRelativePath;
//        File scriptFile = new File(scriptPath);
//        if (scriptFile.exists()) {
//            return scriptFile.getAbsolutePath();
//        } else {
//            logger.log(Level.SEVERE, "Python script file does not exist at the specified path: " + scriptPath);
//            return null;
//        }
//    }

private String getPythonScriptPath() {
    try {
        // Get the URL of the Python script as a resource inside the JAR or file system
        URL scriptUrl = getClass().getResource("/org/andrewbaxter/SteganographyDetection/scripts/autopsySVC.py");
        if (scriptUrl == null) {
            logger.log(Level.SEVERE, "Python script file does not exist within the JAR or project");
            return null;
        }
        
        // Convert the URL to a URI to handle spaces and special characters in path
        URI scriptUri = scriptUrl.toURI();
        
        // Check if the resource is in a JAR or a file on the filesystem
        if ("jar".equalsIgnoreCase(scriptUri.getScheme())) {
            // Extract the script to a temporary file to execute it
            return extractScriptToTempFile(scriptUrl).getAbsolutePath();
        } else {
            // Otherwise, the script can be accessed directly
            return new File(scriptUri).getAbsolutePath();
        }
    } catch (URISyntaxException ex) {
        logger.log(Level.SEVERE, "Error handling script URI", ex);
        return null;
    } catch (IOException ex) {
        logger.log(Level.SEVERE, "Error extracting script", ex);
        return null;
    }
}

private File extractScriptToTempFile(URL resourceUrl) throws IOException {
    // Create a temporary file
    File tempFile = File.createTempFile("autopsySVC", ".py");
    tempFile.deleteOnExit(); // Ensure the file is deleted when the JVM exits

    // Copy the resource content to the temporary file
    try (InputStream in = resourceUrl.openStream();
         OutputStream out = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    return tempFile;
}


private void sendIngestMessage(AbstractFile file, boolean isSuspected) {
    if (!isSuspected) {
        return; // Only send messages for suspected steganography cases
    }

    String title = "🚨 Steganography Detected! 🚨";
    String detailMessage = "<html>"
            + "<h2>Steganography Detection Notification</h2>"
            + "<p><strong>File Information:</strong><br>"
            + "- File Name: " + file.getName() + "<br>"
            + "- Detected On: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "<br>"
            + "- File Location: " + getFilePathSafe(file) + "<br>"
            + "- Detection Method: Support Vector Machine (SVM)</p>"
            + "<p><strong>Action Required:</strong><br>"
            + "1. Verify the detection by reviewing the file.<br>"
            + "2. Report this detection to your IT security team for further investigation.<br>"
            + "3. Follow established security procedures related to steganography findings.</p>"
            + "<p><strong>What is Steganography?</strong><br>"
            + "Steganography involves embedding secret data within non-secret files. Detecting such files is crucial for preventing unauthorized information leaks.</p>"
            + "<p><strong>Extra Steps?</strong><br>"
            + "<p>Use an online tool to extract the payload, ZSteg for example.</p>"
            + "</html>";

    IngestServices.getInstance().postMessage(IngestMessage.createMessage(
            IngestMessage.MessageType.INFO,  // You can use WARNING or ERROR if appropriate
            "SteganographyDetection",
            title,
            detailMessage
    ));
}

private String getFilePathSafe(AbstractFile file) {
    try {
        return file.getUniquePath();
    } catch (TskCoreException e) {
        logger.log(Level.SEVERE, "Could not get unique path for file " + file.getName(), e);
        return "Unavailable";
    }
}



}