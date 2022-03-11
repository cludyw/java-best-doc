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
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultipleDataSourceProperties.class)
public class MultipleDataSourceConfiguration {
    private static final String A_MAPPER_XML_PATH = "classpath*:/mapper/a/*.xml";
    private static final String B_MAPPER_XML_PATH = "classpath*:/mapper/b/*.xml";

    private final MultipleDataSourceProperties multipleDataSourceProperties;
    private final MybatisPlusAutoConfiguration aAutoConfiguration;
    private final MybatisPlusAutoConfiguration bAutoConfiguration;

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
        this.aAutoConfiguration = this.newMybatisPlusAutoConfiguration(
                mybatisPlusProperties,
                interceptorsProvider,
                typeHandlersProvider,
                languageDriversProvider,
                resourceLoader,
                databaseIdProvider,
                configurationCustomizersProvider,
                mybatisPlusPropertiesCustomizerProvider,
                applicationContext,
                A_MAPPER_XML_PATH);
        this.bAutoConfiguration = newMybatisPlusAutoConfiguration(
                mybatisPlusProperties,
                interceptorsProvider,
                typeHandlersProvider,
                languageDriversProvider,
                resourceLoader,
                databaseIdProvider,
                configurationCustomizersProvider,
                mybatisPlusPropertiesCustomizerProvider,
                applicationContext,
                B_MAPPER_XML_PATH);
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(basePackages = "com.**.mapper")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "false", matchIfMissing = true)
    public class DataSourceConfig {
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        @Bean
        @Primary
        public DataSource aDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getADataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("a-hikari-pool");
            return dataSource;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(
            basePackages = "com.**.mapper.a",
            sqlSessionFactoryRef = "aSqlSessionFactory",
            sqlSessionTemplateRef = "aSqlSessionTemplate")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "true")
    public class ADataSourceConfig {
        @Bean
        @Primary
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public DataSource aDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getADataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("a-hikari-pool");
            return dataSource;
        }

        @Bean
        @Primary
        public SqlSessionFactory aSqlSessionFactory(@Qualifier("aDataSource") DataSource dataSource)
                throws Exception {
            return aAutoConfiguration.sqlSessionFactory(dataSource);
        }

        @Bean
        @Primary
        public DataSourceTransactionManager aTransactionManager(@Qualifier("aDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        @Primary
        public SqlSessionTemplate aSqlSessionTemplate(
                @Qualifier("aSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan(
            basePackages = "com.**.mapper.b",
            sqlSessionFactoryRef = "bSqlSessionFactory",
            sqlSessionTemplateRef = "bSqlSessionTemplate")
    @ConditionalOnProperty(value = "spring.datasource.multiple", havingValue = "true")
    public class BDataSourceConfig {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public DataSource bDataSource() {
            HikariDataSource dataSource = multipleDataSourceProperties
                    .getBDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setPoolName("b-hikari-pool");
            return dataSource;
        }

        @Bean
        public SqlSessionFactory bSqlSessionFactory(@Qualifier("bDataSource") DataSource dataSource) throws Exception {
            return bAutoConfiguration.sqlSessionFactory(dataSource);
        }

        @Bean
        public DataSourceTransactionManager bTransactionManager(@Qualifier("bDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public SqlSessionTemplate bSqlSessionTemplate(
                @Qualifier("bSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
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
