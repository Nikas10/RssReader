package com.company.nikas.system;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.exceptions.RssConfigurationNotFoundException;
import com.company.nikas.model.RssConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
public class ActiveStreamMonitor implements Runnable {
    private Map<String, InputStream> activeConnections;
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
    }

    private void checkRunConditions() {
        while (activeConnections.isEmpty()) {
            Thread.yield();
        }
    }

    private void processStreams() {
        for (String feed: activeConnections.keySet()) {
            try {
                checkConfigurationPresense(feed);
                RssConfiguration rssConfiguration = rssFeeds.get(feed);
                InputStream is = activeConnections.get(feed);
                File toWrite = manageFile(rssConfiguration.getFilePath());
                writeToFile(toWrite, is);
            } catch (RssConfigurationNotFoundException | IOException e) {
                log.trace("RSS stream for feed {} will be removed.", feed);
                rssFeeds.remove(feed);
            }
        }
    }

    private File manageFile(String filePath) throws IOException{
        File file = new File(filePath);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    private synchronized void writeToFile(File file, InputStream content) throws IOException {
        Files.write(file.toPath(), IOUtils.toByteArray(content), StandardOpenOption.APPEND);
    }

    private void checkConfigurationPresense(String feed) {
        RssConfiguration rssConfiguration = rssFeeds.get(feed);
        if (isNull(rssConfiguration)) {
            log.error("RSS configuration for feed {} is not present in system!", feed);
            throw new RssConfigurationNotFoundException("RSS configuration for feed is not present in system!");
        }
    }
}
