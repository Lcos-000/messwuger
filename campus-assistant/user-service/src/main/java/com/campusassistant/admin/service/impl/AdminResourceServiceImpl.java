package com.campusassistant.admin.service.impl;

import com.campusassistant.admin.config.AdminResourceProperties;
import com.campusassistant.admin.pojo.vo.AdminResourceItemVO;
import com.campusassistant.admin.pojo.vo.AdminResourceVO;
import com.campusassistant.admin.service.AdminResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResourceServiceImpl implements AdminResourceService {

    private final AdminResourceProperties adminResourceProperties;

    @Override
    public AdminResourceVO getResourceList() {
        List<AdminResourceItemVO> items = new ArrayList<>();

        if (adminResourceProperties.getItems() != null) {
            for (AdminResourceProperties.Item configItem : adminResourceProperties.getItems()) {
                if (configItem == null) {
                    continue;
                }
                if (configItem.getUrl() == null || configItem.getUrl().isBlank()) {
                    continue;
                }

                AdminResourceItemVO item = new AdminResourceItemVO();
                item.setCode(configItem.getCode());
                item.setName(configItem.getName());
                item.setUrl(configItem.getUrl());
                items.add(item);
            }
        }

        AdminResourceVO vo = new AdminResourceVO();
        vo.setItems(items);
        return vo;
    }

}