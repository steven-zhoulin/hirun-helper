package com.microtomato.hirun.modules.bss.order.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.microtomato.hirun.framework.data.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单支付项明细表(OrderPayItem)表实体类
 *
 * @author Jinnian
 * @version 1.0.0
 * @date 2020-02-26 15:56:11
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_pay_item")
public class OrderPayItem extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    @TableField(value = "order_id")
    private Long orderId;

    /** 付款编码 */
    @TableField(value = "pay_no")
    private Long payNo;

    /** 收款项编码，见参数sys_pay_item_cfg */
    @TableField(value = "pay_item_id")
    private Long payItemId;

    /** 上级费用项编码，见参数sys_pay_item_cfg */
    @TableField(value = "parent_pay_item_id")
    private Long parentPayItemId;

    /** 期数 */
    @TableField(value = "periods")
    private Integer periods;

    /** 金额 */
    @TableField(value = "fee")
    private Long fee;


    @TableField(value = "pay_date")
    private LocalDate payDate;


    @TableField(value = "audit_employee_id")
    private Long auditEmployeeId;

    /** 0-未审核 1-审核通过 2-审核不通过 */
    @TableField(value = "audit_status")
    private String auditStatus;

    /** 备注 */
    @TableField(value = "remark")
    private String remark;

    /** 处理员工 */
    @TableField(value = "pay_employee_id")
    private Long payEmployeeId;


    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;


    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}