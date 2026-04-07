package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.entity.Movie;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieMapper;
import com.lzz.backend.service.MovieService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    private static final String FEATURED_SCENE = "featured";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_RATING = "rating";
    private static final String SORT_LATEST = "latest";
    private final MovieMapper movieMapper;

    public MovieServiceImpl(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @Override
    public MovieDetailResponse get(Long id) {
        if (id == null) {
            throw new ServiceException("参数不完整");
        }
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new ServiceException("电影不存在");
        }
        return toDetailResponse(movie);
    }

    @Override
    public List<MovieListResponse> list(String keyword) {
        String normalized = normalizeKeyword(keyword);
        List<Movie> movies = movieMapper.selectList(normalized);
        return movies.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<MovieListResponse> listPage(String keyword, String sort, String scene, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        String normalized = normalizeKeyword(keyword);
        String normalizedSort = normalizeSort(sort);
        Page<Movie> pageData = FEATURED_SCENE.equalsIgnoreCase(scene == null ? "" : scene)
                ? movieMapper.selectFeaturedPage(new Page<>(page, size))
                : movieMapper.selectBrowsePage(new Page<>(page, size), normalized, normalizedSort);
        List<MovieListResponse> items = pageData.getRecords().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSort(String sort) {
        if (sort == null) {
            return SORT_POPULAR;
        }
        String normalized = sort.trim().toLowerCase();
        if (SORT_RATING.equals(normalized) || SORT_LATEST.equals(normalized)) {
            return normalized;
        }
        return SORT_POPULAR;
    }

    private MovieListResponse toListResponse(Movie movie) {
        return new MovieListResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getGenres(),
                movie.getTagline(),
                movie.getReleaseDate(),
                movie.getTmdbVoteAverage(),
                movie.getTmdbVoteCount(),
                movie.getPosterFile()
        );
    }

    private MovieDetailResponse toDetailResponse(Movie movie) {
        return new MovieDetailResponse(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOverview(),
                movie.getGenres(),
                movie.getKeywords(),
                movie.getCastList(),
                movie.getProducers(),
                movie.getReleaseDate(),
                movie.getRuntime(),
                movie.getProductionCompanies(),
                movie.getTmdbVoteAverage(),
                movie.getTmdbVoteCount(),
                movie.getSiteVoteAverage(),
                movie.getSiteVoteCount(),
                movie.getTagline(),
                movie.getPosterFile()
        );
    }
}
