package com.ssafy.algogo.auth.service.impl;

import com.ssafy.algogo.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    @Override
    public void login() {

    }

}
