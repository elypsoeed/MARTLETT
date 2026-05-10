package com.elypsoeed.martlett.core.controller;

import com.elypsoeed.martlett.generated.api.HiApi;
import com.elypsoeed.martlett.generated.model.HiResponse;
import com.elypsoeed.martlett.core.service.HiService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HiController implements HiApi {

    private final HiService hiService;

    @Override
    public ResponseEntity<@NonNull HiResponse> sayHi(@RequestBody String greeting) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new HiResponse(hiService.sayHi(greeting)));
    }
}
