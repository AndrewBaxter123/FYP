/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.andrewbaxter.SteganographyDetection;

/**
 *
 * @author andre
 */
public class SuspectedFile {
    private final String filePath;
    private final String detectionMethod;

    public SuspectedFile(String filePath, String detectionMethod) {
        this.filePath = filePath;
        this.detectionMethod = detectionMethod;

    }

    // Getters
    public String getFilePath() { return filePath; }
    public String getDetectionMethod() { return detectionMethod; }

}
