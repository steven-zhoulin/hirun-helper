package com.microtomato.hirun.modules.bss.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.microtomato.hirun.framework.exception.ErrorKind;
import com.microtomato.hirun.framework.exception.cases.AlreadyExistException;
import com.microtomato.hirun.framework.mybatis.DataSourceKey;
import com.microtomato.hirun.framework.mybatis.annotation.DataSource;
import com.microtomato.hirun.framework.security.UserContext;
import com.microtomato.hirun.framework.util.ArrayUtils;
import com.microtomato.hirun.framework.util.TimeUtils;
import com.microtomato.hirun.framework.util.WebContextUtils;
import com.microtomato.hirun.modules.bss.config.entity.po.PrepareConfig;
import com.microtomato.hirun.modules.bss.config.service.IPrepareConfigService;
import com.microtomato.hirun.modules.bss.customer.entity.dto.CustPreparationDTO;
import com.microtomato.hirun.modules.bss.customer.entity.po.CustBase;
import com.microtomato.hirun.modules.bss.customer.entity.po.CustPreparation;
import com.microtomato.hirun.modules.bss.customer.mapper.CustPreparationMapper;
import com.microtomato.hirun.modules.bss.customer.service.ICustBaseService;
import com.microtomato.hirun.modules.bss.customer.service.ICustPreparationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microtomato.hirun.modules.bss.house.service.IHousesService;
import com.microtomato.hirun.modules.organization.service.IEmployeeService;
import com.microtomato.hirun.modules.system.service.IStaticDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liuhui
 * @since 2020-02-01
 */
@Slf4j
@Service
public class CustPreparationServiceImpl extends ServiceImpl<CustPreparationMapper, CustPreparation> implements ICustPreparationService {

    @Autowired
    private ICustBaseService baseService;

    @Autowired
    private CustPreparationMapper preparationMapper;

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private IStaticDataService staticDataService;

    @Autowired
    private IHousesService housesService;

    @Autowired
    private IPrepareConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void addCustomerPreparation(CustPreparationDTO dto) {
        UserContext userContext = WebContextUtils.getUserContext();
        //校验报备的规则
        this.checkRules(dto);
        //当客户存在报备信息时，联系文员可以直接做报备信息保存，否则客户存在多次，需判断客户之前的报备信息是否失效，如果失效则客户选择继续报备

        //客户未做过报备，但是在客户表存在过数据，可以联系有权限的人员进行报备信息录入
        //houseId不一样判定报备失败

        CustBase custBase = new CustBase();
        CustPreparation preparation = new CustPreparation();
        BeanUtils.copyProperties(dto, preparation);
        BeanUtils.copyProperties(dto, custBase);
        //保存customer信息
        custBase.setCustStatus(0);
        baseService.save(custBase);
        //保存报备信息
        preparation.setCustId(custBase.getCustId());
        preparation.setStatus(1);
        preparation.setEnterEmployeeId(userContext.getEmployeeId());
        preparation.setEnterTime(LocalDateTime.now());
        preparation.setPreparationExpireTime(TimeUtils.addTime(dto.getPrepareTime(), ChronoUnit.DAYS, 5));
        this.preparationMapper.insert(preparation);

    }

    @Override
    public List<CustPreparationDTO> loadPreparationHistory(String mobileNo) {
        List<CustPreparationDTO> list = this.preparationMapper.loadPreparationHistory(mobileNo);
        if (list.size() <= 0) {
            return null;
        }
        for (CustPreparationDTO dto : list) {
            dto.setPrepareEmployeeName(employeeService.getEmployeeNameEmployeeId(dto.getPrepareEmployeeId()));
            dto.setEnterEmployeeName(employeeService.getEmployeeNameEmployeeId(dto.getEnterEmployeeId()));
            dto.setHouseAddress(housesService.queryHouseName(dto.getHouseId()));
            dto.setPreparationStatusName(staticDataService.getCodeName("PREPARATION_STATUS", dto.getStatus() + ""));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void customerRuling(CustPreparationDTO custPreparation) {
        UserContext userContext = WebContextUtils.getUserContext();
        CustPreparation addPreparation = new CustPreparation();
        CustPreparation updatePreparation = new CustPreparation();

        BeanUtils.copyProperties(custPreparation, addPreparation);
        //设置之前的报备信息失败
        updatePreparation.setStatus(3);
        this.baseMapper.update(updatePreparation, new UpdateWrapper<CustPreparation>().lambda()
                .eq(CustPreparation::getCustId, custPreparation.getCustId()).eq(CustPreparation::getStatus, 1));
        //新增主管裁定记录，设置客户属性为主管报备
        addPreparation.setCustProperty("6");
        addPreparation.setPrepareEmployeeId(userContext.getEmployeeId());
        addPreparation.setPrepareTime(LocalDateTime.now());
        addPreparation.setEnterTime(LocalDateTime.now());
        addPreparation.setEnterEmployeeId(userContext.getEmployeeId());
        addPreparation.setRulingTime(LocalDateTime.now());
        addPreparation.setStatus(2);
        this.baseMapper.insert(addPreparation);
    }

    @Override
    public List<CustPreparationDTO> queryCustPreparaton(String mobileNo, Long custId, String status, Long houseId, String isExpire) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(mobileNo), "a.mobile_no", mobileNo);
        queryWrapper.eq(custId != null, "a.cust_id", custId);
        queryWrapper.eq(StringUtils.isNotEmpty(status), "b.status", status);
        queryWrapper.eq(houseId!=null, "b.house_id", houseId);
        //2表示需要判断未过失效期
        queryWrapper.apply(StringUtils.equals(isExpire, "2"), "b.preparation_expire_time > now() ");

        List<CustPreparationDTO> list = this.baseMapper.queryCustomerPreparation(queryWrapper);


        if (ArrayUtils.isEmpty(list)) {
            return null;
        }
        for (CustPreparationDTO dto : list) {
            dto.setPrepareEmployeeName(employeeService.getEmployeeNameEmployeeId(dto.getPrepareEmployeeId()));
            dto.setEnterEmployeeName(employeeService.getEmployeeNameEmployeeId(dto.getEnterEmployeeId()));
            dto.setCustPropertyName(staticDataService.getCodeName("CUSTOMER_PROPERTY", dto.getCustProperty()));
        }
        return list;
    }

    private void checkRules(CustPreparationDTO dto){
        PrepareConfig prepareConfig=configService.queryValid();
        //客户申报状态为有效期内，不允许办理报备
        List<CustPreparationDTO> list = this.queryCustPreparaton(dto.getMobileNo(), null, "1", dto.getHouseId(), "2");
        if (ArrayUtils.isNotEmpty(list)) {
            throw new AlreadyExistException(" 该客户有效期内存在有效的报备！", ErrorKind.ALREADY_EXIST.getCode());
        }
        //限制报备客户周期范围的报备次数limitCycle限制周期limitTimes限制次数
        Integer limitTimes=prepareConfig.getLimitCustPrepareTimes();
        Integer limitCycle=prepareConfig.getLimitCustPrepareCycle();
        LocalDateTime limitTime=TimeUtils.addTime(LocalDateTime.now(), ChronoUnit.DAYS, -limitCycle);
        List<CustPreparationDTO> existList=this.preparationMapper.queryPrepareByTime(dto.getMobileNo(),limitTime);
        if(existList.size()>=limitTimes){
            throw new AlreadyExistException(" 该客户在"+limitCycle+"天内已超过限制报备次数"+limitTimes+"不允许报备！", ErrorKind.ALREADY_EXIST.getCode());
        }
        //限制部门报备次数
        if(!StringUtils.isEmpty(prepareConfig.getLimitPrepareOrgId())){
            String limitOrgId=","+prepareConfig.getLimitPrepareOrgId()+",";
            String prepareOrgId=","+dto.getPrepareOrgId()+",";
            if(limitOrgId.indexOf(prepareOrgId)!=-1){
                Integer limitOrgPrepareTimes=prepareConfig.getLimitOrgPrepareTimes();
                LocalDateTime limitTime1=TimeUtils.addTime(LocalDateTime.now(), ChronoUnit.DAYS, -7);
                List<CustPreparation> existList1=this.preparationMapper.selectList(new QueryWrapper<CustPreparation>().lambda()
                        .eq(CustPreparation::getPrepareOrgId,dto.getPrepareOrgId())
                        .ge(CustPreparation::getPrepareTime,limitTime1));
                if(existList1.size()>=limitOrgPrepareTimes){
                    throw new AlreadyExistException(" 该部门在过去的7天内已超过限制报备次数"+limitOrgPrepareTimes+"不允许报备！", ErrorKind.ALREADY_EXIST.getCode());
                }
            }
        }
        //todo 判断责任楼盘

    }

    public List<CustPreparation> queryPrepareByTime(String mobileNo,Integer limitCycle){
        List<CustPreparation> list=this.preparationMapper.selectList(new QueryWrapper<CustPreparation>().lambda());
        return list;
    }
}
