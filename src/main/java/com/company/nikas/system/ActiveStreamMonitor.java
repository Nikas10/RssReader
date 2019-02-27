package com.company.nikas.system;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.exceptions.RssConfigurationNotFoundException;
import com.company.nikas.exceptions.RssParserException;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.RssProcessor;
import com.company.nikas.system.processing.impl.RomeRssProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Class, responsible for writing RSS data to files.
 */
@Slf4j
public class ActiveStreamMonitor implements Runnable {

    private Class _class;
    private ObjectMapperPreparer objectMapperPreparer;

    private Map<String, String> activeConnections;
    private Map<String, RssConfiguration> rssFeeds;

    public ActiveStreamMonitor(Class _class) {
        this.activeConnections = AppConfiguration.getActiveConnections();
        this.rssFeeds = AppConfiguration.getRssFeeds();
        this.objectMapperPreparer = new ObjectMapperPreparer();
        this._class = _class;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkRunConditions();
            processStreams();
        }
    }

    /**
     * Checks whether to continue monitoring the system.
     */
    private void checkRunConditions() {
        while (activeConnections.isEmpty() && !Thread.currentThread().isInterrupted()) {
            Thread.yield();
        }
    }

    /**
     * Processes activeConnections collection and writes all received feed data to files.
     * After writing, removes entries from collection.
     */
    private void processStreams() {
        activeConnections.entrySet().parallelStream().forEach(entry -> {
            String feed = entry.getKey();
            RssProcessor rssProcessor;
            try {
                 rssProcessor = (RssProcessor) _class.getConstructor().newInstance();
            } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                rssProcessor = new RomeRssProcessor();
            }
            try {
                checkConfigurationPresense(feed);
                String is = activeConnections.get(feed);
                File toWrite = manageFile(rssFeeds.get(feed).getFilePath());
                for (Map<String, Object> element : rssProcessor.parseFeed(feed, is)) {
                    writeToFile(feed, toWrite, element);
                }
                activeConnections.remove(feed);
            } catch (RssConfigurationNotFoundException | IOException | RssParserException e) {
                log.trace("RSS stream for feed {} will be removed.", feed);
                activeConnections.remove(feed);
            }
        });
    }

    /**
     * Creates file from file path if file does not exist. Else, returns a file object
     * @param filePath Path to file (absolute/relative)
     * @return File object representing found/created file.
     * @throws IOException If file was not created (access violation, bad path, etc)
     */
    private File manageFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    /**
     * Writes parsed feed data to a file.
     * @param feed Feed name
     * @param file File object, representing file to write to
     * @param content Mapped feed content
     * @throws IOException in case writing to file was unsuccessful.
     */
    private synchronized void writeToFile(String feed, File file, Map<String, Object> content)
            throws IOException {
        Files.write(file.toPath(),("\n" + new Date().toString() + ", feed: " + feed + "\n").getBytes(),
                StandardOpenOption.APPEND);
        Files.write(file.toPath(),
                objectMapperPreparer.produceInstance()
                        .writeValueAsBytes(content),
                StandardOpenOption.APPEND);
    }

    /**
     * Checks feed subscription metadata by feed name. Throws exception if feed metadata is not found.
     * @param feed Feed name.
     */
    private void checkConfigurationPresense(String feed) {
        RssConfiguration rssConfiguration = rssFeeds.get(feed);
        if (isNull(rssConfiguration)) {
            log.error("RSS configuration for feed {} is not present in system!", feed);
            throw new RssConfigurationNotFoundException("RSS configuration for feed is not present in system!");
        }
    }
}
