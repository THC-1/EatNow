# 点儿啥 - 校园餐饮与商户评价推荐平台 API 接口文档

## 文档说明

本文档描述"点儿啥"平台的所有 RESTful API 接口。所有接口均遵循 RESTful 设计规范:
- URL 路径全部小写,使用中划线 `-` 分隔单词
- 通过 HTTP Method 表达操作: GET(查询)、POST(创建)、PUT(全量更新)、PATCH(局部更新)、DELETE(删除)
- 请求入参使用 DTO,返回出参使用 VO

### 平台说明

平台覆盖多种餐饮场景,包括:
- **食堂**: 校园内各食堂,包含多个独立窗口
- **校内店铺**: 不属于食堂但在校园内的独立店铺
- **校园周边店铺**: 不在校园内的独立合作商户

### 基础信息

- **Base URL**: `/api/v1`
- **认证方式**: JWT Token (请求头: `Authorization: Bearer <token>`)
- **数据格式**: JSON

### 通用响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

---

## 一、认证模块

### 1.1 学生微信登录

- **接口**: `POST /api/v1/auth/student-login`
- **描述**: 学生用户通过微信授权登录(小程序)
- **请求参数**:

```json
{
  "code": "微信登录凭证"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token",
    "refreshToken": "刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000,
    "isNewUser": true,
    "userId": 123
  }
}
```

### 1.2 卖家微信登录

- **接口**: `POST /api/v1/auth/merchant-login`
- **描述**: 卖家通过微信授权登录(小程序)
- **请求参数**:

```json
{
  "code": "微信登录凭证"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token",
    "refreshToken": "刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000,
    "userId": 123,
    "merchantId": 456,
    "applyStatus": "APPROVED",
    "merchantStatus": "OPEN"
  }
}
```

### 1.3 Android账号登录

- **接口**: `POST /api/v1/auth/android/login`
- **描述**: Android端普通账号通过账号密码登录。返回的 `roles` 用于判断是否已具备商家入口权限。
- **请求参数**:

```json
{
  "username": "用户名",
  "password": "密码"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token",
    "refreshToken": "刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000,
    "userId": 123,
    "roles": ["STUDENT"]
  }
}
```

### 1.4 Android账号注册

- **接口**: `POST /api/v1/auth/android/register`
- **描述**: Android端注册普通账号。注册成功后默认绑定 `STUDENT` 角色并直接返回Token。
- **请求参数**:

```json
{
  "username": "用户名",
  "password": "密码",
  "nickname": "昵称"
}
```

- **字段说明**:
  - `username`: 必填,4-64位,全局唯一
  - `password`: 必填,6-64位
  - `nickname`: 可选,不传时默认使用用户名

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token",
    "refreshToken": "刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000,
    "userId": 123,
    "roles": ["STUDENT"]
  }
}
```

### 1.5 管理员登录

- **接口**: `POST /api/v1/auth/admin-login`
- **描述**: 平台管理员通过账号密码登录
- **请求参数**:

```json
{
  "username": "admin",
  "password": "password"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token",
    "refreshToken": "刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000,
    "adminId": 1
  }
}
```

### 1.6 刷新Token

- **接口**: `POST /api/v1/auth/refresh`
- **描述**: 使用刷新令牌获取新的Token对
- **请求参数**:

```json
{
  "refreshToken": "刷新令牌"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "新的访问令牌",
    "refreshToken": "新的刷新令牌",
    "tokenExpiresAt": 1760000000000,
    "refreshTokenExpiresAt": 1760600000000
  }
}
```

### 1.7 用户登出

- **接口**: `POST /api/v1/auth/logout`
- **描述**: 用户登出,使Token失效
- **请求头**: `Authorization: Bearer <accessToken>`
- **请求参数**:

```json
{
  "refreshToken": "刷新令牌"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 1.8 修改密码

- **接口**: `POST /api/v1/auth/change-password`
- **描述**: 修改当前登录用户的密码
- **权限**: 登录用户
- **请求参数**:

```json
{
  "currentPassword": "当前密码",
  "newPassword": "新密码"
}
```

- **新密码长度**: 6-64位

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 二、用户模块

### 2.1 获取学生个人信息

- **接口**: `GET /api/v1/users/me`
- **描述**: 获取当前登录学生的个人信息
- **权限**: 学生用户
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "nickname": "张三",
    "avatar": "头像URL",
    "phone": "13800138000",
    "createdAt": "2026-04-24T10:00:00"
  }
}
```

### 2.2 更新学生个人信息

- **接口**: `PUT /api/v1/users/me`
- **描述**: 更新学生个人信息
- **权限**: 学生用户
- **请求参数**:

```json
{
  "nickname": "张三",
  "avatar": "头像URL",
  "phone": "13800138000"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 2.3 获取用户偏好设置

- **接口**: `GET /api/v1/users/preferences`
- **描述**: 获取当前用户的偏好设置(用于抽奖推荐)
- **权限**: 学生用户
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "defaultCanteenId": 2,
    "defaultCanteenType": "CANTEEN",
    "minPrice": 10.0,
    "maxPrice": 20.0,
    "tastePreference": "微辣,管饱",
    "avoidTags": "香菜,芹菜"
  }
}
```

### 2.4 更新用户偏好设置

- **接口**: `PUT /api/v1/users/preferences`
- **描述**: 更新用户偏好设置
- **权限**: 学生用户
- **请求参数**:

```json
{
  "defaultCanteenId": 2,
  "defaultCanteenType": "CANTEEN",
  "minPrice": 10.0,
  "maxPrice": 20.0,
  "tastePreference": "微辣,管饱",
  "avoidTags": "香菜,芹菜"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

## 三、商业区与店铺模块

> **说明**: `canteen` 代表商业区,包括食堂、商业街、校园周边等;`stall` 代表店铺,包括窗口、独立店铺等。

### 3.1 获取商业区列表

- **接口**: `GET /api/v1/canteens`
- **描述**: 获取所有商业区列表(食堂、商业街、校园周边等)
- **查询参数**:
  - `campusId`: 校区ID(可选)
  - `type`: 商业区类型(可选) - `CANTEEN`(食堂)、`CAMPUS_SHOP`(校内店铺)、`PERIPHERY_SHOP`(校园周边店铺)
  - `status`: 营业状态(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "campusId": 1,
      "name": "第一食堂",
      "type": "CANTEEN",
      "location": "校园东区",
      "description": "主营快餐",
      "openingHours": "06:00-21:00",
      "status": "OPEN",
      "averageScore": 4.5
    },
    {
      "id": 2,
      "campusId": 1,
      "name": "校园商业街",
      "type": "CAMPUS_SHOP",
      "location": "校园南区",
      "description": "各类餐饮商户",
      "openingHours": "08:00-22:00",
      "status": "OPEN",
      "averageScore": 4.3
    }
  ]
}
```

### 3.2 获取商业区详情

- **接口**: `GET /api/v1/canteens/{id}`
- **描述**: 获取指定商业区的详细信息
- **路径参数**: `id` - 商业区ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "campusId": 1,
    "name": "第一食堂",
    "type": "CANTEEN",
    "location": "校园东区",
    "description": "主营快餐",
    "openingHours": "06:00-21:00",
    "status": "OPEN",
    "stallCount": 15,
    "averageScore": 4.5
  }
}
```

### 3.3 获取店铺列表

- **接口**: `GET /api/v1/stalls`
- **描述**: 获取店铺列表,可按商业区筛选
- **查询参数**:
  - `canteenId`: 商业区ID(必填)
  - `status`: 状态(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "canteenId": 1,
      "name": "快餐窗口",
      "location": "一楼1号",
      "description": "各类快餐",
      "status": "OPEN",
      "merchantName": "张三快餐"
    },
    {
      "id": 11,
      "canteenId": 2,
      "name": "奶茶店",
      "location": "商业街A12号",
      "description": "各类奶茶饮品",
      "status": "OPEN",
      "merchantName": "李记奶茶"
    }
  ]
}
```

### 3.4 获取店铺详情

- **接口**: `GET /api/v1/stalls/{id}`
- **描述**: 获取指定店铺的详细信息
- **路径参数**: `id` - 店铺ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "canteenId": 1,
    "name": "快餐窗口",
    "location": "一楼1号",
    "description": "各类快餐",
    "status": "OPEN",
    "merchantId": 5,
    "merchantName": "张三快餐",
    "dishCount": 20,
    "averageScore": 4.3
  }
}
```

---

## 四、商家模块

### 4.1 获取商家列表

- **接口**: `GET /api/v1/merchants`
- **描述**: 获取商家列表,可按商业区筛选
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `stallId`: 店铺ID(可选)
  - `status`: 状态(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 5,
      "userId": 100,
      "stallId": 10,
      "name": "张三快餐",
      "description": "专营各类快餐",
      "logoUrl": "logo URL",
      "businessHours": "10:00-20:00",
      "status": "OPEN",
      "canteenName": "第一食堂",
      "canteenType": "CANTEEN",
      "stallName": "快餐窗口",
      "averageScore": 4.3,
      "dishCount": 20
    }
  ]
}
```

### 4.2 获取商家详情

- **接口**: `GET /api/v1/merchants/{id}`
- **描述**: 获取商家详细信息
- **路径参数**: `id` - 商家ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5,
    "userId": 100,
    "stallId": 10,
    "name": "张三快餐",
    "description": "专营各类快餐",
    "logoUrl": "logo URL",
    "businessHours": "10:00-20:00",
    "status": "OPEN",
    "canteenName": "第一食堂",
    "canteenType": "CANTEEN",
    "stallName": "快餐窗口",
    "averageScore": 4.3,
    "dishCount": 20,
    "favoriteCount": 150,
    "commentCount": 89
  }
}
```

### 4.3 提交卖家入驻申请

- **接口**: `POST /api/v1/merchants/apply`
- **描述**: 普通登录用户提交商家入驻申请。审核通过后,系统会为该账号授予 `MERCHANT` 角色,Android端重新登录或刷新Token后即可进入商家页面。
- **权限**: 登录用户
- **请求参数**:

```json
{
  "merchantName": "张三快餐",
  "stallId": 10,
  "description": "专营各类快餐",
  "businessHours": "10:00-20:00",
  "contactPhone": "13800138000"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "申请已提交",
  "data": {
    "merchantId": 5,
    "applyStatus": "PENDING"
  }
}
```

### 4.4 获取我的商家信息

- **接口**: `GET /api/v1/merchants/me`
- **描述**: 获取当前登录用户的商家信息或入驻申请状态;未提交申请时返回404。
- **权限**: 登录用户
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 4.5 更新商家信息

- **接口**: `PUT /api/v1/merchants/me`
- **描述**: 更新当前商家信息
- **权限**: 卖家用户(`MERCHANT`角色)
- **请求参数**:

```json
{
  "name": "张三快餐",
  "description": "更新后的描述",
  "logoUrl": "新logo URL",
  "businessHours": "10:00-20:00"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

## 五、菜品模块

### 5.1 获取菜品列表

- **接口**: `GET /api/v1/dishes`
- **描述**: 获取菜品列表,支持多种筛选条件
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `stallId`: 店铺ID(可选)
  - `merchantId`: 商家ID(可选)
  - `categoryId`: 分类ID(可选)
  - `tagId`: 标签ID(可选)
  - `minPrice`: 最低价格(可选)
  - `maxPrice`: 最高价格(可选)
  - `minScore`: 最低评分(可选)
  - `keyword`: 搜索关键词(可选)
  - `status`: 状态(可选)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 100,
        "merchantId": 5,
        "canteenId": 1,
        "stallId": 10,
        "name": "黑椒鸡排饭",
        "description": "现炸鸡排配米饭",
        "price": 15.0,
        "categoryId": 1,
        "categoryName": "盖饭",
        "score": 4.5,
        "status": "ON_SALE",
        "images": ["图片URL1", "图片URL2"],
        "tags": [{"id": 10, "name": "微辣"}, {"id": 11, "name": "管饱"}],
        "viewCount": 500,
        "favoriteCount": 120,
        "commentCount": 45
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 5.2 获取菜品详情

- **接口**: `GET /api/v1/dishes/{id}`
- **描述**: 获取菜品详细信息
- **路径参数**: `id` - 菜品ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "merchantId": 5,
    "canteenId": 1,
    "stallId": 10,
    "name": "黑椒鸡排饭",
    "description": "现炸鸡排配米饭",
    "price": 15.0,
    "categoryId": 1,
    "categoryName": "盖饭",
    "score": 4.5,
    "status": "ON_SALE",
    "isJoinLottery": true,
    "images": ["图片URL1", "图片URL2"],
    "tags": [{"id": 10, "name": "微辣"}, {"id": 11, "name": "管饱"}],
    "viewCount": 500,
    "favoriteCount": 120,
    "commentCount": 45,
    "canteenName": "第一食堂",
    "canteenType": "CANTEEN",
    "stallName": "快餐窗口",
    "merchantName": "张三快餐",
    "tasteScore": 4.6,
    "portionScore": 4.3,
    "valueScore": 4.5
  }
}
```

### 5.3 搜索菜品

- **接口**: `GET /api/v1/dishes/search`
- **描述**: 搜索菜品
- **查询参数**:
  - `keyword`: 搜索关键词(必填)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**: 同获取菜品列表

### 5.4 新增菜品(卖家)

- **接口**: `POST /api/v1/dishes`
- **描述**: 卖家新增菜品
- **权限**: 卖家用户
- **请求参数**:

```json
{
  "name": "黑椒鸡排饭",
  "description": "现炸鸡排配米饭",
  "price": 15.0,
  "categoryId": 1,
  "tagIds": [10, 11],
  "images": ["图片URL1", "图片URL2"],
  "isJoinLottery": true
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100
  }
}
```

### 5.5 更新菜品(卖家)

- **接口**: `PUT /api/v1/dishes/{id}`
- **描述**: 卖家更新菜品信息
- **权限**: 卖家用户
- **路径参数**: `id` - 菜品ID
- **请求参数**:

```json
{
  "name": "黑椒鸡排饭",
  "description": "更新后的描述",
  "price": 16.0,
  "categoryId": 1,
  "tagIds": [10, 11],
  "images": ["图片URL1", "图片URL2"],
  "isJoinLottery": true
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 5.6 更新菜品状态

- **接口**: `PATCH /api/v1/dishes/{id}/status`
- **描述**: 更新菜品状态(上架/下架/售罄)
- **权限**: 卖家用户或管理员
- **路径参数**: `id` - 菜品ID
- **请求参数**:

```json
{
  "status": "ON_SALE"
}
```

- **状态枚举**: `ON_SALE`(上架中)、`SOLD_OUT`(已售罄)、`OFF_SHELF`(已下架)、`PENDING`(待审核)

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 5.7 删除菜品

- **接口**: `DELETE /api/v1/dishes/{id}`
- **描述**: 删除菜品
- **权限**: 卖家用户(只能删除自己的菜品)
- **路径参数**: `id` - 菜品ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 5.8 获取商家推荐菜品

- **接口**: `GET /api/v1/dishes/recommendations`
- **描述**: 获取商家推荐的菜品
- **查询参数**:
  - `merchantId`: 商家ID(必填)
  - `recommendType`: 推荐类型(可选) - `TODAY`(今日主推)、`NEW`(新品)、`SPECIAL`(特色)、`VALUE`(高性价比)、`SIGNATURE`(招牌)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

## 六、分类与标签模块

### 6.1 获取分类列表

- **接口**: `GET /api/v1/categories`
- **描述**: 获取所有菜品分类
- **查询参数**:
  - `type`: 分类类型(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "盖饭",
      "type": "DISH",
      "sortOrder": 1,
      "status": "ACTIVE"
    }
  ]
}
```

### 6.2 获取标签列表

- **接口**: `GET /api/v1/tags`
- **描述**: 获取所有标签
- **查询参数**:
  - `type`: 标签类型(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "name": "微辣",
      "type": "TASTE",
      "status": "ACTIVE"
    }
  ]
}
```

---

## 七、收藏模块

### 7.1 收藏菜品/分享

- **接口**: `POST /api/v1/favorites`
- **描述**: 收藏菜品或学生分享
- **权限**: 学生用户
- **请求参数**:

```json
{
  "targetType": "DISH",
  "targetId": 100
}
```

- `targetType` 枚举: `DISH`(商家菜品)、`POST`(学生分享)

- **响应参数**:

```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "id": 1
  }
}
```

### 7.2 取消收藏

- **接口**: `DELETE /api/v1/favorites/{id}`
- **描述**: 取消收藏
- **权限**: 学生用户
- **路径参数**: `id` - 收藏记录ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

### 7.3 获取我的收藏列表

- **接口**: `GET /api/v1/favorites`
- **描述**: 获取当前用户的收藏列表
- **权限**: 学生用户
- **查询参数**:
  - `targetType`: 收藏类型(可选) - `DISH` 或 `POST`
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "targetType": "DISH",
        "targetId": 100,
        "createdAt": "2026-04-24T10:00:00",
        "dish": {
          "id": 100,
          "name": "黑椒鸡排饭",
          "price": 15.0,
          "score": 4.5,
          "images": ["图片URL"]
        }
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

### 7.4 检查是否已收藏

- **接口**: `GET /api/v1/favorites/check`
- **描述**: 检查当前用户是否已收藏指定目标
- **权限**: 学生用户
- **查询参数**:
  - `targetType`: 收藏类型(必填)
  - `targetId`: 目标ID(必填)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isFavorite": true,
    "favoriteId": 1
  }
}
```

---

## 八、评价与评论模块

### 8.1 发表评价

- **接口**: `POST /api/v1/reviews`
- **描述**: 学生对菜品发表评价
- **权限**: 学生用户
- **请求参数**:

```json
{
  "targetType": "DISH",
  "targetId": 100,
  "overallScore": 4.5,
  "tasteScore": 4.6,
  "portionScore": 4.3,
  "valueScore": 4.5,
  "content": "味道不错,分量足",
  "images": ["图片URL1", "图片URL2"],
  "isAnonymous": false
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "评价发表成功",
  "data": {
    "id": 1
  }
}
```

### 8.2 获取评价列表

- **接口**: `GET /api/v1/reviews`
- **描述**: 获取菜品的评价列表
- **查询参数**:
  - `targetType`: 目标类型(必填)
  - `targetId`: 目标ID(必填)
  - `sortBy`: 排序方式(可选) - `latest`(最新)、`highest`(最高分)、`lowest`(最低分)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "userNickname": "张三",
        "userAvatar": "头像URL",
        "targetType": "DISH",
        "targetId": 100,
        "overallScore": 4.5,
        "tasteScore": 4.6,
        "portionScore": 4.3,
        "valueScore": 4.5,
        "content": "味道不错,分量足",
        "images": ["图片URL1"],
        "isAnonymous": false,
        "likeCount": 10,
        "isLiked": false,
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 45,
    "page": 1,
    "size": 10
  }
}
```

### 8.3 点赞评论

- **接口**: `POST /api/v1/reviews/{id}/like`
- **描述**: 点赞某条评价
- **权限**: 学生用户
- **路径参数**: `id` - 评价ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likeCount": 11
  }
}
```

### 8.4 取消点赞

- **接口**: `DELETE /api/v1/reviews/{id}/like`
- **描述**: 取消点赞
- **权限**: 学生用户
- **路径参数**: `id` - 评价ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": {
    "likeCount": 10
  }
}
```

### 8.5 获取我的评价列表

- **接口**: `GET /api/v1/reviews/me`
- **描述**: 获取当前用户发表的评价列表
- **权限**: 学生用户
- **查询参数**:
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

### 8.6 删除评价

- **接口**: `DELETE /api/v1/reviews/{id}`
- **描述**: 删除评价(用户只能删除自己的,管理员可删除任意)
- **权限**: 学生用户(自己的)或管理员
- **路径参数**: `id` - 评价ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 九、反馈模块

### 9.1 提交反馈

- **接口**: `POST /api/v1/feedbacks`
- **描述**: 学生提交反馈给商家
- **权限**: 学生用户
- **请求参数**:

```json
{
  "targetType": "DISH",
  "targetId": 100,
  "feedbackType": "TASTE",
  "content": "口味偏咸,建议调整"
}
```

- `feedbackType` 枚举: `TASTE`(口味)、`PORTION`(分量)、`PRICE`(价格)、`SERVICE`(服务)、`HYGIENE`(卫生)、`OTHER`(其他)

- **响应参数**:

```json
{
  "code": 200,
  "message": "反馈提交成功",
  "data": {
    "id": 1
  }
}
```

### 9.2 获取反馈列表(卖家)

- **接口**: `GET /api/v1/feedbacks`
- **描述**: 卖家查看收到的反馈
- **权限**: 卖家用户
- **查询参数**:
  - `dishId`: 菜品ID(可选)
  - `feedbackType`: 反馈类型(可选)
  - `status`: 反馈状态(可选) - `PENDING`、`VIEWED`、`ACCEPTED`、`IMPROVED`、`REJECTED`
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "userNickname": "张三",
        "targetType": "DISH",
        "targetId": 100,
        "dishName": "黑椒鸡排饭",
        "feedbackType": "TASTE",
        "content": "口味偏咸,建议调整",
        "status": "PENDING",
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

### 9.3 回复反馈

- **接口**: `POST /api/v1/feedbacks/{id}/reply`
- **描述**: 卖家回复学生反馈
- **权限**: 卖家用户
- **路径参数**: `id` - 反馈ID
- **请求参数**:

```json
{
  "replyContent": "感谢反馈,后续会调整口味"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "回复成功",
  "data": null
}
```

### 9.4 更新反馈状态

- **接口**: `PATCH /api/v1/feedbacks/{id}/status`
- **描述**: 更新反馈处理状态
- **权限**: 卖家用户
- **路径参数**: `id` - 反馈ID
- **请求参数**:

```json
{
  "status": "ACCEPTED"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

## 十、想吃清单模块

### 10.1 添加到想吃清单

- **接口**: `POST /api/v1/eat-list`
- **描述**: 将菜品添加到想吃清单
- **权限**: 学生用户
- **请求参数**:

```json
{
  "dishId": 100,
  "sourceLotteryRecordId": 1,
  "note": "备注信息"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

### 10.2 获取想吃清单列表

- **接口**: `GET /api/v1/eat-list`
- **描述**: 获取当前用户的想吃清单列表
- **权限**: 学生用户
- **查询参数**:
  - `status`: 状态(可选) - `WANT_TO_EAT`、`EATEN`、`CANCELLED`
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "dishId": 100,
        "status": "WANT_TO_EAT",
        "sourceLotteryRecordId": 1,
        "note": "备注信息",
        "createdAt": "2026-04-24T10:00:00",
        "eatenAt": null,
        "updatedAt": "2026-04-24T10:00:00",
        "dish": {
          "id": 100,
          "name": "黑椒鸡排饭",
          "price": 15.0,
          "score": 4.5,
          "merchantName": "张三快餐",
          "canteenName": "第一食堂",
          "images": ["图片URL1", "图片URL2"]
        }
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

### 10.3 标记已吃

- **接口**: `PATCH /api/v1/eat-list/{id}/eaten`
- **描述**: 将想吃清单中的菜品标记为已吃
- **权限**: 学生用户
- **路径参数**: `id` - 想吃清单记录ID
- **请求参数**:

```json
{
  "note": "更新后的备注",
  "eatenAt": "2026-04-24T12:00:00"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 10.4 取消想吃清单记录

- **接口**: `DELETE /api/v1/eat-list/{id}`
- **描述**: 取消想吃清单中的某条记录
- **权限**: 学生用户
- **路径参数**: `id` - 想吃清单记录ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 十一、商家推荐模块

### 11.1 发布推荐

- **接口**: `POST /api/v1/merchant-recommendations`
- **描述**: 商家发布菜品推荐
- **权限**: 卖家用户
- **请求参数**:

```json
{
  "dishId": 100,
  "title": "今日主推:黑椒鸡排饭",
  "recommendReason": "今日现炸鸡排,分量足,适合午餐",
  "recommendType": "TODAY",
  "startTime": "2026-04-24T10:00:00",
  "endTime": "2026-04-24T20:00:00",
  "isTop": true
}
```

- `recommendType` 枚举: `TODAY`(今日主推)、`NEW`(新品)、`SPECIAL`(特色)、`VALUE`(高性价比)、`SIGNATURE`(招牌)

- **响应参数**:

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1
  }
}
```

### 11.2 获取我的推荐列表

- **接口**: `GET /api/v1/merchant-recommendations`
- **描述**: 获取当前商家的推荐列表
- **权限**: 卖家用户
- **查询参数**:
  - `recommendType`: 推荐类型(可选)
  - `status`: 状态(可选)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "merchantId": 5,
        "dishId": 100,
        "dishName": "黑椒鸡排饭",
        "title": "今日主推:黑椒鸡排饭",
        "recommendReason": "今日现炸鸡排,分量足,适合午餐",
        "recommendType": "TODAY",
        "startTime": "2026-04-24T10:00:00",
        "endTime": "2026-04-24T20:00:00",
        "status": "ACTIVE",
        "isTop": true,
        "clickCount": 150,
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

### 11.3 更新推荐

- **接口**: `PUT /api/v1/merchant-recommendations/{id}`
- **描述**: 更新推荐信息
- **权限**: 卖家用户
- **路径参数**: `id` - 推荐ID
- **请求参数**:

```json
{
  "title": "更新后的标题",
  "recommendReason": "更新后的理由",
  "recommendType": "NEW",
  "startTime": "2026-04-24T10:00:00",
  "endTime": "2026-04-24T20:00:00",
  "isTop": false
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 11.4 删除推荐

- **接口**: `DELETE /api/v1/merchant-recommendations/{id}`
- **描述**: 删除推荐
- **权限**: 卖家用户
- **路径参数**: `id` - 推荐ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 十二、商家改进记录模块

### 12.1 创建改进记录

- **接口**: `POST /api/v1/merchant/improvement-records`
- **描述**: 商家发布改进记录
- **权限**: 卖家用户
- **请求参数**:

```json
{
  "dishId": 100,
  "feedbackId": 1,
  "title": "已增加米饭分量",
  "content": "根据同学们反馈，我们已增加米饭的标准分量",
  "beforeDescription": "原来米饭分量较少",
  "afterDescription": "现在每份增加50克米饭",
  "status": "PUBLISHED",
  "isPublic": true
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

### 12.2 获取改进记录列表

- **接口**: `GET /api/v1/merchant/improvement-records`
- **描述**: 获取当前商家的改进记录列表
- **权限**: 卖家用户
- **查询参数**:
  - `dishId`: 菜品ID(可选)
  - `feedbackId`: 反馈ID(可选)
  - `status`: 状态(可选) - `DRAFT`、`PUBLISHED`、`ARCHIVED`
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "dishId": 100,
        "dishName": "黑椒鸡排饭",
        "feedbackId": 1,
        "feedbackContent": "米饭分量有点少",
        "title": "已增加米饭分量",
        "content": "根据同学们反馈，我们已增加米饭的标准分量",
        "beforeDescription": "原来米饭分量较少",
        "afterDescription": "现在每份增加50克米饭",
        "status": "PUBLISHED",
        "isPublic": true,
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

---

## 十三、抽奖推荐模块

### 13.1 随机抽奖

- **接口**: `POST /api/v1/lottery/draw`
- **描述**: 全平台随机抽奖
- **权限**: 学生用户
- **请求参数**:

```json
{
  "drawMode": "RANDOM"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recordId": 1,
    "sourceType": "DISH",
    "sourceId": 100,
    "title": "黑椒鸡排饭",
    "canteenName": "第一食堂",
    "canteenType": "CANTEEN",
    "merchantName": "张三快餐",
    "price": 15.0,
    "score": 4.5,
    "tags": ["微辣", "管饱"],
    "images": ["图片URL"],
    "recommendReason": "根据你的口味偏好推荐"
  }
}
```

### 13.2 条件抽奖

- **接口**: `POST /api/v1/lottery/draw-with-condition`
- **描述**: 按条件抽奖
- **权限**: 学生用户
- **请求参数**:

```json
{
  "drawMode": "CONDITION",
  "canteenId": 1,
  "minPrice": 10.0,
  "maxPrice": 20.0,
  "tagIds": [10, 11],
  "minScore": 4.0
}
```

- **响应参数**: 同随机抽奖

### 13.3 从收藏中抽奖

- **接口**: `POST /api/v1/lottery/draw-from-favorites`
- **描述**: 从收藏的菜品中随机抽取
- **权限**: 学生用户
- **请求参数**:

```json
{
  "drawMode": "FAVORITE"
}
```

- **响应参数**: 同随机抽奖

### 13.4 获取抽奖历史

- **接口**: `GET /api/v1/lottery/records`
- **描述**: 获取当前用户的抽奖历史
- **权限**: 学生用户
- **查询参数**:
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "drawMode": "RANDOM",
        "sourceType": "DISH",
        "sourceId": 100,
        "title": "黑椒鸡排饭",
        "price": 15.0,
        "score": 4.5,
        "resultAction": "ACCEPT",
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

### 13.5 记录抽奖结果行为

- **接口**: `POST /api/v1/lottery/records/{id}/action`
- **描述**: 记录用户对抽奖结果的行为
- **权限**: 学生用户
- **路径参数**: `id` - 抽奖记录ID
- **请求参数**:

```json
{
  "resultAction": "ACCEPT"
}
```

- `resultAction` 枚举: `NONE`(无操作)、`ACCEPT`(接受)、`SKIP`(跳过)、`FAVORITE`(收藏)

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 十四、排行榜模块

### 14.1 获取好评榜

- **接口**: `GET /api/v1/rankings/top-rated`
- **描述**: 获取评分最高的菜品排行
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `categoryId`: 分类ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "rank": 1,
      "dishId": 100,
      "dishName": "黑椒鸡排饭",
      "merchantName": "张三快餐",
      "canteenName": "第一食堂",
      "canteenType": "CANTEEN",
      "score": 4.8,
      "price": 15.0,
      "images": ["图片URL"],
      "favoriteCount": 120
    }
  ]
}
```

### 14.2 获取热门榜

- **接口**: `GET /api/v1/rankings/popular`
- **描述**: 获取最热门的菜品排行(按浏览量和收藏数)
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**: 同好评榜

### 14.3 获取收藏榜

- **接口**: `GET /api/v1/rankings/most-favorited`
- **描述**: 获取收藏数最多的菜品排行
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**: 同好评榜

### 14.4 获取高性价比榜

- **接口**: `GET /api/v1/rankings/best-value`
- **描述**: 获取性价比最高的菜品排行
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**: 同好评榜

### 14.5 获取新品推荐榜

- **接口**: `GET /api/v1/rankings/new-dishes`
- **描述**: 获取最新上架的菜品排行
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**: 同好评榜

### 14.6 获取反馈较多榜

- **接口**: `GET /api/v1/rankings/most-feedback`
- **描述**: 获取反馈较多的菜品排行
- **查询参数**:
  - `canteenId`: 商业区ID(可选)
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**: 同好评榜

---

## 十五、商家数据看板模块

### 15.1 获取商家数据概览

- **接口**: `GET /api/v1/merchant/statistics/overview`
- **描述**: 获取当前商家的数据概览
- **权限**: 卖家用户
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dishCount": 20,
    "viewCount": 8000,
    "favoriteCount": 500,
    "reviewCount": 200,
    "averageScore": 4.3,
    "todayRecommendClickCount": 150,
    "pendingFeedbackCount": 10
  }
}
```

### 15.2 获取最受欢迎菜品

- **接口**: `GET /api/v1/merchant/statistics/popular-dishes`
- **描述**: 获取商家最受欢迎的菜品
- **权限**: 卖家用户
- **查询参数**:
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "dishId": 100,
      "dishName": "黑椒鸡排饭",
      "viewCount": 5000,
      "favoriteCount": 200,
      "reviewCount": 100,
      "averageScore": 4.5
    }
  ]
}
```

### 15.3 获取反馈较多菜品

- **接口**: `GET /api/v1/merchant/statistics/most-feedback-dishes`
- **描述**: 获取反馈较多的菜品
- **权限**: 卖家用户
- **查询参数**:
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "dishId": 100,
      "dishName": "黑椒鸡排饭",
      "feedbackCount": 15,
      "pendingFeedbackCount": 5
    }
  ]
}
```

---

## 十六、管理员模块

### 16.1 获取用户列表

- **接口**: `GET /api/v1/admin/users`
- **描述**: 获取所有用户列表
- **权限**: 管理员
- **查询参数**:
  - `role`: 角色(可选) - `STUDENT`、`MERCHANT`
  - `status`: 状态(可选)
  - `keyword`: 搜索关键词(可选)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 123,
        "username": "user123",
        "nickname": "张三",
        "avatar": "头像URL",
        "role": "STUDENT",
        "status": "ACTIVE",
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 16.2 获取用户详情

- **接口**: `GET /api/v1/admin/users/{id}`
- **描述**: 获取用户详细信息
- **权限**: 管理员
- **路径参数**: `id` - 用户ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 16.3 禁用用户

- **接口**: `PATCH /api/v1/admin/users/{id}/disable`
- **描述**: 禁用违规用户
- **权限**: 管理员
- **路径参数**: `id` - 用户ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "禁用成功",
  "data": null
}
```

### 16.4 恢复用户

- **接口**: `PATCH /api/v1/admin/users/{id}/enable`
- **描述**: 恢复被禁用的用户
- **权限**: 管理员
- **路径参数**: `id` - 用户ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "恢复成功",
  "data": null
}
```

### 16.5 获取卖家入驻申请列表

- **接口**: `GET /api/v1/admin/merchant-applications`
- **描述**: 获取卖家入驻申请列表
- **权限**: 管理员
- **查询参数**:
  - `applyStatus`: 申请状态(可选) - `PENDING`、`APPROVED`、`REJECTED`
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "merchantId": 5,
        "userId": 100,
        "merchantName": "张三快餐",
        "stallId": 10,
        "stallName": "快餐窗口",
        "description": "专营各类快餐",
        "applyStatus": "PENDING",
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

### 16.6 审核卖家入驻申请

- **接口**: `PATCH /api/v1/admin/merchant-applications/{merchantId}/approve`
- **描述**: 审核通过卖家入驻申请
- **权限**: 管理员
- **路径参数**: `merchantId` - 商家ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "审核通过",
  "data": null
}
```

### 16.7 驳回卖家入驻申请

- **接口**: `PATCH /api/v1/admin/merchant-applications/{merchantId}/reject`
- **描述**: 驳回卖家入驻申请
- **权限**: 管理员
- **路径参数**: `merchantId` - 商家ID
- **请求参数**:

```json
{
  "rejectReason": "资料不完整"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "已驳回",
  "data": null
}
```

### 16.8 新增商业区

- **接口**: `POST /api/v1/admin/canteens`
- **描述**: 新增商业区
- **权限**: 管理员
- **请求参数**:

```json
{
  "campusId": 1,
  "name": "第一食堂",
  "type": "CANTEEN",
  "location": "校园东区",
  "description": "主营快餐",
  "openingHours": "06:00-21:00"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

### 16.9 更新商业区

- **接口**: `PUT /api/v1/admin/canteens/{id}`
- **描述**: 更新商业区信息
- **权限**: 管理员
- **路径参数**: `id` - 商业区ID
- **请求参数**:

```json
{
  "name": "更新后的名称",
  "location": "更新后的位置",
  "description": "更新后的描述",
  "openingHours": "更新后的营业时间"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 16.10 删除商业区

- **接口**: `DELETE /api/v1/admin/canteens/{id}`
- **描述**: 删除商业区
- **权限**: 管理员
- **路径参数**: `id` - 商业区ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 16.11 新增店铺

- **接口**: `POST /api/v1/admin/stalls`
- **描述**: 新增店铺
- **权限**: 管理员
- **请求参数**:

```json
{
  "canteenId": 1,
  "name": "快餐窗口",
  "location": "一楼1号",
  "description": "各类快餐"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10
  }
}
```

### 16.12 更新店铺

- **接口**: `PUT /api/v1/admin/stalls/{id}`
- **描述**: 更新店铺信息
- **权限**: 管理员
- **路径参数**: `id` - 店铺ID
- **请求参数**:

```json
{
  "name": "更新后的名称",
  "location": "更新后的位置",
  "description": "更新后的描述"
}
```

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 16.13 删除店铺

- **接口**: `DELETE /api/v1/admin/stalls/{id}`
- **描述**: 删除店铺
- **权限**: 管理员
- **路径参数**: `id` - 店铺ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 16.14 获取菜品列表(管理员)

- **接口**: `GET /api/v1/admin/dishes`
- **描述**: 获取菜品列表(管理员视角)
- **权限**: 管理员
- **查询参数**:
  - `merchantId`: 商家ID(可选)
  - `canteenId`: 商业区ID(可选)
  - `stallId`: 店铺ID(可选)
  - `status`: 状态(可选)
  - `keyword`: 搜索关键词(可选)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 100,
        "merchantId": 5,
        "merchantName": "张三快餐",
        "canteenId": 1,
        "canteenName": "第一食堂",
        "stallId": 10,
        "stallName": "快餐窗口",
        "categoryId": 1,
        "categoryName": "盖饭",
        "name": "黑椒鸡排饭",
        "price": 15.0,
        "coverImageUrl": "封面图URL",
        "averageScore": 4.5,
        "status": "ON_SALE",
        "isJoinLottery": true,
        "viewCount": 500,
        "favoriteCount": 120,
        "reviewCount": 45,
        "createdAt": "2026-04-24T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 16.15 获取菜品详情(管理员)

- **接口**: `GET /api/v1/admin/dishes/{id}`
- **描述**: 获取菜品详细信息(管理员视角)
- **权限**: 管理员
- **路径参数**: `id` - 菜品ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "merchantId": 5,
    "merchantName": "张三快餐",
    "merchantStatus": "OPEN",
    "merchantApplyStatus": "APPROVED",
    "canteenId": 1,
    "canteenName": "第一食堂",
    "stallId": 10,
    "stallName": "快餐窗口",
    "categoryId": 1,
    "categoryName": "盖饭",
    "name": "黑椒鸡排饭",
    "description": "现炸鸡排配米饭",
    "price": 15.0,
    "coverImageUrl": "封面图URL",
    "averageScore": 4.5,
    "tasteScore": 4.6,
    "portionScore": 4.3,
    "valueScore": 4.5,
    "status": "ON_SALE",
    "isJoinLottery": true,
    "viewCount": 500,
    "favoriteCount": 120,
    "reviewCount": 45,
    "createdAt": "2026-04-24T10:00:00",
    "updatedAt": "2026-04-24T10:00:00",
    "images": ["图片URL1", "图片URL2"],
    "tags": [{"id": 10, "name": "微辣"}, {"id": 11, "name": "管饱"}]
  }
}
```

### 16.16 更新菜品状态(管理员)

- **接口**: `PATCH /api/v1/admin/dishes/{id}/status`
- **描述**: 管理员更新菜品状态
- **权限**: 管理员
- **路径参数**: `id` - 菜品ID
- **请求参数**:

```json
{
  "status": "ON_SALE"
}
```

- **状态枚举**: `ON_SALE`(上架中)、`SOLD_OUT`(已售罄)、`OFF_SHELF`(已下架)、`PENDING`(待审核)

- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 16.17 获取评价列表(管理员)

- **接口**: `GET /api/v1/admin/reviews`
- **描述**: 获取所有评价列表
- **权限**: 管理员
- **查询参数**:
  - `targetType`: 目标类型(可选)
  - `status`: 状态(可选)
  - `keyword`: 搜索关键词(可选)
  - `page`: 页码(默认1)
  - `size`: 每页数量(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 16.18 删除违规评价

- **接口**: `DELETE /api/v1/admin/reviews/{id}`
- **描述**: 删除违规评价
- **权限**: 管理员
- **路径参数**: `id` - 评价ID
- **响应参数**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 16.19 获取平台统计数据

- **接口**: `GET /api/v1/admin/statistics/overview`
- **描述**: 获取平台整体统计数据
- **权限**: 管理员
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studentCount": 500,
    "merchantCount": 50,
    "dishCount": 800,
    "reviewCount": 2000,
    "todayLotteryCount": 1200,
    "todayActiveUsers": 300
  }
}
```

### 16.20 获取热门菜品排行

- **接口**: `GET /api/v1/admin/statistics/popular-dishes`
- **描述**: 获取热门菜品统计
- **权限**: 管理员
- **查询参数**:
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "dishId": 100,
      "dishName": "黑椒鸡排饭",
      "merchantName": "张三快餐",
      "viewCount": 5000,
      "favoriteCount": 200,
      "reviewCount": 100,
      "averageScore": 4.5
    }
  ]
}
```

### 16.21 获取热门商家排行

- **接口**: `GET /api/v1/admin/statistics/popular-merchants`
- **描述**: 获取热门商家统计
- **权限**: 管理员
- **查询参数**:
  - `limit`: 数量限制(默认10,最大50)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "merchantId": 5,
      "merchantName": "张三快餐",
      "canteenName": "第一食堂",
      "dishCount": 20,
      "viewCount": 8000,
      "favoriteCount": 500,
      "reviewCount": 200,
      "averageScore": 4.3
    }
  ]
}
```

### 16.22 获取用户活跃趋势

- **接口**: `GET /api/v1/admin/statistics/user-activity`
- **描述**: 获取用户活跃趋势数据
- **权限**: 管理员
- **查询参数**:
  - `startDate`: 开始日期(可选)
  - `endDate`: 结束日期(可选)
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2026-04-24",
      "activeUsers": 300,
      "lotteryCount": 1200,
      "reviewCount": 50
    }
  ]
}
```

### 16.23 获取各商业区评分统计

- **接口**: `GET /api/v1/admin/statistics/canteen-scores`
- **描述**: 获取各商业区(食堂、校内店铺、校园周边店铺)评分统计
- **权限**: 管理员
- **响应参数**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "canteenId": 1,
      "canteenName": "第一食堂",
      "canteenType": "CANTEEN",
      "averageScore": 4.5,
      "reviewCount": 500,
      "dishCount": 100
    }
  ]
}
```

---

## 十七、文件上传模块

### 17.1 上传图片

- **接口**: `POST /api/v1/files/upload-image`
- **描述**: 上传图片文件
- **权限**: 登录用户
- **请求参数**: `multipart/form-data`
  - `file`: 图片文件
  - `type`: 图片类型(可选) - `avatar`、`dish`、`review`、`merchant-logo`
- **响应参数**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "图片访问URL"
  }
}
```

---

## 附录:状态枚举说明

### 用户状态

- `ACTIVE`: 正常
- `DISABLED`: 已禁用

### 商业区类型

- `CANTEEN`: 食堂(包含多个窗口)
- `CAMPUS_SHOP`: 校内店铺(不属于食堂但在校园内)
- `PERIPHERY_SHOP`: 校园周边店铺(不在校园内)

### 商家申请状态

- `PENDING`: 待审核
- `APPROVED`: 已通过
- `REJECTED`: 已驳回

### 商家状态

- `OPEN`: 营业中
- `CLOSED`: 已关闭
- `PENDING`: 待审核

### 菜品状态

- `ON_SALE`: 上架中
- `SOLD_OUT`: 已售罄
- `OFF_SHELF`: 已下架
- `PENDING`: 待审核

### 推荐类型

- `TODAY`: 今日主推
- `NEW`: 新品
- `SPECIAL`: 特色
- `VALUE`: 高性价比
- `SIGNATURE`: 招牌

### 反馈类型

- `TASTE`: 口味
- `PORTION`: 分量
- `PRICE`: 价格
- `SERVICE`: 服务
- `HYGIENE`: 卫生
- `OTHER`: 其他

### 反馈状态

- `PENDING`: 待查看
- `VIEWED`: 已查看
- `ACCEPTED`: 已采纳
- `IMPROVED`: 已改进
- `REJECTED`: 暂不处理

### 抽奖结果行为

- `NONE`: 无操作
- `ACCEPT`: 接受
- `SKIP`: 跳过
- `FAVORITE`: 收藏

### 抽奖模式

- `RANDOM`: 随机抽奖
- `CONDITION`: 条件抽奖
- `FAVORITE`: 从收藏中抽

### 收藏类型

- `DISH`: 商家菜品
- `POST`: 学生分享

### 改进记录状态

- `DRAFT`: 草稿
- `PUBLISHED`: 已发布
- `ARCHIVED`: 已归档

### 想吃清单状态

- `WANT_TO_EAT`: 想吃
- `EATEN`: 已吃
- `CANCELLED`: 已取消
