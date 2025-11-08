package com.example.marketservice.model.filter;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import com.example.commondto.dto.filter.BaseFilter;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MarketListingFilter extends BaseFilter {

    private String sellerId;        // ID người bán
    private String creditId;        // ID tín chỉ carbon
    private ListingType type;       // FIXED_PRICE / AUCTION
    private ListingStatus status;   // ACTIVE / SOLD / CANCELED / EXPIRED
    @Positive
    private Double minPrice;        // giá thấp nhất
    @Positive
    private Double maxPrice;        // giá cao nhất
    @Positive
    private Double minQuantity;     // số lượng nhỏ nhất
    @Positive
    private Double maxQuantity;     // số lượng lớn nhất

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startFrom; // thời điểm bắt đầu >=

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endBefore; // thời điểm kết thúc <=
}
