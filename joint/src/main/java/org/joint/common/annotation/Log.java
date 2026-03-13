package org.joint.common.annotation;

import org.joint.common.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String module() default "";

    BusinessType type() default BusinessType.OTHER;

    String description() default "";

    boolean saveRequestData() default true;

    boolean saveResponseData() default true;
}
