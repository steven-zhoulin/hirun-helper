package com.microtomato.hirun.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microtomato.hirun.framework.annotation.Storage;
import com.microtomato.hirun.modules.system.entity.po.Notify;

/**
 * <p>
 * 消息表 Mapper 接口
 * </p>
 *
 * @author Steven
 * @since 2019-11-11
 */
@Storage
public interface NotifyMapper extends BaseMapper<Notify> {

}