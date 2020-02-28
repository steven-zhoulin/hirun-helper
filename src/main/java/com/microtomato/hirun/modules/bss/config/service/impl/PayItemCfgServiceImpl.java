package com.microtomato.hirun.modules.bss.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microtomato.hirun.framework.util.ArrayUtils;
import com.microtomato.hirun.framework.util.SpringContextUtils;
import com.microtomato.hirun.modules.bss.config.entity.po.PayItemCfg;
import com.microtomato.hirun.modules.bss.config.mapper.PayItemCfgMapper;
import com.microtomato.hirun.modules.bss.config.service.IPayItemCfgService;
import com.microtomato.hirun.modules.system.service.IStaticDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 收款项配置表 服务实现类
 * </p>
 *
 * @author jinnian
 * @since 2020-02-23
 */
@Slf4j
@Service
public class PayItemCfgServiceImpl extends ServiceImpl<PayItemCfgMapper, PayItemCfg> implements IPayItemCfgService {

    @Autowired
    private IStaticDataService staticDataService;

    /**
     * 查询所有付款项配置
     * @return
     */
    @Override
    @Cacheable(value = "payitemcfg-all")
    public List<PayItemCfg> queryAll() {
        List<PayItemCfg> configs = this.list(new QueryWrapper<PayItemCfg>().lambda().eq(PayItemCfg::getStatus, "U"));
        return configs;
    }

    /**
     * 根据付款项ID查询付款项配置
     * @param payItemId
     * @return
     */
    @Cacheable(value = "payitemcfg-with-payitemid")
    @Override
    public PayItemCfg getPayItem(Long payItemId) {
        IPayItemCfgService payItemCfgService = SpringContextUtils.getBean(PayItemCfgServiceImpl.class);
        List<PayItemCfg> configs = payItemCfgService.queryAll();

        if (ArrayUtils.isEmpty(configs)) {
            return null;
        }

        for (PayItemCfg config : configs) {
            if (payItemId.equals(config.getId())) {
                return config;
            }
        }
        return null;
    }

    @Cacheable(value = "payitemcfg-plus-all")
    @Override
    public List<PayItemCfg> queryPlusPayItems() {
        IPayItemCfgService payItemCfgService = SpringContextUtils.getBean(PayItemCfgServiceImpl.class);
        List<PayItemCfg> configs = payItemCfgService.queryAll();

        if (ArrayUtils.isEmpty(configs)) {
            return null;
        }

        List<PayItemCfg> result = new ArrayList<>();
        for (PayItemCfg config : configs) {
            if (config.getDirection().equals(new Integer(0))) {
                result.add(config);
            }
        }
        return result;
    }
}
