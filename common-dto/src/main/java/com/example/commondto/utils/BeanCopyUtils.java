package com.example.commondto.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;

public class BeanCopyUtils {

    public static void copyNonNullProperties(Object source, Object target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (source == null || target == null) {
            return;
        }

        BeanUtils.copyProperties(target, source); // Sao chép ngược để giữ giá trị hiện tại nếu source null

        // Lấy tất cả các thuộc tính từ source và kiểm tra
        var sourceClass = source.getClass();
        var targetClass = target.getClass();

        BeanUtils.describe(source).forEach((key, value) -> {
            try {
                if (value != null) {
                    if (value instanceof String && !StringUtils.hasText((String) value)) {
                        return; // Bỏ qua chuỗi rỗng
                    }
                    BeanUtils.setProperty(target, key, value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to copy property: " + key, e);
            }
        });
    }
}