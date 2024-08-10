package com.catas.wicked.server;

import io.micronaut.context.annotation.Requires;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ConditionalTestExecutionExtension.class)
public @interface ConditionalTest {

    String value() default "";

    Requires.Family[] os() default {};
}
