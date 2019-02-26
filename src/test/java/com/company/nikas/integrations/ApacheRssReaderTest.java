package com.company.nikas.integrations;

import com.company.nikas.integrations.impl.ApacheRssReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApacheRssReaderTest {

    private CloseableHttpClient httpClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init(){
        httpClient = mock(CloseableHttpClient.class);
    }

    @Test
    public void testResponseFromHttp() throws IOException {
        String expected = IOUtils.toString(this.getClass().getResource("/rss-dump.xml"), StandardCharsets.UTF_8);
        InputStream io = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(io);
        when(response.getEntity()).thenReturn(entity);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(
                new ProtocolVersion("http", 1, 1),200, "OK"));
        when(httpClient.execute(any())).thenReturn(response);
        String actual = new ApacheRssReader("mock rss", httpClient)
                .getRssFeed("http://rss.cnn.com/rss/cnn_topstories.rss");
        assertEquals(expected, actual);
    }

    @Test
    public void testNoHostFromHttp() {
        String actual = new ApacheRssReader("mock rss", HttpClients.createDefault())
                .getRssFeed("http://dummyurl-sas8udkn2luqhr.com/2342623hfghs/cnn_1252dfses.rss");
        assertEquals(StringUtils.EMPTY, actual);
    }

    @Test
    public void testBadStatusCodeFromHttp() throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(
                new ProtocolVersion("http", 1, 1),400, "Bad_request"));
        when(httpClient.execute(any())).thenReturn(response);
        String actual = new ApacheRssReader("mock rss", httpClient)
                .getRssFeed("http://rss.cnn.com/rss/cnn_topstories.rss");
        assertEquals(StringUtils.EMPTY, actual);
    }
}
