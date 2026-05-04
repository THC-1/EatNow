package com.eatnow.backend.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.dish.entity.Category;
import com.eatnow.backend.dish.vo.CategoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<CategoryVo> selectActiveCategories(@Param("type") String type);
}
