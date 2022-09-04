package com.tianhaolin.groovy.dsl

/**
 * 升级带宽
 */
class BroadbandPlus {

    def rewards = new RewardService()

    // 确保用户账户能够消费，获取当前的资源
    def canConsume = { account, media ->
        def now = new Date()
        if (account.mediaList[media]?.after(now))
            return true
        account.points > media.points
    }

    // 消费，在消费的过程就要知道是否要奖励积分
    def consume = { account, media ->
        // 第一次消费才奖励
        if (account.mediaList[media.title] == null) {
            def now = new Date()
            account.points -= media.points
            // 延长资源的可用时长
            account.mediaList[media] = now + media.daysAccess
            // 应用 DSL 奖励规则 rewards.applyRewardsOnConsume(account, media)
        }
    }

    def extend = {account, media, days ->
        if (account.mediaList[media] != null) {
            account.mediaList[media] += days
            println "extend media ${media.title} $days days for ${account.subscriber}"
        }
    }
}