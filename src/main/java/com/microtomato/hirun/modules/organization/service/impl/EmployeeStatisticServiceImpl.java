package com.microtomato.hirun.modules.organization.service.impl;

import com.microtomato.hirun.framework.util.ArrayUtils;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeePieStatisticDTO;
import com.microtomato.hirun.modules.organization.mapper.EmployeeMapper;
import com.microtomato.hirun.modules.organization.service.IEmployeeStatisticService;
import com.microtomato.hirun.modules.system.service.IStaticDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: hirun-helper
 * @description: 员工图表服务实现类
 * @author: jinnian
 * @create: 2019-11-21 16:47
 **/
@Slf4j
@Service
public class EmployeeStatisticServiceImpl implements IEmployeeStatisticService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private IStaticDataService staticDataService;

    /**
     * 按性别统计员工数量
     * @return
     */
    @Override
    public List<EmployeePieStatisticDTO> countBySex() {
        List<EmployeePieStatisticDTO> sexStatistics = this.employeeMapper.countBySex();

        if (ArrayUtils.isNotEmpty(sexStatistics)) {
            for (EmployeePieStatisticDTO sexStatistic : sexStatistics) {
                sexStatistic.setName(this.staticDataService.getCodeName("SEX", sexStatistic.getName()) + ": " + sexStatistic.getNum());
            }
        }

        return sexStatistics;
    }

    /**
     * 按年龄段统计员工数量
     * @return
     */
    @Override
    public List<EmployeePieStatisticDTO> countByAge() {
        List<EmployeePieStatisticDTO> ageStatistics = this.employeeMapper.countByAge();

        if (ArrayUtils.isNotEmpty(ageStatistics)) {
            for (EmployeePieStatisticDTO ageStatistic : ageStatistics) {
                ageStatistic.setName(ageStatistic.getName() + ": " + ageStatistic.getNum());
            }
        }
        return ageStatistics;
    }

    @Override
    public List<EmployeePieStatisticDTO> countByJobRoleNature() {
        List<EmployeePieStatisticDTO> jobRoleNatureStatistics = this.employeeMapper.countByJobRoleNature();

        if (ArrayUtils.isNotEmpty(jobRoleNatureStatistics)) {
            for (EmployeePieStatisticDTO jobRoleNatureStatistic : jobRoleNatureStatistics) {
                if (StringUtils.isBlank(jobRoleNatureStatistic.getName())) {
                    jobRoleNatureStatistic.setName("无岗位性质: " + jobRoleNatureStatistic.getNum());
                } else {
                    jobRoleNatureStatistic.setName(this.staticDataService.getCodeName("JOB_NATURE", jobRoleNatureStatistic.getName())+": "+jobRoleNatureStatistic.getNum());
                }
            }
        }
        return jobRoleNatureStatistics;
    }

    @Override
    public List<EmployeePieStatisticDTO> countByCompanyAge() {
        List<EmployeePieStatisticDTO> ageStatistics = this.employeeMapper.countByCompanyAge();

        if (ArrayUtils.isNotEmpty(ageStatistics)) {
            for (EmployeePieStatisticDTO ageStatistic : ageStatistics) {
                ageStatistic.setName(ageStatistic.getName() + ": " + ageStatistic.getNum());
            }
        }
        return ageStatistics;
    }

    @Override
    public List<EmployeePieStatisticDTO> countByEducationLevel() {
        List<EmployeePieStatisticDTO> educationLevelStatistics = this.employeeMapper.countByEducationLevel();

        if (ArrayUtils.isNotEmpty(educationLevelStatistics)) {
            for (EmployeePieStatisticDTO educationLevelStatistic : educationLevelStatistics) {
                if (StringUtils.isBlank(educationLevelStatistic.getName())) {
                    educationLevelStatistic.setName("无学历: " + educationLevelStatistic.getNum());
                } else {
                    educationLevelStatistic.setName(this.staticDataService.getCodeName("EDUCATION_LEVEL", educationLevelStatistic.getName()) + ": " + educationLevelStatistic.getNum());
                }
            }
        }
        return educationLevelStatistics;
    }

    @Override
    public List<EmployeePieStatisticDTO> countByType() {
        List<EmployeePieStatisticDTO> typeStatistics = this.employeeMapper.countByType();

        if (ArrayUtils.isNotEmpty(typeStatistics)) {
            for (EmployeePieStatisticDTO typeStatistic : typeStatistics) {
                typeStatistic.setName(this.staticDataService.getCodeName("EMPLOYEE_TYPE", typeStatistic.getName()) + ": " + typeStatistic.getNum());
            }
        }
        return typeStatistics;
    }
}