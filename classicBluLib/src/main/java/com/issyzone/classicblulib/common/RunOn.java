package com.issyzone.classicblulib.common;

import java.lang.annotation.*;

/**
 * 标记方法执行线程
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RunOn {
    /**
     * 运行线程
     */
    ThreadMode value() default ThreadMode.UNSPECIFIED;
}
