package cn.fay.terminal.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Terminal {
    String value();
    String parser() default "default";
}
