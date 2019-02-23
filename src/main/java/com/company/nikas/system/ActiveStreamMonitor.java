package com.company.nikas.system;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.exceptions.RssConfigurationNotFoundException;
import com.company.nikas.model.RssConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
public class ActiveStreamMonitor implements Runnable {

    static Logger logger = Logger.getLogger(ActiveStreamMonitor.class);
    static {
        logger.setLevel(Level.INFO);
    }

    private Map<String, String> activeConnections;
    private Map<String, RssConfiguration> rssFeeds;

    public ActiveStreamMonitor(AppConfiguration appConfiguration) {
        this.activeConnections = appConfiguration.getActiveConnections();
        this.rssFeeds = appConfiguration.getRssFeeds();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkRunConditions();
            processStreams();
        }
        log.info("access finish sect");
    }

    private void checkRunConditions() {
        while (activeConnections.isEmpty() && !Thread.currentThread().isInterrupted()) {
            Thread.yield();
        }
    }

    private void processStreams() {
        activeConnections.entrySet().parallelStream().forEach(entry -> {
            String feed = entry.getKey();
            try {
                checkConfigurationPresense(feed);
                RssConfiguration rssConfiguration = rssFeeds.get(feed);
                String is = activeConnections.get(feed);
                File toWrite = manageFile(rssConfiguration.getFilePath());
                writeToFile(toWrite, is);
                activeConnections.remove(feed);
            } catch (RssConfigurationNotFoundException | IOException e) {
                log.trace("RSS stream for feed {} will be removed.", feed);
                activeConnections.remove(feed);
            }
        });
    }

    private File manageFile(String filePath) throws IOException{
        File file = new File(filePath);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    private synchronized void writeToFile(File file, String content) throws IOException {
        log.info("write to file is active");
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.APPEND);
        log.info("write to file is finished");
    }

    private void checkConfigurationPresense(String feed) {
        RssConfiguration rssConfiguration = rssFeeds.get(feed);
        if (isNull(rssConfiguration)) {
            log.error("RSS configuration for feed {} is not present in system!", feed);
            throw new RssConfigurationNotFoundException("RSS configuration for feed is not present in system!");
        }
    }
}
