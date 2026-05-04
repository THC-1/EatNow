package com.eatnow.backend.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.dish.entity.Tag;
import com.eatnow.backend.dish.vo.TagVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<TagVo> selectActiveTags(@Param("type") String type);
}
