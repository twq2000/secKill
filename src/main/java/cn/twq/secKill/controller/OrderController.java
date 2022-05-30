package cn.twq.secKill.controller;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.service.IOrderService;
import cn.twq.secKill.vo.OrderDetailVO;
import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

  private final IOrderService orderService;

  public OrderController(IOrderService orderService) {
    this.orderService = orderService;
  }

  @RequestMapping("/detail")
  @ResponseBody
  public Result detail(User user, Long orderId) {
    if (user == null) {
      return Result.error(ResultEnum.USER_NOT_LOGIN);
    }
    OrderDetailVO orderDetailVO = orderService.getDetailById(orderId);
    return Result.success(orderDetailVO);
  }

}
