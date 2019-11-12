package com.microtomato.hirun.modules.user.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microtomato.hirun.framework.annotation.Storage;
import com.microtomato.hirun.modules.user.entity.po.FuncRole;

/**
 * <p>
 * 角色权限关系表 Mapper 接口
 * </p>
 *
 * @author Steven
 * @since 2019-09-22
 */
@Storage
@DS("ins")
public interface FuncRoleMapper extends BaseMapper<FuncRole> {

}
