package cn.twq.secKill.service.impl;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.exception.GlobalException;
import cn.twq.secKill.mapper.UserMapper;
import cn.twq.secKill.service.IUserService;
import cn.twq.secKill.util.CookieUtils;
import cn.twq.secKill.util.MD5Utils;
import cn.twq.secKill.util.UUIDUtils;
import cn.twq.secKill.vo.LoginVO;
import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 服务实现类 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

  private final UserMapper userMapper;
  private final RedisTemplate<String, Object> redisTemplate;

  public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
    this.userMapper = userMapper;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Result doLogin(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) {
    String mobile = loginVO.getMobile();
    String password = loginVO.getPassword();
    User user = userMapper.selectById(mobile);
    // 登录名与密码的校验
    if (user == null
        || !MD5Utils.encodePassword2DB(password, user.getSalt()).equals(user.getPassword())) {
      throw new GlobalException(ResultEnum.LOGIN_ERROR);
    }
    // 至此，该用户登录成功。将用户信息存入 session&cookie
    // 首先生成一个标识符uuid作为cookieName
    String loginTicket = UUIDUtils.uuid();
    // 存入 cookie
    CookieUtils.setCookie(request, response, "loginTicket", loginTicket);
    // 存入 session（为了实现分布式session ，这里使用 redis 进行集中存储来代替 session）
    redisTemplate.opsForValue().set("user: " + loginTicket, user);
//     request.getSession().setAttribute(loginTicket, user) ;
    return Result.success(loginTicket);
  }

  @Override
  public User getUserByCookie(
      String loginTicket, HttpServletRequest request, HttpServletResponse response) {
    return ObjectUtils.isEmpty(loginTicket)
        ? null
        : ((User) redisTemplate.opsForValue().get("user: " + loginTicket));
  }

  @Override
  public Result updatePassword(
      String loginTicket,
      String password,
      HttpServletRequest request,
      HttpServletResponse response) {
    User user = getUserByCookie(loginTicket, request, response);
    if (user == null) {
      throw new GlobalException(ResultEnum.USER_NOT_EXIST);
    }
    user.setPassword(MD5Utils.encodePassword(password, user.getSalt()));
    // 为了保证数据的一致性问题，先更新数据库，再删除 redis 中的缓存数据
    if (userMapper.updateById(user) == 1) {
      // 数据库已经更新成功，下一步是从 redis 中删除对应的缓存数据
      redisTemplate.delete(loginTicket);
      return Result.success();
    }
    return Result.error(ResultEnum.PASSWORD_UPDATE_FAIL);
  }
}
