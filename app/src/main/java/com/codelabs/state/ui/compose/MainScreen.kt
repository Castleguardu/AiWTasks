package com.codelabs.state.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codelabs.state.WellnessApplication
import com.codelabs.state.ui.theme.PixelGold
import com.codelabs.state.ui.theme.PixelGreen
import com.codelabs.state.ui.theme.RetroBeige
import com.codelabs.state.ui.theme.RetroDarkBrown
import com.codelabs.state.viewmodel.TasksViewModel

// 定义路由常量
private object Routes {
    const val TASKS = "Tasks"
    const val SCHEDULE = "Schedule"
    const val STORE = "Store"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModel.Factory((LocalContext.current.applicationContext as WellnessApplication).taskRepository)
    )
) {
    val navController = rememberNavController()
    
    // 控制是否显示添加任务对话框（或者跳转到添加页面）
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // 监听玩家状态用于 TopAppBar
    val userStats by tasksViewModel.userStats.collectAsState()

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
                            // 动态展示：如果 userStats 为 null，显示加载中或默认值
                            text = if (userStats != null) "Lv.${userStats!!.level} | 💰 ${userStats!!.gold}" else "Loading...",
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
                ) 
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
                    onClick = { showAddTaskDialog = true },
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
        
        // 如果显示添加对话框，这里可以是一个 ModalBottomSheet 或者 Dialog
        if (showAddTaskDialog) {
             // 简单的对话框包裹 WellnessTaskInput
             androidx.compose.ui.window.Dialog(onDismissRequest = { showAddTaskDialog = false }) {
                 androidx.compose.material3.Surface(
                     shape = RoundedCornerShape(8.dp),
                     color = RetroBeige,
                     modifier = Modifier.padding(16.dp).border(3.dp, RetroDarkBrown, RoundedCornerShape(8.dp))
                 ) {
                     WellnessTaskInput(
                         onTaskAdd = { title, time, rrule ->
                             tasksViewModel.onTaskAdded(title, time, rrule)
                             showAddTaskDialog = false
                         }
                     )
                 }
             }
        }

        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TASKS) {
                // 这里传入 TasksViewModel
                TasksScreen(tasksViewModel = tasksViewModel)
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

// TasksScreen 需要在 TasksScreen.kt 中定义，这里不需要重复
// PlaceholderScreen 保持不变
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
