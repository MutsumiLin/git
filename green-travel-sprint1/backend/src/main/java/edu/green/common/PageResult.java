package edu.green.common;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long total) {
    public static void validate(int page, int size) {
        if (page < 0 || page > 100000 || size < 1 || size > 100) {
            throw BusinessException.badRequest("page 应为 0 至 100000，size 应为 1 至 100");
        }
    }
}
