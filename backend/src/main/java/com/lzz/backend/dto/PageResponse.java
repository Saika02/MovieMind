package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private int page;
    private int size;
    private long total;
    private List<T> items;
}
