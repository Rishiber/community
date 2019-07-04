package com.nowcoder.community.util;

/**
 * @Title: CommunityConstant
 * @Description:
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/7/3 14:55
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 *12;

    /**
     * 记住状态下的登录凭证的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}
