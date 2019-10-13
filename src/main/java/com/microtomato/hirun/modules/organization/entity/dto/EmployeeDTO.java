package com.microtomato.hirun.modules.organization.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * @program: hirun-helper
 * @description: 员工数据传输对象
 * @author: jinnian
 * @create: 2019-10-05 01:44
 **/
@Data
public class EmployeeDTO {

    private int employeeId;

    private String name;

    private String mobileNo;

    private String identityNo;

    private String sex;

    private String birthdayType;

    private String birthday;

    private String natives;

    private String nativeProv;

    private String nativeCity;

    private String nativeRegion;

    private String nativeAddress;

    private String home;

    private String homeProv;

    private String homeCity;

    private String homeRegion;

    private String homeAddress;

    private String inDate;

    private String regularDate;

    private String workNature;

    private String discountRate;

    private String firstEducationLevel;

    private String highestEducationLevel;

    private String schoolType;

    private String school;

    private String major;

    private String certificateNo;

    private String techTitle;

    private String jobDate;

    private EmployeeJobRoleDTO employeeJobRole;

    private List<EmployeeWorkExperienceDTO> employeeWorkExperiences;
}