package com.example.userservice.model.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.Sort;
@Data
public class UserFilter {
    @Min(value = 0, message = "Page index must be greater than or equal to 0")
    @Schema(defaultValue = "0")
    private int page = 0;
    @Min(value = 1, message = "Entry size must be greater than 0")
    @Schema(defaultValue = "10")
    private int entry = 10;
    @NotBlank(message = "Field to sort must not be blank")
    @Schema(defaultValue = "id")
    private String field = "id";
    @NotNull(message = "Sort direction must not be null")
    @Schema(defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    private Sort.Direction sort = Sort.Direction.DESC;
    private String fullName;
    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;
}
