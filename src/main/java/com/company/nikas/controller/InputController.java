package com.company.nikas.controller;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.integrations.impl.ApacheRssReader;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.ActiveStreamMonitor;
import com.company.nikas.system.processing.impl.RomeRssProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
public class InputController implements Runnable {

    private Timer timer;
    private Boolean shutdownFlag = false;
    private Scanner input;
    private Thread monitor;

    public InputController(Scanner input, Timer timer) {
        this.timer = timer;
        this.input = input;
    }

    @Override
    public void run() {
        launchConnectionMonitor();
        generateFeedSubscription();
        prepareFeedSubscriptions();
        printMainMenu();
        while (!Thread.interrupted() && !shutdownFlag) {
            processUserInput();
        }
        saveApplicationData();
        timer.cancel();
    }

    private void launchConnectionMonitor() {
        monitor = new Thread(new ActiveStreamMonitor(
                new RomeRssProcessor()));
        monitor.start();
    }

    private void saveApplicationData() {
        try {
            File toSave = manageFile(System.getProperty("user.dir") + "/configuration.json");
            Files.write(toSave.toPath(),
                    new ObjectMapperPreparer().produceInstance().writeValueAsBytes(AppConfiguration.getRssFeeds()),
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            log.error("Unable to save application configuration, ", e);
        }
    }

    private File manageFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
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

    private void processUserInput() {
        String command = input.nextLine();
        if (command.equals("exit")) {
            log.info("access");
            monitor.interrupt();
            shutdownFlag = true;
            input.close();
            return;
        } else {

        }
    }

    private void printMainMenu() {
        System.out.println("Menu options (enter specified command to proceed): ");
        System.out.println("1. Create feed. (create)");
        System.out.println("2. List all feeds. (list)");
        System.out.println("3. Manage feed by name. (manage [feed name])");
        System.out.println("4. Disable feed by name. (disable [feed name])");
        System.out.println("5. Disable all feeds. (purge)");
        System.out.println("6. Exit. (exit)");
    }
}
