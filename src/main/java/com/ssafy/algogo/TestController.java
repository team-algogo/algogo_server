package com.ssafy.algogo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/api")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok().body("성공적으로 서버 통신이 완료되었습니다.");
    }

}
