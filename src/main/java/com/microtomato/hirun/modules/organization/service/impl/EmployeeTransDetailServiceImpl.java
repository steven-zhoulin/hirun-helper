package com.microtomato.hirun.modules.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.microtomato.hirun.framework.exception.ErrorKind;
import com.microtomato.hirun.framework.exception.cases.AlreadyExistException;
import com.microtomato.hirun.framework.security.UserContext;
import com.microtomato.hirun.framework.util.ArrayUtils;
import com.microtomato.hirun.framework.util.SpringContextUtils;
import com.microtomato.hirun.framework.util.TimeUtils;
import com.microtomato.hirun.framework.util.WebContextUtils;
import com.microtomato.hirun.modules.organization.entity.consts.EmployeeConst;
import com.microtomato.hirun.modules.organization.entity.consts.HrPendingConst;
import com.microtomato.hirun.modules.organization.entity.domain.EmployeeDO;
import com.microtomato.hirun.modules.organization.entity.domain.OrgDO;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeeInfoDTO;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeeTransDetailDTO;
import com.microtomato.hirun.modules.organization.entity.po.*;
import com.microtomato.hirun.modules.organization.mapper.EmployeeTransDetailMapper;
import com.microtomato.hirun.modules.organization.mapper.HrPendingMapper;
import com.microtomato.hirun.modules.organization.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microtomato.hirun.modules.system.service.INotifyService;
import com.microtomato.hirun.modules.user.service.IUserRoleService;
import com.microtomato.hirun.modules.user.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liuhui
 * @since 2019-10-29
 */
@Slf4j
@Service
public class EmployeeTransDetailServiceImpl extends ServiceImpl<EmployeeTransDetailMapper, EmployeeTransDetail> implements IEmployeeTransDetailService {

    @Autowired
    EmployeeTransDetailMapper detailMapper;

    @Autowired
    private IEmployeeJobRoleService employeeJobRoleService;

    @Autowired
    private IHrPendingService hrPendingService;

    @Autowired
    private IStatEmployeeTransitionService transitionService;

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private INotifyService notifyService;

    @Autowired
    private IEmployeeHistoryService historyService;

    @Autowired
    private IUserRoleService userRoleService;

    @Autowired
    IUserService userService;

    @Autowired
    IOrgService orgService;

    @Autowired
    HrPendingMapper hrPendingMapper;

    @Override
    public EmployeeTransDetail queryPendingDetailById(Long id) {
        QueryWrapper<EmployeeTransDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rel_pending_id", id);
        EmployeeTransDetail employeeTransDetail = detailMapper.selectOne(queryWrapper);
        return employeeTransDetail;
    }

    @Override
    public List<EmployeeTransDetail> queryVaildTransDetail(String type) {
        QueryWrapper<EmployeeTransDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(type), "trans_type", type);
        queryWrapper.apply("now() between start_time and end_time ");
        return this.detailMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean confirmReturnDetail(EmployeeTransDetail transDetail) {
        Employee employee = new Employee();
        EmployeeJobRole employeeJobRole = new EmployeeJobRole();

        BeanUtils.copyProperties(transDetail, employeeJobRole);
        BeanUtils.copyProperties(transDetail, employee);
        //修改employee现居住地址
        EmployeeDO employeeDo = SpringContextUtils.getBean(EmployeeDO.class, transDetail.getEmployeeId());
        employeeDo.modify(employee, null, null, null);

        //结束原来生效的jobrole数据
        employeeDo.destroyJob(LocalDateTime.now());
        //新增员工jobRole数据
        employeeJobRole.setStartDate(LocalDateTime.now());
        employeeJobRole.setEndDate(TimeUtils.getForeverTime());
        employeeJobRole.setIsMain(EmployeeConst.JOB_ROLE_MAIN);
        employeeJobRoleService.save(employeeJobRole);

        //分配默认权限
        Org org = this.orgService.getById(transDetail.getOrgId());
        Employee originalEmployee = employeeService.getById(transDetail.getEmployeeId());

        this.userRoleService.switchRole(originalEmployee.getUserId(),transDetail.getOrgId(),
                transDetail.getJobRole(),org.getNature());

        //更新待办
        HrPending hrPending = new HrPending();
        hrPending.setId(transDetail.getRelPendingId());
        hrPending.setPendingStatus(HrPendingConst.PENDING_STATUS_2);

        hrPendingService.updateById(hrPending);

        //更新确认详细
        int result = detailMapper.updateById(transDetail);
        if (result > 0) {
            return true;
        }
        return false;
    }

    /**
     * 确定员工调动待办
     *
     * @param transDetail
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public boolean confirmTransPending(EmployeeTransDetailDTO transDetail) {
        UserContext userContext = WebContextUtils.getUserContext();
        boolean result=false;

        List<EmployeeInfoDTO> childEmployeeList = employeeService.findSubordinate(transDetail.getEmployeeId());
        if (childEmployeeList.size() > 0) {
            throw new AlreadyExistException("该员工下存在下属员工，请将下属员工转移之后，再进行调动。", ErrorKind.ALREADY_EXIST.getCode());
        }

        if(StringUtils.equals(transDetail.getTransType(),HrPendingConst.PENDING_TYPE_INSIDE_TRANS)){

            List<HrPending> pendingList = hrPendingService.queryVaildPendingByEmployeeId(transDetail.getEmployeeId());
            if (ArrayUtils.isNotEmpty(pendingList)) {
                throw new AlreadyExistException(" 该员工存在未处理的调动待办任务，请处理后再新增.", ErrorKind.ALREADY_EXIST.getCode());
            }

            //判断员工是否有在生效的借调记录
            List<HrPending> effectList = hrPendingService.queryEffectBorrowPendingByEmployeeId(transDetail.getEmployeeId());
            if (effectList.size() > 0) {
                throw new AlreadyExistException(" 该员工存在正在生效的借调记录，请处理后再进行内部调动.", ErrorKind.ALREADY_EXIST.getCode());
            }

            //如果是内部调动需要设置开始时间与结束时间
            transDetail.setStartTime(transDetail.getStartTime());
            transDetail.setEndTime(TimeUtils.getForeverTime());
        }

        Employee employee = new Employee();
        EmployeeJobRole employeeJobRole = new EmployeeJobRole();
        EmployeeTransDetail employeeTransDetail = new EmployeeTransDetail();
        HrPending hrPending = new HrPending();
        //根据传入dto复制属性值
        BeanUtils.copyProperties(transDetail, employeeJobRole);
        BeanUtils.copyProperties(transDetail, employee);
        BeanUtils.copyProperties(transDetail, employeeTransDetail);
        BeanUtils.copyProperties(transDetail, hrPending);

        //查询未更新前的员工居住地址信息，为了拼装完整的调动记录
        Employee originalEmployee = employeeService.getById(transDetail.getEmployeeId());
        employeeTransDetail.setSourceHomeAddress(originalEmployee.getHomeAddress());
        employeeTransDetail.setSourceHomeProv(originalEmployee.getHomeProv());
        employeeTransDetail.setSourceHomeCity(originalEmployee.getHomeCity());
        employeeTransDetail.setSourceHomeRegion(originalEmployee.getHomeRegion());
        //修改员工现居住地址
        EmployeeDO employeeDo = SpringContextUtils.getBean(EmployeeDO.class, employee);
        employeeDo.modify(employee, null, null, null);
        //查询出未更新前的员工jobrole数据，拼装完整调动记录
        EmployeeJobRole validEmployeeJobRole = employeeJobRoleService.queryValidMain(transDetail.getEmployeeId());

        employeeTransDetail.setSourceDiscountRate(validEmployeeJobRole.getDiscountRate());
        employeeTransDetail.setSourceJobRoleNature(validEmployeeJobRole.getJobRoleNature());
        employeeTransDetail.setSourceParentEmployeeId(validEmployeeJobRole.getParentEmployeeId());
        employeeTransDetail.setSourceJobRole(validEmployeeJobRole.getJobRole());
        employeeTransDetail.setSourceOrgId(validEmployeeJobRole.getOrgId());
        employeeTransDetail.setSourceJobGrade(validEmployeeJobRole.getJobGrade());
        //结束原来生效的jobrole数据
        employeeDo.destroyJob(transDetail.getStartTime());
        //新增员工jobRole数据
        employeeJobRole.setIsMain(validEmployeeJobRole.getIsMain());
        employeeJobRole.setRemark(validEmployeeJobRole.getRemark());
        employeeJobRole.setStartDate(transDetail.getStartTime());
        employeeJobRole.setEndDate(TimeUtils.getForeverTime());

        //借调同样走借出流程
        employeeJobRoleService.save(employeeJobRole);
        //拼装调动记录的其他数据
        if(StringUtils.equals(transDetail.getTransType(),HrPendingConst.PENDING_TYPE_INSIDE_TRANS)){
            HrPending insideTransPending=new HrPending();
            insideTransPending.setEmployeeId(transDetail.getEmployeeId());
            insideTransPending.setPendingType(HrPendingConst.PENDING_TYPE_INSIDE_TRANS);
            insideTransPending.setStartTime(transDetail.getStartTime());
            insideTransPending.setEndTime(TimeUtils.getForeverTime());
            insideTransPending.setPendingStatus(HrPendingConst.PENDING_STATUS_2);
            insideTransPending.setPendingExecuteId(userContext.getEmployeeId());
            insideTransPending.setPendingCreateId(userContext.getEmployeeId());
            hrPendingMapper.insert(insideTransPending);
            employeeTransDetail.setRelPendingId(insideTransPending.getId());
            this.save(employeeTransDetail);
        }else{
            employeeTransDetail.setRelPendingId(transDetail.getId());
            this.save(employeeTransDetail);
            //更新待办数据
            hrPending.setPendingStatus(HrPendingConst.PENDING_STATUS_2);
             result = hrPendingService.updateById(hrPending);
        }


        //分配默认权限
        Org org = this.orgService.getById(transDetail.getOrgId());

        this.userRoleService.switchRole(originalEmployee.getUserId(),transDetail.getOrgId(),
                transDetail.getJobRole(),org.getNature());

        //新增部门异动信息
        if (StringUtils.equals(transDetail.getTransType(), "1")) {
            transitionService.addEmployeeBorrowTransition(transDetail.getOrgId(), validEmployeeJobRole.getOrgId(), transDetail.getEmployeeId(),
                    transDetail.getStartTime().toLocalDate(),validEmployeeJobRole.getJobRole(),
                    validEmployeeJobRole.getJobRoleNature(),validEmployeeJobRole.getJobGrade());
        }
        if (StringUtils.equals(transDetail.getTransType(), "2")
                ||StringUtils.equals(transDetail.getTransType(), HrPendingConst.PENDING_TYPE_INSIDE_TRANS)) {
            transitionService.addEmployeeTransTransition(transDetail.getOrgId(), validEmployeeJobRole.getOrgId(), transDetail.getEmployeeId(),
                    transDetail.getStartTime().toLocalDate(),validEmployeeJobRole.getJobRole(),validEmployeeJobRole.getJobRoleNature(),
                    validEmployeeJobRole.getJobGrade());
        }

        //新增鸿扬经历信息
        OrgDO targetOrgDO = SpringContextUtils.getBean(OrgDO.class, transDetail.getOrgId());
        OrgDO sourceOrgDO = SpringContextUtils.getBean(OrgDO.class, validEmployeeJobRole.getOrgId());
        String historyContent = "";

        if (StringUtils.equals(transDetail.getTransType(), HrPendingConst.PENDING_TYPE_TRANS)
                ||StringUtils.equals(transDetail.getTransType(), HrPendingConst.PENDING_TYPE_INSIDE_TRANS)) {
            historyContent = "【" + sourceOrgDO.getOrg().getName() + "】调出到【" + targetOrgDO.getOrg().getName() + "】";
        }
        if (StringUtils.equals(transDetail.getTransType(), HrPendingConst.PENDING_TYPE_BORROW)) {
            historyContent = "【" + sourceOrgDO.getOrg().getName() + "】借调到【" + targetOrgDO.getOrg().getName() + "】";
        }
        historyService.createTrans(transDetail.getEmployeeId(), transDetail.getStartTime().toLocalDate(), historyContent, HrPendingConst.PENDING_TYPE_TRANS);

        //调动状态为调出，则发送消息提醒重签合同
        if (StringUtils.equals(transDetail.getTransType(), HrPendingConst.PENDING_TYPE_TRANS)) {
            String content = employeeService.getEmployeeNameEmployeeId(transDetail.getEmployeeId()) +
                    "，已确认调出,请签订变更协议！";
            notifyService.sendMessage(userContext.getEmployeeId(), content);
        }

        return result;
    }
}
