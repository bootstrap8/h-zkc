package com.github.hbq969.code.zkc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Test {
    public static void main(String[] args) throws Exception {
        ZooKeeper zk = new ZooKeeper("localhost:22181", 5000, event -> log.info("成功连接到Zookeeper。"));
        zk.addAuthInfo("digest", ("foo:bar").getBytes());
        List<ACL> acls = new ArrayList<ACL>();
        Id zkUser = new Id("digest", DigestAuthenticationProvider
                .generateDigest("foo:bar"));
        ACL acl = new ACL(ZooDefs.Perms.ALL, zkUser);
        acls.add(acl);
        List<String> paths = zk.getChildren("/test", false);
        log.info("+++++ {}", paths);
    }
}
