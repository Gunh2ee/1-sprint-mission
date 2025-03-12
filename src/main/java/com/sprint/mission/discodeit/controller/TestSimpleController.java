package com.sprint.mission.discodeit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestSimpleController {

    /**
     * 간단 테스트 엔드포인트
     * POST /api/test/simple
     *
     * multipart/form-data로 foo 파트(문자열)를 받아,
     * "Got: foo" 형태의 응답을 반환합니다.
     */
    @PostMapping(path = "/simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> testSimple(
            @RequestPart("foo") String foo
    ) {
        return ResponseEntity.ok("Got: " + foo);
    }
}
