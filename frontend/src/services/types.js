/**
 * Shared type notes for the JS app.
 *
 * ApiResponse<T>: { success: boolean, message: string, data: T | null }
 * PageResponse<T>: { page: number, size: number, total: number, items: T[] }
 * AuthSessionResponse: { loggedIn: boolean, userId: number|null, username: string|null, role: number|null }
 * MovieListResponse: { id, title, genres, tagline, releaseDate, tmdbVoteAverage, tmdbVoteCount, posterFile }
 * MovieDetailResponse: extends list fields with overview, keywords, castList, producers, runtime, productionCompanies, siteVoteAverage, siteVoteCount
 * ReviewResponse: { id, movieId, userId, username, avatarUrl, score, content, createdAt, updatedAt }
 * FavoriteStatusResponse: { movieId, favorited, favoriteId }
 * CurrentUserResponse: { userId, username, role, avatarUrl, bio }
 */

export const typeShapes = {}
