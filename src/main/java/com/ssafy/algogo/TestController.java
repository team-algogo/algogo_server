package com.ssafy.algogo;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/api")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok().body("성공적으로 서버 통신이 완료되었습니다.");
    }

    @GetMapping("/error/1")
    public ResponseEntity<?> error_1() throws CustomException {
        throw new CustomException("커스텀 에러 걸렸습니다.", ErrorCode.BAD_REQUEST_ERROR);
    }

    @GetMapping("/error/2")
    public ResponseEntity<?> error_2() throws Exception  {
        throw new Exception();
    }


}
