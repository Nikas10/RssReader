package com.company.nikas.model;

import com.company.nikas.model.consts.RssType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RssConfiguration implements Serializable {

    private String url;
    private RssType rssType = RssType.RSS;
    private String filePath;
    private Map<String, Boolean> activeTags;

}
