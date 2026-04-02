package com.lzz.backend.mapper;

import org.apache.ibatis.annotations.Param;

public interface MovieEmbeddingMapper {
    Long selectActiveIdByMovieIdAndContentType(@Param("movieId") Long movieId, @Param("contentType") Integer contentType);
    int insertEmbedding(@Param("movieId") Long movieId,
                        @Param("contentType") Integer contentType,
                        @Param("contentId") Long contentId,
                        @Param("embedding") String embedding,
                        @Param("modelName") String modelName);
    int updateEmbeddingById(@Param("id") Long id,
                            @Param("contentId") Long contentId,
                            @Param("embedding") String embedding,
                            @Param("modelName") String modelName);
    int softDeleteByMovieId(@Param("movieId") Long movieId);
}
