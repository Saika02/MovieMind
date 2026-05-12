package com.lzz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.entity.Movie;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MovieMapper extends BaseMapper<Movie> {
    Movie selectById(Long id);
    List<Movie> selectList(@Param("keyword") String keyword);
    Page<Movie> selectBrowsePage(Page<Movie> page, @Param("keyword") String keyword, @Param("sort") String sort);
    Page<Movie> selectFeaturedPage(Page<Movie> page);
    List<Movie> selectRecommendByEmbedding(@Param("vector") String vector, @Param("contentType") Integer contentType, @Param("limit") int limit);
    List<Movie> selectTopByScore(@Param("limit") int limit);
    List<Movie> selectTopByReleaseDate(@Param("limit") int limit);
    List<Movie> selectTopByVoteCount(@Param("limit") int limit);
    int refreshSiteVoteStats(@Param("id") Long id);
    int softDelete(@Param("id") Long id);
}
