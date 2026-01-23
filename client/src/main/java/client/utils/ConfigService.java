package client.utils;

import client.scenes.ErrorCtrl;
import com.google.inject.Inject;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

public class ConfigService {

    private File configFile = new File("config/settings.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ErrorCtrl errors;
    private AppSettings settings;

    @Inject
    public ConfigService(ErrorCtrl errors) {
        this.errors = errors;
    }

    /**
     * Updates the config file location if a custom path is provided
     * @param path - The file path
     */
    public void setCustomConfigPath(String path) {
        File customFile = new File(path);
        // Check if the file exists and is a valid file
        if (customFile.exists() && customFile.isFile()) {
            this.configFile = customFile;
            System.out.println("Using custom config: " + customFile.getAbsolutePath());
        } else {
            System.err.println("Warning: Custom config file not found at " + path + ". Using default.");
        }
    }

    /**
     * Loads AppSettings data from the config file
     */
    public void load() {
        try {
            if (configFile.exists()) {
                String jsonContent = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
                settings = mapper.readValue(jsonContent, AppSettings.class);
            } else {
                settings = new AppSettings();
                save();
            }
            System.out.println("Config file loaded successfully");
        } catch (IOException e) {
            errors.showGenericError("Error in reading config file!");
        }
    }

    /**
     * Saves AppSettings data to the config file
     */
    public void save() {
        try {
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(settings);

            FileUtils.writeStringToFile(configFile, jsonString, StandardCharsets.UTF_8);
        } catch (IOException e) {
            errors.showGenericError("Error in writing config file!");
        }
    }

    public AppSettings get() {
        if (settings == null) load();
        return settings;
    }
}
