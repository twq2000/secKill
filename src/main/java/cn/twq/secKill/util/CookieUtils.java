package cn.twq.secKill.util;

import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Cookie工具类 */

public final class CookieUtils {

  /**
   * 得到Cookie的值, 不编码
   *
   * @param request
   * @param cookieName
   * @return
   */
  public static String getCookieValue(HttpServletRequest request, String cookieName) {
    return getCookieValue(request, cookieName, false);
  }

  /**
   * 得到Cookie的值,
   *
   * @param request
   * @param cookieName
   * @return
   */
  public static String getCookieValue(
      HttpServletRequest request, String cookieName, boolean isDecoder) {
    Cookie[] cookieList = request.getCookies();
    if (cookieList == null || cookieName == null) {
      return null;
    }
    String retValue = null;
    for (Cookie cookie : cookieList) {
      if (cookie.getName().equals(cookieName)) {
        if (isDecoder) {
          retValue = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
        } else {
          retValue = cookie.getValue();
        }
        break;
      }
    }
    return retValue;
  }

  /**
   * 得到Cookie的值,
   *
   * @param request
   * @param cookieName
   * @return
   */
  public static String getCookieValue(
      HttpServletRequest request, String cookieName, String encodedString) {
    Cookie[] cookieList = request.getCookies();
    if (cookieList == null || cookieName == null) {
      return null;
    }
    String retValue = null;
    try {
      for (Cookie cookie : cookieList) {
        if (cookie.getName().equals(cookieName)) {
          retValue = URLDecoder.decode(cookie.getValue(), encodedString);
          break;
        }
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return retValue;
  }

  /**
   * 设置Cookie的值。
   * 不设置生效时间，浏览器关闭即失效；不进行编码
   *
   * @param request
   * @param response
   * @param cookieName
   * @param cookieValue
   */
  public static void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue) {
    setCookie(request, response, cookieName, cookieValue, -1);
  }

  /**
   * 设置Cookie的值。
   * 设置了生效时长；不进行编码
   *
   * @param request
   * @param response
   * @param cookieName
   * @param cookieValue
   * @param cookieMaxAge
   */
  public static void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      int cookieMaxAge) {
    setCookie(request, response, cookieName, cookieValue, cookieMaxAge, false);
  }

  /**
   * 设置Cookie的值。
   * 不设置生效时间，浏览器关闭即失效；不进行编码；进行了编码
   *
   * @param request
   * @param response
   * @param cookieName
   * @param cookieValue
   * @param isEncode
   */
  public static void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      boolean isEncode) {
    setCookie(request, response, cookieName, cookieValue, -1, isEncode);
  }

  /**
   * 设置Cookie的值。
   * 设置了生效时长；进行了编码
   *
   * @param request
   * @param response
   * @param cookieName
   * @param cookieValue
   * @param cookieMaxAge
   * @param isEncode
   */
  public static void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      int cookieMaxAge,
      boolean isEncode) {
    doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, isEncode);
  }

  /**
   * 设置Cookie的值。
   * 设置了生效时长；指定编码格式进行编码
   *
   * @param request
   * @param response
   * @param cookieName
   * @param cookieValue
   * @param cookieMaxAge
   * @param encodedString
   */
  public static void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      int cookieMaxAge,
      String encodedString) {
    doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, encodedString);
  }

  /**
   * 删除Cookie
   * 带有cookie域名
   *
   * @param request
   * @param response
   * @param cookieName
   */
  public static void deleteCookie(
      HttpServletRequest request, HttpServletResponse response, String cookieName) {
    doSetCookie(request, response, cookieName, "", -1, false);
  }

  /**
   * 设置Cookie的值，并使其在指定时间内生效
   *
   * @param cookieMaxAge cookie生效的最大秒数
   */
  private static void doSetCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      int cookieMaxAge,
      boolean isEncode) {
    try {
      if (cookieValue == null) {
        cookieValue = "";
      } else if (isEncode) {
        cookieValue = URLEncoder.encode(cookieValue, StandardCharsets.UTF_8);
      }
      Cookie cookie = new Cookie(cookieName, cookieValue);
      if (cookieMaxAge > 0) {
        cookie.setMaxAge(cookieMaxAge);
      }
      if (request != null) {
        // 设置域名的cookie
        String domainName = getDomainName(request);
        if (!"localhost".equals(domainName)) {
          cookie.setDomain(domainName);
        }
      }
      cookie.setPath("/");
      response.addCookie(cookie);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 设置Cookie的值，并使其在指定时间内生效
   *
   * @param cookieMaxAge cookie生效的最大秒数
   */
  private static void doSetCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String cookieValue,
      int cookieMaxAge,
      String encodedString) {
    try {
      if (cookieValue == null) {
        cookieValue = "";
      } else {
        cookieValue = URLEncoder.encode(cookieValue, encodedString);
      }
      Cookie cookie = new Cookie(cookieName, cookieValue);
      if (cookieMaxAge > 0) {
        cookie.setMaxAge(cookieMaxAge);
      }
      if (request != null) {
        // 设置域名的cookie
        String domainName = getDomainName(request);
        if (!"localhost".equals(domainName)) {
          cookie.setDomain(domainName);
        }
      }
      cookie.setPath("/");
      response.addCookie(cookie);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 得到cookie的域名
   *
   * @param request
   * @return
   */
  private static String getDomainName(HttpServletRequest request) {
    String domainName;
    // 通过request对象获取访问的url地址
    String serverName = request.getRequestURL().toString();
    if (ObjectUtils.isEmpty(serverName)) {
      domainName = "";
    } else {
      // 将ur地址转换为小写
      serverName = serverName.toLowerCase();
      // 如果url地址是以「http://」开头，那么将「http://」截取
      if (serverName.startsWith("http://")) {
        serverName = serverName.substring(7);
      }
      int end = serverName.length();
      // 判断url地址是否包含"/"
      if (serverName.contains("/")) {
        // 得到第一个"/"出现的位置
        end = serverName.indexOf("/");
      }
      // 截取
      serverName = serverName.substring(0, end);
      // 根据"."进行分割
      final String[] domains = serverName.split("\\.");
      int len = domains.length;
      if (len > 3) {
        // www.xxx.com.cn
        domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
      } else if (len > 1) {
        // xxx.com or xxx.cn
        domainName = domains[len - 2] + "." + domains[len - 1];
      } else {
        domainName = serverName;
      }
    }
    if (domainName.indexOf(":") > 0) {
      String[] arr = domainName.split(":");
      domainName = arr[0];
    }
    return domainName;
  }
}
