/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.config;

import com.kurly.cloud.api.common.config.InMemoryUserDetailService;
import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
public class SpringSecurityTestConfig {
  @Bean
  @Primary
  public UserDetailsService userDetailsService() {
    KurlyUserPrincipal user = KurlyUserPrincipal.builder()
        .uuid("user")
        .level(1)
        .no(1L)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    KurlyUserPrincipal admin = KurlyUserPrincipal.builder()
        .uuid("admin")
        .level(100)
        .no(2L)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .build();

    return new InMemoryUserDetailService(Arrays.asList(
        user, admin
    ));
  }
}
