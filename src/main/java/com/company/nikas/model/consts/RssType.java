package com.company.nikas.model.consts;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum RssType {

    @JsonProperty("atom")
    ATOM,

    @JsonProperty("rss")
    RSS

}
