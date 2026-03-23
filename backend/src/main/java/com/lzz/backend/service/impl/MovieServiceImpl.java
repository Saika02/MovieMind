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
    public PageResponse<MovieListResponse> listPage(String keyword, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        String normalized = normalizeKeyword(keyword);
        Page<Movie> pageData = movieMapper.selectPage(new Page<>(page, size), normalized);
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
