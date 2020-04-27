package com.kurly.cloud.point.api.point.controller;

import com.kurly.cloud.api.common.config.KurlyJWTSecurityConfig;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import({SpringSecurityTestConfig.class, KurlyJWTSecurityConfig.class})
abstract class AbstractControllerTest {
}
