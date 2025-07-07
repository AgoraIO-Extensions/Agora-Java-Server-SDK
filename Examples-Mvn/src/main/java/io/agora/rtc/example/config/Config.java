package io.agora.rtc.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import java.lang.String;

@Data
@ConfigurationProperties(prefix = "agora")
@Component
public class Config {
    private String appId = "";
    private String appToken = "";
}
