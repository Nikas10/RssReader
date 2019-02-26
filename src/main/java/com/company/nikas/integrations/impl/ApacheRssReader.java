package com.company.nikas.integrations.impl;

import com.company.nikas.exceptions.InvalidResponseException;
import com.company.nikas.integrations.RssReader;
import com.company.nikas.config.AppConfiguration;
import com.company.nikas.model.RssConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Class, responsible for obtaining RSS feed subscription by HTTP requests.
 * Uses Apache HTTP client implementation
 */
@Slf4j
public class ApacheRssReader implements RssReader, Runnable {

    private Map<String, String> activeConnections;
    private Map<String, RssConfiguration> rssFeeds;
    private CloseableHttpClient httpClient;

    private String requiredRss;

    public ApacheRssReader(String requiredRss, CloseableHttpClient client) {
        this.activeConnections = AppConfiguration.getActiveConnections();
        this.rssFeeds = AppConfiguration.getRssFeeds();
        this.requiredRss = requiredRss;
        this.httpClient = client;
    }

    /**
     * Gets RSS feed data and puts it to active connections storage.
     * Updates subscription's last update date.
     */
    @Override
    public void run() {
        RssConfiguration rssConfig = rssFeeds.get(requiredRss);
        if (isNull(rssConfig)) {
            log.error("RSS configurations for feed {} is not found!", requiredRss);
            return;
        }
        String is = getRssFeed(rssConfig.getUrl());
        activeConnections.put(requiredRss, is);
        rssConfig.setLastUpdateDate(LocalDateTime.now());
    }

    @Override
    public String getRssFeed(String url) {
        CloseableHttpResponse response = initiateConnection(url);
        if (isNull(response)) {
            return StringUtils.EMPTY;
        }
        try {
            return getHttpReponse(response);
        }
        finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("An error occured while closing Http connection, {}", e);
            }
        }
    }

    /**
     * Creates HTTP connection by giver URL and executes request.
     * @param url Web resource address
     * @return Response from server, null in case of error.
     */
    private CloseableHttpResponse initiateConnection(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            return httpClient.execute(httpGet);
        } catch (IOException e) {
            log.error("An error occured during executing Http request, {}", e);
            return null;
        }
    }

    /**
     * Gets response body, received during request to server.
     * @param response Raw response from server.
     * @return response body in String format.
     */
    private String getHttpReponse(CloseableHttpResponse response) {
        try {
            HttpEntity entity = response.getEntity();
            validateStatusCode(response.getStatusLine().getStatusCode());
            String answer = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            return answer;
        } catch (IOException | InvalidResponseException e) {
            log.error("An error occured while parsing server response, {}", e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Validates HTTP status code, throws exception if code is invalid
     * @param status status code
     */
    private void validateStatusCode(int status) {
        if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new InvalidResponseException("Response status code is not allowed!");
        }
    }
}
