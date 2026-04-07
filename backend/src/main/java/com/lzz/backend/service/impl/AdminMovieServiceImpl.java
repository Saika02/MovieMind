package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.AdminMovieUpsertRequest;
import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.entity.Movie;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieMapper;
import com.lzz.backend.service.AdminMovieService;
import com.lzz.backend.service.MovieEmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class AdminMovieServiceImpl implements AdminMovieService {
    private final MovieMapper movieMapper;
    private final MovieEmbeddingService movieEmbeddingService;
    private final String uploadBaseDir;

    public AdminMovieServiceImpl(MovieMapper movieMapper,
                                 MovieEmbeddingService movieEmbeddingService,
                                 @Value("${app.upload-base-dir:${user.dir}/uploads}") String uploadBaseDir) {
        this.movieMapper = movieMapper;
        this.movieEmbeddingService = movieEmbeddingService;
        this.uploadBaseDir = uploadBaseDir;
    }

    @Override
    public MovieDetailResponse get(Long id) {
        Movie movie = requireMovie(id);
        return toDetailResponse(movie);
    }

    @Override
    public PageResponse<MovieListResponse> page(String keyword, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Movie> pageData = movieMapper.selectPage(new Page<>(page, size), normalizedKeyword);
        List<MovieListResponse> items = pageData.getRecords().stream()
                .map(this::toListResponse)
                .toList();
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    @Transactional
    public MovieDetailResponse create(AdminMovieUpsertRequest request) {
        Movie movie = buildMovie(null, request);
        movieMapper.insert(movie);
        Movie saved = requireMovie(movie.getId());
        movieEmbeddingService.syncMovieEmbedding(saved);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public MovieDetailResponse update(Long id, AdminMovieUpsertRequest request) {
        Movie existing = requireMovie(id);
        Movie movie = buildMovie(existing, request);
        int updated = movieMapper.updateById(movie);
        if (updated == 0) {
            throw new ServiceException("电影不存在");
        }
        Movie saved = requireMovie(id);
        movieEmbeddingService.syncMovieEmbedding(saved);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        int updated = movieMapper.softDelete(id);
        if (updated == 0) {
            throw new ServiceException("电影不存在");
        }
        movieEmbeddingService.deleteMovieEmbedding(id);
    }

    @Override
    public String uploadPoster(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("海报文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException("仅支持图片文件");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String filename = "movie_" + UUID.randomUUID() + extension;

        try {
            Path dir = Paths.get(uploadBaseDir).resolve("posters").toAbsolutePath();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return "/uploads/posters/" + filename;
        } catch (Exception ex) {
            throw new ServiceException("海报上传失败");
        }
    }

    private Movie requireMovie(Long id) {
        if (id == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new ServiceException("电影不存在");
        }
        return movie;
    }

    private Movie buildMovie(Movie target, AdminMovieUpsertRequest request) {
        if (request == null) {
            throw new ServiceException("参数不完整");
        }

        String title = normalizeRequired(request.getTitle(), "电影标题不能为空");
        Movie movie = target == null ? new Movie() : target;
        movie.setTmdbId(null);
        movie.setTitle(title);
        movie.setOverview(normalizeOptional(request.getOverview()));
        movie.setGenres(normalizeOptional(request.getGenres()));
        movie.setKeywords(normalizeOptional(request.getKeywords()));
        movie.setCastList(normalizeOptional(request.getCastList()));
        movie.setProducers(normalizeOptional(request.getProducers()));
        movie.setReleaseDate(request.getReleaseDate());
        movie.setRuntime(request.getRuntime());
        movie.setProductionCompanies(normalizeOptional(request.getProductionCompanies()));
        movie.setTmdbVoteAverage(defaultScore(target == null ? null : target.getTmdbVoteAverage()));
        movie.setTmdbVoteCount(defaultCount(target == null ? null : target.getTmdbVoteCount()));
        movie.setSiteVoteAverage(defaultScore(target == null ? null : target.getSiteVoteAverage()));
        movie.setSiteVoteCount(defaultCount(target == null ? null : target.getSiteVoteCount()));
        movie.setTagline(normalizeOptional(request.getTagline()));
        movie.setPosterFile(normalizeOptional(request.getPosterFile()));
        return movie;
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null) {
            return ".jpg";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0) {
            return ".jpg";
        }
        return originalFilename.substring(dot);
    }

    private BigDecimal defaultScore(BigDecimal existingValue) {
        return existingValue == null ? BigDecimal.ZERO : existingValue;
    }

    private Integer defaultCount(Integer existingValue) {
        return existingValue == null ? 0 : existingValue;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ServiceException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeKeyword(String keyword) {
        return normalizeOptional(keyword);
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
