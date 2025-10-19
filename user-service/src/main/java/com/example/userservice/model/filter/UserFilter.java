package com.example.userservice.model.filter;

import com.example.commondto.dto.filter.BaseFilter;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserFilter extends BaseFilter {
    private String fullName;
    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;
}
