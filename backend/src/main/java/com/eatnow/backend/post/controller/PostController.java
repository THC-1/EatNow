package com.eatnow.backend.post.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.post.dto.PostCreateRequest;
import com.eatnow.backend.post.service.PostService;
import com.eatnow.backend.post.vo.PostItemVo;
import com.eatnow.backend.post.vo.PostLikeCountVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<IdResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.success(new IdResponse(postService.createPost(request)));
    }

    @GetMapping
    public ApiResponse<PageResult<PostItemVo>> listPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) Long stallId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(postService.listPosts(keyword, canteenId, stallId, categoryId, tagId, sortBy, page, size));
    }

    @GetMapping("/me")
    public ApiResponse<PageResult<PostItemVo>> listMyPosts(
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(postService.listMyPosts(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostItemVo> getPostDetail(@PathVariable Long id) {
        return ApiResponse.success(postService.getPostDetail(id));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<PostLikeCountVo> likePost(@PathVariable Long id) {
        return ApiResponse.success(postService.likePost(id));
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<PostLikeCountVo> unlikePost(@PathVariable Long id) {
        return ApiResponse.success(postService.unlikePost(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success();
    }
}
