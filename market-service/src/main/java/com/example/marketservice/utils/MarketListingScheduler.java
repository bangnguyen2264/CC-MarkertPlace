package com.example.marketservice.utils;

import com.example.marketservice.repository.MarketListingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketListingScheduler {

    private final MarketListingRepository repository;

    // Chạy mỗi 5 phút
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void expireListings() {
        LocalDateTime now = LocalDateTime.now();

        int fixedExpired = repository.expireFixedPriceListings(now);
        int auctionExpired = repository.expireAuctionListings(now);

        if (fixedExpired > 0 || auctionExpired > 0) {
            log.info("✅ [Scheduler] Updated listings: {} fixed-price expired, {} auction ended at {}",
                    fixedExpired, auctionExpired, now);
        }
    }
}
