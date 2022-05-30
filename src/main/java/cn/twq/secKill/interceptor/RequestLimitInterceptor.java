package cn.twq.secKill.interceptor;

import cn.twq.secKill.config.RequestLimit;
import cn.twq.secKill.config.UserContext;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.service.IUserService;
import cn.twq.secKill.util.CookieUtils;
import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class RequestLimitInterceptor implements HandlerInterceptor {

  private final IUserService userService;
  private final RedisTemplate<String, Object> redisTemplate;

  public RequestLimitInterceptor(
      IUserService userService, RedisTemplate<String, Object> redisTemplate) {
    this.userService = userService;
    this.redisTemplate = redisTemplate;
  }

  /**
   * 前置拦截器。负责拦截所有带有 @RequestLimit 的方法
   *
   * @param request
   * @param response
   * @param handler
   * @return
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      User user = getUser(request, response);
      UserContext.setUser(user);
      RequestLimit requestLimit = handlerMethod.getMethodAnnotation(RequestLimit.class);
      // 如果该注解不是 @RequestLimit ，那么直接放行
      if (requestLimit == null) {
        return true;
      }
      int second = requestLimit.second(), limit = requestLimit.limit();
      boolean needLogin = requestLimit.needLogin();
      // 限制对同一路径参数的访问频率。规定：5秒内最多进行5次访问请求
      String requestURI = request.getRequestURI();
      if (needLogin) {
        if (user == null) {
          returnResult(response, ResultEnum.USER_NOT_LOGIN);
          return false;
        }
        requestURI += ": " + user.getId();
      }
      ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
      Integer counter = (Integer) opsForValue.get(requestURI);
      if (counter == null) {
        opsForValue.set(requestURI, 1, second, TimeUnit.SECONDS);
      } else if (counter < limit) {
        opsForValue.increment(requestURI);
      } else {
        returnResult(response, ResultEnum.REQUEST_ACCESS_LIMIT);
        return false;
      }
    }
    return true;
  }

  private void returnResult(HttpServletResponse response, ResultEnum resultEnum) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    Result result = Result.error(resultEnum);
    writer.write(new ObjectMapper().writeValueAsString(result));
    writer.flush();
    writer.close();
  }

  private User getUser(HttpServletRequest request, HttpServletResponse response) {
    String loginTicket = CookieUtils.getCookieValue(request, "loginTicket");
    return userService.getUserByCookie(loginTicket, request, response);
  }
}
