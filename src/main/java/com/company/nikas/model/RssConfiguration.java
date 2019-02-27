package com.company.nikas.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/**
 * Class, responsible for storing RSS subscription metadata.
 */
@Data
public class RssConfiguration implements Serializable {

    /** Subscription URL address*/
    private String url;
    /** Path to file where feed data will be stored*/
    private String filePath;
    /** Delay between fetching feed data, in millis*/
    private Long requestPeriod;
    /** Feed data last fetch date*/
    private LocalDateTime lastUpdateDate;
    /** Feed's last updated date*/
    private Date lastUpdatedFeedDate = null;
    /** Number of feed entries to be written to file per request*/
    private Integer elementsPerRequest;
    /** Tags, that will be parsed and written to a file*/
    private Set<String> activeTags;
}
