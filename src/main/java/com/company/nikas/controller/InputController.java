package com.company.nikas.controller;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.integrations.impl.ApacheRssReader;
import com.company.nikas.model.RssConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class InputController implements Runnable {

    private Timer timer;
    private Scanner input;
    Map<String, String> rssTemplate;
    Map<String, String> atomTemplate;

    public InputController(Scanner input, Timer timer) {
        this.timer = timer;
        this.input = input;
        ObjectMapper objectMapper = new ObjectMapperPreparer().produceInstance();
        try {
            rssTemplate = objectMapper.convertValue(
                    objectMapper.readTree(InputController.class.getResourceAsStream("/templates/rss-template.json"))
                    ,Map.class);
            atomTemplate = objectMapper.convertValue(
                    objectMapper.readTree(InputController.class.getResourceAsStream("/templates/atom-template.json"))
                    ,Map.class);
        } catch (IOException e) {
            log.error("An error occured while processing json templates,", e);
            rssTemplate = new HashMap<>();
            atomTemplate = new HashMap<>();
        }
    }

    @Override
    public void run() {
        generateFeedSubscription();



        do {
            Thread.yield();
        } while (Thread.interrupted());
    }

    public void generateFeedSubscription() {
        String name = "CNN Top stories";
        RssConfiguration rssConfiguration = new RssConfiguration();
        rssConfiguration.setFilePath("G:\\DISKRELATED\\UNIVER\\git\\RssReader\\target\\x.txt");
        rssConfiguration.setRequestPeriod(1000);
        rssConfiguration.setElementsPerRequest(5);
        rssConfiguration.setUrl("http://rss.cnn.com/rss/cnn_topstories.rss");
        rssConfiguration.setActiveTags(rssTemplate);
        AppConfiguration.getRssFeeds().put(name, rssConfiguration);
        TimerTask rssTest = new ApacheRssReader(name);
        System.out.println("scheduling the task");
        timer.schedule(rssTest, rssConfiguration.getRequestPeriod());
    }
}
