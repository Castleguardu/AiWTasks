package com.codelabs.state.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelabs.state.data.RewardItem
import com.codelabs.state.data.repository.TaskRepository
import com.codelabs.state.domain.PurchaseRewardUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ShopViewModel(
    private val repository: TaskRepository,
    private val purchaseUseCase: PurchaseRewardUseCase
) : ViewModel() {

    // 商品列表流
    val rewards: StateFlow<List<RewardItem>> = repository.getAllRewards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 金币余额流
    val currentGold: StateFlow<Int> = repository.getUserStats()
        .map { it.gold }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // 一次性事件流（Snackbar）
    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    init {
        // 初始化默认商品
        viewModelScope.launch {
            repository.ensureDefaultRewards()
        }
    }

    /**
     * 处理购买意图
     */
    fun onPurchaseClick(item: RewardItem) {
        viewModelScope.launch {
            // 1. 获取当前余额
            val gold = currentGold.value

            // 2. 调用用例校验并计算
            val result = purchaseUseCase(gold, item.cost)

            // 3. 处理结果
            if (result.isSuccess) {
                // 扣款（更新 DB）
                repository.updatePlayerGold(result.getOrNull() ?: gold)
                _snackbarEvents.emit("成功购买：${item.title}")
            } else {
                // 失败提示
                _snackbarEvents.emit(result.exceptionOrNull()?.message ?: "购买失败")
            }
        }
    }

    /**
     * 添加自定义商品
     */
    fun addCustomReward(title: String, cost: Int) {
        viewModelScope.launch {
            repository.addReward(RewardItem(title = title, cost = cost))
        }
    }
    
    // Factory
    class Factory(
        private val repository: TaskRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShopViewModel(
                    repository = repository,
                    purchaseUseCase = PurchaseRewardUseCase()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
