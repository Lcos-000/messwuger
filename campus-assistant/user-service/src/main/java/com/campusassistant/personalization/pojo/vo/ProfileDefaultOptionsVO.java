package com.campusassistant.personalization.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProfileDefaultOptionsVO {

    private List<String> avatars;

    private List<String> backgrounds;

    private List<String> wallpapers;
}