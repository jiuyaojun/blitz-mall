package com.blitz.common.exception;

import com.blitz.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, msg={}", e.getCode(), e.getMsg());
        return Result.fail(e.getCode(), e.getMsg());
    }

    /**
     * 静态资源 / 不存在的路径必须回 404，不能被下面的兜底吞成 500。
     *
     * <p>之前缺这个方法：访问一个不存在的 URL 会走到 {@code Exception.class} 的兜底，
     * 返回 500。结果是「路径写错」和「服务真的炸了」在监控上长得一模一样，
     * 排查时会被误导（本次排查就踩了一次）。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        log.debug("No resource: {}", e.getResourcePath());
        return Result.fail(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception e) {
        log.error("Unknown error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        log.debug("Stacktrace:", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部异常，请稍后重试");
    }
}
