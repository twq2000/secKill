package cn.twq.secKill.config;

import cn.twq.secKill.interceptor.RequestLimitInterceptor;
import cn.twq.secKill.interceptor.UserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** MVC配置类 */

@Configuration
public class MVCConfig implements WebMvcConfigurer {

  private final UserArgumentResolver resolver;
  private final RequestLimitInterceptor requestLimitInterceptor;

  public MVCConfig(UserArgumentResolver resolver, RequestLimitInterceptor requestLimitInterceptor) {
    this.resolver = resolver;
    this.requestLimitInterceptor = requestLimitInterceptor;
  }

  /**
   * 将自定义的用户参数解析器 配置到 MVC容器中
   *
   * @param resolvers
   */
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(resolver);
  }

  /**
   * 将自定义的拦截器 配置到 MVC容器中
   *
   * @param registry
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestLimitInterceptor);
  }
}
