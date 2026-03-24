package com.simon.lawrag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simon.lawrag.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
