package com.example.marketservice.model.filter;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import com.example.commondto.dto.filter.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MarketListingFilter extends BaseFilter {

    private String sellerId;        // ID người bán
    private String creditId;        // ID tín chỉ carbon
    private ListingType type;       // FIXED_PRICE / AUCTION
    private ListingStatus status;   // ACTIVE / SOLD / CANCELED / EXPIRED

    private Double minPrice;        // giá thấp nhất
    private Double maxPrice;        // giá cao nhất

    private Double minQuantity;     // số lượng nhỏ nhất
    private Double maxQuantity;     // số lượng lớn nhất

    private LocalDateTime startFrom; // thời điểm bắt đầu >=
    private LocalDateTime endBefore; // thời điểm kết thúc <=
}
