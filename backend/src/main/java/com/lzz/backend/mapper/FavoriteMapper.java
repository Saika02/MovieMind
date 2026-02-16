package com.lzz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper extends BaseMapper<Favorite> {
    Favorite selectByIdAndUser(Long id, Long userId);
    Favorite selectByUserAndMovie(Long userId, Long movieId);
    List<Favorite> selectByUser(Long userId);
    Page<Favorite> selectPageByUser(Page<Favorite> page, @Param("userId") Long userId);
    int softDelete(Long id, Long userId);
}
