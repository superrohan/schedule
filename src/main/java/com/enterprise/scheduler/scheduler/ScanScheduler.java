package com.enterprise.scheduler.scheduler;

import com.enterprise.scheduler.audit.ServiceAuditService;
import com.enterprise.scheduler.client.ControllerClient;
import com.enterprise.scheduler.filter.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScanScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScanScheduler.class);

    private final ControllerClient controllerClient;
    private final ServiceAuditService auditService;

    public ScanScheduler(ControllerClient controllerClient, ServiceAuditService auditService) {
        this.controllerClient = controllerClient;
        this.auditService = auditService;
    }

    @Scheduled(cron = "${scheduler.scan.cron}")
    public void triggerScanCycle() {
        String correlationId = CorrelationIdFilter.generateCorrelationId();
        String scanId = UUID.randomUUID().toString();

        log.info("Scan cycle triggered. scanId={}, correlationId={}", scanId, correlationId);
        auditService.logAction("LAUNCH_SCAN", scanId, "INITIATED");

        try {
            controllerClient.launchScan(scanId);
            auditService.logAction("LAUNCH_SCAN", scanId, "SUCCESS");
            log.info("Scan cycle completed successfully. scanId={}", scanId);
        } catch (Exception e) {
            auditService.logAction("LAUNCH_SCAN", scanId, "FAILED");
            log.error("Scan cycle failed. scanId={}, error={}", scanId, e.getMessage(), e);
        } finally {
            CorrelationIdFilter.clearCorrelationId();
        }
    }
}
