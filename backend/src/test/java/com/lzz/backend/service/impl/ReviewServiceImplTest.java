package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewCreateRequest;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewUpdateRequest;
import com.lzz.backend.dto.ReviewView;
import com.lzz.backend.entity.Review;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieMapper;
import com.lzz.backend.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceImplTest {

    @Test
    void listMovieReviewsReturnsPublicReviews() {
        ReviewMapper mapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        when(mapper.selectViewsByMovie(5L)).thenReturn(List.of(reviewView(1L, 5L, 2L, "bob")));
        ReviewServiceImpl service = new ReviewServiceImpl(mapper, movieMapper);

        List<ReviewResponse> responses = service.listMovieReviews(5L);

        assertEquals(1, responses.size());
        assertEquals("bob", responses.get(0).getUsername());
        assertEquals(2L, responses.get(0).getUserId());
        assertNotNull(responses.get(0).getCreatedAt());
    }

    @Test
    void listMovieReviewsPageRejectsInvalidPaging() {
        ReviewServiceImpl service = new ReviewServiceImpl(mock(ReviewMapper.class), mock(MovieMapper.class));

        assertThrows(ServiceException.class, () -> service.listMovieReviewsPage(5L, 0, 10));
    }

    @Test
    void listMovieReviewsPageMapsPagedResults() {
        ReviewMapper mapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        Page<ReviewView> page = new Page<>(1, 10);
        page.setRecords(List.of(reviewView(1L, 5L, 2L, "bob")));
        page.setTotal(1);
        when(mapper.selectViewPageByMovie(any(Page.class), eq(5L))).thenReturn(page);
        ReviewServiceImpl service = new ReviewServiceImpl(mapper, movieMapper);

        PageResponse<ReviewResponse> response = service.listMovieReviewsPage(5L, 1, 10);

        assertEquals(1, response.getTotal());
        assertEquals("bob", response.getItems().get(0).getUsername());
    }

    @Test
    void createRefreshesMovieSiteVoteStats() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, movieMapper);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setMovieId(5L);
        request.setScore(new BigDecimal("7.0"));
        request.setContent("good");
        when(reviewMapper.selectByUserAndMovie(3L, 5L)).thenReturn(null);
        when(reviewMapper.selectAnyByUserAndMovie(3L, 5L)).thenReturn(null);
        doAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(9L);
            return 1;
        }).when(reviewMapper).insert(any(Review.class));
        when(reviewMapper.selectViewByIdAndUser(9L, 3L)).thenReturn(reviewView(9L, 5L, 3L, "alice"));

        service.create(3L, request);

        verify(movieMapper).refreshSiteVoteStats(5L);
    }

    @Test
    void createRestoresDeletedReviewInsteadOfInsertingDuplicate() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, movieMapper);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setMovieId(5L);
        request.setScore(new BigDecimal("7.0"));
        request.setContent("good");
        Review deletedReview = new Review();
        deletedReview.setId(12L);
        deletedReview.setMovieId(5L);
        when(reviewMapper.selectByUserAndMovie(3L, 5L)).thenReturn(null);
        when(reviewMapper.selectAnyByUserAndMovie(3L, 5L)).thenReturn(deletedReview);
        when(reviewMapper.restoreReview(12L, 3L, new BigDecimal("7.0"), "good")).thenReturn(1);
        when(reviewMapper.selectViewByIdAndUser(12L, 3L)).thenReturn(reviewView(12L, 5L, 3L, "alice"));

        service.create(3L, request);

        verify(reviewMapper).restoreReview(12L, 3L, new BigDecimal("7.0"), "good");
        verify(movieMapper).refreshSiteVoteStats(5L);
    }

    @Test
    void createMapsDuplicateInsertToBusinessError() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, movieMapper);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setMovieId(5L);
        request.setScore(new BigDecimal("7.0"));
        request.setContent("good");
        when(reviewMapper.selectByUserAndMovie(3L, 5L)).thenReturn(null);
        when(reviewMapper.selectAnyByUserAndMovie(3L, 5L)).thenReturn(null);
        doAnswer(invocation -> {
            throw new DuplicateKeyException("duplicate");
        }).when(reviewMapper).insert(any(Review.class));

        assertThrows(ServiceException.class, () -> service.create(3L, request));
    }

    @Test
    void updateRefreshesMovieSiteVoteStats() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, movieMapper);
        Review existing = new Review();
        existing.setId(7L);
        existing.setMovieId(5L);
        when(reviewMapper.selectByIdAndUser(7L, 3L)).thenReturn(existing);
        when(reviewMapper.updateReview(7L, 3L, new BigDecimal("8.0"), "better")).thenReturn(1);
        when(reviewMapper.selectViewByIdAndUser(7L, 3L)).thenReturn(reviewView(7L, 5L, 3L, "alice"));
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setScore(new BigDecimal("8.0"));
        request.setContent("better");

        service.update(3L, 7L, request);

        verify(movieMapper).refreshSiteVoteStats(5L);
    }

    @Test
    void deleteRefreshesMovieSiteVoteStats() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        MovieMapper movieMapper = mock(MovieMapper.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, movieMapper);
        Review existing = new Review();
        existing.setId(7L);
        existing.setMovieId(5L);
        when(reviewMapper.selectByIdAndUser(7L, 3L)).thenReturn(existing);
        when(reviewMapper.softDelete(7L, 3L)).thenReturn(1);

        service.delete(3L, 7L);

        verify(movieMapper).refreshSiteVoteStats(5L);
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
