package com.example.dust.mqttv5demo.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * Airsense 告警通知
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AirsenseWarning {
    private String icao;
    private Integer warningLevel;
    private Float latitude;
    private Float longitude;
    private Integer altitude;
    private Integer altitudeType;
    private Float heading;
    private Integer relativeAltitude;
    private Integer vertTrend;
    private Integer distance;

}
