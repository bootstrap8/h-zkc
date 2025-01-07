package com.github.hbq969.code.zkc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.InitScriptUtils;
import com.github.hbq969.code.zkc.config.ZkcProperties;
import com.github.hbq969.code.zkc.service.InitialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author : hbq969@gmail.com
 * @description : 初始化业务类
 * @createTime : 2025/1/2 10:35
 */
@Service("zkc-InitialServiceImpl")
@Slf4j
public class InitialServiceImpl implements InitialService, InitializingBean {

    @Autowired
    private SpringContext context;

    @Autowired
    private ZkcProperties conf;

    @Override
    public void afterPropertiesSet() throws Exception {
        String profile = StringUtils.equals("oracle", conf.getDialect()) ? "oracle" :
                StringUtils.equals("embedded", conf.getDialect()) ? null : "mysql";
        log.info("zkc.dialect: {}", conf.getDialect());
        InitScriptUtils.initial(context, "/", "zkc-schema.sql", profile, StandardCharsets.UTF_8, null);
        InitScriptUtils.initial(context, "/", "zkc-data.sql", null, StandardCharsets.UTF_8, null);
    }
}
