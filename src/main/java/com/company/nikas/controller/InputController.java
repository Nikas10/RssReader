package com.company.nikas.controller;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.integrations.impl.ApacheRssReader;
import com.company.nikas.model.RssConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
public class InputController implements Runnable {

    private Timer timer;
    private Boolean shutdownFlag = false;
    private Scanner input;


    public InputController(Scanner input, Timer timer) {
        this.timer = timer;
        this.input = input;
    }

    @Override
    public void run() {
        generateFeedSubscription();
        prepareFeedSubscriptions();
        do {

        } while (!Thread.interrupted() || !shutdownFlag);
    }

    private void generateFeedSubscription() {
        String name = "CNN Top stories";
        RssConfiguration rssConfiguration = new RssConfiguration();
        rssConfiguration.setFilePath("G:\\DISKRELATED\\UNIVER\\git\\RssReader\\target\\x.txt");
        rssConfiguration.setRequestPeriod(8000L);
        rssConfiguration.setElementsPerRequest(5);
        rssConfiguration.setUrl("http://rss.cnn.com/rss/cnn_topstories.rss");
        rssConfiguration.setActiveTags(AppConfiguration.getRssTemplate().keySet());
        AppConfiguration.getRssFeeds().put(name, rssConfiguration);
    }

    private void prepareFeedSubscriptions() {
        AppConfiguration.getRssFeeds().entrySet().parallelStream().forEach(entry -> {
            RssConfiguration rssConfiguration = entry.getValue();
            TimerTask rssTest = new ApacheRssReader(entry.getKey());
            if (isNull(rssConfiguration.getLastUpdateDate())) {
                rssConfiguration.setLastUpdateDate(LocalDateTime.now());
            }
            long diff = ChronoUnit.MILLIS.between(LocalDateTime.now(), rssConfiguration.getLastUpdateDate())
                    + rssConfiguration.getRequestPeriod();
            if (diff <= 0) diff = 0L;
            timer.schedule(rssTest, diff,
                    rssConfiguration.getRequestPeriod());
        });
    }
}
