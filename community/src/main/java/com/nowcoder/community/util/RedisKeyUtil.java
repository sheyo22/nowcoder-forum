package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPCHA = "kapcha";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_FORGET = "forget";

    //实体的赞
    //like:entity:{entityType}:{entityId} -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //用户登录凭证
    //ticket:{ticket} -> value
    public static String getUserTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户获得的赞
    //like:user:{userId} -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //用户关注的实体
    //followee:user:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //用户拥有的粉丝
    //follower:entityType:entityId
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPCHA + SPLIT + owner;
    }

    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    public static String getForgetCodeKey(String email) {
        return PREFIX_FORGET + SPLIT + email;
    }
}
