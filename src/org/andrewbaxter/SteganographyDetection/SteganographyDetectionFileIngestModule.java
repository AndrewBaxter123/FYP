/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.andrewbaxter.SteganographyDetection;

/**
 *
 * @author andre
 * 
 * SteganographyDetectionFileIngestModule class: 
 * This is the main class implementing the FileIngestModule interface. It contains methods for startup, processing, and shutdown.
 * 

startUp method: Called when the module is started. This is where you can perform any initialization needed for your module.

process method: The main processing method called for each file in the ingest pipeline. 
* Your steganography detection logic goes here. It logs file information, checks for steganography, and allows processing to continue.

shutDown method: Called when the module is shutting down. Cleanup and resource release can be performed here.

detectSteganography method: A private method where you should implement your steganography detection logic. It receives an AbstractFile representing the file being processed and returns true if steganography is detected, and false otherwise.
 * 
 * 
 * 
 */


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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities; // Note: In newer versions, this might be ChartUtils
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;


public class SteganographyDetectionFileIngestModule implements FileIngestModule {

    private final Logger logger = IngestServices.getInstance().getLogger("SteganographyDetection");
    private int totalFilesProcessed = 0;
    private int suspectedFilesCount = 0;
    private List<SuspectedFile> suspectedFiles = new ArrayList<>();  // List to hold suspected files

    
    // Placeholder values for detection method and confidence score
    String detectionMethod = "Placeholder Method";
    double confidenceScore = 0.97; // Placeholder confidence score

    @Override
    public void startUp(IngestJobContext ijc) throws IngestModuleException {
        // Perform any initialization here
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
        generateReport(reportPath, pieChartFileName); // Pass the filename to the generateReport method
    } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
    }

    logger.log(Level.INFO, "SteganographyDetectionFileIngestModule shutting down");
}



    
    private boolean placeholderDetectSteganography(AbstractFile file) {
    return Math.random() > 0.97; // 5% chance to mark a file as suspected
}
    
@Override
public ProcessResult process(AbstractFile file) {
    // Placeholder logic for steganography detection
    boolean isSuspected = placeholderDetectSteganography(file);
    totalFilesProcessed++;  // Increment for every file processed

    if (isSuspected) {
        suspectedFilesCount++;  // Increment when steganography is suspected

        String uniquePath;
        try {
            uniquePath = file.getUniquePath();
        } catch (TskCoreException e) {
            logger.log(Level.SEVERE, "Could not get unique path for file " + file.getName(), e);
            // Decide how to handle the error. For example, skip this file:
            return ProcessResult.OK;
        }

        // Placeholder values for detection method and confidence score
        String detectionMethod = "LSB";  // This should eventually be the actual detection method used
        double confidenceScore = 0.95;   // And the actual confidence score obtained

        // Create a suspected file object and add it to the list
        SuspectedFile suspectedFile = new SuspectedFile(uniquePath, detectionMethod, confidenceScore);
        suspectedFiles.add(suspectedFile);

        try {
            // Log detected steganography
            logger.log(Level.INFO, "Steganography suspected in {0}", uniquePath);

            // Add artifact to the Blackboard
            addArtifactToBlackboard(file, detectionMethod, confidenceScore);

            // Tag the file as suspected of containing steganography
            tagFile(file, "Suspected Steganography");

            // Send ingest message
            String title = "Steganography Suspected";
            String detailMessage = "Steganography suspected in file: " + file.getName() +
                                   " using method " + detectionMethod +
                                   " with confidence score " + confidenceScore;
            sendIngestMessage(title, detailMessage);

        } catch (TagsManager.TagNameAlreadyExistsException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    return ProcessResult.OK;
}




private void addArtifactToBlackboard(AbstractFile file, String detectionMethod, double confidenceScore) {
    try {
        BlackboardArtifact artifact = file.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT);
        
        BlackboardAttribute methodAttribute = new BlackboardAttribute(
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
            "SteganographyDetection",
            "Detection Method: " + detectionMethod
        );
        
        BlackboardAttribute confidenceAttribute = new BlackboardAttribute(
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
        "SteganographyDetection",
        "Confidence Score: " + String.format("%.2f", confidenceScore) 
        );
        
        artifact.addAttribute(methodAttribute);
        artifact.addAttribute(confidenceAttribute);

        // Post the artifact to the Blackboard for review
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

    // Add the tag with a comment
    tagsManager.addContentTag(file, tag, "Steganography detection plugin flagged this file.");

    } catch (TskCoreException | NoCurrentCaseException e) {
        logger.log(Level.SEVERE, "Error tagging file", e);
    }
}


private void sendIngestMessage(String title, String message) {
    IngestServices.getInstance().postMessage(IngestMessage.createMessage(
        IngestMessage.MessageType.INFO, // or WARNING/ERROR
        "SteganographyDetection",
        title,
        message
    ));
}

private void generateReport(String reportPath, String pieChartFileName) {
    // Change the file extension to .html
    File reportFile = new File(reportPath.replace(".txt", ".html"));

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
        // Start of the HTML document
        writer.write("<!DOCTYPE html><html><head><title>Steganography Detection Report</title></head><body>");
        writer.write("<h1>Steganography Detection Report</h1>");
        writer.write("<p>Generated on: " + new Date().toString() + "</p>");

        // Summary section
        writer.write("<h2>Summary</h2>");
        writer.write("<p>Total files processed: " + totalFilesProcessed + "</p>");
        writer.write("<p>Files suspected of containing steganography: " + suspectedFilesCount + "</p>");

        // Detailed list of suspected files
        writer.write("<h2>Detail</h2><ul>");
        for (SuspectedFile file : suspectedFiles) {
            writer.write("<li>" + file.getFilePath() + " - Method: " + file.getDetectionMethod() + ", Confidence: " + file.getConfidenceLevel() + "</li>");
        }
        writer.write("</ul>");

        // Embedding the pie chart image (make sure the image path is accessible from where the HTML will be opened)
        writer.write("<img src='" + pieChartFileName + "' alt='Pie Chart'/>");

        // End of the HTML document
        writer.write("</body></html>");
    } catch (IOException e) {
        logger.log(Level.SEVERE, "Error generating HTML report", e);
    }
}


private void generatePieChart(String imagePath) throws IOException {
    // Create a dataset for the pie chart
    PieDataset dataset = createDataset();

    // Create a chart
    JFreeChart chart = ChartFactory.createPieChart(
            "Steganography Detection Summary",   // chart title
            dataset,          // dataset
            true,             // include legend
            true,
            false);

    // Save the chart as a PNG file
    ChartUtilities.saveChartAsPNG(new File(imagePath), chart, 500, 300);
}

private PieDataset createDataset() {
    DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue("Files with Steganography", suspectedFilesCount);
    dataset.setValue("Other Files", totalFilesProcessed - suspectedFilesCount);
    return dataset;
}




    private boolean detectSteganography(AbstractFile file) {
    // Implement your steganography detection logic here
    // You can access the content of the file using file.readContent()
    
    // Example: Call LSB script
    boolean lsbResult = detectSteganographyUsingLSB(file);

    // Example: Call machine learning model
    boolean machineLearningResult = detectSteganographyUsingML(file);

    // Example: Call deep learning model
    boolean deepLearningResult = detectSteganographyUsingDL(file);

    // Combine results based on your specific requirements
    // For example, return true if at least one method detects steganography
    return lsbResult || machineLearningResult || deepLearningResult;
}

private boolean detectSteganographyUsingLSB(AbstractFile file) {
    // Implement LSB steganography detection logic
    // Return true if steganography is detected, false otherwise
    return false;
}

private boolean detectSteganographyUsingML(AbstractFile file) {
    // Implement machine learning steganography detection logic
    // Return true if steganography is detected, false otherwise
    return false;
}

private boolean detectSteganographyUsingDL(AbstractFile file) {
    // Implement deep learning steganography detection logic
    // Return true if steganography is detected, false otherwise
    return false;
}

}    