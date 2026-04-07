package com.lzz.backend.service.impl;

import com.lzz.backend.dto.FavoriteStatusResponse;
import com.lzz.backend.entity.Favorite;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.FavoriteMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceImplTest {

    @Test
    void getStatusReturnsFavoritedWhenRecordExists() {
        FavoriteMapper mapper = mock(FavoriteMapper.class);
        Favorite favorite = new Favorite();
        favorite.setId(9L);
        favorite.setMovieId(11L);
        when(mapper.selectByUserAndMovie(3L, 11L)).thenReturn(favorite);
        FavoriteServiceImpl service = new FavoriteServiceImpl(mapper);

        FavoriteStatusResponse response = service.getStatus(3L, 11L);

        assertTrue(response.isFavorited());
        assertEquals(11L, response.getMovieId());
        assertEquals(9L, response.getFavoriteId());
    }

    @Test
    void getStatusReturnsNotFavoritedWhenRecordMissing() {
        FavoriteMapper mapper = mock(FavoriteMapper.class);
        when(mapper.selectByUserAndMovie(3L, 11L)).thenReturn(null);
        FavoriteServiceImpl service = new FavoriteServiceImpl(mapper);

        FavoriteStatusResponse response = service.getStatus(3L, 11L);

        assertFalse(response.isFavorited());
        assertEquals(11L, response.getMovieId());
        assertNull(response.getFavoriteId());
    }

    @Test
    void getStatusRejectsMissingMovieId() {
        FavoriteServiceImpl service = new FavoriteServiceImpl(mock(FavoriteMapper.class));

        assertThrows(ServiceException.class, () -> service.getStatus(1L, null));
    }
}
