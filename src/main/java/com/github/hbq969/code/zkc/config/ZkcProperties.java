package com.github.hbq969.code.zkc.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : hbq969@gmail.com
 * @description : 配置类
 * @createTime : 2025/1/2 10:38
 */
@ConfigurationProperties(prefix = "zkc")
@Component
@Data
public class ZkcProperties {
    /**
     * zookeeper集群地址
     */
    private List<String> urls;

    /**
     * 是否启用写入zk数据添加acl认证，默认：false
     */
    private boolean enableWriteAcl = false;

    /**
     * 认证方式，默认
     */
    private String authSchema = "digest";

    /**
     * 账号
     */
    private String user;

    /**
     * 密码
     */
    private String pwd;

    public ArrayList<ACL> writeAcls() throws NoSuchAlgorithmException {
        ArrayList<ACL> acls = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        if (isAuth() && enableWriteAcl) {
            Id zkUser = new Id(authSchema, DigestAuthenticationProvider.generateDigest(user + ":" + pwd));
            ACL acl = new ACL(ZooDefs.Perms.ALL, zkUser);
            acls = new ArrayList<>();
            acls.add(acl);
        }
        return acls;
    }

    /**
     * 连接超时时间（毫秒）
     */
    private long connectTimeoutMills = 5000;

    /**
     * cookie最大存活时间（秒）
     */
    private long cookieMaxAgeSec = 1800;

    /**
     * 无需校验会话的url
     */
    private List<String> excludeUrls;

    /**
     * 数据库dialect，默认mysql，可支持oracle
     */
    private String dialect = "mysql";

    public boolean isAuth() {
        return StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(pwd);
    }
}
