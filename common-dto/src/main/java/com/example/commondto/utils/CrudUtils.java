package com.example.commondto.utils;

import com.example.commondto.dto.filter.BaseFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class CrudUtils {
    public static Pageable createPageable(BaseFilter filterRequest) {
        // Tạo sort
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                filterRequest.getSort(),
                filterRequest.getField() != null ? filterRequest.getField() : "id"
        );

        // Tạo pageable
        return PageRequest.of(filterRequest.getPage(), filterRequest.getEntry(), sort);
    }
}
