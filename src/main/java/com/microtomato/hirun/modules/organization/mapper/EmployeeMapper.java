package com.microtomato.hirun.modules.organization.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microtomato.hirun.framework.annotation.Storage;
import com.microtomato.hirun.framework.mybatis.DataSourceKey;
import com.microtomato.hirun.framework.mybatis.annotation.DataSource;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeeExampleDTO;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeeInfoDTO;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeePieStatisticDTO;
import com.microtomato.hirun.modules.organization.entity.dto.EmployeeQueryConditionDTO;
import com.microtomato.hirun.modules.organization.entity.po.Employee;
import com.microtomato.hirun.modules.organization.entity.po.StatEmployeeQuantityMonth;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuhui
 * @since 2019-09-24
 */
@Storage
@DataSource(DataSourceKey.INS)
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("select a.employee_id,a.user_id,a.name,a.sex,a.identity_no,a.birthday_type,a.birthday,a.mobile_no,a.home_prov,a.home_city,a.home_region,a.home_address,a.native_prov,a.native_city,a.native_region,a.native_address,a.in_date,a.regular_date,a.destroy_date,a.destroy_way,a.destroy_reason,a.destroy_times,a.job_date,a.work_nature,a.workplace,a.education_level,a.first_education_level,a.major,a.school,a.school_type,a.tech_title,a.certificate_no,a.before_hirun_year,a.status,b.job_role_id,b.employee_id,b.job_role,b.discount_rate,b.is_main,b.job_role_nature,b.org_id,b.parent_employee_id,c.name orgName from ins_employee a, ins_employee_job_role b, ins_org c \n" +
            "where (a.name like concat('%',#{text},'%') or a.mobile_no like concat('%',#{text},'%'))\n" +
            " and a.status = '0' \n" +
            " and b.employee_id = a.employee_id\n" +
            " and (now() between b.start_date and b.end_date) \n" +
            " and c.org_id = b.org_id")
    List<EmployeeInfoDTO> searchByNameMobileNo(String text);


    /**
     * 测试（后期删除）
     *
     * @param page
     * @param wrapper
     * @return
     */
    @Select("SELECT a.name, a.employee_id, a.sex, a.mobile_no, a.identity_no, a.status, date_format( a.in_date, '%Y-%m-%d' ) in_date, b.job_role, c.org_id, c.name org_name\n" +
        "FROM ins_employee a, ins_employee_job_role b, ins_org c\n" +
        "${ew.customSqlSegment}")
    IPage<EmployeeExampleDTO> selectEmployeePageExample(Page<EmployeeExampleDTO> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 查询员工档案信息
     * @param page
     * @param wrapper
     * @return
     */
    @Select("select a.name,a.employee_id,a.sex,a.mobile_no ,a.identity_no,a.status employee_status, date_format(a.in_date,'%Y-%m-%d') in_date," +
            " b.job_role,b.org_id, c.name org_name,a.type,a.job_date,b.parent_employee_id,b.job_role_nature,x.age, x.company_age,a.birthday,x.job_age,b.discount_rate from " +
            " ins_org c, ins_employee a " +
            " LEFT JOIN ( select * from ins_employee_job_role k where k.job_role_id in(select max(i.job_role_id) from (select * from ins_employee_job_role h order by h.start_date desc) i\n" +
            " group by i.employee_id)) b on (a.employee_id=b.employee_id) " +
            " LEFT JOIN (SELECT y.employee_id,TIMESTAMPDIFF(YEAR,y.birthday,NOW()) as age,TIMESTAMPDIFF(YEAR,y.in_date,NOW()) as company_age, TIMESTAMPDIFF(YEAR,y.job_date,NOW()) as job_age from ins_employee y ) x\n" +
            " on (a.employee_id=x.employee_id)" +
            " ${ew.customSqlSegment}"
    )
    IPage<EmployeeInfoDTO> selectEmployeePage(Page<EmployeeQueryConditionDTO> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 查询员工的下级员工
     */
    @Select("select a.employee_id,b.org_id from ins_employee a, ins_employee_job_role b " +
            " where a.employee_id=b.employee_id" +
            " and a.status='0'" +
            " and (now() between b.start_date and b.end_date)" +
            " and b.parent_employee_id=#{parentEmployeeId}")
    List<EmployeeInfoDTO> queryEmployeeByParentEmployeeId(Long parentEmployeeId);

    /**
     * 导出员工档案信息
     * @param wrapper
     * @return
     */
    @Select("select a.name,a.employee_id,a.sex,a.mobile_no ,a.identity_no,a.status employee_status, date_format(a.in_date,'%Y-%m-%d') in_date," +
            " b.job_role,b.org_id, c.name org_name,a.type,a.job_date,b.discount_rate,a.education_level,a.first_education_level,a.school_type," +
            " a.tech_title,a.native_prov,a.native_city,a.native_region,a.native_address,a.home_prov,a.home_city,a.home_region,a.home_address,b.parent_employee_id,b.job_role_nature,x.age, x.company_age,a.birthday,x.job_age from " +
            " ins_org c, ins_employee a " +
            " LEFT JOIN ( select * from ins_employee_job_role k where k.job_role_id in(select max(i.job_role_id) from (select * from ins_employee_job_role h order by h.start_date desc) i\n" +
            " group by i.employee_id)) b on (a.employee_id=b.employee_id) " +
            " LEFT JOIN (SELECT y.employee_id,TIMESTAMPDIFF(YEAR,y.birthday,NOW()) as age,TIMESTAMPDIFF(YEAR,y.in_date,NOW()) as company_age, TIMESTAMPDIFF(YEAR,y.job_date,NOW()) as job_age from ins_employee y ) x\n" +
            " on (a.employee_id=x.employee_id)\n" +
            " ${ew.customSqlSegment}"
    )
    List<EmployeeInfoDTO> queryEmployeeList(@Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 按性别统计员工信息
     * @return
     */
    @Select("select sex name, count(1) num from ins_employee where status = '0' group by sex ")
    List<EmployeePieStatisticDTO> countBySex();

    /**
     * 按年龄段统计员工信息
     * @return
     */
    @Select("select age name, count(*) num from (" +
            "select case when TIMESTAMPDIFF(YEAR,birthday,NOW()) between 0 and 20 then '0-20' " +
            "            when TIMESTAMPDIFF(YEAR,birthday,NOW()) between 21 and 30 then '21-30' " +
            "            when TIMESTAMPDIFF(YEAR,birthday,NOW()) between 31 and 40 then '31-40' " +
            "            when TIMESTAMPDIFF(YEAR,birthday,NOW()) between 41 and 50 then '41-50' " +
            "            when TIMESTAMPDIFF(YEAR,birthday,NOW()) between 51 and 60 then '51-60' " +
            "            when TIMESTAMPDIFF(YEAR,birthday,NOW()) > 60 then '60+' " +
            "       end as age " +
            "from ins_employee " +
            "where birthday is not null and status = '0') temp_employee " +
            "group by age " +
            "order by age asc")
    List<EmployeePieStatisticDTO> countByAge();

    /**
     * 按岗位性质统计员工信息
     * @return
     */
    @Select("select b.job_role_nature name, count(1) num from ins_employee a, ins_employee_job_role b where b.employee_id = a.employee_id and a.status = '0' and now() between b.start_date and b.end_date group by b.job_role_nature ")
    List<EmployeePieStatisticDTO> countByJobRoleNature();

    /**
     * 按司龄统计员工信息
     * @return
     */
    @Select("select company_age name, count(*) num from (" +
            "select case when TIMESTAMPDIFF(YEAR,in_date,NOW()) between 0 and 1 then '小于1年' " +
            "            when TIMESTAMPDIFF(YEAR,in_date,NOW()) between 1 and 3 then '1-3年' " +
            "            when TIMESTAMPDIFF(YEAR,in_date,NOW()) between 3.1 and 5 then '3-5年' " +
            "            when TIMESTAMPDIFF(YEAR,in_date,NOW()) between 5.1 and 10 then '5-10年' " +
            "            when TIMESTAMPDIFF(YEAR,in_date,NOW()) > 10 then '10年+' " +
            "       end as company_age " +
            "from ins_employee " +
            "where in_date is not null and status = '0') temp_employee " +
            "group by company_age " +
            "order by company_age asc")
    List<EmployeePieStatisticDTO> countByCompanyAge();

    /**
     * 按最高学历统计员工信息
     * @return
     */
    @Select("select education_level name, count(1) num from ins_employee where status = '0' group by education_level ")
    List<EmployeePieStatisticDTO> countByEducationLevel();

    /**
     * 按员工在职类型统计员工信息
     * @return
     */
    @Select("select type name, count(1) num from ins_employee where status = '0' group by type ")
    List<EmployeePieStatisticDTO> countByType();

    /**
     * 根据员工id查询员工有效信息
     * @param employeeId
     * @return
     */
    @Select("select a.employee_id,a.user_id,a.name,a.sex,a.identity_no,a.birthday_type,a.birthday,a.mobile_no,a.home_prov,a.home_city,a.home_region,a.home_address,a.native_prov,a.native_city,a.native_region,a.native_address,a.in_date,a.regular_date,a.destroy_date,a.destroy_way,a.destroy_reason,a.destroy_times,a.job_date,a.work_nature,a.workplace,a.education_level,a.first_education_level,a.major,a.school,a.school_type,a.tech_title,a.certificate_no,a.before_hirun_year,a.status,b.job_role_id,b.employee_id,b.job_role,b.discount_rate,b.is_main,b.job_role_nature,b.org_id,b.parent_employee_id,c.name orgName from ins_employee a, ins_employee_job_role b, ins_org c \n" +
            " where a.employee_id =#{employeeId} \n" +
            " and a.status = '0' \n" +
            " and b.employee_id = a.employee_id\n" +
            " and (now() between b.start_date and b.end_date) \n" +
            " and c.org_id = b.org_id")
    EmployeeInfoDTO queryEmployeeInfoByEmployeeId(Long employeeId);

    /**
     * 统计一年内员工的入职数
     * @return
     */
    @Select("select ifnull(b.num, 0) value from (\n" +
            "  SELECT date_format(date_add(now(), INTERVAL 0 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -1 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -2 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -3 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -4 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -5 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -6 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -7 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -8 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -9 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -10 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -11 MONTH), '%Y%m') AS month\n" +
            ") a left join (select date_format(in_date, '%Y%m') as indate, count(1) num from ins_employee where in_date >= concat(date_format(date_add(now(), interval -11 month), '%Y-%m'), '-01') group by indate) b\n" +
            "  on b.indate = a.month\n" +
            "order by month ")
    List<Integer> countInOneYear();

    /**
     * 统计一年内员工的离职数
     * @return
     */
    @Select("select ifnull(b.num, 0) value from (\n" +
            "  SELECT date_format(date_add(now(), INTERVAL 0 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -1 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -2 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -3 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -4 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -5 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -6 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -7 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -8 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -9 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -10 MONTH), '%Y%m') AS month\n" +
            "  UNION SELECT date_format(date_add(now(), INTERVAL -11 MONTH), '%Y%m') AS month\n" +
            ") a left join (select date_format(destroy_date, '%Y%m') as destroydate, count(1) num from ins_employee where destroy_date >= concat(date_format(date_add(now(), interval -11 month), '%Y-%m'), '-01') group by destroydate) b\n" +
            "  on b.destroydate = a.month\n" +
            "order by month ")
    List<Integer> countDestroyOneYear();

    /**
     * 按部门统计员工数量
     * @return
     */
    @Select("select d.org_id,IFNULL(c.employee_quantity,0) employee_quantity from ins_org d " +
            " LEFT JOIN (select b.org_id,count(1) employee_quantity from ins_employee a, ins_employee_job_role b " +
            " where a.employee_id=b.employee_id" +
            " and a.status='0' and now() BETWEEN b.start_date and b.end_date group by b.org_id) c " +
            " on d.org_id=c.org_id")
    List<StatEmployeeQuantityMonth> countEmployeeQuantityByOrgId();
}
