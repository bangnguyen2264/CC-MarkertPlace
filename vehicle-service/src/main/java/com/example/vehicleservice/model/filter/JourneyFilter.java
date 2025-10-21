package com.example.vehicleservice.model.filter;

import com.example.commondto.dto.filter.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class JourneyFilter extends BaseFilter {
    private String journeyId;
    private String journeyStatus;
}
