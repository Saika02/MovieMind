package com.lzz.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdminMovieUpsertRequest {
    private Long tmdbId;
    private String title;
    private String overview;
    private String genres;
    private String keywords;
    private String castList;
    private String producers;
    private LocalDate releaseDate;
    private Integer runtime;
    private String productionCompanies;
    private BigDecimal tmdbVoteAverage;
    private Integer tmdbVoteCount;
    private BigDecimal siteVoteAverage;
    private Integer siteVoteCount;
    private String tagline;
    private String posterFile;
}
