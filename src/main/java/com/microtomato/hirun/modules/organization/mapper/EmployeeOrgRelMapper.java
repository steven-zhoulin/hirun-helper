package com.microtomato.hirun.modules.organization.mapper;

import com.microtomato.hirun.modules.organization.entity.po.EmployeeOrgRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microtomato.hirun.framework.annotation.Storage;
import com.microtomato.hirun.framework.mybatis.DataSourceKey;
import com.microtomato.hirun.framework.mybatis.annotation.DataSource;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jinnian
 * @since 2019-12-23
 */
@Storage
@DataSource(DataSourceKey.INS)
public interface EmployeeOrgRelMapper extends BaseMapper<EmployeeOrgRel> {

}
