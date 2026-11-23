package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.Result;
import com.example.library.entity.Borrow;
import com.example.library.service.BorrowService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/{bookId}")
    public Result<Long> borrow(@PathVariable @Min(value = 1, message = "图书ID必须大于0") Long bookId,
                               @RequestParam @NotNull(message = "用户ID不能为空")
                               @Min(value = 1, message = "用户ID必须大于0") Long userId) {
        return Result.ok(borrowService.borrow(userId, bookId));
    }

    @PostMapping("/return/{borrowId}")
    public Result<Void> giveBack(@PathVariable @Min(value = 1, message = "借阅ID必须大于0") Long borrowId) {
        borrowService.giveBack(borrowId);
        return Result.ok();
    }

    @GetMapping
    public Result<Page<Borrow>> page(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size) {
        return Result.ok(borrowService.page(page, size));
    }
}