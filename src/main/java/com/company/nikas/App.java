package com.company.nikas;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.controller.InputController;
import com.company.nikas.model.RssConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
public class App
{
    private static ObjectMapper objectMapper;

    public static void main( String[] args )
    {
        objectMapper = new ObjectMapperPreparer().produceInstance();
        initiateConfiguration();
        prepareRssTemplates();
        initiateAsyncProcesses();
    }

    /**
     * Obtains properties from resources, and saved application configuration from file system.
     * If no configuration is present, creates an empty one.
     */
    private static void initiateConfiguration() {
        PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));
        try {
            File file = new File(System.getProperty("user.dir") + "/configuration.json");
            AppConfiguration.setRssFeeds(
                    objectMapper.readValue(file, new TypeReference<ConcurrentHashMap<String, RssConfiguration>>(){}));
        } catch (IOException e) {
            log.info("Unable to fetch configuration file, creating initial settings.");
            AppConfiguration.setRssFeeds(new ConcurrentHashMap<>());
        }
        AppConfiguration.setActiveConnections(new ConcurrentHashMap<>());
    }

    /**
     * Fetches RSS tag template from application resources.
     * Sets default tag mapping if no template is present
     */
    private static void prepareRssTemplates(){
        try {
            AppConfiguration.setSyndTemplate(objectMapper.convertValue(
                    objectMapper.readTree(InputController.class.
                            getResourceAsStream("/templates/synd-template.json"))
                    ,Map.class));
        } catch (IOException e) {
            log.error("An error occured while processing json templates,", e);
            Map<String, String> tagMapping = new HashMap<>();
            tagMapping.put("authors", "getAuthors");
            tagMapping.put("description", "getDescription");
            tagMapping.put("title", "getTitle");
            tagMapping.put("published", "getPublishedData");
            AppConfiguration.setSyndTemplate(tagMapping);
        }
    }

    /**
     * Launches controller class.
     */
    private static void initiateAsyncProcesses() {
        new InputController(
                new Scanner(System.in), Executors.newScheduledThreadPool(4)).run();
    }
}
