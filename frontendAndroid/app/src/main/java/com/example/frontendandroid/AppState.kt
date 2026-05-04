package com.example.frontendandroid

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.frontendandroid.data.Canteen
import com.example.frontendandroid.data.Category
import com.example.frontendandroid.data.Dish
import com.example.frontendandroid.data.EatListItem
import com.example.frontendandroid.data.EatNowApi
import com.example.frontendandroid.data.FavoriteItem
import com.example.frontendandroid.data.Feedback
import com.example.frontendandroid.data.ImprovementRecord
import com.example.frontendandroid.data.LotteryRecord
import com.example.frontendandroid.data.LotteryResult
import com.example.frontendandroid.data.MerchantFeedbackDish
import com.example.frontendandroid.data.MerchantOverview
import com.example.frontendandroid.data.MerchantPopularDish
import com.example.frontendandroid.data.MerchantProfile
import com.example.frontendandroid.data.RankingItem
import com.example.frontendandroid.data.Recommendation
import com.example.frontendandroid.data.Review
import com.example.frontendandroid.data.Stall
import com.example.frontendandroid.data.Tag
import com.example.frontendandroid.data.UserPreference
import com.example.frontendandroid.data.UserProfile
import com.example.frontendandroid.data.dataObjectOrEmpty
import com.example.frontendandroid.data.parseCanteen
import com.example.frontendandroid.data.parseCategory
import com.example.frontendandroid.data.parseDish
import com.example.frontendandroid.data.parseEatList
import com.example.frontendandroid.data.parseFavorite
import com.example.frontendandroid.data.parseFeedback
import com.example.frontendandroid.data.parseImprovementRecord
import com.example.frontendandroid.data.parseLotteryRecord
import com.example.frontendandroid.data.parseLotteryResult
import com.example.frontendandroid.data.parseMerchantFeedbackDish
import com.example.frontendandroid.data.parseMerchantOverview
import com.example.frontendandroid.data.parseMerchantPopularDish
import com.example.frontendandroid.data.parseMerchantProfile
import com.example.frontendandroid.data.parseRanking
import com.example.frontendandroid.data.parseRecommendation
import com.example.frontendandroid.data.parseReview
import com.example.frontendandroid.data.parseSession
import com.example.frontendandroid.data.parseStall
import com.example.frontendandroid.data.parseUserPreference
import com.example.frontendandroid.data.parseUserProfile
import com.example.frontendandroid.data.recordsOrArray
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class MainArea { STUDENT, MERCHANT }
enum class StudentMainTab { HOME, CANTEENS, LOTTERY, MINE }
enum class StudentRoute { HOME, CANTEENS, LOTTERY, MINE, DISHES, DETAIL }
enum class MerchantRoute { DASHBOARD, APPLY, DISHES, RECOMMENDATIONS, FEEDBACK }

class EatNowAppState(context: Context) {
    private val prefs = context.getSharedPreferences("eat_now_android", Context.MODE_PRIVATE)
    private val api = EatNowApi(baseUrlProvider = { baseUrl }, tokenProvider = { token })

    var baseUrl by mutableStateOf(prefs.getString("base_url", "http://192.168.1.155:8080").orEmpty())
        private set
    var token by mutableStateOf(prefs.getString("token", null))
        private set
    var refreshToken by mutableStateOf(prefs.getString("refresh_token", null))
        private set
    var roles by mutableStateOf(prefs.getStringSet("roles", emptySet())?.toSet().orEmpty())
        private set

    var area by mutableStateOf(MainArea.STUDENT)
    var studentTab by mutableStateOf(StudentMainTab.HOME)
    var studentRoute by mutableStateOf(StudentRoute.HOME)
    var merchantRoute by mutableStateOf(MerchantRoute.DASHBOARD)
    var loading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    var profile by mutableStateOf<UserProfile?>(null)
    var preferences by mutableStateOf<UserPreference?>(null)
    var canteens by mutableStateOf<List<Canteen>>(emptyList())
    var canteenType by mutableStateOf("ALL")
    var selectedCanteen by mutableStateOf<Canteen?>(null)
    var stalls by mutableStateOf<List<Stall>>(emptyList())
    var dishes by mutableStateOf<List<Dish>>(emptyList())
    var selectedDish by mutableStateOf<Dish?>(null)
    var reviews by mutableStateOf<List<Review>>(emptyList())
    var rankings by mutableStateOf<List<RankingItem>>(emptyList())
    var favorites by mutableStateOf<List<FavoriteItem>>(emptyList())
    var eatList by mutableStateOf<List<EatListItem>>(emptyList())
    var lotteryResult by mutableStateOf<LotteryResult?>(null)
    var lotteryHistory by mutableStateOf<List<LotteryRecord>>(emptyList())
    var activeRankingType by mutableStateOf("")

    var categories by mutableStateOf<List<Category>>(emptyList())
    var tags by mutableStateOf<List<Tag>>(emptyList())
    var merchantProfile by mutableStateOf<MerchantProfile?>(null)
    var merchantOverview by mutableStateOf<MerchantOverview?>(null)
    var merchantPopularDishes by mutableStateOf<List<MerchantPopularDish>>(emptyList())
    var merchantFeedbackDishes by mutableStateOf<List<MerchantFeedbackDish>>(emptyList())
    var recommendations by mutableStateOf<List<Recommendation>>(emptyList())
    var feedbacks by mutableStateOf<List<Feedback>>(emptyList())
    var improvements by mutableStateOf<List<ImprovementRecord>>(emptyList())

    val loggedIn: Boolean get() = !token.isNullOrBlank()
    val canUseMerchant: Boolean get() = roles.contains("MERCHANT")

    fun updateBaseUrl(value: String) {
        baseUrl = value.trim()
        prefs.edit().putString("base_url", baseUrl).apply()
    }

    fun goStudent(tab: StudentMainTab) {
        area = MainArea.STUDENT
        studentTab = tab
        studentRoute = when (tab) {
            StudentMainTab.HOME -> StudentRoute.HOME
            StudentMainTab.CANTEENS -> StudentRoute.CANTEENS
            StudentMainTab.LOTTERY -> StudentRoute.LOTTERY
            StudentMainTab.MINE -> StudentRoute.MINE
        }
    }

    fun goDishes() {
        area = MainArea.STUDENT
        studentRoute = StudentRoute.DISHES
    }

    fun goDetail(dish: Dish) {
        selectedDish = dish
        studentRoute = StudentRoute.DETAIL
        area = MainArea.STUDENT
    }

    fun goMerchant(route: MerchantRoute = MerchantRoute.DASHBOARD) {
        area = MainArea.MERCHANT
        merchantRoute = if (canUseMerchant) route else MerchantRoute.APPLY
    }

    fun backToStudentHome() {
        selectedDish = null
        goStudent(StudentMainTab.HOME)
    }

    fun backFromDetail() {
        selectedDish = null
        studentRoute = StudentRoute.DISHES
    }

    suspend fun login(username: String, password: String) = guarded("登录成功") {
        val json = api.post(
            "/api/v1/auth/android/login",
            JSONObject().put("username", username).put("password", password)
        )
        saveSession(parseSession(json.dataObjectOrEmpty()))
        goStudent(StudentMainTab.HOME)
        refreshStudentHome()
    }

    suspend fun register(username: String, password: String, nickname: String) = guarded("注册成功") {
        val json = api.post(
            "/api/v1/auth/android/register",
            JSONObject()
                .put("username", username)
                .put("password", password)
                .put("nickname", nickname.ifBlank { username })
        )
        saveSession(parseSession(json.dataObjectOrEmpty()))
        goStudent(StudentMainTab.HOME)
        refreshStudentHome()
    }

    fun logout() {
        token = null
        refreshToken = null
        roles = emptySet()
        prefs.edit().clear().putString("base_url", baseUrl).apply()
        profile = null
        preferences = null
        selectedDish = null
        backToStudentHome()
        message = "已退出登录"
    }

    suspend fun refreshStudentHome() = guarded() {
        loadDishes(mapOf("page" to 1, "size" to 6, "status" to "ON_SALE"))
        loadRanking("top-rated")
    }

    suspend fun loadProfileAndPreferences() = guarded() {
        runCatching { profile = parseUserProfile(api.get("/api/v1/users/me", auth = true).dataObjectOrEmpty()) }
        runCatching { preferences = parseUserPreference(api.get("/api/v1/users/preferences", auth = true).dataObjectOrEmpty()) }
    }

    suspend fun loadCanteens() = guarded() {
        val array = api.get("/api/v1/canteens").recordsOrArray()
        canteens = List(array.length()) { parseCanteen(array.getJSONObject(it)) }
        val first = filteredCanteens().firstOrNull()
        if (selectedCanteen == null && first != null) selectCanteen(first)
    }

    fun filteredCanteens(): List<Canteen> = if (canteenType == "ALL") canteens else canteens.filter { it.type == canteenType }

    suspend fun selectCanteen(canteen: Canteen) {
        selectedCanteen = canteen
        val array = api.get("/api/v1/stalls", mapOf("canteenId" to canteen.id)).recordsOrArray()
        stalls = List(array.length()) { parseStall(array.getJSONObject(it)) }
    }

    suspend fun setCanteenType(type: String) = guarded() {
        canteenType = type
        val first = filteredCanteens().firstOrNull()
        if (first != null) selectCanteen(first)
    }

    suspend fun loadDishes(query: Map<String, Any?> = emptyMap()) {
        activeRankingType = ""
        val finalQuery = mapOf("page" to 1, "size" to 20, "status" to "ON_SALE") + query
        val array = api.get("/api/v1/dishes", finalQuery).recordsOrArray()
        dishes = List(array.length()) { parseDish(array.getJSONObject(it)) }
    }

    suspend fun searchDishes(
        keyword: String = "",
        canteenId: Long? = null,
        stallId: Long? = null,
        minScore: String = "",
        priceRange: String = ""
    ) = guarded() {
        val price = priceRange.split("-")
        loadDishes(
            mapOf(
                "keyword" to keyword,
                "canteenId" to canteenId,
                "stallId" to stallId,
                "minScore" to minScore,
                "minPrice" to price.getOrNull(0).orEmpty(),
                "maxPrice" to price.getOrNull(1).orEmpty()
            )
        )
        goDishes()
    }

    suspend fun loadRankingAsDishes(type: String) = guarded() {
        activeRankingType = type
        val array = api.get("/api/v1/rankings/$type", mapOf("limit" to 20)).recordsOrArray()
        val ranks = List(array.length()) { parseRanking(array.getJSONObject(it)) }
        rankings = ranks
        dishes = ranks.map {
            Dish(
                id = it.dishId,
                merchantId = 0,
                canteenId = 0,
                stallId = 0,
                name = it.dishName,
                description = "${it.favoriteCount} 人收藏，榜单第 ${it.rank} 名",
                price = it.price,
                categoryId = null,
                categoryName = "",
                score = it.score,
                status = "ON_SALE",
                images = emptyList(),
                tags = listOf(Tag(-1, rankingTitle(type))),
                viewCount = 0,
                favoriteCount = it.favoriteCount,
                commentCount = 0,
                canteenName = it.canteenName,
                merchantName = it.merchantName
            )
        }
        goDishes()
    }

    suspend fun openDish(id: Long) = guarded() {
        selectedDish = parseDish(api.get("/api/v1/dishes/$id").dataObjectOrEmpty())
        loadReviews(id)
        studentRoute = StudentRoute.DETAIL
    }

    suspend fun loadReviews(dishId: Long) {
        val array = api.get(
            "/api/v1/reviews",
            mapOf("targetType" to "DISH", "targetId" to dishId, "page" to 1, "size" to 20)
        ).recordsOrArray()
        reviews = List(array.length()) { parseReview(array.getJSONObject(it)) }
    }

    suspend fun createFavorite(dishId: Long) = guarded("已收藏") {
        api.post("/api/v1/favorites", JSONObject().put("targetType", "DISH").put("targetId", dishId), auth = true)
        loadFavorites()
    }

    suspend fun addEatList(dishId: Long, sourceLotteryRecordId: Long? = null) = guarded("已加入想吃") {
        api.post(
            "/api/v1/eat-list",
            JSONObject()
                .put("dishId", dishId)
                .put("sourceLotteryRecordId", sourceLotteryRecordId ?: JSONObject.NULL)
                .put("note", ""),
            auth = true
        )
        loadEatList()
    }

    suspend fun createReview(dishId: Long, score: Double, content: String, anonymous: Boolean) = guarded("发布成功") {
        api.post(
            "/api/v1/reviews",
            JSONObject()
                .put("targetType", "DISH")
                .put("targetId", dishId)
                .put("overallScore", score)
                .put("tasteScore", score)
                .put("portionScore", score)
                .put("valueScore", score)
                .put("content", content)
                .put("images", JSONArray())
                .put("isAnonymous", anonymous),
            auth = true
        )
        loadReviews(dishId)
    }

    suspend fun createFeedback(targetId: Long, type: String, content: String) = guarded("留言已提交") {
        api.post(
            "/api/v1/feedbacks",
            JSONObject().put("targetType", "DISH").put("targetId", targetId).put("feedbackType", type).put("content", content),
            auth = true
        )
    }

    suspend fun drawLottery(mode: String, maxPrice: Double = 20.0, minScore: Double = 4.0) = guarded("抽到啦") {
        val path = when (mode) {
            "CONDITION" -> "/api/v1/lottery/draw-with-condition"
            "FAVORITE" -> "/api/v1/lottery/draw-from-favorites"
            else -> "/api/v1/lottery/draw"
        }
        val body = JSONObject().put("drawMode", mode)
        if (mode == "CONDITION") {
            body.put("maxPrice", maxPrice).put("minScore", minScore)
        }
        lotteryResult = parseLotteryResult(api.post(path, body, auth = true).dataObjectOrEmpty())
    }

    suspend fun recordLotteryAction(recordId: Long?, action: String) = guarded() {
        if (recordId != null) {
            api.post("/api/v1/lottery/records/$recordId/action", JSONObject().put("resultAction", action), auth = true)
        }
    }

    suspend fun acceptLotteryResult() = guarded("已加入想吃") {
        val result = lotteryResult ?: return@guarded
        addEatList(result.dishId, result.recordId)
        if (result.recordId != null) api.post("/api/v1/lottery/records/${result.recordId}/action", JSONObject().put("resultAction", "ACCEPT"), auth = true)
        loadLotteryHistory()
    }

    suspend fun favoriteLotteryResult() = guarded("已收藏") {
        val result = lotteryResult ?: return@guarded
        createFavorite(result.dishId)
        if (result.recordId != null) api.post("/api/v1/lottery/records/${result.recordId}/action", JSONObject().put("resultAction", "FAVORITE"), auth = true)
    }

    suspend fun loadRanking(type: String) {
        val array = api.get("/api/v1/rankings/$type", mapOf("limit" to 10)).recordsOrArray()
        rankings = List(array.length()) { parseRanking(array.getJSONObject(it)) }
    }

    suspend fun loadFavorites() = guarded() {
        val array = api.get("/api/v1/favorites", mapOf("targetType" to "DISH", "page" to 1, "size" to 30), auth = true).recordsOrArray()
        favorites = List(array.length()) { parseFavorite(array.getJSONObject(it)) }
    }

    suspend fun loadEatList() = guarded() {
        val array = api.get("/api/v1/eat-list", mapOf("status" to "WANT_TO_EAT", "page" to 1, "size" to 30), auth = true).recordsOrArray()
        eatList = List(array.length()) { parseEatList(array.getJSONObject(it)) }
    }

    suspend fun loadLotteryHistory() = guarded() {
        val array = api.get("/api/v1/lottery/records", mapOf("page" to 1, "size" to 20), auth = true).recordsOrArray()
        lotteryHistory = List(array.length()) { parseLotteryRecord(array.getJSONObject(it)) }
    }

    suspend fun loadMine() = guarded() {
        runCatching { profile = parseUserProfile(api.get("/api/v1/users/me", auth = true).dataObjectOrEmpty()) }
        runCatching { preferences = parseUserPreference(api.get("/api/v1/users/preferences", auth = true).dataObjectOrEmpty()) }
        val fav = api.get("/api/v1/favorites", mapOf("targetType" to "DISH", "page" to 1, "size" to 30), auth = true).recordsOrArray()
        favorites = List(fav.length()) { parseFavorite(fav.getJSONObject(it)) }
        val eat = api.get("/api/v1/eat-list", mapOf("status" to "WANT_TO_EAT", "page" to 1, "size" to 30), auth = true).recordsOrArray()
        eatList = List(eat.length()) { parseEatList(eat.getJSONObject(it)) }
        val hist = api.get("/api/v1/lottery/records", mapOf("page" to 1, "size" to 20), auth = true).recordsOrArray()
        lotteryHistory = List(hist.length()) { parseLotteryRecord(hist.getJSONObject(it)) }
    }

    suspend fun loadCategoriesAndTags() = guarded() {
        val categoryArray = api.get("/api/v1/categories", mapOf("type" to "DISH")).recordsOrArray()
        categories = List(categoryArray.length()) { parseCategory(categoryArray.getJSONObject(it)) }
        val tagArray = api.get("/api/v1/tags", mapOf("type" to "TASTE")).recordsOrArray()
        tags = List(tagArray.length()) { parseCategory(tagArray.getJSONObject(it)).let { Tag(it.id, it.name) } }
    }

    suspend fun refreshMerchant() = guarded() {
        loadMerchantProfileSafe()
        if (merchantProfile != null) {
            loadMerchantDashboard()
        }
    }

    suspend fun loadMerchantProfileSafe() {
        runCatching {
            merchantProfile = parseMerchantProfile(api.get("/api/v1/merchants/me", auth = true).dataObjectOrEmpty())
        }.onFailure {
            merchantProfile = null
        }
    }

    suspend fun applyMerchant(name: String, stallId: String, description: String, hours: String, phone: String) = guarded("已提交审核") {
        api.post(
            "/api/v1/merchants/apply",
            JSONObject()
                .put("merchantName", name)
                .put("stallId", stallId.toLongOrNull() ?: 0L)
                .put("description", description)
                .put("businessHours", hours)
                .put("contactPhone", phone),
            auth = true
        )
        loadMerchantProfileSafe()
    }

    suspend fun updateMerchant(name: String, description: String, hours: String, phone: String) = guarded("已保存资料") {
        api.put(
            "/api/v1/merchants/me",
            JSONObject()
                .put("name", name)
                .put("description", description)
                .put("logoUrl", "")
                .put("businessHours", hours)
                .put("contactPhone", phone),
            auth = true
        )
        loadMerchantProfileSafe()
    }

    suspend fun loadMerchantDashboard() {
        runCatching {
            merchantOverview = parseMerchantOverview(api.get("/api/v1/merchant/statistics/overview", auth = true).dataObjectOrEmpty())
            val popular = api.get("/api/v1/merchant/statistics/popular-dishes", mapOf("limit" to 4), auth = true).recordsOrArray()
            merchantPopularDishes = List(popular.length()) { parseMerchantPopularDish(popular.getJSONObject(it)) }
            val feedback = api.get("/api/v1/merchant/statistics/most-feedback-dishes", mapOf("limit" to 3), auth = true).recordsOrArray()
            merchantFeedbackDishes = List(feedback.length()) { parseMerchantFeedbackDish(feedback.getJSONObject(it)) }
        }
    }

    suspend fun loadMerchantDishes(status: String = "") = guarded() {
        val merchantId = merchantProfile?.id ?: run {
            loadMerchantProfileSafe()
            merchantProfile?.id
        }
        val array = api.get("/api/v1/dishes", mapOf("merchantId" to merchantId, "page" to 1, "size" to 30, "status" to status), auth = true).recordsOrArray()
        dishes = List(array.length()) { parseDish(array.getJSONObject(it)) }
        if (categories.isEmpty() || tags.isEmpty()) loadCategoriesAndTags()
    }

    suspend fun saveDish(
        id: Long?,
        name: String,
        description: String,
        price: String,
        categoryId: Long?,
        tagIds: List<Long>,
        joinLottery: Boolean
    ) = guarded(if (id == null) "已发布菜品" else "已保存菜品") {
        val body = JSONObject()
            .put("name", name)
            .put("description", description)
            .put("price", price.toDoubleOrNull() ?: 0.0)
            .put("categoryId", categoryId ?: JSONObject.NULL)
            .put("tagIds", JSONArray(tagIds))
            .put("images", JSONArray())
            .put("isJoinLottery", joinLottery)
        if (id == null) api.post("/api/v1/dishes", body, auth = true) else api.put("/api/v1/dishes/$id", body, auth = true)
        loadMerchantDishes()
    }

    suspend fun updateDishStatus(id: Long, status: String) = guarded("状态已更新") {
        api.patch("/api/v1/dishes/$id/status", JSONObject().put("status", status), auth = true)
        loadMerchantDishes()
    }

    suspend fun loadRecommendations() = guarded() {
        val array = api.get("/api/v1/merchant-recommendations", mapOf("page" to 1, "size" to 30), auth = true).recordsOrArray()
        recommendations = List(array.length()) { parseRecommendation(array.getJSONObject(it)) }
    }

    suspend fun saveRecommendation(
        id: Long?,
        dishId: String,
        title: String,
        reason: String,
        type: String,
        startTime: String,
        endTime: String,
        top: Boolean
    ) = guarded(if (id == null) "已发布推荐" else "已保存推荐") {
        val body = JSONObject()
            .put("dishId", dishId.toLongOrNull() ?: 0L)
            .put("title", title)
            .put("recommendReason", reason)
            .put("recommendType", type)
            .put("startTime", startTime.ifBlank { dayTime(10, 0) })
            .put("endTime", endTime.ifBlank { dayTime(20, 30) })
            .put("isTop", top)
        if (id == null) api.post("/api/v1/merchant-recommendations", body, auth = true) else api.put("/api/v1/merchant-recommendations/$id", body, auth = true)
        loadRecommendations()
    }

    suspend fun deleteRecommendation(id: Long) = guarded("已删除推荐") {
        api.delete("/api/v1/merchant-recommendations/$id", auth = true)
        loadRecommendations()
    }

    suspend fun loadFeedbacks(status: String = "") = guarded() {
        val array = api.get("/api/v1/feedbacks", mapOf("page" to 1, "size" to 30, "status" to status), auth = true).recordsOrArray()
        feedbacks = List(array.length()) { parseFeedback(array.getJSONObject(it)) }
    }

    suspend fun replyFeedback(id: Long, reply: String) = guarded("已回复") {
        api.post("/api/v1/feedbacks/$id/reply", JSONObject().put("replyContent", reply), auth = true)
        loadFeedbacks()
    }

    suspend fun updateFeedbackStatus(id: Long, status: String) = guarded("状态已更新") {
        api.patch("/api/v1/feedbacks/$id/status", JSONObject().put("status", status), auth = true)
        loadFeedbacks()
    }

    suspend fun loadImprovements() = guarded() {
        val array = api.get("/api/v1/merchant/improvement-records", mapOf("page" to 1, "size" to 20), auth = true).recordsOrArray()
        improvements = List(array.length()) { parseImprovementRecord(array.getJSONObject(it)) }
    }

    suspend fun createImprovementRecord(
        dishId: Long,
        feedbackId: Long,
        title: String,
        content: String,
        before: String,
        after: String,
        isPublic: Boolean
    ) = guarded("已发布改进") {
        api.post(
            "/api/v1/merchant/improvement-records",
            JSONObject()
                .put("dishId", dishId)
                .put("feedbackId", feedbackId)
                .put("title", title)
                .put("content", content)
                .put("beforeDescription", before)
                .put("afterDescription", after)
                .put("status", "PUBLISHED")
                .put("isPublic", isPublic),
            auth = true
        )
        if (feedbackId > 0) api.patch("/api/v1/feedbacks/$feedbackId/status", JSONObject().put("status", "IMPROVED"), auth = true)
        loadFeedbacks()
        loadImprovements()
    }

    private fun saveSession(session: com.example.frontendandroid.data.Session) {
        token = session.token
        refreshToken = session.refreshToken
        roles = session.roles.ifEmpty { setOf("STUDENT") }
        prefs.edit()
            .putString("token", session.token)
            .putString("refresh_token", session.refreshToken)
            .putStringSet("roles", roles)
            .apply()
    }

    private suspend fun guarded(successMessage: String? = null, block: suspend () -> Unit) {
        loading = true
        message = null
        try {
            block()
            if (successMessage != null) message = successMessage
        } catch (error: Exception) {
            message = error.message ?: "操作失败"
        } finally {
            loading = false
        }
    }
}

fun rankingTitle(type: String): String = when (type) {
    "top-rated" -> "好评榜"
    "popular" -> "热门榜"
    "most-favorited" -> "收藏榜"
    "best-value" -> "性价比榜"
    "new-dishes" -> "新品榜"
    "most-feedback" -> "待改进榜"
    else -> "排行榜"
}

fun dayTime(hour: Int, minute: Int): String {
    val now = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    return now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
