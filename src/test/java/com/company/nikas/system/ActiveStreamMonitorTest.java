package com.company.nikas.system;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.config.ObjectMapperPreparer;
import com.company.nikas.controller.InputController;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.system.processing.impl.RomeRssProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActiveStreamMonitorTest {

    private static ObjectMapper objectMapper = new ObjectMapperPreparer().produceInstance();
    private static String rss;
    private static RssConfiguration rssConfiguration;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void getData() throws IOException {
        rssConfiguration = new RssConfiguration();
        AppConfiguration.setSyndTemplate(objectMapper.convertValue(
                objectMapper.readTree(InputController.class.
                        getResourceAsStream("/templates/synd-template.json"))
                , Map.class));
    }

    @AfterClass
    public static void cleanData() {
        File file = new File(rssConfiguration.getFilePath());
        file.delete();
    }

    @Before
    public void init() throws IOException {
        rss = IOUtils.toString(this.getClass().getResource("/rss-dump.xml"), StandardCharsets.UTF_8);
        setDefaultParameters(rssConfiguration);
        AppConfiguration.setRssFeeds(new ConcurrentHashMap<>());
        AppConfiguration.setActiveConnections(new ConcurrentHashMap<>());
        File file = new File(rssConfiguration.getFilePath());
        file.delete();
    }

    @Test
    public void testWriteToFileSingle() throws InterruptedException, IOException {
        pushFeed("feed 1");
        handleThread();
        File file = new File(rssConfiguration.getFilePath());
        String content = new String(Files.readAllBytes( file.toPath()));
        assertTrue(file.isFile());
        assertTrue(AppConfiguration.getActiveConnections().isEmpty());
        assertTags(content);
    }

    @Test
    public void testNotWriteToFileSingle() throws InterruptedException {
        AppConfiguration.getActiveConnections().put("mock feed", rss);
        handleThread();
        File file = new File(rssConfiguration.getFilePath());
        assertTrue(AppConfiguration.getActiveConnections().isEmpty());
        assertFalse(file.exists());
    }

    @Test
    public void testWriteToFileMultiple() throws InterruptedException, IOException{
        pushFeed("feed 1");
        pushFeed("feed 2");
        handleThread();
        File file = new File(rssConfiguration.getFilePath());
        String content = new String(Files.readAllBytes( file.toPath()));
        assertTrue(file.isFile());
        assertTrue(content.contains("feed 1"));
        assertTrue(content.contains("feed 2"));
        assertTrue(AppConfiguration.getActiveConnections().isEmpty());
        assertTags(content);
    }

    @Test
    public void testWriteToFileMultipleButOneFails() throws InterruptedException, IOException{
        pushFeed("feed 1");
        AppConfiguration.getActiveConnections().put("feed 2", rss);
        handleThread();
        File file = new File(rssConfiguration.getFilePath());
        String content = new String(Files.readAllBytes( file.toPath()));
        assertTrue(AppConfiguration.getActiveConnections().isEmpty());
        assertTrue(file.isFile());
        assertTrue(content.contains("feed 1"));
        assertFalse(content.contains("feed 2"));
        assertTags(content);
    }

    private void handleThread() throws InterruptedException {
        Thread monitor = new Thread(new ActiveStreamMonitor(
                new RomeRssProcessor()));
        monitor.start();
        Thread.sleep(5000);
        monitor.interrupt();
    }

    private void pushFeed(String feedName) {
        AppConfiguration.getRssFeeds().put(feedName, rssConfiguration);
        AppConfiguration.getActiveConnections().put(feedName, rss);
    }

    private void assertTags(String content) {
        assertTrue(content.contains("authors"));
        assertTrue(content.contains("description"));
        assertTrue(content.contains("description"));
        assertFalse(content.contains("published"));
        assertFalse(content.contains("enclosure"));
        assertFalse(content.contains("category"));
    }

    private void setDefaultParameters(RssConfiguration rssConfiguration) {
        rssConfiguration.setUrl("some url");
        rssConfiguration.setRequestPeriod(1000L);
        rssConfiguration.setLastUpdateDate(LocalDateTime.now());
        rssConfiguration.setFilePath("./testFile.txt");
        rssConfiguration.setElementsPerRequest(1);
        Set<String> tags = new HashSet<>();
        tags.add("authors");
        tags.add("description");
        tags.add("title");
        rssConfiguration.setActiveTags(tags);
    }
}
