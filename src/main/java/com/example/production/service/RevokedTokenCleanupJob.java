package com.example.production.service;

import com.example.production.repositpry.InvalidtedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class RevokedTokenCleanupJob {

    private final InvalidtedTokenRepository invalidtedTokenRepository;

    @Scheduled(fixedDelayString = "${jwt.revoked-token-cleanup-interval-ms:3600000}")
    public void deleteExpiredRevokedTokens() {
        long deleted = invalidtedTokenRepository.deleteByExpirationTimeBefore(new Date());
        if (deleted > 0) {
            log.info("Deleted {} expired revoked tokens", deleted);
        }
    }
}
