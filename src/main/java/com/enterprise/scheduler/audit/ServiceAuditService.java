package com.enterprise.scheduler.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ServiceAuditService {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuditService.class);
    private static final String SERVICE_NAME = "schedulerapp-service";

    public void logAction(String action, String scanId, String status) {
        String correlationId = MDC.get("correlationId");
        String timestamp = Instant.now().toString();

        log.info("SERVICE_AUDIT {{\"service\":\"{}\",\"action\":\"{}\",\"scanId\":\"{}\","
                        + "\"timestamp\":\"{}\",\"correlationId\":\"{}\",\"status\":\"{}\"}}",
                SERVICE_NAME, action, scanId, timestamp, correlationId, status);
    }
}
