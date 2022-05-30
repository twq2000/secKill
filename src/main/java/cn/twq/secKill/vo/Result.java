package cn.twq.secKill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 前后端数据交互格式 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
  private Integer code;
  private String message;
  private Object data;

  /**
   * 执行成功的返回结果
   *
   * @return
   */
  public static Result success() {
        return new Result(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), null);
  }

  public static Result success(Object data) {
    return new Result(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
  }

  /**
   * 执行失败的返回结果
   *
   * @param resultEnum 因为错误的状态码有很多种，如 403、404、500……因此，它最好作为一个传入参数
   * @return
   */
  public static Result error(ResultEnum resultEnum) {
    return new Result(resultEnum.getCode(), resultEnum.getMessage(), null);
  }

  public static Result error(ResultEnum resultEnum, Object data) {
    return new Result(resultEnum.getCode(), resultEnum.getMessage(), data);
  }
}
