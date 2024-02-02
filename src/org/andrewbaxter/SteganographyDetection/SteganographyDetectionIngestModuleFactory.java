/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.andrewbaxter.SteganographyDetection;

import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleGlobalSettingsPanel;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.Exceptions;

@ServiceProvider(service = IngestModuleFactory.class)
public class SteganographyDetectionIngestModuleFactory extends IngestModuleFactoryAdapter {

    @Override
    public String getModuleDisplayName() {
        return "Steganography Detection";
    }

    @Override
    public String getModuleDescription() {
        return "Detects steganography in image files.";
    }

    @Override
    public String getModuleVersionNumber() {
        return "1.0";
    }

    @Override
    public boolean isFileIngestModuleFactory() {
        return true; // Indicates this factory is for file ingest modules.
    }

    @Override
    public FileIngestModule createFileIngestModule(IngestModuleIngestJobSettings settings) {
        return new SteganographyDetectionFileIngestModule();
    }

    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        throw new UnsupportedOperationException("No data source ingest module provided.");
    }

    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return false;
    }

    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return null; // Using default settings
    }

    @Override
    public boolean hasIngestJobSettingsPanel() {
        return false; // No custom settings panel required
    }

    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        return null; // No custom settings panel required
    }

    @Override
    public boolean hasGlobalSettingsPanel() {
        return false; // No global settings panel required
    }

    @Override
    public IngestModuleGlobalSettingsPanel getGlobalSettingsPanel() {
        return null; // No global settings panel required
    }
}
