package com.aston.utils.sql.anot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aston.utils.sql.IConverter;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Column {

	String name() default "";

	Class<? extends IConverter> convert() default IConverter.class;

	String format() default "";
}
