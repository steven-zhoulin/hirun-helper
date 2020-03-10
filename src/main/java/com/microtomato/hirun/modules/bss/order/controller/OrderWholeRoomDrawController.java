package com.microtomato.hirun.modules.bss.order.controller;

/**
 * @author ：mmzs
 * @date ：Created in 2020/2/6 22:49
 * @description：订单全房图流程控制
 * @modified By：
 * @version: 1$
 */

import com.microtomato.hirun.framework.annotation.RestResult;
import com.microtomato.hirun.framework.security.UserContext;
import com.microtomato.hirun.framework.util.WebContextUtils;
import com.microtomato.hirun.modules.bss.order.entity.dto.OrderPlaneSketchDTO;
import com.microtomato.hirun.modules.bss.order.entity.dto.OrderWholeRoomDrawDTO;
import com.microtomato.hirun.modules.bss.order.entity.po.OrderPlaneSketch;
import com.microtomato.hirun.modules.bss.order.entity.po.OrderWholeRoomDraw;
import com.microtomato.hirun.modules.bss.order.service.IOrderWholeRoomDrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/bss.order/order-wholeRoomDrawing")
public class OrderWholeRoomDrawController {
    @Autowired
    private IOrderWholeRoomDrawService iOrderWholeRoomDrawService;

    @PostMapping("/submitWholeRoomDrawing")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submit(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.save(orderWholeRoomDraw);
    }

    @GetMapping("/getWholeRoomDraw")
    @RestResult
    public OrderWholeRoomDrawDTO getWholeRoomDraw(Long orderId) {
        UserContext userContext = WebContextUtils.getUserContext();
        Long employeeId = userContext.getEmployeeId();
        OrderWholeRoomDraw orderWholeRoomDraw =  iOrderWholeRoomDrawService.getOrderWholeRoomDrawByOrderId(orderId);
        OrderWholeRoomDrawDTO orderWholeRoomDrawDTO = new OrderWholeRoomDrawDTO();
        if (orderWholeRoomDraw != null) {
            BeanUtils.copyProperties(orderWholeRoomDraw,orderWholeRoomDrawDTO);
        }
        orderWholeRoomDrawDTO.setDesigner(employeeId);
        return orderWholeRoomDrawDTO;
    }

    @PostMapping("/submitToAuditPicturesFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToDelayTimeFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToAuditPicturesFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToConfirmFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToConfirmFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToConfirmFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitCancelDesignExpensesFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitCancelDesignExpensesFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitCancelDesignExpensesFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToWholeRoomDelayTimeFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToWholeRoomDelayTimeFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToWholeRoomDelayTimeFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToCustomerLeaderFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToCustomerLeaderFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToCustomerLeaderFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToBackWholeRoomFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToBackWholeRoomFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToBackWholeRoomFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToTwoLevelActuarialCalculationFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToTwoLevelActuarialCalculationFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToTwoLevelActuarialCalculationFlow(orderWholeRoomDraw.getOrderId());
    }

    @PostMapping("/submitToBackToDesignerFlow")
    @Transactional(rollbackFor = Exception.class)
    @RestResult
    public void submitToBackToDesignerFlow(@RequestBody OrderWholeRoomDraw orderWholeRoomDraw) {
        iOrderWholeRoomDrawService.submitToBackToDesignerFlow(orderWholeRoomDraw.getOrderId());
    }

}
