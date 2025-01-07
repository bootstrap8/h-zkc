package com.github.hbq969.code.zkc.dao;

import com.github.hbq969.code.zkc.dao.entity.MenuEntity;
import com.github.hbq969.code.zkc.dao.entity.RoleEntity;
import com.github.hbq969.code.zkc.dao.entity.UserEntity;
import com.github.hbq969.code.zkc.view.request.PasswordModify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author : hbq969@gmail.com
 * @description : 系统管理数据库访问层接口
 * @createTime : 2025/1/2 11:06
 */
@Mapper
public interface SystemDao {
    List<RoleEntity> queryRoleList(RoleEntity q);

    void saveRoleEntity(RoleEntity entity);

    void updateRoleEntity(RoleEntity entity);

    void deleteRoleEntity(@Param("id") Long id);

    List<Map> queryRoleMenus(@Param("id") Long id);

    List<UserEntity> queryUserList(UserEntity q);

    UserEntity queryUserEntity(@Param("id") Long id);

    void saveUserEntity(UserEntity entity);

    void updateUserEntity(UserEntity entity);

    void deleteUserEntity(@Param("id") Long id);

    void deleteUserEntities(@Param("roleId") Long roleId);

    void updateUserPassword(PasswordModify passwordModify);

    List<MenuEntity> queryMenuList(MenuEntity q);

    void saveMenuEntity(MenuEntity entity);

    void updateMenuEntity(MenuEntity entity);

    void deleteMenuEntity(@Param("id") Long id);

    void deleteMenuEntities(@Param("roleId") Long roleId);

    UserEntity queryUserByName(@Param("name") String name);

}
