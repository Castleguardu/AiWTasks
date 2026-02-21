package com.codelabs.state.domain

/**
 * 购买商品用例
 * 职责：校验金币是否足够，并计算扣款后的余额
 */
class PurchaseRewardUseCase {

    /**
     * @param currentGold 当前玩家金币数
     * @param cost 商品价格
     * @return 成功返回剩余金币数，失败抛出异常
     */
    operator fun invoke(currentGold: Int, cost: Int): Result<Int> {
        if (currentGold >= cost) {
            val newGold = currentGold - cost
            return Result.success(newGold)
        } else {
            return Result.failure(Exception("金币不足 ($currentGold/$cost)，请去完成任务！"))
        }
    }
}
