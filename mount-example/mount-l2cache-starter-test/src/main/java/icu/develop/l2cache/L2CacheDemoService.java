package icu.develop.l2cache;

import com.vevor.prm.common.cache.annotation.CacheKeyType;
import com.vevor.prm.common.cache.annotation.L2CacheEvict;
import com.vevor.prm.common.cache.annotation.L2CachePut;
import com.vevor.prm.common.cache.annotation.L2Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/8 13:35
 */
@Slf4j
@Service
public class L2CacheDemoService {

    @L2Cacheable(cacheKeyType = CacheKeyType.FIX, cacheKey = "test2221")
    public Demo test() {

        return new Demo("lsda", "1");
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.FIX, cacheKey = "test2221")
    public void test2() {

        return;
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.FIX, cacheKey = "testlist221")
    public List<Demo> test3() {

        List<Demo> demoList = new ArrayList<>();
        demoList.add(new Demo("lsda", "1"));
        return demoList;
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.FIX, cacheKey = "testlist22122288811")
    public List<Demo> test4() {

        return new ArrayList<>();
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.EXPRESSION, cacheKey = "#test1 + ':' +#test2", strategy = DemoStrauss.class)
    public List<Demo> test5(String test1, String test2) {

        List<Demo> demoList = new ArrayList<>();
        demoList.add(new Demo("lsda", "1"));
        return demoList;
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.REQUEST_ARGS, strategy = DemoStrauss.class)
    public List<Demo> test6(String test1, String test2) {

        List<Demo> demoList = new ArrayList<>();
        demoList.add(new Demo("lsda", "1"));
        return demoList;
    }

    @L2Cacheable(cacheNames = "demo", cacheKeyType = CacheKeyType.REQUEST_ARGS, strategy = DemoStrauss.class)
    public List<Demo> test7(Demo demo) {

        List<Demo> demoList = new ArrayList<>();
        demoList.add(demo);
        return demoList;
    }

    @L2Cacheable(cacheKeyType = CacheKeyType.FIX, cacheKey = "test222100111009900")
    public Demo test8() {
        log.info("data from db");
        return new Demo("lsda", "1");
    }

    @L2CacheEvict(cacheKeyType = CacheKeyType.FIX, cacheKey = "test222100111009900")
    public void test9() {
        log.info("data cache evict");
    }

    @L2CachePut(cacheKeyType = CacheKeyType.FIX, cacheKey = "test222100111009900")
    public Demo test10() {
        log.info("data cache put");
        return new Demo("lsda", "1");
    }
}
