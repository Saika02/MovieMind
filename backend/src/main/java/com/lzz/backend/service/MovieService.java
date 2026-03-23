package com.lzz.backend.service;

import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;

import java.util.List;

public interface MovieService {
    MovieDetailResponse get(Long id);
    List<MovieListResponse> list(String keyword);
    PageResponse<MovieListResponse> listPage(String keyword, int page, int size);
}
