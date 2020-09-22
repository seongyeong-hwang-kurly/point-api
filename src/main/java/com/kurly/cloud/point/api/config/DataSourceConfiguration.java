package com.kurly.cloud.point.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

/**
 * 데이터 소스 설정.
 */
@Configuration
class DataSourceConfiguration {

  @Bean
  @ConfigurationProperties("spring.datasource.hikari")
  public HikariConfig hikariConfig() {
    return new HikariConfig();
  }

  @Bean("writeDataSource")
  DataSource writeDataSource(HikariConfig hikariConfig) {
    return new HikariDataSource(hikariConfig);
  }

  @Bean("readDataSource")
  DataSource readDataSource(
      HikariConfig hikariConfig,
      @Value("${db.read:}") String secondaryUrl) {
    if (secondaryUrl == null || "".equals(secondaryUrl)) {
      return null;
    }
    hikariConfig.setJdbcUrl(secondaryUrl);
    return new HikariDataSource(hikariConfig);
  }

  @Bean("routingDataSource")
  public DataSource routingDataSource(
      @Qualifier("writeDataSource") DataSource writeDataSource,
      @Autowired(required = false) @Qualifier("readDataSource") DataSource readDataSource) {

    if (readDataSource == null) {
      return writeDataSource;
    }

    RoutingDataSource routingDataSource = new RoutingDataSource();

    Map<Object, Object> dataSourceMap = new HashMap<>();
    dataSourceMap.put("write", writeDataSource);
    dataSourceMap.put("read", readDataSource);
    routingDataSource.setTargetDataSources(dataSourceMap);
    routingDataSource.setDefaultTargetDataSource(writeDataSource);

    return routingDataSource;
  }

  @Bean
  @Primary
  public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
    return new LazyConnectionDataSourceProxy(routingDataSource);
  }
}