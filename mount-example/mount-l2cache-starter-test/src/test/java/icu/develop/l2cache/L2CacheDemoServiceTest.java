package icu.develop.l2cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/8 13:37
 */
@Slf4j
@SpringBootTest(classes = {L2CacheApplication.class}, webEnvironment = RANDOM_PORT)
class L2CacheDemoServiceTest {

    @Resource
    private L2CacheDemoService l2CacheDemoService;

    @Test
    void test1() {
        l2CacheDemoService.test();
    }

    @Test
    void test2() {
        l2CacheDemoService.test2();
    }

    @Test
    void test3() {
        l2CacheDemoService.test3();
    }

    @Test
    void test4() {
        l2CacheDemoService.test4();
        l2CacheDemoService.test4();
    }
    @Test
    void test5() {
        l2CacheDemoService.test5("test111s78", "test3");
    }

    @Test
    void test6() {
        l2CacheDemoService.test6("test111s78", "test3");
    }
    @Test
    void test7() {
        l2CacheDemoService.test7(new Demo("test1111s78", "test2223"));
    }
    @Test
    void test8() {
        l2CacheDemoService.test8();
    }

    @Test
    void test9() {
        l2CacheDemoService.test8();
        l2CacheDemoService.test8();
        l2CacheDemoService.test9();
        l2CacheDemoService.test8();
    }
}
