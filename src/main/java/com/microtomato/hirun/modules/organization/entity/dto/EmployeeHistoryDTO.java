package com.microtomato.hirun.modules.organization.entity.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @program: hirun-helper
 * @description: 员工公司历史数据传输对象
 * @author: jinnian
 * @create: 2019-11-17 13:19
 **/
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long employeeId;

    private LocalDate eventDate;

    private String eventType;

    private String eventTypeName;

    private String eventContent;
}
