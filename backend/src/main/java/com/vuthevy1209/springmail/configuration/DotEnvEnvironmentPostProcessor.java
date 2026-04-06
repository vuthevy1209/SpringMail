package com.vuthevy1209.springmail.configuration;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads key=value pairs from a .env file at project root into Spring's Environment.
 * The .env file is optional — if not present, the application starts normally.
 * System environment variables always take precedence over .env values.
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEvent> {

    private static final DeferredLog log = new DeferredLog();
    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";
    private static final String[] SEARCH_PATHS = { ".env", "../.env" };

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.replayTo(DotEnvEnvironmentPostProcessor.class);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = findEnvFile();
        if (envFile == null) return;

        Map<String, Object> properties = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // skip blank lines and comments
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eqIndex = line.indexOf('=');
                if (eqIndex <= 0) continue;

                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();

                // strip optional surrounding quotes
                if (value.length() >= 2 &&
                        ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }

                // system env vars take precedence — only set if not already defined
                if (!environment.containsProperty(key)) {
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env file: " + envFile.getAbsolutePath(), e);
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addLast(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, properties)
            );
            log.info("[DotEnv] Loaded " + properties.size() + " properties from " + envFile.getAbsolutePath());
            // properties.forEach((key, value) -> log.info("[DotEnv]  " + key + " = " + value));
        }
    }

    private File findEnvFile() {
        for (String path : SEARCH_PATHS) {
            File file = new File(path);
            if (file.exists() && file.isFile()) return file;
        }
        return null;
    }
}
