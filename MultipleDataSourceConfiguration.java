/*
 * Copyright (c) 2006-2021 Hzins Ltd. All Rights Reserved.
 * <p>
 * This code is the confidential and proprietary information of
 * Hzins.You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Hzins,https://www.huize.com.
 * </p>
 */
package com.hzins.travel.byt.starter.jdbc.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.List;

/**
 * 多数据源配置，参照{@link org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration}, {@link MybatisPlusAutoConfiguration}
 * <p>
 * 1. spring.datasource.multiple=true 时才启用多数据源配置
 * 2. 保运通mapper路径：com.hzins.travel.byt.*.*.mapper.byt
 *    携保mapper路径：com.hzins.travel.byt.*.*.mapper.travel
 * 3. 保运通mapper xml路径：classpath:/mapper/byt/*.xml
 *    携保mapper xml路径：classpath:/mapper/travel/*.xml
 * </p>
 *
 * @author hz21056680
 * @date 2021/12/13 13:57
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultipleDataSourceProperties.class)
public class MultipleDataSourceConfiguration {
    private static final String BYT_MAPPER_XML_PATH = "classpath*:/mapper/byt/*.xml";
    private static final String TRAVEL_MAPPER_XML_PATH = "classpath*:/mapper/travel/*.xml";

    private final MultipleDataSourceProperties multipleDataSourceProperties;
    private final MybatisPlusAutoConfiguration bytAutoConfiguration;
    private final MybatisPlusAutoConfiguration travelAutoConfiguration;

    public MultipleDataSourceConfiguration(
            MultipleDataSourceProperties multipleDataSourceProperties,
            MybatisPlusProperties mybatisPlusProperties,
            ObjectProvider<Interceptor[]> interceptorsProvider,
            ObjectProvider<TypeHandler[]> typeHandlersProvider,
            ObjectProvider<LanguageDriver[]> languageDriversProvider,
            ResourceLoader resourceLoader,
            ObjectProvider<DatabaseIdProvider> databaseIdProvider,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
            ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider,
            ApplicationContext applicationContext) {
        this.multipleDataSourceProperties = multipleDataSourceProperties;
        this.bytAutoConfiguration = this.newMybatisPlusAutoConfiguration(
                mybatisPlusProperties,
                interceptorsProvider,
                typeHandlersProvider,
                languageDriversProvider,
                resourceLoader,
                databaseIdProvider,
                configurationCustomizersProvider,
                mybatisPlusPropertiesCustomizerProvider,
                applicationContext,
                BYT_MAPPER_XML_PATH);
        this.travelAutoConfiguration = newMybatisPlusAutoConfiguration(
                mybatisPlusProperties,
                interceptorsProvider,
                typeHandlersProvider,
                languageDriversProvider,
                resourceLoader,
                databaseIdProvider,
                configurationCustomizersProvider,
                mybatisPlusPropertiesCustomizerProvider,
                applicationContext,
                TRAVEL_MAPPER_XML_PATH);
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(basePackages = "com.hzins.travel.byt.**.mapper")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "false", matchIfMissing = true)
    public class DataSourceConfig {
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        @Bean
        @Primary
        public DataSource bytDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getBytDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("byt-hikari-pool");
            return dataSource;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(
            basePackages = "com.hzins.travel.byt.**.mapper.byt",
            sqlSessionFactoryRef = "bytSqlSessionFactory",
            sqlSessionTemplateRef = "bytSqlSessionTemplate")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "true")
    public class BytDataSourceConfig {
        @Bean
        @Primary
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public DataSource bytDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getBytDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("byt-hikari-pool");
            return dataSource;
        }

        @Bean
        @Primary
        public SqlSessionFactory bytSqlSessionFactory(@Qualifier("bytDataSource") DataSource dataSource)
                throws Exception {
            return bytAutoConfiguration.sqlSessionFactory(dataSource);
        }

        @Bean
        @Primary
        public DataSourceTransactionManager bytTransactionManager(@Qualifier("bytDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        @Primary
        public SqlSessionTemplate bytSqlSessionTemplate(
                @Qualifier("bytSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(
            basePackages = "com.hzins.travel.byt.**.mapper.travel",
            sqlSessionFactoryRef = "travelSqlSessionFactory",
            sqlSessionTemplateRef = "travelSqlSessionTemplate")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "true")
    public class TravelDataSourceConfig {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public DataSource travelDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getTravelDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("travel-hikari-pool");
            return dataSource;
        }

        @Bean
        public SqlSessionFactory travelSqlSessionFactory(@Qualifier("travelDataSource") DataSource dataSource)
                throws Exception {
            return travelAutoConfiguration.sqlSessionFactory(dataSource);
        }

        @Bean
        public DataSourceTransactionManager travelTransactionManager(
                @Qualifier("travelDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public SqlSessionTemplate travelSqlSessionTemplate(
                @Qualifier("travelSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * 利用{@link MybatisPlusAutoConfiguration}现有代码创建 {@link SqlSessionFactory}
     */
    private MybatisPlusAutoConfiguration newMybatisPlusAutoConfiguration(
            MybatisPlusProperties mybatisPlusProperties,
            ObjectProvider<Interceptor[]> interceptorsProvider,
            ObjectProvider<TypeHandler[]> typeHandlersProvider,
            ObjectProvider<LanguageDriver[]> languageDriversProvider,
            ResourceLoader resourceLoader,
            ObjectProvider<DatabaseIdProvider> databaseIdProvider,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
            ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider,
            ApplicationContext applicationContext,
            String mapperLocation) {
        // 覆盖MybatisPlusProperties的mapperLocations值
        MybatisPlusProperties properties = new MybatisPlusProperties();
        BeanUtils.copyProperties(mybatisPlusProperties, properties);
        properties.setMapperLocations(new String[] {mapperLocation});

        // MybatisConfiguration隔离
        MybatisConfiguration configuration = new MybatisConfiguration();
        BeanUtils.copyProperties(mybatisPlusProperties.getConfiguration(), configuration);
        properties.setConfiguration(configuration);
        return new MybatisPlusAutoConfiguration(
                properties,
                interceptorsProvider,
                typeHandlersProvider,
                languageDriversProvider,
                resourceLoader,
                databaseIdProvider,
                configurationCustomizersProvider,
                mybatisPlusPropertiesCustomizerProvider,
                applicationContext);
    }
}
