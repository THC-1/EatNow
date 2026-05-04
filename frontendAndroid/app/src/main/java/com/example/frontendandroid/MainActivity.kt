package com.example.frontendandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontendandroid.data.Canteen
import com.example.frontendandroid.data.Dish
import com.example.frontendandroid.data.Feedback
import com.example.frontendandroid.data.LotteryRecord
import com.example.frontendandroid.data.Recommendation
import com.example.frontendandroid.ui.theme.FrontendAndroidTheme
import kotlinx.coroutines.launch

private val PageBg = Color(0xFFFFF7EA)
private val CardBg = Color(0xFFFFFDF7)
private val Ink = Color(0xFF2B2118)
private val TitleInk = Color(0xFF241811)
private val Red = Color(0xFFE94B35)
private val Green = Color(0xFF7FBD41)
private val GreenDark = Color(0xFF4F8F20)
private val Gold = Color(0xFFFFD64E)
private val Orange = Color(0xFFFF9F43)
private val SubText = Color(0xFF8A7A68)
private val Muted = Color(0xFF9D8466)
private val Line = Color(0x1A2B2118)
private val GreenSoft = Color(0xFFE9F8CF)
private val OrangeSoft = Color(0xFFFFE2D0)
private val YellowSoft = Color(0xFFFFF1D1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrontendAndroidTheme(dynamicColor = false) {
                EatNowApp()
            }
        }
    }
}

@Composable
fun EatNowApp() {
    val context = LocalContext.current.applicationContext
    val state = remember(context) { EatNowAppState(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it) }
    }
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) state.refreshStudentHome()
    }

    if (!state.loggedIn) {
        LoginScreen(state)
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppTopBar(
                state = state,
                onBack = {
                    when {
                        state.area == MainArea.MERCHANT -> state.backToStudentHome()
                        state.studentRoute == StudentRoute.DETAIL -> state.backFromDetail()
                        state.studentRoute == StudentRoute.DISHES -> state.goStudent(state.studentTab)
                        else -> state.logout()
                    }
                }
            )
        },
        bottomBar = {
            if (state.area == MainArea.STUDENT && state.studentRoute in mainStudentRoutes) {
                StudentBottomBar(state)
            }
        },
        containerColor = PageBg
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(pageBrush())
        ) {
            if (state.area == MainArea.STUDENT) {
                StudentContent(state)
            } else {
                MerchantContent(state)
            }
            if (state.loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Red,
                    trackColor = CardBg
                )
            }
        }
    }
}

private val mainStudentRoutes = setOf(StudentRoute.HOME, StudentRoute.CANTEENS, StudentRoute.LOTTERY, StudentRoute.MINE)

@Composable
fun LoginScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBrush())
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("EatNow", color = Green, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(
                if (register) "注册账号" else "今天吃点啥",
                color = TitleInk,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Black
            )
            Text("交给校园里的真实口碑", color = SubText, fontSize = 15.sp)
            MiniTextField(username, { username = it }, "用户名")
            MiniTextField(password, { password = it }, "密码", password = true)
            if (register) MiniTextField(nickname, { nickname = it }, "昵称")
            PrimaryButton(if (register) "注册并进入" else "登录", Modifier.fillMaxWidth()) {
                scope.launch {
                    if (register) state.register(username, password, nickname) else state.login(username, password)
                }
            }
            TextButton(onClick = { register = !register }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(if (register) "已有账号，去登录" else "没有账号？注册一个", color = Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(state: EatNowAppState, onBack: () -> Unit) {
    val isBack = state.area == MainArea.MERCHANT || state.studentRoute == StudentRoute.DISHES || state.studentRoute == StudentRoute.DETAIL
    val title = when {
        state.area == MainArea.MERCHANT -> when (state.merchantRoute) {
            MerchantRoute.DASHBOARD -> "卖家工作台"
            MerchantRoute.APPLY -> "商家入驻"
            MerchantRoute.DISHES -> "菜品管理"
            MerchantRoute.RECOMMENDATIONS -> "推荐管理"
            MerchantRoute.FEEDBACK -> "留言反馈"
        }
        state.studentRoute == StudentRoute.DISHES -> "菜品"
        state.studentRoute == StudentRoute.DETAIL -> "菜品详情"
        else -> "饭点盲盒"
    }
    CenterAlignedTopAppBar(
        title = { Text(title, color = TitleInk, fontWeight = FontWeight.Black, fontSize = 17.sp) },
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text(if (isBack) "返回" else "退出", color = if (isBack) Red else Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        actions = {
            if (state.area == MainArea.STUDENT && state.studentRoute in mainStudentRoutes) {
                TextButton(onClick = { state.goMerchant() }) {
                    Text(if (state.canUseMerchant) "商家端" else "入驻", color = GreenDark, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg)
    )
}

@Composable
fun StudentBottomBar(state: EatNowAppState) {
    NavigationBar(containerColor = CardBg, tonalElevation = 0.dp) {
        StudentMainTab.entries.forEach { tab ->
            val selected = state.studentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { state.goStudent(tab) },
                icon = { Text(tabIcon(tab), fontWeight = FontWeight.Black, color = if (selected) Red else SubText) },
                label = { Text(tabText(tab), color = if (selected) Red else SubText, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun StudentContent(state: EatNowAppState) {
    when (state.studentRoute) {
        StudentRoute.HOME -> HomeScreen(state)
        StudentRoute.CANTEENS -> CanteenScreen(state)
        StudentRoute.LOTTERY -> LotteryScreen(state)
        StudentRoute.MINE -> MineScreen(state)
        StudentRoute.DISHES -> DishListScreen(state)
        StudentRoute.DETAIL -> DishDetailScreen(state, state.selectedDish)
    }
}

@Composable
fun MerchantContent(state: EatNowAppState) {
    when (state.merchantRoute) {
        MerchantRoute.DASHBOARD -> MerchantDashboardScreen(state)
        MerchantRoute.APPLY -> MerchantApplyScreen(state)
        MerchantRoute.DISHES -> MerchantDishScreen(state)
        MerchantRoute.RECOMMENDATIONS -> MerchantRecommendationScreen(state)
        MerchantRoute.FEEDBACK -> MerchantFeedbackScreen(state)
    }
}

@Composable
fun HomeScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { state.refreshStudentHome() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("EatNow", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("今天吃点啥", color = TitleInk, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black)
            }
        }
        item {
            SearchSurface("搜菜名、窗口、口味标签") { state.goDishes() }
        }
        item {
            LotteryHero { state.goStudent(StudentMainTab.LOTTERY) }
        }
        item {
            QuickGrid(
                onCanteen = { state.goStudent(StudentMainTab.CANTEENS) },
                onDishes = { state.goDishes() },
                onRanking = { scope.launch { state.loadRankingAsDishes("top-rated") } },
                onMerchant = { state.goMerchant() }
            )
        }
        item { SectionHead("今日热门", "全部菜品") { state.goDishes() } }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(state.dishes.take(6)) { dish ->
                    HotDishCard(dish) { scope.launch { state.openDish(dish.id) } }
                }
            }
        }
        item { SectionHead("好评榜", "更多") { scope.launch { state.loadRankingAsDishes("top-rated") } } }
        items(state.rankings.take(4)) { item ->
            RankRow(
                rank = item.rank,
                title = item.dishName,
                subtitle = "${item.canteenName} · ${item.merchantName}",
                score = scorePlain(item.score)
            ) { scope.launch { state.openDish(item.dishId) } }
        }
    }
}

@Composable
fun CanteenScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { state.loadCanteens() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageTitle("去哪儿吃", "先选食堂、商圈，再进窗口找菜。")
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "全部", "CANTEEN" to "食堂", "CAMPUS_SHOP" to "校内店").forEach { (value, label) ->
                    SelectPill(label, state.canteenType == value, dark = true) { scope.launch { state.setCanteenType(value) } }
                }
            }
        }
        items(state.filteredCanteens()) { canteen ->
            CanteenCard(canteen, selected = state.selectedCanteen?.id == canteen.id) {
                scope.launch { state.selectCanteen(canteen) }
            }
        }
        state.selectedCanteen?.let { current ->
            item {
                SectionHead("${current.name}窗口", "看全部菜") {
                    scope.launch { state.searchDishes(canteenId = current.id) }
                }
            }
            items(state.stalls) { stall ->
                InfoLineCard(
                    title = stall.name,
                    subtitle = "${stall.merchantName} · ${stall.location}",
                    right = scorePlain(stall.averageScore.takeIf { it > 0 } ?: 4.5)
                ) {
                    scope.launch { state.searchDishes(canteenId = current.id, stallId = stall.id) }
                }
            }
        }
    }
}

@Composable
fun DishListScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var keyword by remember { mutableStateOf("") }
    var minScore by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (state.dishes.isEmpty()) state.searchDishes()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                MiniTextField(keyword, { keyword = it }, "搜索菜品、窗口或口味", modifier = Modifier.weight(1f))
                Button(
                    onClick = { scope.launch { state.searchDishes(keyword, minScore = minScore, priceRange = priceRange) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(52.dp)
                ) { Text("搜索", fontWeight = FontWeight.Black, color = CardBg) }
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("" to "全部评分", "4.5" to "4.5 分以上", "4.0" to "4.0 分以上").forEach { (value, label) ->
                    SelectPill(label, minScore == value) {
                        minScore = value
                        scope.launch { state.searchDishes(keyword, minScore = minScore, priceRange = priceRange) }
                    }
                }
                listOf("" to "全部价格", "0-15" to "15 元内", "15-20" to "15-20 元").forEach { (value, label) ->
                    SelectPill(label, priceRange == value) {
                        priceRange = value
                        scope.launch { state.searchDishes(keyword, minScore = minScore, priceRange = priceRange) }
                    }
                }
            }
        }
        if (state.activeRankingType.isNotBlank()) {
            item {
                RankingStrip(state.activeRankingType) {
                    scope.launch {
                        state.activeRankingType = ""
                        state.searchDishes(keyword, minScore = minScore, priceRange = priceRange)
                    }
                }
            }
        }
        items(state.dishes) { dish ->
            DishListCard(dish) { scope.launch { state.openDish(dish.id) } }
        }
        if (state.dishes.isEmpty()) item { EmptyState("暂时没有匹配的菜品") }
    }
}

@Composable
fun DishDetailScreen(state: EatNowAppState, dish: Dish?) {
    val scope = rememberCoroutineScope()
    if (dish == null) {
        EmptyState("菜品不存在")
        return
    }
    var panelMode by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }
    var score by remember { mutableFloatStateOf(5f) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { FoodArt(dish.name, Modifier.fillMaxWidth().height(210.dp), 42.sp) }
        item {
            MiniCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(dish.name, color = TitleInk, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Text("${dish.canteenName} · ${dish.stallName.ifBlank { dish.merchantName }}", color = SubText, fontSize = 13.sp)
                    }
                    Text("¥${money(dish.price)}", color = Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
                }
                ScoreGrid(dish)
                if (dish.description.isNotBlank()) Text(dish.description, color = Color(0xFF5D4A3B), fontSize = 15.sp, lineHeight = 22.sp)
                TagRow(dish.tags.map { it.name })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton("收藏", Modifier.weight(1f)) { scope.launch { state.createFavorite(dish.id) } }
                    GhostButton("想吃", Modifier.weight(1f)) { scope.launch { state.addEatList(dish.id) } }
                    GhostButton("留言", Modifier.weight(1f)) { panelMode = "feedback" }
                    PrimaryButton("评价", Modifier.weight(1f)) { panelMode = "review" }
                }
            }
        }
        item { SectionHead("同学评价", "${state.reviews.size} 条") {} }
        items(state.reviews) { review ->
            MiniCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(review.nickname, color = TitleInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("${scorePlain(review.overallScore)} 分", color = Red, fontWeight = FontWeight.Black)
                }
                Text(review.content.ifBlank { "这位同学只留下了分数。" }, color = Color(0xFF5D4A3B), fontSize = 14.sp)
                Text("${review.createdAt.take(10)} · ${review.likeCount} 赞", color = Muted, fontSize = 12.sp)
            }
        }
        if (state.reviews.isEmpty()) item { EmptyState("还没有评价，等你来开第一口。") }
    }
    if (panelMode.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { panelMode = "" },
            title = { Text(if (panelMode == "review") "写评价" else "给商家留言", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (panelMode == "review") {
                        Text("综合评分 ${scorePlain(score.toDouble())}", color = Red, fontWeight = FontWeight.Black)
                        Slider(value = score, onValueChange = { score = it }, valueRange = 1f..5f, steps = 7)
                    }
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        minLines = 4,
                        label = { Text(if (panelMode == "review") "味道、分量、排队情况都可以说说" else "比如分量、口味、卫生、服务方面的建议") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (panelMode == "review") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = anonymous, onCheckedChange = { anonymous = it })
                            Text("匿名发布")
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(if (panelMode == "review") "发布评价" else "提交留言") {
                    scope.launch {
                        if (panelMode == "review") state.createReview(dish.id, score.toDouble(), content, anonymous)
                        else state.createFeedback(dish.id, "OTHER", content)
                        content = ""
                        panelMode = ""
                    }
                }
            },
            dismissButton = { TextButton(onClick = { panelMode = "" }) { Text("关闭") } }
        )
    }
}

@Composable
fun LotteryScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("RANDOM") }
    var maxPrice by remember { mutableDoubleStateOf(20.0) }
    var minScore by remember { mutableDoubleStateOf(4.0) }
    LaunchedEffect(Unit) { state.loadLotteryHistory() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { LotteryDarkHero() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LotteryModeCard("随机抽", "全平台随机", mode == "RANDOM", Modifier.weight(1f)) { mode = "RANDOM" }
                LotteryModeCard("条件抽", "预算评分筛选", mode == "CONDITION", Modifier.weight(1f)) { mode = "CONDITION" }
                LotteryModeCard("收藏抽", "只抽收藏", mode == "FAVORITE", Modifier.weight(1f)) { mode = "FAVORITE" }
            }
        }
        if (mode == "CONDITION") {
            item {
                MiniCard {
                    StepperRow("预算上限", "¥${maxPrice.toInt()}", { maxPrice = (maxPrice - 2).coerceAtLeast(8.0) }, { maxPrice += 2 })
                    StepperRow("最低评分", scorePlain(minScore), { minScore = (minScore - 0.5).coerceAtLeast(3.0) }, { minScore = (minScore + 0.5).coerceAtMost(5.0) })
                }
            }
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { scope.launch { state.drawLottery(mode, maxPrice, minScore) } },
                    modifier = Modifier.size(132.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("开抽", fontSize = 28.sp, fontWeight = FontWeight.Black, color = CardBg) }
            }
        }
        state.lotteryResult?.let { result ->
            item {
                MiniCard {
                    FoodArt(result.title, Modifier.fillMaxWidth().height(170.dp), 34.sp)
                    Text("今天可以吃", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text(result.title, color = TitleInk, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("${result.canteenName} · ${result.merchantName}", color = SubText)
                    Text(result.reason, color = Color(0xFF6F5E4E))
                    TagRow(result.tags)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¥${money(result.price)}", color = Red, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("${scorePlain(result.score)} 分", color = Red, fontWeight = FontWeight.Black)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GhostButton("再抽一次", Modifier.weight(1f)) {
                            scope.launch {
                                state.recordLotteryAction(result.recordId, "SKIP")
                                state.drawLottery(mode, maxPrice, minScore)
                            }
                        }
                        GhostButton("收藏", Modifier.weight(1f)) { scope.launch { state.favoriteLotteryResult() } }
                        PrimaryButton("就吃它", Modifier.weight(1f)) { scope.launch { state.acceptLotteryResult() } }
                    }
                }
            }
        }
        item { SectionHead("抽签历史", "最近") {} }
        items(state.lotteryHistory) { record ->
            HistoryRow(record) { if (record.dishId > 0) scope.launch { state.openDish(record.dishId) } }
        }
    }
}

@Composable
fun MineScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("favorite") }
    LaunchedEffect(Unit) { state.loadMine() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileCard(state.profile?.nickname.orEmpty())
        }
        item {
            val pref = state.preferences
            MiniCard {
                SectionHead("我的偏好", "用于抽签") {}
                PreferenceLine("预算", "¥${money(pref?.minPrice ?: 0.0)}-${money(pref?.maxPrice ?: 0.0)}")
                PreferenceLine("口味", pref?.tastePreference?.ifBlank { "暂未设置" } ?: "暂未设置")
                PreferenceLine("忌口", pref?.avoidTags?.ifBlank { "暂无" } ?: "暂无")
            }
        }
        item {
            MerchantEntry { state.goMerchant() }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("favorite" to "收藏", "eatList" to "想吃", "history" to "抽签").forEach { (value, label) ->
                    SelectPill(label, tab == value, modifier = Modifier.weight(1f)) { tab = value }
                }
            }
        }
        when (tab) {
            "favorite" -> items(state.favorites) { item ->
                MiniFoodRow(item.dishName, "${item.canteenName} · ¥${money(item.price)}") {
                    scope.launch { state.openDish(item.dishId) }
                }
            }
            "eatList" -> items(state.eatList) { item ->
                MiniFoodRow(item.dishName, "想吃 · ${item.merchantName}") {
                    scope.launch { state.openDish(item.dishId) }
                }
            }
            else -> items(state.lotteryHistory) { record ->
                HistoryRow(record) { if (record.dishId > 0) scope.launch { state.openDish(record.dishId) } }
            }
        }
    }
}

@Composable
fun MerchantDashboardScreen(state: EatNowAppState) {
    LaunchedEffect(Unit) { state.refreshMerchant() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MerchantHero(state) }
        val profile = state.merchantProfile
        if (profile?.applyStatus?.isNotBlank() == true && profile.applyStatus != "APPROVED") {
            item {
                MiniCard(Modifier.clickable { state.merchantRoute = MerchantRoute.APPLY }) {
                    Text(applyText(profile.applyStatus), color = Red, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("完善窗口资料，等待管理员审核后即可管理菜品。", color = SubText)
                }
            }
        }
        item {
            val overview = state.merchantOverview
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("菜品", "${overview?.dishCount ?: 0}", Modifier.weight(1f))
                MetricCard("浏览", shortNumber(overview?.viewCount ?: 0), Modifier.weight(1f))
                MetricCard("收藏", shortNumber(overview?.favoriteCount ?: 0), Modifier.weight(1f))
                MetricCard("评价", shortNumber(overview?.reviewCount ?: 0), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MerchantQuick("菜", "菜品管理", "新增、编辑、售罄", OrangeSoft, Red, Modifier.weight(1f)) { state.merchantRoute = MerchantRoute.DISHES }
                MerchantQuick("推", "今日推荐", "主推、新品、招牌", GreenSoft, GreenDark, Modifier.weight(1f)) { state.merchantRoute = MerchantRoute.RECOMMENDATIONS }
                MerchantQuick("信", "留言反馈", "${state.merchantOverview?.pendingFeedbackCount ?: 0} 条待处理", Ink, CardBg, Modifier.weight(1f)) { state.merchantRoute = MerchantRoute.FEEDBACK }
            }
        }
        item {
            MiniCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("今日推荐点击 ${state.merchantOverview?.todayRecommendClickCount ?: 0} 次", color = TitleInk, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text("把高评分菜品保持上架，能让抽签和首页曝光更稳。", color = SubText, fontSize = 13.sp)
                    }
                    Text("${scorePlain(state.merchantOverview?.averageScore ?: 0.0)} 分", color = GreenDark, fontWeight = FontWeight.Black)
                }
            }
        }
        item { SectionHead("受欢迎菜品", "全部") { state.merchantRoute = MerchantRoute.DISHES } }
        items(state.merchantPopularDishes) {
            RankRow(0, it.dishName, "${it.viewCount} 浏览 · ${it.favoriteCount} 收藏", scorePlain(it.averageScore)) {}
        }
        item { SectionHead("反馈较多", "查看") { state.merchantRoute = MerchantRoute.FEEDBACK } }
        items(state.merchantFeedbackDishes) {
            InfoLineCard(it.dishName, "${it.pendingFeedbackCount} 待处理", "${it.feedbackCount}") { state.merchantRoute = MerchantRoute.FEEDBACK }
        }
    }
}

@Composable
fun MerchantApplyScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var stallId by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        state.loadMerchantProfileSafe()
        state.merchantProfile?.let {
            name = it.name
            stallId = it.stallId?.toString().orEmpty()
            hours = it.businessHours
            phone = it.contactPhone
            desc = it.description
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ApplyHero() }
        item {
            MiniCard {
                FormLine("商家名称") { MiniTextField(name, { name = it }, "例如：张记盖饭") }
                FormLine("窗口 ID") { MiniTextField(stallId, { stallId = it }, "由管理员分配", keyboardType = KeyboardType.Number) }
                FormLine("营业时间") { MiniTextField(hours, { hours = it }, "10:00-20:30") }
                FormLine("联系电话") { MiniTextField(phone, { phone = it }, "用于审核联系", keyboardType = KeyboardType.Phone) }
                MiniTextField(desc, { desc = it }, "写清楚主营品类、窗口位置和特色菜", minLines = 4)
                PrimaryButton("提交申请", Modifier.fillMaxWidth()) {
                    scope.launch { state.applyMerchant(name, stallId, desc, hours, phone) }
                }
            }
        }
        item {
            MiniCard {
                Text("审核后可以做什么", color = TitleInk, fontSize = 18.sp, fontWeight = FontWeight.Black)
                HintLine("1", "发布和维护自己的菜品")
                HintLine("2", "设置今日主推、新品和招牌菜")
                HintLine("3", "回复学生反馈并发布改进记录")
            }
        }
    }
}

@Composable
fun MerchantDishScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var editId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var selectedTags by remember { mutableStateOf<List<Long>>(emptyList()) }
    var joinLottery by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { state.loadMerchantDishes(status) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TopPanel("Menu Control", "菜品管理", dark = true) {
                SmallButton("新增", Gold, Ink) {
                    editId = null; name = ""; price = ""; desc = ""; categoryId = state.categories.firstOrNull()?.id
                    selectedTags = emptyList(); joinLottery = true; editing = true
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("" to "全部", "ON_SALE" to "上架", "SOLD_OUT" to "售罄", "OFF_SHELF" to "下架").forEach { (value, label) ->
                    SelectPill(label, status == value, modifier = Modifier.weight(1f)) {
                        status = value
                        scope.launch { state.loadMerchantDishes(status) }
                    }
                }
            }
        }
        if (editing) {
            item {
                MiniCard {
                    SectionHead(if (editId == null) "新增菜品" else "编辑菜品", "收起") { editing = false }
                    MiniTextField(name, { name = it }, "名称")
                    MiniTextField(price, { price = it }, "价格", keyboardType = KeyboardType.Decimal)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.categories.forEach { category ->
                            SelectPill(category.name, categoryId == category.id) { categoryId = category.id }
                        }
                    }
                    MiniTextField(desc, { desc = it }, "一句话讲清楚菜品特色", minLines = 3)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tags.forEach { tag ->
                            SelectPill(tag.name, selectedTags.contains(tag.id)) {
                                selectedTags = if (selectedTags.contains(tag.id)) selectedTags - tag.id else selectedTags + tag.id
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("参与抽签推荐", color = TitleInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Switch(joinLottery, { joinLottery = it }, colors = miniSwitchColors())
                    }
                    PrimaryButton(if (editId == null) "发布菜品" else "保存修改", Modifier.fillMaxWidth()) {
                        scope.launch {
                            state.saveDish(editId, name, desc, price, categoryId, selectedTags, joinLottery)
                            editing = false
                        }
                    }
                }
            }
        }
        items(state.dishes) { dish ->
            MerchantDishCard(
                dish = dish,
                onEdit = {
                    editId = dish.id; name = dish.name; price = money(dish.price); desc = dish.description
                    categoryId = dish.categoryId; selectedTags = dish.tags.map { it.id }; joinLottery = dish.isJoinLottery; editing = true
                },
                onStatus = { next -> scope.launch { state.updateDishStatus(dish.id, next) } }
            )
        }
        if (state.dishes.isEmpty()) item { EmptyState("还没有符合条件的菜品") }
    }
}

@Composable
fun MerchantRecommendationScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var editId by remember { mutableStateOf<Long?>(null) }
    var dishId by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TODAY") }
    var title by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(dayTime(10, 0)) }
    var endTime by remember { mutableStateOf(dayTime(20, 30)) }
    var top by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        state.loadMerchantDishes("ON_SALE")
        state.loadRecommendations()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopPanel("Spotlight", "推荐管理", "把今天最值得吃的菜推到学生首页和抽签结果里。") }
        item {
            MiniCard {
                SectionHead(if (editId == null) "发布今日推荐" else "编辑推荐", if (editId == null) "" else "取消编辑") {
                    editId = null
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.dishes.forEach { dish ->
                        SelectPill(dish.name, dishId == dish.id.toString()) {
                            dishId = dish.id.toString()
                            if (editId == null) title = "${recommendTypeText(type)}：${dish.name}"
                        }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TODAY", "NEW", "SPECIAL", "VALUE", "SIGNATURE").forEach { value ->
                        SelectPill(recommendTypeText(value), type == value) { type = value }
                    }
                }
                MiniTextField(title, { title = it }, "标题")
                MiniTextField(reason, { reason = it }, "推荐理由", minLines = 3)
                MiniTextField(startTime, { startTime = it }, "开始时间")
                MiniTextField(endTime, { endTime = it }, "结束时间")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("置顶展示", color = TitleInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Switch(top, { top = it }, colors = miniSwitchColors())
                }
                PrimaryButton(if (editId == null) "发布推荐" else "保存推荐", Modifier.fillMaxWidth()) {
                    scope.launch {
                        state.saveRecommendation(editId, dishId, title, reason, type, startTime, endTime, top)
                        editId = null; title = ""; reason = ""
                    }
                }
            }
        }
        item { SectionHead("推荐记录", "刷新") { scope.launch { state.loadRecommendations() } } }
        items(state.recommendations) { item ->
            RecommendationCard(
                item = item,
                onEdit = {
                    editId = item.id; dishId = item.dishId.toString(); type = item.recommendType
                    title = item.title; reason = item.recommendReason; startTime = item.startTime.ifBlank { dayTime(10, 0) }
                    endTime = item.endTime.ifBlank { dayTime(20, 30) }; top = item.isTop
                },
                onDelete = { scope.launch { state.deleteRecommendation(item.id) } }
            )
        }
    }
}

@Composable
fun MerchantFeedbackScreen(state: EatNowAppState) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var activeReplyId by remember { mutableStateOf<Long?>(null) }
    var reply by remember { mutableStateOf("") }
    var improving by remember { mutableStateOf<Feedback?>(null) }
    var improveTitle by remember { mutableStateOf("") }
    var improveContent by remember { mutableStateOf("") }
    var before by remember { mutableStateOf("") }
    var after by remember { mutableStateOf("") }
    var public by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        state.loadFeedbacks()
        state.loadImprovements()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopPanel("Feedback Loop", "留言反馈", "回复学生建议，把已处理的问题沉淀成改进记录。") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("" to "全部", "PENDING" to "待查看", "VIEWED" to "已查看", "ACCEPTED" to "已采纳", "IMPROVED" to "已改进").forEach { (value, label) ->
                    SelectPill(label, status == value) {
                        status = value
                        scope.launch { state.loadFeedbacks(status) }
                    }
                }
            }
        }
        items(state.feedbacks) { item ->
            FeedbackCard(
                item = item,
                activeReply = activeReplyId == item.id,
                reply = reply,
                onReplyChange = { reply = it },
                onToggleReply = {
                    activeReplyId = if (activeReplyId == item.id) null else item.id
                    reply = item.replyContent
                },
                onSubmitReply = {
                    scope.launch { state.replyFeedback(item.id, reply); activeReplyId = null; reply = "" }
                },
                onStatus = { next -> scope.launch { state.updateFeedbackStatus(item.id, next) } },
                onImprove = {
                    improving = item
                    improveTitle = "${item.dishName.ifBlank { "反馈" }}已优化"
                    improveContent = ""
                    before = item.content
                    after = ""
                }
            )
        }
        if (state.feedbacks.isEmpty()) item { EmptyState("暂无对应反馈") }
        improving?.let { item ->
            item {
                MiniCard {
                    SectionHead("发布改进记录", "收起") { improving = null }
                    MiniCard(background = PageBg) {
                        Text(item.dishName.ifBlank { "通用反馈" }, color = Red, fontWeight = FontWeight.Black)
                        Text(item.content, color = SubText)
                    }
                    MiniTextField(improveTitle, { improveTitle = it }, "标题")
                    MiniTextField(improveContent, { improveContent = it }, "改进内容", minLines = 4)
                    MiniTextField(before, { before = it }, "改进前")
                    MiniTextField(after, { after = it }, "改进后")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("公开展示", color = TitleInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Switch(public, { public = it }, colors = miniSwitchColors())
                    }
                    PrimaryButton("发布改进", Modifier.fillMaxWidth()) {
                        scope.launch {
                            state.createImprovementRecord(item.dishId, item.id, improveTitle, improveContent, before, after, public)
                            improving = null
                        }
                    }
                }
            }
        }
        item { SectionHead("改进记录", "刷新") { scope.launch { state.loadImprovements() } } }
        items(state.improvements) { item ->
            MiniCard {
                Text(item.title, color = TitleInk, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("${item.dishName.ifBlank { "通用改进" }} · ${statusText(item.status)}", color = Muted, fontSize = 12.sp)
                Text(item.content, color = Color(0xFF5D4A3B), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SearchSurface(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg)
            .border(1.dp, Line, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⌕", color = Red, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(10.dp))
        Text(text, color = Muted, fontSize = 14.sp)
    }
}

@Composable
fun LotteryHero(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFE09A), Color(0xFFFF8B4A), Red)))
            .clickable { onClick() }
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("饭点盲盒", color = TitleInk, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("选择困难时，交给今天的食欲算法。", color = Color(0xCC2B2118), fontSize = 14.sp, lineHeight = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("马上抽签", modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(CardBg).padding(horizontal = 14.dp, vertical = 9.dp), color = Red, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(10.dp))
                Text("全平台 / 条件 / 收藏", color = Color(0x992B2118), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        BentoDots()
    }
}

@Composable
fun BentoDots() {
    Box(Modifier.size(90.dp)) {
        Box(Modifier.size(62.dp).align(Alignment.TopEnd).clip(CircleShape).background(CardBg))
        Box(Modifier.size(42.dp).align(Alignment.BottomStart).clip(CircleShape).background(Green))
        Box(Modifier.size(30.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Ink))
    }
}

@Composable
fun QuickGrid(onCanteen: () -> Unit, onDishes: () -> Unit, onRanking: () -> Unit, onMerchant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickItem("堂", "找食堂", GreenSoft, GreenDark, Modifier.weight(1f), onCanteen)
            QuickItem("菜", "逛菜品", GreenSoft, GreenDark, Modifier.weight(1f), onDishes)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickItem("榜", "看排行", GreenSoft, GreenDark, Modifier.weight(1f), onRanking)
            QuickItem("店", "卖家端", OrangeSoft, Red, Modifier.weight(1f), onMerchant)
        }
    }
}

@Composable
fun QuickItem(mark: String, title: String, markBg: Color, markColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(markBg), contentAlignment = Alignment.Center) {
            Text(mark, color = markColor, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
fun SectionHead(title: String, link: String, onLink: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TitleInk, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (link.isNotBlank()) Text(link, color = Red, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable { onLink() })
    }
}

@Composable
fun HotDishCard(dish: Dish, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        FoodArt(dish.name, Modifier.fillMaxWidth().height(86.dp), 24.sp)
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(dish.name, color = TitleInk, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${dish.canteenName} · ${dish.stallName}", color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¥${money(dish.price)}", color = Red, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text("${scorePlain(dish.score)} 分", color = GreenDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun CanteenCard(canteen: Canteen, selected: Boolean, onClick: () -> Unit) {
    MiniCard(Modifier.clickable { onClick() }, background = if (selected) Color(0xFFFFFBF0) else CardBg) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Gold, Red))),
                contentAlignment = Alignment.Center
            ) { Text(canteen.name.take(1), color = CardBg, fontSize = 20.sp, fontWeight = FontWeight.Black) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(canteen.name, color = TitleInk, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text(if (canteen.status == "OPEN") "营业中" else "休息中", modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(GreenSoft).padding(horizontal = 8.dp, vertical = 4.dp), color = GreenDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Text(canteen.description, color = Color(0xFF6F5E4E), fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${canteen.location} · ${canteen.openingHours} · ${scorePlain(canteen.averageScore)} 分", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DishListCard(dish: Dish, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FoodArt(dish.name, Modifier.size(92.dp), 22.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dish.name, color = TitleInk, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("¥${money(dish.price)}", color = Red, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Text(dish.description, color = Color(0xFF6F5E4E), fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            TagRow(dish.tags.map { it.name })
            Row {
                Text("${dish.canteenName} · ${dish.stallName.ifBlank { dish.merchantName }}", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${scorePlain(dish.score)} 分 · ${dish.favoriteCount} 收藏", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ScoreGrid(dish: Dish) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(PageBg)) {
        ScoreCell(scorePlain(dish.score), "综合评分", Modifier.weight(1f))
        ScoreCell(scorePlain(dish.tasteScore), "口味", Modifier.weight(1f))
        ScoreCell(scorePlain(dish.portionScore), "分量", Modifier.weight(1f))
        ScoreCell(scorePlain(dish.valueScore), "性价比", Modifier.weight(1f))
    }
}

@Composable
fun ScoreCell(value: String, label: String, modifier: Modifier) {
    Column(modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Red, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text(label, color = SubText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LotteryDarkHero() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(Ink, Color(0xFF604229)))).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("饭点盲盒", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text("把选择困难拆开吃掉", color = CardBg, fontSize = 25.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
        Text("全平台随缘抽、按预算评分抽，或者只从收藏里抽。", color = Color(0xB3FFFDF7), fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun LotteryModeCard(title: String, desc: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) YellowSoft else CardBg).border(1.dp, if (selected) Color(0x52E94B35) else Line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = TitleInk, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(desc, color = SubText, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun StepperRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color(0xFF5D4A3B), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Row(Modifier.clip(RoundedCornerShape(999.dp)).background(PageBg), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onMinus) { Text("-", color = Red, fontWeight = FontWeight.Black) }
            Text(value, color = Red, fontWeight = FontWeight.Black)
            TextButton(onClick = onPlus) { Text("+", color = Red, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
fun ProfileCard(nickname: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ink).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Gold, Red))), contentAlignment = Alignment.Center) {
            Text(nickname.take(1).ifBlank { "饭" }, color = CardBg, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f)) {
            Text(nickname.ifBlank { "未登录同学" }, color = CardBg, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text("收藏、想吃和抽签记录都在这里", color = Color(0xADFFFDF7), fontSize = 12.sp)
        }
    }
}

@Composable
fun MerchantEntry(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.horizontalGradient(listOf(YellowSoft, Orange, Red))).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("卖家工作台", color = TitleInk, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("管理菜品、发布推荐、处理学生反馈", color = Color(0xAD2B2118), fontSize = 12.sp)
        }
        Text("进入", modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(CardBg).padding(horizontal = 12.dp, vertical = 8.dp), color = Red, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MerchantHero(state: EatNowAppState) {
    val profile = state.merchantProfile
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFFFF0B8), Color(0xFFFF9657), Red))).padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("Merchant Studio", color = GreenDark, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(profile?.name?.ifBlank { "卖家工作台" } ?: "卖家工作台", color = TitleInk, fontSize = 25.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
            Text("${profile?.canteenName?.ifBlank { "第一食堂" } ?: "第一食堂"} · ${profile?.stallName?.ifBlank { "窗口" } ?: "窗口"} · ${statusText(profile?.status.orEmpty())}", color = Color(0xB32B2118), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("刷新", modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Ink).padding(horizontal = 14.dp, vertical = 9.dp), color = CardBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("入驻资料", modifier = Modifier.padding(top = 12.dp).clickable { state.merchantRoute = MerchantRoute.APPLY }, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ApplyHero() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFFFF7D6), Gold, Color(0xFFFF8B4A)))).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Merchant Apply", color = GreenDark, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text("商家入驻申请", color = TitleInk, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("填写窗口信息，审核通过后即可管理菜品和学生反馈。", color = Color(0xFF6F5E4E), fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
fun TopPanel(eyebrow: String, title: String, subtitle: String = "", dark: Boolean = false, action: @Composable (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (dark) Modifier.background(Ink)
                else Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFFFF4C2), Gold, Color(0xFFFF8B4A))))
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow, color = if (dark) Gold else GreenDark, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(title, color = if (dark) CardBg else TitleInk, fontSize = 24.sp, fontWeight = FontWeight.Black)
            if (subtitle.isNotBlank()) Text(subtitle, color = if (dark) Color(0xB3FFFDF7) else Color(0xFF6F5E4E), fontSize = 13.sp, lineHeight = 19.sp)
        }
        action?.invoke()
    }
}

@Composable
fun MerchantQuick(mark: String, title: String, sub: String, markBg: Color, markColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(markBg), contentAlignment = Alignment.Center) {
            Text(mark, color = markColor, fontWeight = FontWeight.Black)
        }
        Text(title, color = TitleInk, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text(sub, color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun MerchantDishCard(dish: Dish, onEdit: () -> Unit, onStatus: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FoodArt(dish.name, Modifier.size(86.dp), 20.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(dish.name, color = TitleInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("¥${money(dish.price)}", color = Red, fontWeight = FontWeight.Black)
            }
            Text(dish.description, color = Color(0xFF6F5E4E), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${statusText(dish.status)} · ${scorePlain(dish.score)} 分 · ${dish.favoriteCount} 收藏", color = Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TinyAction("编辑", dark = true, onEdit)
                TinyAction("上架") { onStatus("ON_SALE") }
                TinyAction("售罄") { onStatus("SOLD_OUT") }
                TinyAction("下架") { onStatus("OFF_SHELF") }
            }
        }
    }
}

@Composable
fun RecommendationCard(item: Recommendation, onEdit: () -> Unit, onDelete: () -> Unit) {
    MiniCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, color = TitleInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("${item.dishName} · ${recommendTypeText(item.recommendType)}", color = SubText, fontSize = 12.sp)
            }
            Text(statusText(item.status), modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(GreenSoft).padding(horizontal = 8.dp, vertical = 5.dp), color = GreenDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        if (item.recommendReason.isNotBlank()) Text(item.recommendReason, color = SubText, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${item.clickCount} 点击", color = SubText, fontSize = 12.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onEdit) { Text("编辑", color = Red, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onDelete) { Text("删除", color = Red, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun FeedbackCard(
    item: Feedback,
    activeReply: Boolean,
    reply: String,
    onReplyChange: (String) -> Unit,
    onToggleReply: () -> Unit,
    onSubmitReply: () -> Unit,
    onStatus: (String) -> Unit,
    onImprove: () -> Unit
) {
    MiniCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.dishName.ifBlank { "菜品反馈" }, color = TitleInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("${item.userNickname} · ${feedbackTypeText(item.feedbackType)} · ${item.createdAt.take(10)}", color = Muted, fontSize = 12.sp)
            }
            Text(statusText(item.status), modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(YellowSoft).padding(horizontal = 8.dp, vertical = 5.dp), color = Red, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Text(item.content, color = Color(0xFF4D3D31), fontSize = 14.sp, lineHeight = 21.sp)
        if (item.replyContent.isNotBlank()) {
            MiniCard(background = PageBg) {
                Text("商家回复", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(item.replyContent, color = Color(0xFF5D4A3B), fontSize = 13.sp)
            }
        }
        if (activeReply) {
            MiniTextField(reply, onReplyChange, "输入回复内容", minLines = 3)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TinyAction(if (activeReply) "取消" else "回复", dark = true, onToggleReply)
            TinyAction("发送", dark = true, onSubmitReply)
            TinyAction("采纳") { onStatus("ACCEPTED") }
            TinyAction("已改进") { onStatus("IMPROVED") }
        }
        MiniCard(Modifier.clickable { onImprove() }, background = GreenSoft) {
            Text("发布改进记录", color = GreenDark, fontWeight = FontWeight.Black)
            Text("让学生看到这条反馈的处理结果", color = Color(0xFF5D7341), fontSize = 12.sp)
        }
    }
}

@Composable
fun MiniCard(modifier: Modifier = Modifier, background: Color = CardBg, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun FoodArt(seed: String, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit = 24.sp) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Gold, Orange, Red)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(44.dp).align(Alignment.TopEnd).clip(CircleShape).background(Color(0x66FFFDF7)))
        Box(Modifier.size(34.dp).align(Alignment.BottomStart).clip(CircleShape).background(Color(0x807FBD41)))
        Text(seed.take(2).ifBlank { "饭" }, color = CardBg, fontSize = fontSize, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MiniTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
        singleLine = minLines == 1,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Red,
            unfocusedBorderColor = Line,
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg
        )
    )
}

@Composable
fun PrimaryButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Red)
    ) {
        Text(label, color = CardBg, fontWeight = FontWeight.Black)
    }
}

@Composable
fun GhostButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
fun SelectPill(label: String, selected: Boolean, modifier: Modifier = Modifier, dark: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        dark && selected -> Ink
        selected -> GreenSoft
        else -> CardBg
    }
    val fg = when {
        dark && selected -> CardBg
        selected -> GreenDark
        else -> SubText
    }
    Box(
        modifier
            .height(38.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, if (selected) Color(0x557FBD41) else Line, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SmallButton(label: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 10.dp),
        color = fg,
        fontWeight = FontWeight.Black
    )
}

@Composable
fun TinyAction(label: String, dark: Boolean = false, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (dark) Ink else YellowSoft).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 7.dp),
        color = if (dark) CardBg else Color(0xFF7B6046),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black
    )
}

@Composable
fun TagRow(tags: List<String>) {
    if (tags.isEmpty()) return
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.filter { it.isNotBlank() }.forEach {
            Text(it, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(GreenSoft).padding(horizontal = 8.dp, vertical = 5.dp), color = GreenDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RankRow(rank: Int, title: String, subtitle: String, score: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(Gold), contentAlignment = Alignment.Center) {
            Text(if (rank > 0) rank.toString() else "-", color = Ink, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, color = TitleInk, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(score, color = Red, fontWeight = FontWeight.Black)
    }
}

@Composable
fun InfoLineCard(title: String, subtitle: String, right: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TitleInk, fontWeight = FontWeight.Black)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Text(right, color = Red, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RankingStrip(type: String, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(YellowSoft).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(rankingTitle(type), color = Color(0xFF7B6046), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text("查看普通列表", color = Red, fontWeight = FontWeight.Black, modifier = Modifier.clickable { onClear() })
    }
}

@Composable
fun PageTitle(title: String, subtitle: String) {
    Column {
        Text(title, color = TitleInk, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = SubText, fontSize = 14.sp)
    }
}

@Composable
fun PreferenceLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PageBg).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(label, color = SubText, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text(value, color = TitleInk, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MiniFoodRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        FoodArt(title, Modifier.size(62.dp), 17.sp)
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, color = TitleInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
fun HistoryRow(record: LotteryRecord, onClick: () -> Unit) {
    InfoLineCard(record.title, "${record.createdAt.take(10)} · ${actionText(record.resultAction)}", scorePlain(record.score), onClick)
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(CardBg).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TitleInk, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = SubText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FormLine(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, color = Color(0xFF7B6046), fontWeight = FontWeight.Black, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun HintLine(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(GreenSoft), contentAlignment = Alignment.Center) {
            Text(number, color = GreenDark, fontWeight = FontWeight.Black)
        }
        Text(text, color = Color(0xFF5D4A3B), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Muted, textAlign = TextAlign.Center)
    }
}

@Composable
fun pageBrush(): Brush = Brush.verticalGradient(
    listOf(Color(0xFFFFF4DF), Color(0xFFFFFAF2), Color(0xFFF7FBEC))
)

@Composable
fun miniSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Red,
    checkedTrackColor = OrangeSoft,
    uncheckedThumbColor = SubText,
    uncheckedTrackColor = PageBg
)

fun tabText(tab: StudentMainTab): String = when (tab) {
    StudentMainTab.HOME -> "首页"
    StudentMainTab.CANTEENS -> "食堂"
    StudentMainTab.LOTTERY -> "抽签"
    StudentMainTab.MINE -> "我的"
}

fun tabIcon(tab: StudentMainTab): String = when (tab) {
    StudentMainTab.HOME -> "首"
    StudentMainTab.CANTEENS -> "堂"
    StudentMainTab.LOTTERY -> "抽"
    StudentMainTab.MINE -> "我"
}

fun money(value: Double): String = if (value > 0) "%.1f".format(value) else "-"

fun scorePlain(value: Double): String = if (value > 0) "%.1f".format(value) else "-"

fun statusText(status: String): String = when (status) {
    "OPEN" -> "营业中"
    "CLOSED" -> "已休息"
    "PENDING" -> "待审核"
    "APPROVED" -> "已通过"
    "REJECTED" -> "已驳回"
    "ON_SALE" -> "上架中"
    "SOLD_OUT" -> "已售罄"
    "OFF_SHELF" -> "已下架"
    "VIEWED" -> "已查看"
    "ACCEPTED" -> "已采纳"
    "IMPROVED" -> "已改进"
    "DRAFT" -> "草稿"
    "PUBLISHED" -> "已发布"
    "ARCHIVED" -> "已归档"
    else -> status.ifBlank { "未知" }
}

fun feedbackTypeText(type: String): String = when (type) {
    "TASTE" -> "口味"
    "PORTION" -> "分量"
    "PRICE" -> "价格"
    "SERVICE" -> "服务"
    "HYGIENE" -> "卫生"
    "OTHER" -> "其他"
    else -> type.ifBlank { "其他" }
}

fun recommendTypeText(type: String): String = when (type) {
    "TODAY" -> "今日主推"
    "NEW" -> "新品"
    "SPECIAL" -> "特色"
    "VALUE" -> "高性价比"
    "SIGNATURE" -> "招牌"
    else -> type
}

fun applyText(status: String): String = when (status) {
    "PENDING" -> "入驻审核中"
    "REJECTED" -> "入驻被驳回，去修改资料"
    else -> "去提交入驻申请"
}

fun actionText(action: String): String = when (action) {
    "ACCEPT" -> "就吃它"
    "FAVORITE" -> "已收藏"
    "SKIP" -> "跳过"
    else -> "看过"
}

fun shortNumber(value: Int): String = if (value >= 1000) "${"%.1f".format(value / 1000.0)}k" else value.toString()

@Preview(showBackground = true)
@Composable
fun EatNowPreview() {
    FrontendAndroidTheme(dynamicColor = false) {
        Box(Modifier.fillMaxSize().background(pageBrush()).padding(16.dp)) {
            LotteryHero {}
        }
    }
}
