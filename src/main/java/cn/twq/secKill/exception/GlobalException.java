package cn.twq.secKill.exception;

import cn.twq.secKill.vo.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {
  private ResultEnum resultEnum;
}
