package com.company.nikas.integrations.impl;

import com.company.nikas.exceptions.InvalidResponseException;
import com.company.nikas.integrations.RssReader;
import com.company.nikas.config.AppConfiguration;
import com.company.nikas.model.RssConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.io.EmptyInputStream;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TimerTask;

import static java.util.Objects.isNull;

@Slf4j
public class ApacheRssReader extends TimerTask implements RssReader {

    private Map<String, InputStream> activeConnections;
    private Map<String, RssConfiguration> rssFeeds;
    private String requiredRss;

    public ApacheRssReader(AppConfiguration appConfiguration, String requiredRss) {
        this.activeConnections = appConfiguration.getActiveConnections();
        this.rssFeeds = appConfiguration.getRssFeeds();
        this.requiredRss = requiredRss;
    }

    @Override
    public void run() {
        RssConfiguration rssConfig = rssFeeds.get(requiredRss);
        if (isNull(rssConfig)) {
            log.error("RSS configurations for feed {} is not found!", requiredRss);
            return;
        }
        activeConnections.put(requiredRss, getRssFeed(rssConfig.getUrl()));
    }

    @Override
    public InputStream getRssFeed(String url) {
        CloseableHttpResponse response = initiateConnection(url);
        if (isNull(response)) {
            return EmptyInputStream.INSTANCE;
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

    private CloseableHttpResponse initiateConnection(String url) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            return httpclient.execute(httpGet);
        } catch (IOException e) {
            log.error("An error occured during executing Http request, {}", e);
            return null;
        }
    }

    private InputStream getHttpReponse(CloseableHttpResponse response) {
        try {
            HttpEntity entity = response.getEntity();
            validateStatusCode(response.getStatusLine().getStatusCode());
            EntityUtils.consume(entity);
            return entity.getContent();
        } catch (IOException | InvalidResponseException e) {
            log.error("An error occured while parsing server response, {}", e);
            return EmptyInputStream.INSTANCE;
        }
    }

    private void validateStatusCode(int status) {
        if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new InvalidResponseException("Response status code is not allowed!");
        }
    }
}
