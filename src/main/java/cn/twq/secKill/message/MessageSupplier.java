package cn.twq.secKill.message;

import cn.twq.secKill.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageSupplier {

  private final RabbitTemplate rabbitTemplate;

  public MessageSupplier(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * 发送秒杀信息
   *
   * @param message
   */
  public void sendSecKillMessage(String message) {
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "secKill.message", message);
  }

}
