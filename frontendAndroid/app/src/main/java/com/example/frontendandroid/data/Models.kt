package com.example.frontendandroid.data

import org.json.JSONArray
import org.json.JSONObject

data class Session(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val roles: Set<String>
)

data class UserProfile(
    val id: Long,
    val nickname: String,
    val avatar: String,
    val phone: String,
    val createdAt: String
)

data class UserPreference(
    val minPrice: Double,
    val maxPrice: Double,
    val tastePreference: String,
    val avoidTags: String,
    val defaultCanteenId: Long?
)

data class Canteen(
    val id: Long,
    val name: String,
    val type: String,
    val location: String,
    val description: String,
    val openingHours: String,
    val status: String,
    val averageScore: Double
)

data class Stall(
    val id: Long,
    val canteenId: Long,
    val name: String,
    val location: String,
    val description: String,
    val status: String,
    val merchantName: String,
    val averageScore: Double
)

data class Dish(
    val id: Long,
    val merchantId: Long,
    val canteenId: Long,
    val stallId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: Long?,
    val categoryName: String,
    val score: Double,
    val status: String,
    val images: List<String>,
    val tags: List<Tag>,
    val viewCount: Int,
    val favoriteCount: Int,
    val commentCount: Int,
    val canteenName: String = "",
    val stallName: String = "",
    val merchantName: String = "",
    val tasteScore: Double = 0.0,
    val portionScore: Double = 0.0,
    val valueScore: Double = 0.0,
    val isJoinLottery: Boolean = false
)

data class Tag(val id: Long, val name: String)

data class Category(val id: Long, val name: String)

data class Review(
    val id: Long,
    val targetId: Long,
    val nickname: String,
    val content: String,
    val overallScore: Double,
    val likeCount: Int,
    val createdAt: String
)

data class FavoriteItem(
    val id: Long,
    val targetId: Long,
    val dishId: Long,
    val dishName: String,
    val canteenName: String,
    val merchantName: String,
    val price: Double,
    val score: Double
)

data class EatListItem(
    val id: Long,
    val dishId: Long,
    val dishName: String,
    val canteenName: String,
    val merchantName: String,
    val price: Double,
    val status: String
)

data class LotteryResult(
    val recordId: Long?,
    val dishId: Long,
    val title: String,
    val merchantName: String,
    val canteenName: String,
    val price: Double,
    val score: Double,
    val reason: String,
    val tags: List<String>
)

data class LotteryRecord(
    val id: Long,
    val dishId: Long,
    val title: String,
    val price: Double,
    val score: Double,
    val resultAction: String,
    val createdAt: String
)

data class RankingItem(
    val rank: Int,
    val dishId: Long,
    val dishName: String,
    val merchantName: String,
    val canteenName: String,
    val score: Double,
    val price: Double,
    val favoriteCount: Int
)

data class MerchantProfile(
    val id: Long,
    val stallId: Long?,
    val name: String,
    val description: String,
    val businessHours: String,
    val status: String,
    val applyStatus: String,
    val canteenName: String,
    val stallName: String,
    val contactPhone: String
)

data class MerchantOverview(
    val dishCount: Int,
    val viewCount: Int,
    val favoriteCount: Int,
    val reviewCount: Int,
    val averageScore: Double,
    val todayRecommendClickCount: Int,
    val pendingFeedbackCount: Int
)

data class MerchantPopularDish(
    val dishId: Long,
    val dishName: String,
    val viewCount: Int,
    val favoriteCount: Int,
    val reviewCount: Int,
    val averageScore: Double
)

data class MerchantFeedbackDish(
    val dishId: Long,
    val dishName: String,
    val feedbackCount: Int,
    val pendingFeedbackCount: Int
)

data class Recommendation(
    val id: Long,
    val dishId: Long,
    val dishName: String,
    val title: String,
    val recommendReason: String,
    val recommendType: String,
    val startTime: String,
    val endTime: String,
    val isTop: Boolean,
    val status: String,
    val clickCount: Int
)

data class Feedback(
    val id: Long,
    val targetType: String,
    val targetId: Long,
    val dishId: Long,
    val dishName: String,
    val userNickname: String,
    val feedbackType: String,
    val content: String,
    val status: String,
    val replyContent: String,
    val createdAt: String
)

data class ImprovementRecord(
    val id: Long,
    val dishId: Long,
    val dishName: String,
    val title: String,
    val content: String,
    val status: String
)

fun JSONObject.stringOrEmpty(name: String): String = optString(name, "").takeIf { it != "null" } ?: ""

fun JSONObject.longOrZero(name: String): Long = if (has(name) && !isNull(name)) optLong(name) else 0L

fun JSONObject.doubleOrZero(name: String): Double = if (has(name) && !isNull(name)) optDouble(name) else 0.0

fun JSONObject.intOrZero(name: String): Int = if (has(name) && !isNull(name)) optInt(name) else 0

fun JSONObject.optionalLong(name: String): Long? = if (has(name) && !isNull(name)) optLong(name) else null

fun JSONObject.recordsOrArray(): JSONArray {
    val data = opt("data")
    if (data is JSONArray) return data
    if (data is JSONObject) return data.optJSONArray("records") ?: JSONArray()
    return JSONArray()
}

fun JSONObject.dataObjectOrEmpty(): JSONObject = optJSONObject("data") ?: JSONObject()

fun JSONArray.toStringList(): List<String> = List(length()) { index -> optString(index) }.filter { it.isNotBlank() }

fun JSONArray.toTagList(): List<Tag> = List(length()) { index ->
    val item = optJSONObject(index) ?: JSONObject()
    Tag(item.longOrZero("id"), item.stringOrEmpty("name"))
}.filter { it.id != 0L || it.name.isNotBlank() }

fun parseSession(data: JSONObject): Session {
    val roles = data.optJSONArray("roles") ?: JSONArray()
    return Session(
        token = data.optString("token", data.optString("accessToken")),
        refreshToken = data.stringOrEmpty("refreshToken"),
        userId = data.longOrZero("userId"),
        roles = List(roles.length()) { roles.optString(it) }.filter { it.isNotBlank() }.toSet()
    )
}

fun parseUserProfile(item: JSONObject) = UserProfile(
    id = item.longOrZero("id"),
    nickname = item.stringOrEmpty("nickname"),
    avatar = item.stringOrEmpty("avatar"),
    phone = item.stringOrEmpty("phone"),
    createdAt = item.stringOrEmpty("createdAt")
)

fun parseUserPreference(item: JSONObject) = UserPreference(
    minPrice = item.doubleOrZero("minPrice"),
    maxPrice = item.doubleOrZero("maxPrice"),
    tastePreference = item.stringOrEmpty("tastePreference"),
    avoidTags = item.stringOrEmpty("avoidTags"),
    defaultCanteenId = item.optionalLong("defaultCanteenId")
)

fun parseCanteen(item: JSONObject) = Canteen(
    id = item.longOrZero("id"),
    name = item.stringOrEmpty("name"),
    type = item.stringOrEmpty("type"),
    location = item.stringOrEmpty("location"),
    description = item.stringOrEmpty("description"),
    openingHours = item.stringOrEmpty("openingHours"),
    status = item.stringOrEmpty("status"),
    averageScore = item.doubleOrZero("averageScore")
)

fun parseStall(item: JSONObject) = Stall(
    id = item.longOrZero("id"),
    canteenId = item.longOrZero("canteenId"),
    name = item.stringOrEmpty("name"),
    location = item.stringOrEmpty("location"),
    description = item.stringOrEmpty("description"),
    status = item.stringOrEmpty("status"),
    merchantName = item.stringOrEmpty("merchantName"),
    averageScore = item.doubleOrZero("averageScore")
)

fun parseDish(item: JSONObject) = Dish(
    id = item.longOrZero("id").takeIf { it > 0 } ?: item.longOrZero("dishId"),
    merchantId = item.longOrZero("merchantId"),
    canteenId = item.longOrZero("canteenId"),
    stallId = item.longOrZero("stallId"),
    name = item.stringOrEmpty("name").ifBlank { item.stringOrEmpty("dishName") },
    description = item.stringOrEmpty("description"),
    price = item.doubleOrZero("price"),
    categoryId = item.optionalLong("categoryId"),
    categoryName = item.stringOrEmpty("categoryName"),
    score = item.doubleOrZero("score").takeIf { it > 0.0 } ?: item.doubleOrZero("averageScore"),
    status = item.stringOrEmpty("status"),
    images = (item.optJSONArray("images") ?: JSONArray()).toStringList(),
    tags = (item.optJSONArray("tags") ?: JSONArray()).toTagList(),
    viewCount = item.intOrZero("viewCount"),
    favoriteCount = item.intOrZero("favoriteCount"),
    commentCount = item.intOrZero("commentCount").takeIf { it > 0 } ?: item.intOrZero("reviewCount"),
    canteenName = item.stringOrEmpty("canteenName"),
    stallName = item.stringOrEmpty("stallName"),
    merchantName = item.stringOrEmpty("merchantName"),
    tasteScore = item.doubleOrZero("tasteScore"),
    portionScore = item.doubleOrZero("portionScore"),
    valueScore = item.doubleOrZero("valueScore"),
    isJoinLottery = item.optBoolean("isJoinLottery", false)
)

fun parseCategory(item: JSONObject) = Category(item.longOrZero("id"), item.stringOrEmpty("name"))

fun parseReview(item: JSONObject) = Review(
    id = item.longOrZero("id"),
    targetId = item.longOrZero("targetId"),
    nickname = item.stringOrEmpty("userNickname").ifBlank { item.stringOrEmpty("nickname").ifBlank { "匿名同学" } },
    content = item.stringOrEmpty("content"),
    overallScore = item.doubleOrZero("overallScore"),
    likeCount = item.intOrZero("likeCount"),
    createdAt = item.stringOrEmpty("createdAt")
)

fun parseFavorite(item: JSONObject): FavoriteItem {
    val dish = item.optJSONObject("dish") ?: JSONObject()
    val dishId = item.longOrZero("targetId").takeIf { it > 0 } ?: item.longOrZero("dishId").takeIf { it > 0 } ?: dish.longOrZero("id")
    return FavoriteItem(
        id = item.longOrZero("id"),
        targetId = item.longOrZero("targetId"),
        dishId = dishId,
        dishName = item.stringOrEmpty("dishName").ifBlank { dish.stringOrEmpty("name").ifBlank { "收藏菜品" } },
        canteenName = item.stringOrEmpty("canteenName").ifBlank { dish.stringOrEmpty("canteenName") },
        merchantName = item.stringOrEmpty("merchantName").ifBlank { dish.stringOrEmpty("merchantName") },
        price = item.doubleOrZero("price").takeIf { it > 0 } ?: dish.doubleOrZero("price"),
        score = item.doubleOrZero("score").takeIf { it > 0 } ?: dish.doubleOrZero("score")
    )
}

fun parseEatList(item: JSONObject): EatListItem {
    val dish = item.optJSONObject("dish") ?: JSONObject()
    val dishId = item.longOrZero("dishId").takeIf { it > 0 } ?: dish.longOrZero("id")
    return EatListItem(
        id = item.longOrZero("id"),
        dishId = dishId,
        dishName = item.stringOrEmpty("dishName").ifBlank { dish.stringOrEmpty("name") },
        canteenName = item.stringOrEmpty("canteenName").ifBlank { dish.stringOrEmpty("canteenName") },
        merchantName = item.stringOrEmpty("merchantName").ifBlank { dish.stringOrEmpty("merchantName") },
        price = item.doubleOrZero("price").takeIf { it > 0 } ?: dish.doubleOrZero("price"),
        status = item.stringOrEmpty("status")
    )
}

fun parseLotteryResult(item: JSONObject): LotteryResult {
    val dishId = item.longOrZero("dishId").takeIf { it > 0 } ?: item.longOrZero("sourceId")
    return LotteryResult(
        recordId = item.optionalLong("recordId").takeIf { it != 0L },
        dishId = dishId,
        title = item.stringOrEmpty("title").ifBlank { item.stringOrEmpty("dishName") },
        merchantName = item.stringOrEmpty("merchantName"),
        canteenName = item.stringOrEmpty("canteenName"),
        price = item.doubleOrZero("price"),
        score = item.doubleOrZero("score"),
        reason = item.stringOrEmpty("recommendReason").ifBlank { item.stringOrEmpty("reason") },
        tags = (item.optJSONArray("tags") ?: JSONArray()).toStringList()
    )
}

fun parseLotteryRecord(item: JSONObject) = LotteryRecord(
    id = item.longOrZero("id").takeIf { it > 0 } ?: item.longOrZero("recordId"),
    dishId = item.longOrZero("dishId").takeIf { it > 0 } ?: item.longOrZero("sourceId"),
    title = item.stringOrEmpty("title").ifBlank { item.stringOrEmpty("dishName") },
    price = item.doubleOrZero("price"),
    score = item.doubleOrZero("score"),
    resultAction = item.stringOrEmpty("resultAction"),
    createdAt = item.stringOrEmpty("createdAt")
)

fun parseRanking(item: JSONObject) = RankingItem(
    rank = item.intOrZero("rank"),
    dishId = item.longOrZero("dishId"),
    dishName = item.stringOrEmpty("dishName"),
    merchantName = item.stringOrEmpty("merchantName"),
    canteenName = item.stringOrEmpty("canteenName"),
    score = item.doubleOrZero("score"),
    price = item.doubleOrZero("price"),
    favoriteCount = item.intOrZero("favoriteCount")
)

fun parseMerchantProfile(item: JSONObject) = MerchantProfile(
    id = item.longOrZero("id").takeIf { it > 0 } ?: item.longOrZero("merchantId"),
    stallId = item.optionalLong("stallId"),
    name = item.stringOrEmpty("name").ifBlank { item.stringOrEmpty("merchantName") },
    description = item.stringOrEmpty("description"),
    businessHours = item.stringOrEmpty("businessHours"),
    status = item.stringOrEmpty("status").ifBlank { item.stringOrEmpty("merchantStatus") },
    applyStatus = item.stringOrEmpty("applyStatus"),
    canteenName = item.stringOrEmpty("canteenName"),
    stallName = item.stringOrEmpty("stallName"),
    contactPhone = item.stringOrEmpty("contactPhone")
)

fun parseMerchantOverview(item: JSONObject) = MerchantOverview(
    dishCount = item.intOrZero("dishCount"),
    viewCount = item.intOrZero("viewCount"),
    favoriteCount = item.intOrZero("favoriteCount"),
    reviewCount = item.intOrZero("reviewCount"),
    averageScore = item.doubleOrZero("averageScore"),
    todayRecommendClickCount = item.intOrZero("todayRecommendClickCount"),
    pendingFeedbackCount = item.intOrZero("pendingFeedbackCount")
)

fun parseMerchantPopularDish(item: JSONObject) = MerchantPopularDish(
    dishId = item.longOrZero("dishId"),
    dishName = item.stringOrEmpty("dishName"),
    viewCount = item.intOrZero("viewCount"),
    favoriteCount = item.intOrZero("favoriteCount"),
    reviewCount = item.intOrZero("reviewCount"),
    averageScore = item.doubleOrZero("averageScore")
)

fun parseMerchantFeedbackDish(item: JSONObject) = MerchantFeedbackDish(
    dishId = item.longOrZero("dishId"),
    dishName = item.stringOrEmpty("dishName"),
    feedbackCount = item.intOrZero("feedbackCount"),
    pendingFeedbackCount = item.intOrZero("pendingFeedbackCount")
)

fun parseRecommendation(item: JSONObject) = Recommendation(
    id = item.longOrZero("id"),
    dishId = item.longOrZero("dishId"),
    dishName = item.stringOrEmpty("dishName"),
    title = item.stringOrEmpty("title"),
    recommendReason = item.stringOrEmpty("recommendReason"),
    recommendType = item.stringOrEmpty("recommendType"),
    startTime = item.stringOrEmpty("startTime"),
    endTime = item.stringOrEmpty("endTime"),
    isTop = item.optBoolean("isTop", false),
    status = item.stringOrEmpty("status"),
    clickCount = item.intOrZero("clickCount")
)

fun parseFeedback(item: JSONObject) = Feedback(
    id = item.longOrZero("id"),
    targetType = item.stringOrEmpty("targetType"),
    targetId = item.longOrZero("targetId"),
    dishId = item.longOrZero("dishId").takeIf { it > 0 } ?: item.longOrZero("targetId"),
    dishName = item.stringOrEmpty("dishName"),
    userNickname = item.stringOrEmpty("userNickname").ifBlank { "匿名同学" },
    feedbackType = item.stringOrEmpty("feedbackType"),
    content = item.stringOrEmpty("content"),
    status = item.stringOrEmpty("status"),
    replyContent = item.stringOrEmpty("replyContent"),
    createdAt = item.stringOrEmpty("createdAt")
)

fun parseImprovementRecord(item: JSONObject) = ImprovementRecord(
    id = item.longOrZero("id"),
    dishId = item.longOrZero("dishId"),
    dishName = item.stringOrEmpty("dishName"),
    title = item.stringOrEmpty("title"),
    content = item.stringOrEmpty("content"),
    status = item.stringOrEmpty("status")
)
