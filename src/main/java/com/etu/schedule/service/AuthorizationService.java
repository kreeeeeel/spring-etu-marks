package com.etu.schedule.service;

public interface AuthorizationService {
    String authEtu(Long telegram, String email, String password);
}
