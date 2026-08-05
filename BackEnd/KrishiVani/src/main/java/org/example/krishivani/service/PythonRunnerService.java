package org.example.krishivani.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PythonRunnerService {

    private Process pythonProcess;

    @PostConstruct
    public void startPythonScript() {
        System.out.println("Starting YouTube Weather Analyzer Python Script...");

        try {

            File scriptDir = new File("youtube_analyzer");

            if (scriptDir.exists()) {
                // Determine OS to use the correct Python executable
                String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";
                ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-u", "youtube_weather_analyzer.py");
                pb.directory(scriptDir);


                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);


                pythonProcess = pb.start();
                System.out.println("Python script started successfully in the background.");
            } else {
                System.err.println("Python script directory not found: " + scriptDir.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Failed to start Python script: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopPythonScript() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("Stopping YouTube Weather Analyzer Python Script...");
            pythonProcess.destroy();
        }
    }
}
