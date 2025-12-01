package com.ssafy.algogo;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/api")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok().body("성공적으로 서버 통신이 완료되었습니다.");
    }

    @GetMapping("/error/1")
    public ResponseEntity<?> error_1() throws CustomException {
        throw new CustomException("커스텀 에러 걸렸습니다.", ErrorCode.BAD_REQUEST);
    }

    @GetMapping("/error/2")
    public ResponseEntity<?> error_2() throws Exception {
        throw new Exception();
    }

    @GetMapping("/auth/admin")
    public ResponseEntity<?> auth_1(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        try {
            String username = (customUserDetails != null) ? customUserDetails.getUsername() : null;
            Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;
            return ResponseEntity.ok().body("userId : " + userId + " / username(userId) : " + username);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @GetMapping("/auth/user")
    public ResponseEntity<?> auth_2(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        try {
            String username = (customUserDetails != null) ? customUserDetails.getUsername() : null;
            Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;
            return ResponseEntity.ok().body("userId : " + userId + " / username(userId) : " + username);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


}
