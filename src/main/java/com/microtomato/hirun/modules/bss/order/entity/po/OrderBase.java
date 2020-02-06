package com.microtomato.hirun.modules.bss.order.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.microtomato.hirun.framework.data.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单主表
 * </p>
 *
 * @author jinnian
 * @since 2020-02-03
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderBase extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @TableField("cust_id")
    private Long custId;

    /**
     * 0-预订单 1-转入咨询阶段后的正式订单
     */
    @TableField("type")
    private String type;

    /**
     * 楼盘ID
     */
    @TableField("houses_id")
    private Long housesId;

    /**
     * 装修具体地址
     */
    @TableField("decorate_address")
    private String decorateAddress;

    /**
     * 户型，见静态参数HOUSE_LAYOUT
     */
    @TableField("house_layout")
    private String houseLayout;

    /**
     * 建筑面积
     */
    @TableField("floorage")
    private String floorage;

    /**
     * 套内面积
     */
    @TableField("indoor_area")
    private String indoorArea;

    /**
     * 上一订单阶段，对应0-酝酿，10-初选，30-初步决策，50-决策，60-施工，95-维护
     */
    @TableField("stage")
    private Integer previousStage;

    /**
     * 上一订单状态，见静态参数ORDER_STATUS
     */
    @TableField("status")
    private String previousStatus;

    /**
     * 订单阶段，对应0-酝酿，10-初选，30-初步决策，50-决策，60-施工，95-维护
     */
    @TableField("stage")
    private Integer stage;

    /**
     * 订单状态，见静态参数ORDER_STATUS
     */
    @TableField("status")
    private String status;

    /**
     * 订单结束时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /**
     * 风格主题ID
     */
    @TableField("style_id")
    private Long styleId;

    /**
     * 功能蓝图内容
     */
    @TableField("func_content")
    private String funcContent;

    /**
     * 施工开始时间
     */
    @TableField("project_start_date")
    private LocalDate projectStartDate;

    /**
     * 施工结束时间
     */
    @TableField("project_finish_date")
    private LocalDate projectFinishDate;

    /**
     * 合同总金额
     */
    @TableField("contract_fee")
    private Integer contractFee;

    /**
     * 总应收费用
     */
    @TableField("total_fee")
    private Integer totalFee;

    /**
     * 总实收费用
     */
    @TableField("total_act_fee")
    private Integer totalActFee;

    /**
     * 参与的活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
