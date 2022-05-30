package cn.twq.secKill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String QUEUE = "secKillQueue";
  public static final String EXCHANGE = "secKillExchange";
  /** '#' 代表通配符 */
  public static final String ROUTING_KEY = "secKill.#";

  @Bean
  public Queue queue() {
    return new Queue(QUEUE);
  }

  @Bean
  public TopicExchange topicExchange() {
    return new TopicExchange(EXCHANGE);
  }

  @Bean
  public Binding binding() {
    return BindingBuilder.bind(queue()).to(topicExchange()).with(ROUTING_KEY);
  }

}
