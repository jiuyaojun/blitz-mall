package com.blitz.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.result.Result;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewMapper reviewMapper;

    /** List reviews for a product, optionally filter by rating */
    @GetMapping("/list/{productId}")
    public Result<IPage<Review>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int rating) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getProductId, productId);
        if (rating > 0 && rating <= 5) {
            qw.eq(Review::getRating, rating);
        }
        qw.orderByDesc(Review::getCreateTime);
        return Result.success(reviewMapper.selectPage(new Page<>(page, size), qw));
    }

    /** Submit a review — requires login */
    @PostMapping
    public Result<Review> submit(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        Review review = new Review();
        review.setProductId(Long.valueOf(body.get("productId").toString()));
        review.setUserId(userId);
        review.setUsername(username != null ? username : "用户" + userId);
        review.setRating(Integer.valueOf(body.get("rating").toString()));
        review.setContent((String) body.getOrDefault("content", ""));
        review.setImages((String) body.getOrDefault("images", "[]"));
        review.setSkuName((String) body.getOrDefault("skuName", ""));
        review.setLikeCount(0);
        reviewMapper.insert(review);
        return Result.success(review);
    }

    /** Like a review (mark as helpful) */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) return Result.fail(404, "评价不存在");
        int newCount = (review.getLikeCount() != null ? review.getLikeCount() : 0) + 1;
        Review upd = new Review();
        upd.setLikeCount(newCount);
        reviewMapper.update(upd, new LambdaUpdateWrapper<Review>().eq(Review::getId, id));
        return Result.success();
    }

    /** Merchant replies to a review */
    @PostMapping("/{id}/reply")
    @RequireRole("MERCHANT")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Review review = reviewMapper.selectById(id);
        if (review == null) return Result.fail(404, "评价不存在");
        review.setReply((String) body.getOrDefault("reply", ""));
        reviewMapper.updateById(review);
        return Result.success();
    }

    /** Get review statistics for a product */
    @GetMapping("/stats/{productId}")
    public Result<Map<String, Object>> stats(@PathVariable Long productId) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getProductId, productId);
        Long total = reviewMapper.selectCount(qw);
        // Use a single GROUP BY query instead of N+1 individual COUNT queries
        List<Map<String, Object>> ratingCounts = reviewMapper.selectMaps(
            new LambdaQueryWrapper<Review>()
                .select(Review::getRating, com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(Review.class).apply("COUNT(*) as cnt"))
                .eq(Review::getProductId, productId)
                .groupBy(Review::getRating));
        long[] counts = new long[6];
        double avg = 0;
        for (Map<String, Object> row : ratingCounts) {
            int r = ((Number) row.get("rating")).intValue();
            long c = ((Number) row.get("cnt")).longValue();
            if (r >= 1 && r <= 5) {
                counts[r] = c;
                avg += r * c;
            }
        }
        if (total > 0) avg /= total;
        long goodCount = counts[4] + counts[5];
        return Result.success(Map.of(
            "total", total,
            "average", Math.round(avg * 10) / 10.0,
            "goodPct", total > 0 ? Math.round(goodCount * 100.0 / total) : 0,
            "counts", new long[]{0, counts[1], counts[2], counts[3], counts[4], counts[5]}
        ));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Number) return ((Number) principal).longValue();
        return Long.valueOf(principal.toString());
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Principal is userId (Long), we return a default username
        // The JWT filter stores userId as principal
        return "用户" + (principal instanceof Number ? ((Number) principal).longValue() : principal.toString());
    }
}
