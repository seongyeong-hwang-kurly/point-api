package com.kurly.cloud.point.api.point.common;

import com.kurly.cloud.api.common.config.KurlyJWTSecurityConfig;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TransactionalTest
@Import({SpringSecurityTestConfig.class, KurlyJWTSecurityConfig.class})
public @interface ControllerTest {
}
