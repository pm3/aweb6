package com.aston.utils.servlet.japi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
// can use in method only.
public @interface ApiDescription {

	String value() default "";

	Class<?>[] usedTypes();
}
