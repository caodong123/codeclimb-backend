package com.yupi.codeclimb.constant;

public interface RedisConstant {

    /**
     * 用户登录记录key前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signins:";

    static String getUserSignInRedisKey(int year,long userId){
        return String.format("%s:%s:%s",USER_SIGN_IN_REDIS_KEY_PREFIX,year,userId);

    }

}
