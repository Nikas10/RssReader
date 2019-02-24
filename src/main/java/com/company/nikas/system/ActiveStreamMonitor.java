package com.company.nikas.system;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.exceptions.RssConfigurationNotFoundException;
import com.company.nikas.exceptions.RssParserException;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.RssProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
public class ActiveStreamMonitor implements Runnable {

    private RssProcessor rssProcessor;
    private ObjectMapperPreparer objectMapperPreparer;

    private Map<String, String> activeConnections;
    private Map<String, RssConfiguration> rssFeeds;

    public ActiveStreamMonitor(RssProcessor rssProcessor) {
        this.activeConnections = AppConfiguration.getActiveConnections();
        this.rssFeeds = AppConfiguration.getRssFeeds();
        this.objectMapperPreparer = new ObjectMapperPreparer();
        this.rssProcessor = rssProcessor;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkRunConditions();
            processStreams();
        }
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
                writeToFile(feed, toWrite, getParsedFeed(feed, is));
                activeConnections.remove(feed);
            } catch (RssConfigurationNotFoundException | IOException | RssParserException e) {
                log.trace("RSS stream for feed {} will be removed.", feed);
                activeConnections.remove(feed);
            }
        });
    }

    private File manageFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    private List<Map<String, Object>> getParsedFeed(String feedId, String content)
            throws RssParserException {
        return rssProcessor.parseFeed(feedId, content);
    }

    private synchronized void writeToFile(String feed, File file, List<Map<String, Object>> content)
            throws IOException {
        Files.write(file.toPath(),("\n" + new Date().toString() + ", feed: " + feed + "\n").getBytes(),
                StandardOpenOption.APPEND);
        Files.write(file.toPath(),
                objectMapperPreparer.produceInstance()
                        .writeValueAsBytes(content),
                StandardOpenOption.APPEND);
    }

    private void checkConfigurationPresense(String feed) {
        RssConfiguration rssConfiguration = rssFeeds.get(feed);
        if (isNull(rssConfiguration)) {
            log.error("RSS configuration for feed {} is not present in system!", feed);
            throw new RssConfigurationNotFoundException("RSS configuration for feed is not present in system!");
        }
    }
}
