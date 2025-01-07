package com.github.hbq969.code.zkc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import lombok.Data;

/**
 * @author : hbq969@gmail.com
 * @description : 用户信息
 * @createTime : 2025/1/2 11:13
 */
@Data
public class UserEntity implements DictModel, DictAware {
    private Long id;
    private String username;
    private String password;
    private Long roleId;
    private String roleName;
    private Long createdAt;
    private String fmtCreatedAt;
    private Long updatedAt;
    private String fmtUpdatedAt;

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if (createdAt != null) {
            this.fmtCreatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(createdAt.longValue());
        }
        if (updatedAt != null) {
            this.fmtUpdatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(updatedAt.longValue());
        }
    }

    public void initial() {
        this.createdAt = FormatTime.nowSecs();
    }

    public void update() {
        this.updatedAt = FormatTime.nowSecs();
    }
}
