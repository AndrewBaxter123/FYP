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
        return "StegCatcher";
    }

    @Override
    public String getModuleDescription() {
        return "Detects LSB steganography in image files.";
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
