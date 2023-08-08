package com.hyc.qps.aop;

import com.hyc.qps.annotation.QpsLimit;
import com.hyc.qps.enums.DimensionType;
import com.hyc.qps.enums.Fallback;
import com.hyc.qps.exception.QpsLimitException;
import com.hyc.qps.factory.RateLimiterFactory;
import com.hyc.qps.limilter.RateLimiter;
import com.hyc.qps.rule.RateLimitRule;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 类<code>QpsLimitAop</code>说明：限流切面
 *
 * @author houyachao
 * @since 2023/08/08
 */
@Slf4j
@Aspect
public class QpsLimitAop {

    /**
     * 限流工厂
     */
    private RateLimiterFactory rateLimiterFactory;

    /**
     * 回调方法缓存
     */
    private Map<String, Method> callbackMethodMap = new HashMap<>();

    public QpsLimitAop(RateLimiterFactory rateLimiterFactory) {
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @Pointcut(value = "@annotation(com.hyc.qps.annotation.QpsLimit)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取签名和注解
        MethodSignature methodSignature = (MethodSignature) (joinPoint.getSignature());
        Method method = methodSignature.getMethod();
        QpsLimit qpsLimit = method.getAnnotation(QpsLimit.class);
        // 转换为Rule，并校验
        RateLimitRule rule = getRule(qpsLimit);

        // 根据类型获取limiter
        RateLimiter rateLimiter = rateLimiterFactory.create(qpsLimit.type());
        // 以merchantId或ShopId维度限制，获取其值
        Object merchantIdOrShopIdValue = getMerchantIdOrShopIdValue(qpsLimit.dimension(), joinPoint);
        // 尝试获取请求机会
        boolean pass = rateLimiter.tryAcquire(rule, merchantIdOrShopIdValue);
        // 默认抛出异常，后续再考虑添加其它场景
        if (pass) {
            return joinPoint.proceed();
        }
        // 限流后 fallback 处理
        fallback(joinPoint, method, qpsLimit, rule, rateLimiter, merchantIdOrShopIdValue);
        return this;
    }

    private void fallback(ProceedingJoinPoint joinPoint, Method method, QpsLimit qpsLimit, RateLimitRule rule, RateLimiter rateLimiter, Object merchantIdOrShopIdValue)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 如果没有限流策略，直接抛异常
        if (qpsLimit.fallback() == Fallback.EXCEPTION) {
            long waitTime = rateLimiter.getWaitTime(rule, merchantIdOrShopIdValue);
            throw new QpsLimitException(500, rule.getKey() + "触发限流规则, 请等待" + waitTime, waitTime);
        }

        // 策略为调用回调方法
        if (Fallback.CALLBACK == qpsLimit.fallback()) {
            // 获取类
            Class<?> declaringClass = method.getDeclaringClass();
            String methodKey = declaringClass.getName() + "_" + method.getName();
            // 从map中获取回调方法，如果不存在则使用反射获取
            Method callbackMethod = callbackMethodMap.get(methodKey);
            if (callbackMethod == null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                callbackMethod = declaringClass.getMethod(qpsLimit.callbackMethod(), parameterTypes);
                callbackMethod.setAccessible(true);
                callbackMethodMap.put(methodKey, callbackMethod);
            }
            // 执行回调方法
            callbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());
        }
    }

    private Object getMerchantIdOrShopIdValue(DimensionType dimension, ProceedingJoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException {
        if (dimension == null) {
            return "";
        }
        // 参数值
        Object[] args = joinPoint.getArgs();
        // 参数名
        String[] fieldsName = getFieldsName(joinPoint);
        for (int i = 0; i < fieldsName.length; i++) {
            String fieldName = fieldsName[i];
            if ("merchantId".equals(fieldName) || "shopId".equals(fieldName)) {
                return args[i];
            }
        }
        throw new QpsLimitException(500, " DimensionType: " + " 参数名必须含有merchantId或shopId");
    }

    /**
     * 方法<code>getFieldsName</code>说明: 返回方法的参数名
     *
     * @param joinPoint joinPoint
     * @return 参数名
     */
    private static String[] getFieldsName(JoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException {
        String classType = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Class<?>[] classes = new Class[args.length];
        for (int k = 0; k < args.length; k++) {
            if (!args[k].getClass().isPrimitive()) {
                //获取的是封装类型而不是基础类型
                String result = args[k].getClass().getName();
                Class s = map.get(result);
                classes[k] = s == null ? args[k].getClass() : s;
            }
        }
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        //获取指定的方法，第二个参数可以不传，但是为了防止有重载的现象，还是需要传入参数的类型
        Method method = Class.forName(classType).getMethod(methodName, classes);
        String[] parameterNames = pnd.getParameterNames(method);
        return parameterNames;
    }

    private static HashMap<String, Class> map = new HashMap<String, Class>() {
        {
            put("java.lang.Integer", int.class);
            put("java.lang.Double", double.class);
            put("java.lang.Float", float.class);
            put("java.lang.Long", long.class);
            put("java.lang.Short", short.class);
            put("java.lang.Boolean", boolean.class);
            put("java.lang.Char", char.class);
        }
    };

    /**
     * 通过注解构建限流规则
     *
     * @param qpsLimit 注解
     * @return 限流规则
     */
    private RateLimitRule getRule(QpsLimit qpsLimit) {
        return RateLimitRule.commonRule(qpsLimit.key(), qpsLimit.limit())
                .withTime(qpsLimit.time())
                .withUnit(qpsLimit.unit())
                .withRate(qpsLimit.rate())
                .dimension(qpsLimit.dimension())
                .build();
    }

}
