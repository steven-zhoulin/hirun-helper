package com.microtomato.hirun.framework.harbour.excel.demo;

import com.alibaba.excel.context.AnalysisContext;
import com.microtomato.hirun.framework.harbour.excel.ExcelEventListener;
import com.microtomato.hirun.framework.util.SpringContextUtils;
import com.microtomato.hirun.framework.util.ValidationUtils;
import com.microtomato.hirun.modules.demo.entity.po.Steven;
import com.microtomato.hirun.modules.demo.service.IStevenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * @author Steven
 * @date 2019-10-30
 */
@Slf4j
public class UserReadListener extends ExcelEventListener<UserExcelImportDTO> {

    private IStevenService stevenServiceImpl = SpringContextUtils.getBean(IStevenService.class);

    /**
     * 每读取一行触发一次该函数
     *
     * @param data    excel 读过来的一行数据
     * @param context 上下文信息
     */
    @Override
    public void invoke(UserExcelImportDTO data, AnalysisContext context) {

        String checkResult = ValidationUtils.jsr303Check(data);
        if (null != checkResult) {
            /**
             * 业务校验失败时，亦可调用该函数，将异常数据收集起来。
             */
            addErrData(context, data, checkResult);
            log.error("{} 数据校验结果：{}", data, checkResult);
            return;
        }

        Steven steven = new Steven();
        BeanUtils.copyProperties(data, steven);
        try {
            stevenServiceImpl.save(steven);
        } catch (Exception e) {
            try {
                addErrData(context, data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            log.debug("数据 save 异常！{}", steven);
        }
    }

}