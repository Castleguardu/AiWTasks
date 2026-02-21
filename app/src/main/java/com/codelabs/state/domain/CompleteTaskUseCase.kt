package com.codelabs.state.domain

import com.codelabs.state.data.UserStats

/**
 * 纯业务逻辑类
 * 职责：计算任务完成后的奖励发放和等级提升
 */
class CompleteTaskUseCase {

    /**
     * @param currentStats 当前玩家状态
     * @param expReward 任务奖励的经验值
     * @param goldReward 任务奖励的金币数
     * @return 更新后的玩家状态
     */
    operator fun invoke(currentStats: UserStats, expReward: Int, goldReward: Int): UserStats {
        var newExp = currentStats.currentExp + expReward
        var newLevel = currentStats.level
        var newGold = currentStats.gold + goldReward

        // 升级逻辑：每 100 经验升一级，经验溢出
        while (newExp >= 100) {
            newLevel++
            newExp -= 100
        }

        return currentStats.copy(
            level = newLevel,
            currentExp = newExp,
            gold = newGold
        )
    }
}
