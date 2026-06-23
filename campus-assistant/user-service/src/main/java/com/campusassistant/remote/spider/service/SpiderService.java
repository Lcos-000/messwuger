package com.campusassistant.remote.spider.service;

public interface SpiderService {

    boolean validateCredentials(String studentId, String encryptedPassword);

    void asyncStartFullCrawl(String studentId, String encryptedPassword);

    void asyncStartPunchCard(String studentId, String encryptedPassword);
}
