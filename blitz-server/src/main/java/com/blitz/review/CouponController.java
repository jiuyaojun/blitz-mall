package com.blitz.review;

import com.blitz.common.result.Result;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final JdbcTemplate jdbc;

    @GetMapping("/official")
    public Result<List<Map<String,Object>>> official() {
        return Result.success(jdbc.queryForList(
            "SELECT * FROM coupon WHERE type='OFFICIAL' AND status=1 AND claimed_count < total_count ORDER BY discount_value DESC"));
    }

    @GetMapping("/shop/{shopId}")
    public Result<List<Map<String,Object>>> shopCoupons(@PathVariable Long shopId) {
        return Result.success(jdbc.queryForList(
            "SELECT * FROM coupon WHERE type='SHOP' AND shop_id=? AND status=1 AND claimed_count < total_count", shopId));
    }

    @RequireRole("USER")
    @PostMapping("/claim/{couponId}")
    public Result<Void> claim(@PathVariable Long couponId) {
        Long userId = getCurrentUserId();
        // Check already claimed
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM user_coupon WHERE user_id=? AND coupon_id=?", Integer.class, userId, couponId);
        if (count != null && count > 0) return Result.fail(400, "已领取过该优惠券");
        // Check stock
        Map<String,Object> c = jdbc.queryForMap("SELECT * FROM coupon WHERE id=?", couponId);
        if (c == null || c.isEmpty()) return Result.fail(404, "优惠券不存在");
        int claimed = ((Number)c.get("claimed_count")).intValue();
        int total = ((Number)c.get("total_count")).intValue();
        if (claimed >= total) return Result.fail(400, "优惠券已抢光");
        // Claim
        jdbc.update("UPDATE coupon SET claimed_count=claimed_count+1 WHERE id=?", couponId);
        jdbc.update("INSERT INTO user_coupon (user_id, coupon_id, status) VALUES (?,?,'UNUSED')", userId, couponId);
        return Result.success();
    }

    @RequireRole("USER")
    @GetMapping("/my")
    public Result<List<Map<String,Object>>> myCoupons() {
        Long userId = getCurrentUserId();
        return Result.success(jdbc.queryForList(
            "SELECT uc.id,uc.status,uc.claim_time,c.name,c.type,c.discount_type,c.discount_value,c.min_spend,c.shop_id " +
            "FROM user_coupon uc JOIN coupon c ON uc.coupon_id=c.id WHERE uc.user_id=? ORDER BY uc.claim_time DESC", userId));
    }

    @RequireRole("USER")
    @GetMapping("/available")
    public Result<List<Map<String,Object>>> available(@RequestParam(defaultValue="0") double amount) {
        Long userId = getCurrentUserId();
        return Result.success(jdbc.queryForList(
            "SELECT uc.id,uc.coupon_id,c.name,c.discount_type,c.discount_value,c.min_spend,c.shop_id " +
            "FROM user_coupon uc JOIN coupon c ON uc.coupon_id=c.id " +
            "WHERE uc.user_id=? AND uc.status='UNUSED' AND c.min_spend <= ? ORDER BY c.discount_value DESC", userId, amount));
    }

    @RequireRole("USER")
    @PostMapping("/use/{userCouponId}")
    public Result<Void> use(@PathVariable Long userCouponId) {
        Long userId = getCurrentUserId();
        int updated = jdbc.update(
            "UPDATE user_coupon SET status='USED', use_time=NOW() WHERE id=? AND user_id=?",
            userCouponId, userId);
        if (updated == 0) return Result.fail(403, "无权使用此优惠券或优惠券不存在");
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof Number ? ((Number) p).longValue() : Long.valueOf(p.toString());
    }
}
