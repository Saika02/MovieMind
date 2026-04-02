package com.lzz.backend.service;

import com.lzz.backend.entity.Movie;

public interface MovieEmbeddingService {
    void syncMovieEmbedding(Movie movie);
    void deleteMovieEmbedding(Long movieId);
}
