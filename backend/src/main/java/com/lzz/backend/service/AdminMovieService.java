package com.lzz.backend.service;

import com.lzz.backend.dto.AdminMovieUpsertRequest;
import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;

public interface AdminMovieService {
    MovieDetailResponse get(Long id);
    PageResponse<MovieListResponse> page(String keyword, int page, int size);
    MovieDetailResponse create(AdminMovieUpsertRequest request);
    MovieDetailResponse update(Long id, AdminMovieUpsertRequest request);
    void delete(Long id);
}
