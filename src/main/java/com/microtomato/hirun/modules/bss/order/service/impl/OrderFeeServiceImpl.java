package com.microtomato.hirun.modules.bss.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microtomato.hirun.framework.util.ArrayUtils;
import com.microtomato.hirun.framework.util.WebContextUtils;
import com.microtomato.hirun.modules.bss.customer.entity.po.CustBase;
import com.microtomato.hirun.modules.bss.order.entity.consts.OrderConst;
import com.microtomato.hirun.modules.bss.order.entity.dto.OrderFeeDTO;
import com.microtomato.hirun.modules.bss.order.entity.dto.OrderWorkerDTO;
import com.microtomato.hirun.modules.bss.order.entity.dto.SecondInstallmentCollectionDTO;
import com.microtomato.hirun.modules.bss.order.entity.po.OrderFee;
import com.microtomato.hirun.modules.bss.order.entity.po.OrderPayNo;
import com.microtomato.hirun.modules.bss.order.mapper.OrderFeeMapper;
import com.microtomato.hirun.modules.bss.order.mapper.OrderPayNoMapper;
import com.microtomato.hirun.modules.bss.order.service.IOrderDomainService;
import com.microtomato.hirun.modules.bss.order.service.IOrderFeeService;
import com.microtomato.hirun.modules.bss.order.service.IOrderPayNoService;
import com.microtomato.hirun.modules.bss.order.service.IOrderWorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单费用表 服务实现类
 * </p>
 *
 * @author sunxin
 * @since 2020-02-05
 */
@Slf4j
@Service
public class OrderFeeServiceImpl extends ServiceImpl<OrderFeeMapper, OrderFee> implements IOrderFeeService {

    @Autowired
    private IOrderWorkerService workerService;

    @Autowired
    private IOrderDomainService orderDomainService;

    @Autowired
    private IOrderPayNoService OrderPayNoService;


    @Override
    public OrderFee queryOrderCollectFee(Long orderId) {
        OrderFee orderFee = null;
        orderFee = this.baseMapper.selectOne(new QueryWrapper<OrderFee>().lambda()
                .eq(OrderFee::getOrderId, orderId));

        if (orderFee == null) {
            List<OrderWorkerDTO> workerList = workerService.queryByOrderId(orderId);
            if (ArrayUtils.isEmpty(workerList)) {
                return null;
            }

            orderFee = new OrderFee();
            for (OrderWorkerDTO dto : workerList) {
                if (dto.getRoleId().equals(15L)) {
                    //  OrderFee.setCustServiceEmployeeId(dto.getEmployeeId());
                } else if (dto.getRoleId().equals(30L)) {
                    //  OrderFee.setDesignEmployeeId(dto.getEmployeeId());
                }
            }
        }
        return orderFee;
    }
    /**
     * 财务审核
     *
     * @param dto
     */

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void submitAudit(OrderFeeDTO dto) {
       System.out.println("OrderFeeDTO=========="+dto);
        String auditStatus =dto.getAuditStatus();//1是审核通过，2是审核不通过

        if(auditStatus.equals("1")){
            orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_NEXT_STEP);
        }
        else
            orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_AUDIT_NO);

        //如果需要流转到指定人，才需要处理worker记录 首期款需要选择工程文员
        String orderStatus =dto.getOrderStatus();//判断当前状态，处理worker表
        if(orderStatus.equals("18")&&auditStatus.equals("1")){
            System.out.println("进来处理worker");
            workerService.updateOrderWorker(dto.getOrderId(), 32L,dto.getEngineeringClerk());
        }
       //
    }

    /**
     * 财务复核
     *
     * @param orderPayNo
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void costReview(OrderPayNo orderPayNo) {
        System.out.println("orderPayNo============" + orderPayNo);
        Long employeeId = WebContextUtils.getUserContext().getEmployeeId();
//        OrderPayNoService.update(new UpdateWrapper<OrderPayNo>().lambda().eq(OrderPayNo::getPayNo, orderPayNo.getPayNo()).gt(OrderPayNo::getEndDate, LocalDateTime.now()).set(OrderPayNo::getAuditStatus, orderPayNo.getAuditStatus()).set(OrderPayNo::getAuditEmployeeId, employeeId).set(OrderPayNo::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 工程文员提交项目经理审核
     *
     * @param dto
     */

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void submitTask(OrderFeeDTO dto) {
         System.out.println("OrderFeeDTO=========="+dto);
        orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_NEXT_STEP);
        //如果需要流转到指定人，才需要处理worker记录，流转到角色33项目经理
        workerService.updateOrderWorker(dto.getOrderId(), 33L,dto.getProjectManager());


    }

    /**
     * 项目经理审核
     *
     * @param dto
     */

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void submitAuditProject(OrderFeeDTO dto) {
        System.out.println("OrderFeeDTO=========="+dto);
        String auditStatus =dto.getAuditStatus();//1是审核通过，2是审核不通过

        if(auditStatus.equals("1")){
            orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_NEXT_STEP);
            //如果需要流转到指定人，才需要处理worker记录 流转到角色31项目助理
            workerService.updateOrderWorker(dto.getOrderId(), 31L,dto.getEngineeringAssistant());
        }
        else
            orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_AUDIT_NO);

    }

    /**
     * 开工交底
     *
     * @param dto
     */

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void submitAssignment(OrderFeeDTO dto) {
        System.out.println("OrderFeeDTO=========="+dto);
            orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_NEXT_STEP);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void secondInstallmentCollect(SecondInstallmentCollectionDTO dto) {
        orderDomainService.orderStatusTrans(dto.getOrderId(), OrderConst.OPER_NEXT_STEP);

        workerService.updateOrderWorker(dto.getOrderId(), 35L, dto.getFinanceEmployeeId());
    }
}
