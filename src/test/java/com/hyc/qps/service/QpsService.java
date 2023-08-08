package com.hyc.qps.service;

import com.hyc.qps.annotation.QpsLimit;
import com.hyc.qps.enums.DimensionType;
import com.hyc.qps.enums.Fallback;
import com.hyc.qps.enums.LimiterType;
import org.springframework.stereotype.Service;

/**
 * 类<code>$</code>说明：
 *
 * @author houyachao
 * @since $
 */
@Service
public class QpsService {

    @QpsLimit(type = LimiterType.TOKEN_BUCKET, key = "AMZ", dimension = DimensionType.MERCHANT_ID, limit = 10, rate = 0.15,
        fallback = Fallback.CALLBACK, callbackMethod = "call")
    public void testQps(String merchantId) {
        System.out.println("11111111111111111111111111111");
    }

    public void call(String merchantId) {
        System.out.println("回调");
    }
}
