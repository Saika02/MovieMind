package com.lzz.backend.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResponseFormat;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzz.backend.dto.AgentIntent;
import com.lzz.backend.dto.AgentRecommendRequest;
import com.lzz.backend.dto.AgentRecommendResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.entity.Movie;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieMapper;
import com.lzz.backend.service.AgentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int CONTENT_TYPE_COMBINED = 3;
    private static final String STRATEGY_STRUCTURED = "structured";
    private static final String STRATEGY_SEMANTIC = "semantic";
    private static final String SORT_SCORE_DESC = "score_desc";
    private static final String SORT_RELEASE_DESC = "release_date_desc";
    private static final String SORT_VOTE_DESC = "vote_count_desc";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MovieMapper movieMapper;

    public AgentServiceImpl(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @Override
    public AgentRecommendResponse recommend(AgentRecommendRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new ServiceException("参数不完整");
        }
        int limit = request.getLimit() == null ? DEFAULT_LIMIT : request.getLimit();
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new ServiceException("分页参数不合法");
        }

        String question = request.getQuestion().trim();
        AgentIntent intent = parseIntent(question, limit);
        int resolvedLimit = intent == null ? limit : clampLimit(intent.getLimit() == null ? limit : intent.getLimit());
        List<Movie> movies = fetchMoviesByIntent(question, intent, resolvedLimit);
        List<Movie> rankedMovies = rankMoviesByIntent(movies, intent);
        List<MovieListResponse> items = rankedMovies.stream()
                .map(this::toListResponse)
                .toList();

        String answer = generateAnswer(question, rankedMovies);
        return new AgentRecommendResponse(answer, items);
    }

    private List<Movie> fetchMoviesByIntent(String question, AgentIntent intent, int limit) {
        if (intent != null && STRATEGY_STRUCTURED.equals(intent.getStrategy())) {
            String sort = intent.getSort();
            if (SORT_RELEASE_DESC.equals(sort)) {
                return movieMapper.selectTopByReleaseDate(limit);
            }
            if (SORT_VOTE_DESC.equals(sort)) {
                return movieMapper.selectTopByVoteCount(limit);
            }
            return movieMapper.selectTopByScore(limit);
        }
        List<Double> vector = embedQuestion(question);
        if (vector == null || vector.isEmpty()) {
            return List.of();
        }
        String vectorLiteral = toVectorLiteral(vector);
        return movieMapper.selectRecommendByEmbedding(vectorLiteral, CONTENT_TYPE_COMBINED, limit);
    }

    private List<Movie> rankMoviesByIntent(List<Movie> movies, AgentIntent intent) {
        if (movies == null || movies.isEmpty()) {
            return List.of();
        }
        if (intent == null) {
            return movies;
        }
        String sort = intent.getSort();
        if (SORT_SCORE_DESC.equals(sort)) {
            return rankByScore(movies);
        }
        if (SORT_RELEASE_DESC.equals(sort)) {
            return rankByReleaseDate(movies);
        }
        if (SORT_VOTE_DESC.equals(sort)) {
            return rankByVoteCount(movies);
        }
        return movies;
    }

    private List<Double> embedQuestion(String question) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            Constants.apiKey = apiKey;
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model("text-embedding-v4")
                    .texts(List.of(question))
                    .build();
            TextEmbeddingResult result = textEmbedding.call(param);
            List<?> raw = result.getOutput().getEmbeddings().get(0).getEmbedding();
            List<Double> vector = new ArrayList<>(raw.size());
            for (Object item : raw) {
                if (item instanceof Number) {
                    vector.add(((Number) item).doubleValue());
                }
            }
            return vector;
        } catch (Exception ex) {
            return null;
        }
    }

    private AgentIntent parseIntent(String question, int limit) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackIntent(question, limit);
        }
        try {
            Constants.apiKey = apiKey;
            Generation generation = new Generation();
            Message message = Message.builder()
                    .role(Role.USER.getValue())
                    .content(buildIntentPrompt(question, limit))
                    .build();
            ResponseFormat jsonMode = ResponseFormat.builder()
                    .type("json_object")
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .model("qwen-plus")
                    .messages(List.of(message))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .responseFormat(jsonMode)
                    .build();
            GenerationResult result = generation.call(param);
            String content = result.getOutput().getChoices().get(0).getMessage().getContent();
            if (content == null || content.isBlank()) {
                return fallbackIntent(question, limit);
            }
            String sanitized = sanitizeJson(content);
            JsonNode node = objectMapper.readTree(sanitized);
            String strategy = textValue(node.get("strategy"));
            String sort = textValue(node.get("sort"));
            Integer parsedLimit = intValue(node.get("limit"), limit);
            if (!STRATEGY_STRUCTURED.equals(strategy) && !STRATEGY_SEMANTIC.equals(strategy)) {
                strategy = STRATEGY_SEMANTIC;
            }
            if (!SORT_SCORE_DESC.equals(sort) && !SORT_RELEASE_DESC.equals(sort) && !SORT_VOTE_DESC.equals(sort)) {
                sort = "none";
            }
            return new AgentIntent(strategy, sort, clampLimit(parsedLimit));
        } catch (Exception ex) {
            return fallbackIntent(question, limit);
        }
    }

    private String sanitizeJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            if (firstLineEnd >= 0) {
                trimmed = trimmed.substring(firstLineEnd + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private AgentIntent fallbackIntent(String question, int limit) {
        String normalized = question.replace(" ", "");
        if (isHighScoreQuery(normalized)) {
            return new AgentIntent(STRATEGY_STRUCTURED, SORT_SCORE_DESC, limit);
        }
        if (isRecentQuery(normalized)) {
            return new AgentIntent(STRATEGY_STRUCTURED, SORT_RELEASE_DESC, limit);
        }
        if (isPopularQuery(normalized)) {
            return new AgentIntent(STRATEGY_STRUCTURED, SORT_VOTE_DESC, limit);
        }
        return new AgentIntent(STRATEGY_SEMANTIC, "none", limit);
    }

    private String buildIntentPrompt(String question, int limit) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是电影推荐意图解析器，只输出 JSON，不要输出任何解释文字。\n");
        builder.append("根据用户问题，输出以下字段：\n");
        builder.append("strategy: structured 或 semantic\n");
        builder.append("sort: score_desc / release_date_desc / vote_count_desc / none\n");
        builder.append("limit: 1-20 的整数\n");
        builder.append("规则：\n");
        builder.append("1. 高分/评分最高/最好看 等 -> structured + score_desc\n");
        builder.append("2. 最新/最近/新片 -> structured + release_date_desc\n");
        builder.append("3. 热门/热度/人气 -> structured + vote_count_desc\n");
        builder.append("4. 其他情况 -> semantic + none\n");
        builder.append("必须返回有效 JSON，例如：{\"strategy\":\"structured\",\"sort\":\"score_desc\",\"limit\":10}\n");
        builder.append("用户问题：").append(question).append("\n");
        builder.append("limit: ").append(limit).append("\n");
        return builder.toString();
    }

    private String textValue(JsonNode node) {
        return node == null ? "" : node.asText("");
    }

    private int intValue(JsonNode node, int fallback) {
        if (node == null || !node.isNumber()) {
            return fallback;
        }
        return node.asInt(fallback);
    }

    private int clampLimit(int limit) {
        if (limit < 1) {
            return 1;
        }
        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        }
        return limit;
    }

    private String generateAnswer(String question, List<Movie> movies) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return buildFallbackAnswer(movies);
        }
        try {
            Constants.apiKey = apiKey;
            Generation generation = new Generation();
            String prompt = buildPrompt(question, movies);
            Message message = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .model("qwen-plus")
                    .messages(List.of(message))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = generation.call(param);
            String content = result.getOutput().getChoices().get(0).getMessage().getContent();
            if (content == null || content.isBlank()) {
                return buildFallbackAnswer(movies);
            }
            return content.trim();
        } catch (Exception ex) {
            return buildFallbackAnswer(movies);
        }
    }

    private String buildPrompt(String question, List<Movie> movies) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是 MovieMind 网站的智能推荐助手，目标是为用户提供清晰、可靠的电影推荐。\n");
        builder.append("回答要求：\n");
        builder.append("1. 使用中文，语气友好专业。\n");
        builder.append("2. 只能从候选电影中推荐，不要编造不存在的电影。\n");
        builder.append("3. 只能使用候选电影提供的字段信息，不引用外部资料，不纠正或质疑数据准确性。\n");
        builder.append("4. 给出简洁的推荐理由，可结合题材、剧情、氛围、主演、评分等。\n");
        builder.append("5. 可以推荐 3-6 部电影，格式为序号列表。\n");
        builder.append("6. 若用户要求高分/最高评分，请优先按评分从高到低推荐。\n");
        builder.append("用户问题：").append(question).append("\n");
        builder.append("候选电影：\n");
        int index = 1;
        for (Movie movie : movies) {
            builder.append(index).append(". ");
            builder.append(safeText(movie.getTitle()));
            String genres = safeText(movie.getGenres());
            if (!genres.isEmpty()) {
                builder.append(" | ").append(genres);
            }
            String tagline = safeText(movie.getTagline());
            if (!tagline.isEmpty()) {
                builder.append(" | ").append(tagline);
            }
            LocalDate releaseDate = movie.getReleaseDate();
            if (releaseDate != null) {
                builder.append(" | ").append(releaseDate);
            }
            BigDecimal score = movie.getTmdbVoteAverage();
            if (score != null) {
                builder.append(" | 评分 ").append(score);
            }
            builder.append("\n");
            index++;
        }
        builder.append("请严格遵守回答要求并输出推荐结果。");
        return builder.toString();
    }

    private String buildFallbackAnswer(List<Movie> movies) {
        if (movies.isEmpty()) {
            return "暂时没有匹配的推荐结果";
        }
        StringBuilder builder = new StringBuilder("为你推荐：");
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                builder.append("、");
            }
            builder.append(safeText(movies.get(i).getTitle()));
        }
        return builder.toString();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private List<Movie> rankMovies(List<Movie> movies, boolean preferHighScore) {
        if (!preferHighScore || movies == null || movies.isEmpty()) {
            return movies;
        }
        return movies.stream()
                .sorted((a, b) -> {
                    BigDecimal scoreA = a.getTmdbVoteAverage();
                    BigDecimal scoreB = b.getTmdbVoteAverage();
                    if (scoreA == null && scoreB == null) {
                        return compareIdDesc(a, b);
                    }
                    if (scoreA == null) {
                        return 1;
                    }
                    if (scoreB == null) {
                        return -1;
                    }
                    int scoreCompare = scoreB.compareTo(scoreA);
                    return scoreCompare != 0 ? scoreCompare : compareIdDesc(a, b);
                })
                .toList();
    }

    private List<Movie> rankByScore(List<Movie> movies) {
        return movies.stream()
                .sorted((a, b) -> {
                    BigDecimal scoreA = a.getTmdbVoteAverage();
                    BigDecimal scoreB = b.getTmdbVoteAverage();
                    if (scoreA == null && scoreB == null) {
                        return compareIdDesc(a, b);
                    }
                    if (scoreA == null) {
                        return 1;
                    }
                    if (scoreB == null) {
                        return -1;
                    }
                    int scoreCompare = scoreB.compareTo(scoreA);
                    return scoreCompare != 0 ? scoreCompare : compareIdDesc(a, b);
                })
                .toList();
    }

    private List<Movie> rankByReleaseDate(List<Movie> movies) {
        return movies.stream()
                .sorted((a, b) -> {
                    LocalDate dateA = a.getReleaseDate();
                    LocalDate dateB = b.getReleaseDate();
                    if (dateA == null && dateB == null) {
                        return compareIdDesc(a, b);
                    }
                    if (dateA == null) {
                        return 1;
                    }
                    if (dateB == null) {
                        return -1;
                    }
                    int dateCompare = dateB.compareTo(dateA);
                    return dateCompare != 0 ? dateCompare : compareIdDesc(a, b);
                })
                .toList();
    }

    private List<Movie> rankByVoteCount(List<Movie> movies) {
        return movies.stream()
                .sorted((a, b) -> {
                    Integer countA = a.getTmdbVoteCount();
                    Integer countB = b.getTmdbVoteCount();
                    if (countA == null && countB == null) {
                        return compareIdDesc(a, b);
                    }
                    if (countA == null) {
                        return 1;
                    }
                    if (countB == null) {
                        return -1;
                    }
                    int countCompare = countB.compareTo(countA);
                    return countCompare != 0 ? countCompare : compareIdDesc(a, b);
                })
                .toList();
    }

    private int compareIdDesc(Movie a, Movie b) {
        Long idA = a.getId();
        Long idB = b.getId();
        if (idA == null && idB == null) {
            return 0;
        }
        if (idA == null) {
            return 1;
        }
        if (idB == null) {
            return -1;
        }
        return idB.compareTo(idA);
    }

    private boolean isHighScoreQuery(String normalized) {
        return normalized.contains("评分最高")
                || normalized.contains("最高评分")
                || normalized.contains("高分")
                || normalized.contains("评分最高的")
                || normalized.contains("高评分")
                || normalized.contains("评分高");
    }

    private boolean isRecentQuery(String normalized) {
        return normalized.contains("最新")
                || normalized.contains("最近")
                || normalized.contains("新片")
                || normalized.contains("新上映");
    }

    private boolean isPopularQuery(String normalized) {
        return normalized.contains("热门")
                || normalized.contains("热度")
                || normalized.contains("人气")
                || normalized.contains("最火");
    }

    private String toVectorLiteral(List<Double> vector) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(vector.get(i));
        }
        builder.append("]");
        return builder.toString();
    }

    private MovieListResponse toListResponse(Movie movie) {
        return new MovieListResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getGenres(),
                movie.getTagline(),
                movie.getReleaseDate(),
                movie.getTmdbVoteAverage(),
                movie.getTmdbVoteCount(),
                movie.getPosterFile()
        );
    }
}
