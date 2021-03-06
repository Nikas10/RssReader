package com.company.nikas.controller;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.integrations.impl.ApacheRssReader;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.ActiveStreamMonitor;
import com.company.nikas.system.processing.impl.RomeRssProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

/**
 * Class that manages feed subscriptions, user input and active connection monitor state.
 */
@Slf4j
public class InputController {

    /** Service for launching subscriptions*/
    private ScheduledExecutorService timer;
    private Boolean shutdownFlag = false;
    private Scanner input;
    /** Thread object for active connection monitor. Declared explicitly for a quicker interrupt procedure.*/
    private Thread monitor;
    /** Contains all information about planned reguests for RSS subscriptions for managing operations*/
    private Map<String, ScheduledFuture> executionSchedule;

    public InputController(Scanner input, ScheduledExecutorService timer) {
        this.timer = timer;
        this.input = input;
        this.executionSchedule = new ConcurrentHashMap<>();
    }

    /**
     * Run method. Activates connection monitor, prepares feed subscriptions and processes
     * user input. On shutdown saves user data and kills subscription service.
     */
    public void run() {
        launchConnectionMonitor();
        prepareFeedSubscriptions();
        while (!shutdownFlag) {
            processUserInput();
        }
        saveApplicationData();
        timer.shutdown();
    }

    /**
     * Method for activating connection monitor, responsible for file operations.
     * Starts monitor in a new thread.
     */
    private void launchConnectionMonitor() {
        monitor = new Thread(new ActiveStreamMonitor(
                RomeRssProcessor.class));
        monitor.start();
    }

    /**
     * Saves RSS feed subscriptions to a configuration.json file
     * in JSON format.
     */
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
     * Creates RSS feed subscriptions from initial application configuration, if one is present (not empty)
     */
    private void prepareFeedSubscriptions() {
        AppConfiguration.getRssFeeds().entrySet().parallelStream().forEach(entry -> {
            RssConfiguration rssConfiguration = entry.getValue();
            Runnable rssTest = new ApacheRssReader(entry.getKey(), HttpClients.createDefault());
            if (isNull(rssConfiguration.getLastUpdateDate())) {
                rssConfiguration.setLastUpdateDate(LocalDateTime.now());
            }
            long diff = ChronoUnit.MILLIS.between(LocalDateTime.now(), rssConfiguration.getLastUpdateDate())
                    + rssConfiguration.getRequestPeriod();
            if (diff <= 0) diff = 0L;
            executionSchedule.put(entry.getKey(),
            timer.scheduleAtFixedRate(rssTest, diff, rssConfiguration.getRequestPeriod(), TimeUnit.MILLISECONDS));
        });
    }

    /**
     * Manages user input, redirecting to other processing methods.
     */
    private void processUserInput() {
        printMainMenu();
        String command = input.nextLine();
        switch (command) {
            case ("exit") : {
                monitor.interrupt();
                shutdownFlag = true;
                input.close();
                return;
            }
            case ("list") : {
                printAllFeeds();
                break;
            }
            case ("purge") : {
                removeAllFeeds();
                break;
            }
            case ("disable") : {
                disableFeed();
                break;
            }
            case ("create") : {
                createFeed();
                break;
            }
            case ("manage") : {
                manageFeed();
                break;
            }
            case ("help") : {
                printMainMenu();
                break;
            }
        }
    }

    /**
     * Prints main menu with list of all application features.
     */
    private void printMainMenu() {
        System.out.println("1. Create feed. (create)");
        System.out.println("2. List all feeds. (list)");
        System.out.println("3. Manage feed by name. (manage)");
        System.out.println("4. Disable feed by name. (disable)");
        System.out.println("5. Disable all feeds. (purge)");
        System.out.println("6. Exit. (exit)");
    }

    /**
     * Prints all feed names and RSS urls.
     */
    private void printAllFeeds() {
        System.out.println("Available feeds: ");
        AppConfiguration.getRssFeeds()
                .forEach((key, value) ->
                        System.out.println(key + " : " + value.getUrl()));
    }

    /**
     * Purges all current subscriptions
     */
    private void removeAllFeeds() {
        timer.shutdown();
        timer = Executors.newScheduledThreadPool(4);
        executionSchedule = new ConcurrentHashMap<>();
        AppConfiguration.setRssFeeds(new ConcurrentHashMap<>());
        log.info("Feed subscriptions were purged!");
    }

    /**
     * Disables feed subscription by feed name. Name is entered by user.
     */
    private void disableFeed() {
        System.out.println("Enter feed name: ");
        String feedName = input.nextLine();
        if (isNull(AppConfiguration.getRssFeeds().remove(feedName))) {
            log.info("Feed by name {} was not found!", feedName);
        } else {
            executionSchedule.remove(feedName).cancel(false);
            AppConfiguration.getRssFeeds().remove(feedName);
            log.info("Feed {} was removed!", feedName);
        }
    }

    /**
     * Creates and launches a new RSS feed subscription. Name, URL, and other options
     * are entered by user.
     */
    private void createFeed() {
        System.out.println("Enter feed name: ");
        String feedName = input.nextLine();
        if (isNull(AppConfiguration.getRssFeeds().get(feedName))) {
            RssConfiguration rssConfiguration = new RssConfiguration();
            System.out.println("Enter feed url: ");
            String url = input.nextLine().trim();

            System.out.println("Enter feed request delay: ");
            long delay = input.nextLong();

            System.out.println("Enter feed entry limit: ");
            int limit = input.nextInt();

            String file = "./" + url.trim()
                    .replace("\\/\\/:", "/")
                    .replaceAll("[;:*?\"<>|&']", "/");

            System.out.println("Enter tags to parse: ");
            String tags = input.nextLine();
            rssConfiguration.setActiveTags(Stream.of(tags.trim().split(",")).filter(entry ->
                AppConfiguration.getSyndTemplate().containsKey(entry)
            ).collect(Collectors.toSet()));
            rssConfiguration.setElementsPerRequest(limit);
            rssConfiguration.setFilePath(file);
            rssConfiguration.setRequestPeriod(delay);
            rssConfiguration.setUrl(url);
            AppConfiguration.getRssFeeds().put(feedName, rssConfiguration);
            ApacheRssReader reader = new ApacheRssReader(feedName, HttpClients.createDefault());
            String initialData = reader.getRssFeed(rssConfiguration.getUrl());
            if (!(initialData.contains("entry") && initialData.contains("item"))) {
                log.error("Feed with name {} is invalid!", feedName);
                return;
            }
            Runnable task = reader;
            executionSchedule.put(feedName,
                    timer.scheduleAtFixedRate(task, rssConfiguration.getRequestPeriod(),
                            rssConfiguration.getRequestPeriod(), TimeUnit.MILLISECONDS));
            log.info("Feed {} was succesfully added", feedName);
        } else {
            log.error("Feed with name {} already exists!", feedName);
        }
    }

    /**
     * Changes feed subscription parameters by feed name. Name is entered by user.
     * In case of changing delay between requests, subscription is rescheduled.
     */
    private void manageFeed() {
        System.out.println("Enter feed name: ");
        String feedName = input.nextLine();
        RssConfiguration rssConfiguration = AppConfiguration.getRssFeeds().get(feedName);
        if (isNull(rssConfiguration)) {
            log.info("Feed by name {} was not found!", feedName);
        } else {
            printOptionMenu();
            String option = input.nextLine();
            switch (option) {
                case ("delay") : {
                    System.out.println("Enter feed request delay: ");
                    long delay = input.nextLong();
                    rssConfiguration.setRequestPeriod(delay);
                    executionSchedule.get(feedName).cancel(false);
                    timer.scheduleAtFixedRate(new ApacheRssReader(feedName, HttpClients.createDefault()),
                            0, delay, TimeUnit.MILLISECONDS);
                    break;
                }
                case ("tags") : {
                    System.out.println("All available tags: ");
                    printAllTags();
                    System.out.println("Enter feed tags: ");
                    String line = input.nextLine();
                    List<String> tags = Arrays.asList(StringUtils.split(line, ','));
                    Set<String> formattedTags = tags.stream()
                            .distinct()
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    rssConfiguration.setActiveTags(formattedTags);
                    break;
                }
                case ("file") : {
                    System.out.println("Enter path to file: ");
                    String fileName = input.nextLine();
                    rssConfiguration.setFilePath(fileName);
                    break;
                }
                case ("limit") : {
                    System.out.println("Enter feed entry limitation: ");
                    int limit = input.nextInt();
                    rssConfiguration.setElementsPerRequest(limit);
                    break;
                }
            }
        }
    }

    /**
     * Prints all RSS tags according to ROME library SyndEntry class,
     * which supports both RSS all version and Atom notations.
     */
    private void printAllTags() {
        AppConfiguration.getSyndTemplate()
                .keySet()
                .forEach(System.out::println);
    }

    /**
     * Prints subscription management options menu with all
     * available features.
     */
    private void printOptionMenu() {
        System.out.println("Feed options:");
        System.out.println("1. Delay period (delay)");
        System.out.println("2. Available tags (tags)");
        System.out.println("3. File name (file)");
        System.out.println("3. Elements per requests (limit)");
    }
}
