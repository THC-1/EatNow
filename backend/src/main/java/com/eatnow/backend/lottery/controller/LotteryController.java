package com.eatnow.backend.lottery.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.lottery.dto.LotteryConditionDrawRequest;
import com.eatnow.backend.lottery.dto.LotteryDrawRequest;
import com.eatnow.backend.lottery.dto.LotteryRecordActionRequest;
import com.eatnow.backend.lottery.service.LotteryService;
import com.eatnow.backend.lottery.vo.LotteryDrawResultVo;
import com.eatnow.backend.lottery.vo.LotteryRecordVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @PostMapping("/draw")
    public ApiResponse<LotteryDrawResultVo> draw(@Valid @RequestBody LotteryDrawRequest request) {
        return ApiResponse.success(lotteryService.randomDraw(request));
    }

    @PostMapping("/draw-with-condition")
    public ApiResponse<LotteryDrawResultVo> drawWithCondition(@Valid @RequestBody LotteryConditionDrawRequest request) {
        return ApiResponse.success(lotteryService.conditionDraw(request));
    }

    @PostMapping("/draw-from-favorites")
    public ApiResponse<LotteryDrawResultVo> drawFromFavorites(@Valid @RequestBody LotteryDrawRequest request) {
        return ApiResponse.success(lotteryService.favoriteDraw(request));
    }

    @GetMapping("/records")
    public ApiResponse<PageResult<LotteryRecordVo>> listRecords(
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(lotteryService.listRecords(page, size));
    }

    @PostMapping("/records/{id}/action")
    public ApiResponse<Void> recordAction(@PathVariable Long id, @Valid @RequestBody LotteryRecordActionRequest request) {
        lotteryService.recordAction(id, request);
        return ApiResponse.success();
    }
}
