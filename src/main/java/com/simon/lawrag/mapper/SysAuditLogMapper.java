package com.simon.lawrag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simon.lawrag.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
