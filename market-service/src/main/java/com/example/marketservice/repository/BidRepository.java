package com.example.marketservice.repository;

import com.example.marketservice.model.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, String>, JpaSpecificationExecutor<Bid> {
    List<Bid> findByListingId(String listingId);
}
