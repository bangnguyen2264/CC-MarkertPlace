package com.example.commondto.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.domain.Sort;
@Data
public class BaseFilter {
    @Min(value = 0, message = "Page index must be greater than or equal to 0")
    @Schema(defaultValue = "0")
    private int page = 0;
    @Min(value = 1, message = "Entry size must be greater than 0")
    @Schema(defaultValue = "10")
    private int entry = 10;

    @NotBlank(message = "Field to sort must not be blank")
    @Schema(defaultValue = "id")
    private String field = "id";

    @Schema(defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    private Sort.Direction sort = Sort.Direction.DESC;
}
