package com.company.nikas;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.controller.InputController;
import com.company.nikas.system.ActiveStreamMonitor;
import com.company.nikas.system.processing.impl.RomeRssProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class App
{
    public static void main( String[] args )
    {
        initiateConfiguration();
        Thread monitor = new Thread(new ActiveStreamMonitor(
                new RomeRssProcessor()));
        monitor.setName("systemMonitor");
        monitor.start();
        Thread controller = new Thread(new InputController(
                new Scanner(System.in), new Timer()));
        controller.start();
    }

    private static void initiateConfiguration() {
        PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));
        try {
            ObjectMapper objectMapper = new ObjectMapperPreparer().produceInstance();
            File file = new File(System.getProperty("user.dir") + "/config.json");
            JsonNode configuration = objectMapper.readTree(file);
            AppConfiguration.setRssFeeds(objectMapper.convertValue(configuration.get("rssFeeds"), Map.class));
        } catch (IOException e) {
            log.info("Unable to fetch configuration file, creating initial settings.");
            AppConfiguration.setRssFeeds(new ConcurrentHashMap<>());
        }
        AppConfiguration.setActiveConnections(new ConcurrentHashMap<>());
    }
}
