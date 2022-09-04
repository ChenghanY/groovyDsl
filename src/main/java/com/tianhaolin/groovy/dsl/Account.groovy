package com.tianhaolin.groovy.dsl

class Account {
    /**
     * 订阅服务的人
     */
    String subscriber

    /**
     * 订阅等级 Basic / Plus / Premium
     */
    String plan

    /**
     * 订阅者获得的积分
     */
    int points

    /**
     * 订阅者花的钱
     */
    double spend

    /**
     * 订阅者订阅的媒体
     */
    Map mediaList = [:]
    void addMedia (media, expiry) {
        // 为用户订阅的媒体类型设置可用期限
        mediaList[media] = expiry
    }
    void extendMedia(media, length) {
        mediaList[media] += length
    }
    Date getMediaExpiry(media) {
        if(mediaList[media] != null) {
            return mediaList[media]
        }
    }

    @Override
    String toString() {
        String str = "subscriber:"+subscriber+"\n" +
                "plan:"+plan+"\n" +
                "points:"+points+"\n" +
                "spend:"+spend+"\n"

        mediaList.keySet().each {
            str +=  it.title+","+mediaList.get(it)+"\n"
        }
        return str
    }
}