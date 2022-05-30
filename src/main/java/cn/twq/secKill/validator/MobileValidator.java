package cn.twq.secKill.validator;

import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/** 配合 @Mobile 一起使用，负责校验前台传过来的手机号参数是否合法 */

public class MobileValidator implements ConstraintValidator<Mobile, String> {

  private boolean required;
  /** 手机号码格式校验的正则表达式 */
  private static final Pattern MOBILE_PATTERN = Pattern.compile("[1]([3-9])[0-9]{9}$");

  @Override
  public void initialize(Mobile constraintAnnotation) {
    required = constraintAnnotation.required();
  }

  /**
   * 自定义校验规则。
   * 判断手机号码格式是否正确
   *
   * @param mobile
   * @param constraintValidatorContext
   * @return 成功则放行进入 controller 层，否则就会以抛出异常的方式（BindException）显示在后端的控制台上（但是前台页面上并不会提示，这也是为什么我们需要一个「全局异常处理器」来处理这种情况）
   */
  @Override
  public boolean isValid(String mobile, ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isEmpty(mobile)) {
      return !required;
    }
    System.out.println();
    return MOBILE_PATTERN.matcher(mobile).matches();
  }
}
