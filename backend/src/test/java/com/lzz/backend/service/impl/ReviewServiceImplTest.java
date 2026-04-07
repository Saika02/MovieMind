package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewView;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviewServiceImplTest {

    @Test
    void listMovieReviewsReturnsPublicReviews() {
        ReviewMapper mapper = mock(ReviewMapper.class);
        when(mapper.selectViewsByMovie(5L)).thenReturn(List.of(reviewView(1L, 5L, 2L, "bob")));
        ReviewServiceImpl service = new ReviewServiceImpl(mapper);

        List<ReviewResponse> responses = service.listMovieReviews(5L);

        assertEquals(1, responses.size());
        assertEquals("bob", responses.get(0).getUsername());
        assertEquals(2L, responses.get(0).getUserId());
        assertNotNull(responses.get(0).getCreatedAt());
    }

    @Test
    void listMovieReviewsPageRejectsInvalidPaging() {
        ReviewServiceImpl service = new ReviewServiceImpl(mock(ReviewMapper.class));

        assertThrows(ServiceException.class, () -> service.listMovieReviewsPage(5L, 0, 10));
    }

    @Test
    void listMovieReviewsPageMapsPagedResults() {
        ReviewMapper mapper = mock(ReviewMapper.class);
        Page<ReviewView> page = new Page<>(1, 10);
        page.setRecords(List.of(reviewView(1L, 5L, 2L, "bob")));
        page.setTotal(1);
        when(mapper.selectViewPageByMovie(any(Page.class), eq(5L))).thenReturn(page);
        ReviewServiceImpl service = new ReviewServiceImpl(mapper);

        PageResponse<ReviewResponse> response = service.listMovieReviewsPage(5L, 1, 10);

        assertEquals(1, response.getTotal());
        assertEquals("bob", response.getItems().get(0).getUsername());
    }

    private ReviewView reviewView(Long id, Long movieId, Long userId, String username) {
        ReviewView view = new ReviewView();
        view.setId(id);
        view.setMovieId(movieId);
        view.setUserId(userId);
        view.setUsername(username);
        view.setAvatarUrl("/uploads/avatars/a.png");
        view.setScore(new BigDecimal("8.5"));
        view.setContent("great");
        view.setCreatedAt(OffsetDateTime.now().minusDays(1));
        view.setUpdatedAt(OffsetDateTime.now());
        return view;
    }
}
