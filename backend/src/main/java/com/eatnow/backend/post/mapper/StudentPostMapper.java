package com.eatnow.backend.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.post.entity.StudentPost;
import com.eatnow.backend.post.vo.PostImageRelationVo;
import com.eatnow.backend.post.vo.PostItemVo;
import com.eatnow.backend.post.vo.PostTagRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentPostMapper extends BaseMapper<StudentPost> {

    long countPosts(
            @Param("keyword") String keyword,
            @Param("canteenId") Long canteenId,
            @Param("stallId") Long stallId,
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId
    );

    List<PostItemVo> selectPostList(
            @Param("keyword") String keyword,
            @Param("canteenId") Long canteenId,
            @Param("stallId") Long stallId,
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId,
            @Param("sortBy") String sortBy,
            @Param("offset") long offset,
            @Param("size") int size
    );

    PostItemVo selectPostDetail(@Param("id") Long id);

    long countMyPosts(@Param("userId") Long userId);

    List<PostItemVo> selectMyPostList(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("size") int size
    );

    List<PostImageRelationVo> selectPostImages(@Param("postIds") List<Long> postIds);

    List<PostTagRelationVo> selectPostTags(@Param("postIds") List<Long> postIds);

    List<Long> selectLikedPostIds(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);

    Integer countOpenCanteenById(@Param("id") Long id);

    Integer countOpenStallById(@Param("id") Long id, @Param("canteenId") Long canteenId);

    Integer countActiveCategoryById(@Param("id") Long id);

    Integer countActiveTagsByIds(@Param("tagIds") List<Long> tagIds);

    int increaseViewCount(@Param("id") Long id);

    int increaseLikeCount(@Param("id") Long id);

    int decreaseLikeCount(@Param("id") Long id);

    int deletePostImages(@Param("postId") Long postId);

    int insertPostImages(@Param("postId") Long postId, @Param("images") List<String> images);

    int deletePostTags(@Param("postId") Long postId);

    int insertPostTags(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);

    int upsertPostLotteryPool(@Param("postId") Long postId);

    int updatePostLotteryPoolStatus(@Param("postId") Long postId, @Param("status") String status);
}
