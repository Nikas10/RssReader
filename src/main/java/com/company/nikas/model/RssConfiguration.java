package com.company.nikas.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RssConfiguration implements Serializable {

    private String url;
    private String filePath;
    private Long requestPeriod;

    private LocalDateTime lastUpdateDate;

    private Integer elementsPerRequest;
    private Set<String> activeTags;
}
