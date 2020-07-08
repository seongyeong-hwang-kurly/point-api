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
        .no(999999999L)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    KurlyUserPrincipal admin = KurlyUserPrincipal.builder()
        .uuid("admin")
        .level(100)
        .no(999999998L)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .build();

    return new InMemoryUserDetailService(Arrays.asList(
        user, admin
    ));
  }
}
