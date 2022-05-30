package cn.twq.secKill.exception;

import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理器 */

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public Result exceptionHandler(Exception ex) {
    if (ex instanceof GlobalException) {
      return Result.error(((GlobalException) ex).getResultEnum());
    }
    // 这是被 spring-validator 拦截的非法参数
    if (ex instanceof BindException) {
      Result result = Result.error(ResultEnum.BIND_ERROR);
      result.setMessage(((BindException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage());
      return result;
    }
    return Result.error(ResultEnum.ERROR);
  }

}
