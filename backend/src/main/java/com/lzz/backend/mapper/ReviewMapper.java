package com.lzz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.entity.Review;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReviewMapper extends BaseMapper<Review> {
    Review selectByIdAndUser(Long id, Long userId);
    Review selectByUserAndMovie(Long userId, Long movieId);
    List<Review> selectByUser(Long userId);
    List<Review> selectByUserAndMovieList(Long userId, Long movieId);
    Page<Review> selectPageByUser(Page<Review> page, @Param("userId") Long userId);
    Page<Review> selectPageByUserAndMovie(Page<Review> page, @Param("userId") Long userId, @Param("movieId") Long movieId);
    int updateReview(Long id, Long userId, java.math.BigDecimal score, String content);
    int softDelete(Long id, Long userId);
}
