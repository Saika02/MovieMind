package com.lzz.backend.service.impl;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.utils.Constants;
import com.lzz.backend.entity.Movie;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieEmbeddingMapper;
import com.lzz.backend.service.MovieEmbeddingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieEmbeddingServiceImpl implements MovieEmbeddingService {
    private static final int CONTENT_TYPE_COMBINED = 3;
    private static final String MODEL_NAME = "text-embedding-v4";
    private final MovieEmbeddingMapper movieEmbeddingMapper;

    public MovieEmbeddingServiceImpl(MovieEmbeddingMapper movieEmbeddingMapper) {
        this.movieEmbeddingMapper = movieEmbeddingMapper;
    }

    @Override
    public void syncMovieEmbedding(Movie movie) {
        if (movie == null || movie.getId() == null) {
            throw new ServiceException("电影信息不存在");
        }
        List<Double> vector = embed(buildEmbeddingText(movie));
        String vectorLiteral = toVectorLiteral(vector);
        Long existingId = movieEmbeddingMapper.selectActiveIdByMovieIdAndContentType(movie.getId(), CONTENT_TYPE_COMBINED);
        if (existingId == null) {
            movieEmbeddingMapper.insertEmbedding(movie.getId(), CONTENT_TYPE_COMBINED, movie.getId(), vectorLiteral, MODEL_NAME);
            return;
        }
        movieEmbeddingMapper.updateEmbeddingById(existingId, movie.getId(), vectorLiteral, MODEL_NAME);
    }

    @Override
    public void deleteMovieEmbedding(Long movieId) {
        if (movieId == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        movieEmbeddingMapper.softDeleteByMovieId(movieId);
    }

    private List<Double> embed(String content) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServiceException("缺少 DASHSCOPE_API_KEY，无法同步电影向量");
        }
        try {
            Constants.apiKey = apiKey;
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(MODEL_NAME)
                    .texts(List.of(content))
                    .build();
            TextEmbeddingResult result = textEmbedding.call(param);
            List<?> raw = result.getOutput().getEmbeddings().get(0).getEmbedding();
            List<Double> vector = new ArrayList<>(raw.size());
            for (Object item : raw) {
                if (item instanceof Number number) {
                    vector.add(number.doubleValue());
                }
            }
            if (vector.isEmpty()) {
                throw new ServiceException("电影向量生成失败");
            }
            return vector;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("电影向量同步失败");
        }
    }

    private String buildEmbeddingText(Movie movie) {
        return "标题：" + safeText(movie.getTitle()) + "\n"
                + "简介：" + safeText(movie.getOverview()) + "\n"
                + "类型：" + safeText(movie.getGenres()) + "\n"
                + "关键词：" + safeText(movie.getKeywords()) + "\n"
                + "演员：" + safeText(movie.getCastList()) + "\n"
                + "制作人：" + safeText(movie.getProducers()) + "\n"
                + "制作公司：" + safeText(movie.getProductionCompanies()) + "\n"
                + "上映日期：" + (movie.getReleaseDate() == null ? "" : movie.getReleaseDate()) + "\n"
                + "标语：" + safeText(movie.getTagline());
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String toVectorLiteral(List<Double> vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(vector.get(i));
        }
        builder.append("]");
        return builder.toString();
    }
}
