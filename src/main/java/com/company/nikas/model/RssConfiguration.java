package com.company.nikas.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RssConfiguration implements Serializable {

    private String url;
    private String filePath;
    private Long requestPeriod;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private LocalDateTime lastUpdateDate;

    private Integer elementsPerRequest;
    private Set<String> activeTags;

}
