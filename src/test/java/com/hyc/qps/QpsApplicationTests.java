package com.hyc.qps;

import com.hyc.qps.service.QpsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@SpringBootTest(classes = QpsApplicationContext.class)
class QpsApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QpsService qpsService;

    @Test
    void contextLoads() {
        System.out.println(redisTemplate.opsForValue().get("a"));
    }

    public void test(String merchantId) {

    }

    public static void main(String[] args) throws Exception {
        QpsApplicationTests qpsApplicationTests = new QpsApplicationTests();
        Class<? extends QpsApplicationTests> aClass = qpsApplicationTests.getClass();
        Method method = aClass.getMethod("test", String.class);

        Parameter[] parameters = method.getParameters();
        Class<?>[] parameterTypes = method.getParameterTypes();
        System.out.println();
    }

    @Test
    public void testQps() {
        for (int i = 0; i < 15; i++) {
            qpsService.testQps("HYC12121");
        }
    }
}
