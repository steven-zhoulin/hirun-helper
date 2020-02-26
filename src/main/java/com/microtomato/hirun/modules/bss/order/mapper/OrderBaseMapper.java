package com.microtomato.hirun.modules.bss.order.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microtomato.hirun.framework.annotation.Storage;
import com.microtomato.hirun.framework.mybatis.DataSourceKey;
import com.microtomato.hirun.framework.mybatis.annotation.DataSource;
import com.microtomato.hirun.modules.bss.order.entity.dto.CustOrderInfoDTO;
import com.microtomato.hirun.modules.bss.order.entity.dto.CustOrderQueryDTO;
import com.microtomato.hirun.modules.bss.order.entity.dto.PendingOrderDTO;
import com.microtomato.hirun.modules.bss.order.entity.po.OrderBase;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * <p>
 * 订单主表 Mapper 接口
 * </p>
 *
 * @author jinnian
 * @since 2020-02-03
 */
@Storage
@DataSource(DataSourceKey.INS)
public interface OrderBaseMapper extends BaseMapper<OrderBase> {

    @Select("select a.cust_id, a.order_id, a.status,a.decorate_address, b.cust_name, b.mobile_no, a.create_time from order_base a, cust_base b, (select distinct x.order_id from order_worker x, order_base y where y.order_id = x.order_id and x.employee_id = #{employeeId} and y.status in (${statuses})) c " +
            " where b.cust_id = a.cust_id and a.order_id = c.order_id and a.status in (${statuses}) " +
            " order by status, create_time desc;"
    )
    List<PendingOrderDTO> queryAttentionStatusOrders(@Param("statuses")String statuses, @Param("employeeId") Long employeeId);

    @Select("select a.order_id, a.cust_id, a.houses_id, a.decorate_address, a.house_layout, a.floorage, a.indoor_area, a.stage,a. status, b.cust_name, b.mobile_no, b.sex from order_base a, cust_base b " +
            "where b.cust_id = a.cust_id " +
            "${ew.customSqlSegment}")
    IPage<CustOrderInfoDTO> queryCustOrderInfo(Page<CustOrderQueryDTO> queryCondition, @Param(Constants.WRAPPER) Wrapper wrapper);
}