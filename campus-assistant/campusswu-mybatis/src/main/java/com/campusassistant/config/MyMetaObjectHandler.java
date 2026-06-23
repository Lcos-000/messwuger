package com.campusassistant.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.campusassistant.provider.UserIdProvider;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";

    private static final String FIELD_CREATE_USER = "createUser";
    private static final String FIELD_UPDATE_USER = "updateUser";

    @Autowired(required = false)
    private UserIdProvider userIdProvider;

    @Override
    public void insertFill(MetaObject metaObject) {
        this.fillStrategy(metaObject, FIELD_CREATE_TIME, LocalDateTime.now());
        this.fillStrategy(metaObject, FIELD_UPDATE_TIME, LocalDateTime.now());

        if (userIdProvider != null && userIdProvider.getCurrentUserId() != null) {
            Long userId = userIdProvider.getCurrentUserId();
            this.fillStrategy(metaObject, FIELD_CREATE_USER, userId);
            this.fillStrategy(metaObject, FIELD_UPDATE_USER, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.fillStrategy(metaObject, FIELD_UPDATE_TIME, LocalDateTime.now());

        if (userIdProvider != null && userIdProvider.getCurrentUserId() != null) {
            Long userId = userIdProvider.getCurrentUserId();
            this.fillStrategy(metaObject, FIELD_UPDATE_USER, userId);
        }
    }
}
