package com.erdongj.common;

import com.erdongj.pojo.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 * <p>
 * 改造前：异常直接抛给前端（{@code throw new RuntimeException(e)}），控制器里到处 if-else 拼失败响应，
 * 且 HTTP 状态码恒为 200，接口对调试工具完全不可读。
 * <p>
 * 改造后：业务异常 / 参数异常 / 未知异常统一在此转换成标准响应体与语义化状态码，
 * 堆栈只进日志、不进响应体。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ResultBean<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(toHttpStatus(e.getCode()))
                .body(ResultBean.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultBean<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ResultBean.fail(ResultBean.FAIL, msg));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ResultBean<Void>> handleParam(Exception e) {
        log.warn("请求参数异常: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ResultBean.fail(ResultBean.FAIL, "请求参数格式不正确"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultBean<Void>> handleOther(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultBean.fail(ResultBean.ERROR, "服务器繁忙，请稍后重试"));
    }

    private HttpStatus toHttpStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status == null ? HttpStatus.BAD_REQUEST : status;
    }
}
