package com.example.marketservice.repository;

import com.example.commondto.constant.ListingStatus;
import com.example.marketservice.model.entity.MarketListing;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketListingRepository extends JpaRepository<MarketListing, String>, JpaSpecificationExecutor<MarketListing> {

    List<MarketListing> findByStatus(ListingStatus status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MarketListing l 
        SET l.status = 'EXPIRED' 
        WHERE l.type = 'FIXED_PRICE' 
          AND l.endTime < :now 
          AND l.status = 'ACTIVE'
    """)
    int expireFixedPriceListings(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MarketListing l 
        SET l.status = CASE 
            WHEN l.highestBidderId IS NOT NULL THEN 'SOLD'
            ELSE 'EXPIRED'
        END
        WHERE l.type = 'AUCTION' 
          AND l.endTime < :now 
          AND l.status = 'ACTIVE'
    """)
    int expireAuctionListings(@Param("now") LocalDateTime now);
}
