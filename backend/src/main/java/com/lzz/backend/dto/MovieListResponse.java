package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MovieListResponse {
    private Long id;
    private String title;
    private String genres;
    private String tagline;
    private LocalDate releaseDate;
    private BigDecimal tmdbVoteAverage;
    private Integer tmdbVoteCount;
    private String posterFile;
}
