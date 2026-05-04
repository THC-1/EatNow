package com.eatnow.backend.eatlist.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.eatlist.dto.EatListCreateRequest;
import com.eatnow.backend.eatlist.dto.EatListMarkEatenRequest;
import com.eatnow.backend.eatlist.service.EatListService;
import com.eatnow.backend.eatlist.vo.EatListItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/eat-list")
@RequiredArgsConstructor
public class EatListController {

    private final EatListService eatListService;

    @PostMapping
    public ApiResponse<IdResponse> addToEatList(@Valid @RequestBody EatListCreateRequest request) {
        return ApiResponse.success(new IdResponse(eatListService.addToEatList(request)));
    }

    @PatchMapping("/{id}/eaten")
    public ApiResponse<Void> markAsEaten(@PathVariable Long id, @Valid @RequestBody EatListMarkEatenRequest request) {
        eatListService.markAsEaten(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        eatListService.cancel(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<EatListItemVo>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(eatListService.list(status, page, size));
    }
}
