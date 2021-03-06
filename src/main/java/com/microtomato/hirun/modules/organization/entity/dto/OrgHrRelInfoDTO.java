package com.microtomato.hirun.modules.organization.entity.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @program: hirun-helper
 * @description: 部门人资关系传输对象
 * @author: liuhui7
 **/
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrgHrRelInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long Id;

    private Long orgId;

    private String orgPath;

    private Long archiveManagerEmployeeId;

    private String  archiveManagerEmployeeName;

    private Long relationManagerEmployeeId;

    private String relationManagerEmployeeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
