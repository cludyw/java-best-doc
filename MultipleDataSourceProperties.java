import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigurationProperties("spring.datasource")
@Data
public class MultipleDataSourceProperties {
    private Map<String, Object> a;
    private Map<String, Object> b;

    public DataSourceProperties getADataSourceProperties() {
        return convertDataSourceProperties(a);
    }

    public DataSourceProperties getBDataSourceProperties() {
        return convertDataSourceProperties(b);
    }

    private DataSourceProperties convertDataSourceProperties(Map<String, Object> values) {
        DataSourceProperties properties = new DataSourceProperties();
        Class<DataSourceProperties> cls = DataSourceProperties.class;
        try {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String methodName = "set"
                        + Arrays.stream(entry.getKey().split("-"))
                                .map(item -> item.substring(0, 1).toUpperCase() + item.substring(1))
                                .collect(Collectors.joining());
                try {
                    Method setMethod =
                            cls.getMethod(methodName, entry.getValue().getClass());
                    setMethod.invoke(properties, entry.getValue());
                } catch (NoSuchMethodException ex) {
                    // 一些自定义参数，设置方法不存在，不做处理
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("动态数据源配置转化失败", ex);
        }
        return properties;
    }
}
