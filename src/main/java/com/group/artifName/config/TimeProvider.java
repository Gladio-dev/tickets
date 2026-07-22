package com.group.artifName.config;


import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeProvider {

    private static final ZoneId MEXICO_ZONE = ZoneId.of("America/Mexico_City");

    public LocalDateTime now() {
        return LocalDateTime.now(MEXICO_ZONE);
    }
}