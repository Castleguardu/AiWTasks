package com.codelabs.state.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelabs.state.data.RewardItem
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.ShopViewModel

@Composable
fun ShopScreen(
    shopViewModel: ShopViewModel = viewModel()
) {
    val rewards by shopViewModel.rewards.collectAsState()
    val currentGold by shopViewModel.currentGold.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // UI State for Dialog
    var showAddDialog by remember { mutableStateOf(false) }

    // 监听 ViewModel 的一次性事件
    LaunchedEffect(Unit) {
        shopViewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = RetroBeige,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // 简单的顶部金币显示栏，替代 TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(RetroDarkBrown, RoundedCornerShape(8.dp))
                    .border(2.dp, PixelGold, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "我的金库: 💰 $currentGold",
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelGold,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PixelGold,
                contentColor = RetroDarkBrown,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(2.dp, RetroDarkBrown, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = rewards, key = { it.id }) { item ->
                RewardItemCard(
                    item = item,
                    onBuyClick = { shopViewModel.onPurchaseClick(item) }
                )
            }
        }
        
        if (showAddDialog) {
            AddRewardDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, cost ->
                    shopViewModel.addCustomReward(title, cost)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun RewardItemCard(
    item: RewardItem,
    onBuyClick: () -> Unit
) {
    PixelCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = RetroDarkBrown,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "💰 ${item.cost}",
                style = MaterialTheme.typography.bodyLarge,
                color = RetroDarkBrown
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 购买按钮
            Button(
                onClick = onBuyClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PixelGreen,
                    contentColor = RetroDarkBrown
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("购买", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun AddRewardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        PixelCard(
            modifier = Modifier.padding(16.dp),
            backgroundColor = RetroBeige
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("上架新商品", style = MaterialTheme.typography.titleLarge)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("商品名称") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = costStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) costStr = it },
                    label = { Text("价格 (金币)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(
                        onClick = {
                            val cost = costStr.toIntOrNull() ?: 0
                            if (title.isNotBlank() && cost > 0) {
                                onConfirm(title, cost)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = PixelGreen,
                            contentColor = RetroDarkBrown
                        )
                    ) {
                        Text("上架")
                    }
                }
            }
        }
    }
}
