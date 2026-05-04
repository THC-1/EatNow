package com.eatnow.backend.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.user.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT id FROM sys_role WHERE role_code = #{roleCode} LIMIT 1")
    Long selectIdByRoleCode(@Param("roleCode") String roleCode);
}
