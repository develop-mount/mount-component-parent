package icu.develop.l2cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/7 9:46
 */
@Data
@ConfigurationProperties(prefix = "l2cache")
public class L2CacheProperties {

    private Long ttl = -1L;
    private Map<String, CacheItem> items = new LinkedHashMap<>();

    @Data
    public static class CacheItem {
        private Long ttl = 1200L;
    }
}
