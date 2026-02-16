package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.FavoriteCreateRequest;
import com.lzz.backend.dto.FavoriteResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.entity.Favorite;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.FavoriteMapper;
import com.lzz.backend.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public FavoriteResponse create(Long userId, FavoriteCreateRequest request) {
        if (request == null || request.getMovieId() == null) {
            throw new ServiceException("参数不完整");
        }
        Favorite existing = favoriteMapper.selectByUserAndMovie(userId, request.getMovieId());
        if (existing != null) {
            throw new ServiceException("已存在");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setMovieId(request.getMovieId());
        favoriteMapper.insert(favorite);
        return new FavoriteResponse(favorite.getId(), favorite.getMovieId());
    }

    @Override
    public FavoriteResponse get(Long userId, Long id) {
        Favorite favorite = favoriteMapper.selectByIdAndUser(id, userId);
        if (favorite == null) {
            throw new ServiceException("记录不存在");
        }
        return new FavoriteResponse(favorite.getId(), favorite.getMovieId());
    }

    @Override
    public List<FavoriteResponse> list(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectByUser(userId);
        return favorites.stream()
                .map(item -> new FavoriteResponse(item.getId(), item.getMovieId()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<FavoriteResponse> listPage(Long userId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        Page<Favorite> pageData = favoriteMapper.selectPageByUser(new Page<>(page, size), userId);
        List<FavoriteResponse> items = pageData.getRecords().stream()
                .map(item -> new FavoriteResponse(item.getId(), item.getMovieId()))
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    public void delete(Long userId, Long id) {
        int updated = favoriteMapper.softDelete(id, userId);
        if (updated == 0) {
            throw new ServiceException("记录不存在");
        }
    }
}
