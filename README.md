# FYP - Steganography Detection Plugin for Autopsy

## License
This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

# Installation and Usage Guide for Windows

This guide provides step-by-step instructions on how to download, install, and use the steganography detection plugin for Autopsy.

## Prerequisites

- Python 3.10 ( tested with 3.12 too)
- download requirements.txt file
- git (for cloning the repository)

## Step 1: Clone the Repository

To get started, clone the repository to your local machine using git:

```bash
git clone https://github.com/AndrewBaxter123/FYP.git
```

## Step 2: Install Requirements

Navigate to the cloned repository directory and install the required Python libraries:

```bash
cd FYP
pip install -r requirements.txt
```

## Step 3: Install Autopsy

- Download Autopsy from The Sleuth Kit website if you haven't already installed it. (https://www.autopsy.com/download/) (- Autopsy 4.18.0 or later (tested on Autopsy 4.18.0 and the latest version)
- Navigate to your downloads and double click the msi installer, you might get a pop up from windows defender, click more info -> run anyway.
- Follow the Autopsy setup wizard.

## Step 4: Create a New Case in Autopsy
- Open Autopsy and create a new case
- if not prompted to do so, click File and new case
- Provide the necessary case information and click Finish.

## Step 5: Add a Data Source
- With your case open, if you aren't prompted to add a Data Source, go to File > Add Data Source.
- Browse and select the logical files you want to analyze. (images)
- Follow the prompts to add the data source to your case.
- Deselect all modules/tools and finish

## Step 6: Install the Plugin

- In Autopsy, go to Tools > Plugins > Downloaded.
- Click Add Plugins... and navigate to where the clone got downloaded to and find the .nbm file
- Select the file and install the plugin by following the on-screen instructions, bypass unsigned problem.

## Step 7: Run Ingest Modules
- With your case and data source ready, go to Tools > Run Ingest Modules.
- Ensure that the steganography detection module is selected.
- Click Start Ingest to begin the analysis.
