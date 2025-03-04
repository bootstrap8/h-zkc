package com.github.hbq969.code.zkc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.zkc.config.ZkcProperties;
import com.github.hbq969.code.zkc.model.LeafBean;
import com.github.hbq969.code.zkc.model.ZKNode;
import com.github.hbq969.code.zkc.service.ZookeeperService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author hbq
 */
@Component("zkui-service-ZookeeperServiveImpl")
@Slf4j
public class ZookeeperServiceImpl implements ZookeeperService, InitializingBean, DisposableBean {

    public final static int MAX_CONNECT_ATTEMPT = 5;
    public final static String ZK_ROOT_NODE = "/";
    public final static String ZK_SYSTEM_NODE = "zookeeper";
    public final static String ZK_HOSTS = "/appconfig/hosts";
    public final static String ROLE_USER = "USER";
    public final static String ROLE_ADMIN = "ADMIN";
    public final static String SOPA_PIPA = "SOPA/PIPA BLACKLISTED VALUE";

    @Autowired
    private ZkcProperties conf;

    @Autowired
    private SpringContext context;

    private ZooKeeper zk;

    private Cache<String, Set<LeafBean>> searchCache = CacheBuilder.newBuilder()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .maximumSize(500)
            .initialCapacity(100)
            .removalListener((RemovalListener<String, Set<LeafBean>>) notification -> {
                log.info("缓存失效, {}", notification.getKey());
            }).build();


    @Override
    public void afterPropertiesSet() throws Exception {
        connectionZookeeper();
//        initialSearchCache();
    }

    private void initialSearchCache() {
        CompletableFuture.runAsync(() -> {
            Set<LeafBean> leaves = new TreeSet<>();
            try {
                exportTreeInternal(leaves, ZK_ROOT_NODE);
            } catch (Exception e) {
                log.error("初始化加载全量配置异常", e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        Optional.ofNullable(zk).ifPresent(z -> {
            try {
                z.close();
                log.info("断开与Zookeeper的连接");
            } catch (InterruptedException e) {
            }
        });
    }

    @Override
    public ZKNode listNodeEntries(String path, String authRole) throws Exception {
        List<String> folders = new ArrayList<>();
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {

                    List<String> subChildren =
                            zk.getChildren(path + ("/".equals(path) ? "" : "/") + child, false);
                    boolean isFolder = subChildren != null && !subChildren.isEmpty();
                    if (isFolder) {
                        folders.add(child);
                    } else {
                        String childPath = getNodePath(path, child);
                        leaves.add(this.getNodeValue(zk, path, childPath, child, authRole));
                    }

                }

            }
        }

        Collections.sort(folders);
        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ZKNode zkNode = new ZKNode();
        zkNode.setLeafBeanLSt(leaves);
        zkNode.setNodeLst(folders);
        return zkNode;
    }

    @Override
    public void setPropertyValue(String path, String name, String value) throws Exception {
        String nodePath = path + (path.endsWith("/") ? "" : '/') + name;
        if (log.isDebugEnabled()) {
            log.debug("Setting property {} to {}", nodePath, value);
        }
        zk.setData(nodePath, value.getBytes(), -1);
    }

    @Override
    public void deleteLeaves(List<String> leafNames) throws Exception {

        for (String leafPath : leafNames) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting leaf {}", leafPath);
            }
            zk.delete(leafPath, -1);
        }
    }

    @Override
    public void createFolder(String folderPath, String propertyName, String propertyValue) throws Exception {
        ArrayList<ACL> acls = conf.writeAcls();
        zk.create(folderPath, "".getBytes(), acls, CreateMode.PERSISTENT);
        zk.create(folderPath + "/" + propertyName, propertyValue == null ? null : propertyValue
                .getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Override
    public void createNode(String path, String name, String value) throws Exception {
        String nodePath = path.endsWith("/") ? path + name : path + "/" + name;
        ArrayList<ACL> acls = conf.writeAcls();
        zk.create(nodePath, value == null ? null : value.getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Override
    public void deleteFolders(List<String> folderNames) throws Exception {
        for (String folderPath : folderNames) {
            deleteFolderInternal(folderPath, zk);
        }
    }

    @Override
    public Set<LeafBean> exportTree(String zkPath) throws Exception {
        // 1. Collect nodes
        long startTime = System.currentTimeMillis();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, zkPath);
        long estimatedTime = System.currentTimeMillis() - startTime;
        log.info("导出目录: {} 下的配置耗时: {} ms", zkPath, (estimatedTime));
        return leaves;
    }

    @Override
    public void importData(List<String> importFile, boolean overwrite) throws Exception {
        for (String line : importFile) {
            // Delete Operation
            if (line.startsWith("-")) {
                String nodeToDelete = line.substring(1);
                deleteNodeIfExists(nodeToDelete, zk);
            } else {
                int firstEq = line.indexOf('=');
                int secEq = line.indexOf('=', firstEq + 1);

                String path = line.substring(0, firstEq);
                if ("/".equals(path)) {
                    path = "";
                }
                String name = line.substring(firstEq + 1, secEq);
                String value = readExternalizedNodeValue(line.substring(secEq + 1));
                String fullNodePath = path + "/" + name;

                // Skip import of system node
                if (fullNodePath.startsWith(ZK_SYSTEM_NODE)) {
                    continue;
                }
                boolean nodeExists = nodeExists(fullNodePath);

                if (!nodeExists) {
                    //If node doesnt exist then create it.
                    createPathAndNode(path, name, value.getBytes(), true);
                } else {
                    //If node exists then update only if overwrite flag is set.
                    if (overwrite) {
                        setPropertyValue(path + "/", name, value);
                    } else {
                        log.info("Skipping update for existing property " + path + "/" + name + " as overwrite is not enabled!");
                    }
                }
            }
        }
    }

    @Override
    public Set<LeafBean> searchTree(String path, String name, String value) throws Exception {
        //Export all nodes and then search.
        Set<LeafBean> searchResult = new TreeSet<>();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, ZK_ROOT_NODE);
        leaves.parallelStream()
                .filter(leaf -> leaf.containKey(path, name, value))
                .forEach(searchResult::add);
        return searchResult;
    }

    private String externalizeNodeValue(byte[] value) {
        return value == null ? "" : new String(value).replaceAll("\\n", "\\\\n").replaceAll("\\r", "");
        // We might want to BASE64 encode it
    }

    private void createPathAndNode(String path, String name, byte[] data, boolean force) throws InterruptedException, KeeperException, NoSuchAlgorithmException {
        // 1. Create path nodes if necessary
        StringBuilder currPath = new StringBuilder();
        for (String folder : path.split("/")) {
            if (folder.length() == 0) {
                continue;
            }
            currPath.append('/');
            currPath.append(folder);

            if (!nodeExists(currPath.toString())) {
                createIfDoesntExist(currPath.toString(), new byte[0], true);
            }
        }

        // 2. Create leaf node
        createIfDoesntExist(path + '/' + name, data, force);
    }

    private void createIfDoesntExist(String path, byte[] data, boolean force) throws InterruptedException, KeeperException, NoSuchAlgorithmException {
        ArrayList<ACL> acls = conf.writeAcls();
        try {
            zk.create(path, data, acls, CreateMode.PERSISTENT);
        } catch (KeeperException ke) {
            //Explicit Overwrite
            if (KeeperException.Code.NODEEXISTS.equals(ke.code())) {
                if (force) {
                    zk.delete(path, -1);
                    zk.create(path, data, acls, CreateMode.PERSISTENT);
                }
            } else {
                throw ke;
            }
        }
    }

    public boolean nodeExists(String nodeFullPath) throws KeeperException,
            InterruptedException {
        return zk.exists(nodeFullPath, false) != null;
    }

    private String readExternalizedNodeValue(String raw) {
        return raw.replaceAll("\\\\n", "\n");
    }

    private void deleteNodeIfExists(String path, ZooKeeper zk) throws InterruptedException,
            KeeperException {
        zk.delete(path, -1);
    }

    private void exportTreeInternal(Set<LeafBean> entries, String path) throws InterruptedException, KeeperException {
        // 1. List leaves
        entries.addAll(this.listLeaves(path));
        // 2. Process folders
        for (String folder : this.listFolders(path)) {
            exportTreeInternal(entries, this.getNodePath(path, folder));
        }
    }

    private List<String> listFolders(String path) throws KeeperException,
            InterruptedException {
        List<String> folders = new ArrayList<>();
        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {
                    List<String> subChildren =
                            zk.getChildren(path + ("/".equals(path) ? "" : "/") + child, false);
                    boolean isFolder = subChildren != null && !subChildren.isEmpty();
                    if (isFolder) {
                        folders.add(child);
                    }
                }

            }
        }

        Collections.sort(folders);
        return folders;
    }

    private List<LeafBean> listLeaves(String path) throws
            InterruptedException, KeeperException {
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            leaves = children.parallelStream()
                    .map(child -> {
                        String childPath = getNodePath(path, child);
                        List<String> subChildren = null;
                        try {
                            subChildren = zk.getChildren(childPath, false);
                        } catch (Exception e) {
                            log.error(String.format("查询路径: %s 下子元素异常", childPath), e);
                        }
                        boolean isFolder = subChildren != null && !subChildren.isEmpty();
                        return !isFolder ? this.getNodeValue(zk, path, childPath, child, "ADMIN") : null;
                    })
                    .filter(leaf -> leaf != null)
                    .collect(Collectors.toList());
        }

        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return leaves;
    }

    public static Set<String> UN_REMOVE_DIR = Sets.newHashSet("/", "/zookeeper", "/zookeeper/quota", "/zookeeper/config");

    private void deleteFolderInternal(String folderPath, ZooKeeper zk) throws KeeperException,
            InterruptedException {
        for (String child : zk.getChildren(folderPath, false)) {
            deleteFolderInternal(getNodePath(folderPath, child), zk);
        }
        if (!UN_REMOVE_DIR.contains(folderPath)) {
            zk.delete(folderPath, -1);
        }
    }

    private void connectionZookeeper() throws IOException, InterruptedException {
        int connectAttempt = 0;
        this.zk = new ZooKeeper(String.join(",", conf.getUrls()), (int) conf.getConnectTimeoutMills(), event -> log.info("成功连接到Zookeeper。"));
        if (conf.isAuth()) {
            log.info("zookeeper认证信息, authschema: {}, user: {}, pwd: {}", conf.getAuthSchema(), conf.getUser(), conf.getPwd());
            zk.addAuthInfo(conf.getAuthSchema(), (conf.getUser() + ":" + conf.getPwd()).getBytes());
            try {
                List<ACL> acls = new ArrayList<ACL>();
                Id zkUser = new Id(conf.getAuthSchema(), DigestAuthenticationProvider
                        .generateDigest(conf.getUser() + ":" + conf.getPwd()));
                ACL acl = new ACL(ZooDefs.Perms.ALL, zkUser);
                acls.add(acl);
                // 漏洞扫描的路径，必须加密
                String[] paths = {"/", "/zookeeper", "/zookeeper/quota", "/zookeeper/config"};
                for (String path : paths) {
                    try {
                        zk.setACL(path, acls, -1);
                    } catch (Exception e) {
                        log.error("提示信息: 目录 [{}] 不存在, 添加认证失败。", path);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        //Wait till connection is established.
        while (zk.getState() != ZooKeeper.States.CONNECTED) {
            Thread.sleep(30);
            connectAttempt++;
            if (connectAttempt == MAX_CONNECT_ATTEMPT) {
                break;
            }
        }
    }

    private String getNodePath(String path, String name) {
        return path + ("/".equals(path) ? "" : "/") + name;

    }

    private LeafBean getNodeValue(ZooKeeper zk, String path, String childPath, String child,
                                  String authRole) {
        //Reason exception is caught here is so that lookup can continue to happen if a particular property is not found at parent level.
        try {
//            if (log.isDebugEnabled()) {
//                log.debug("Lookup: path=" + path + ",childPath=" + childPath + ",child=" + child + ",authRole=" + authRole);
//            }
            byte[] dataBytes = zk.getData(childPath, false, new Stat());
            if (!authRole.equals(ROLE_ADMIN)) {
                if (checkIfPwdField(child)) {
                    return (new LeafBean(path, child, SOPA_PIPA.getBytes()));
                } else {
                    return (new LeafBean(path, child, dataBytes));
                }
            } else {
                return (new LeafBean(path, child, dataBytes));
            }
        } catch (KeeperException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
        return null;

    }

    private Boolean checkIfPwdField(String property) {
        if (property.contains("PWD") || property.contains("pwd") || property
                .contains("PASSWORD") || property.contains("password") || property
                .contains("PASSWD") || property.contains("passwd")) {
            return true;
        } else {
            return false;
        }
    }
}
