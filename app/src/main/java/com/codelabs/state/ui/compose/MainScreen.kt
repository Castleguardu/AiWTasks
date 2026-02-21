package com.codelabs.state.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codelabs.state.ui.theme.BasicStateCodelabTheme
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown

// 定义路由常量
private object Routes {
    const val TASKS = "Tasks"
    const val SCHEDULE = "Schedule"
    const val STORE = "Store"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = RetroBeige,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AiWTasks",
                        style = MaterialTheme.typography.titleLarge,
                        color = RetroDarkBrown
                    )
                },
                actions = {
                    // 玩家等级和财富展示
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(RetroDarkBrown, shape = RectangleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Lv.5 | 💰 250",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PixelGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RetroBeige,
                    titleContentColor = RetroDarkBrown,
                    actionIconContentColor = RetroDarkBrown
                ),
                modifier = Modifier.border(
                    width = 3.dp,
                    color = RetroDarkBrown,
                    shape = RectangleShape
                ) // 给 TopBar 加个下边框效果（通过整体边框模拟，实际只想要下边框可能需要自定义 Modifier，这里简单处理）
            )
        },
        bottomBar = {
            // 获取当前路由，用于选中状态
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(
                containerColor = RetroBeige,
                contentColor = RetroDarkBrown,
                modifier = Modifier.border(
                    width = 3.dp,
                    color = RetroDarkBrown,
                    shape = RectangleShape
                )
            ) {
                val items = listOf(
                    Triple(Routes.TASKS, "任务板", Icons.Default.List),
                    Triple(Routes.SCHEDULE, "日程表", Icons.Default.DateRange),
                    Triple(Routes.STORE, "商店", Icons.Default.ShoppingCart)
                )

                items.forEach { (route, label, icon) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RetroBeige, // 选中时图标变浅色
                            selectedTextColor = RetroDarkBrown,
                            indicatorColor = PixelGreen, // 选中背景色
                            unselectedIconColor = RetroDarkBrown,
                            unselectedTextColor = RetroDarkBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 仅在任务板显示 FAB
            if (currentRoute == Routes.TASKS) {
                FloatingActionButton(
                    onClick = { /* TODO: Open Add Task Dialog */ },
                    containerColor = PixelGreen,
                    contentColor = RetroDarkBrown,
                    shape = RoundedCornerShape(4.dp), // 低圆角，接近方形
                    modifier = Modifier.border(2.dp, RetroDarkBrown, RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TASKS) {
                TasksScreen()
            }
            composable(Routes.SCHEDULE) {
                PlaceholderScreen("日程表功能开发中...")
            }
            composable(Routes.STORE) {
                PlaceholderScreen("商店功能开发中...")
            }
        }
    }
}

@Composable
fun TasksScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 模拟测试数据
        PixelCard {
            Column {
                Text(
                    text = "给海莉送向日葵",
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroDarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "好感度 +1",
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroDarkBrown.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "💰50 | ✨10",
                        style = MaterialTheme.typography.labelLarge,
                        color = PixelGold,
                        modifier = Modifier
                            .background(RetroDarkBrown, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        // 可以再复制一个看列表效果
        PixelCard {
            Column {
                Text(
                    text = "浇灌农场作物",
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroDarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "每日必做任务",
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroDarkBrown.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = RetroDarkBrown
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BasicStateCodelabTheme {
        MainScreen()
    }
}
