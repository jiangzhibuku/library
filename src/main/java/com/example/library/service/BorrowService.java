package com.example.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.BizException;
import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BorrowService {

    private final BookMapper bookMapper;
    private final BorrowMapper borrowMapper;

    public BorrowService(BookMapper bookMapper, BorrowMapper borrowMapper) {
        this.bookMapper = bookMapper;
        this.borrowMapper = borrowMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long borrow(Long userId, Long bookId) {
        int rows = bookMapper.decreaseStock(bookId);
        if (rows == 0) {
            throw new BizException("库存不足或图书不存在");
        }

        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setStatus(0);
        borrow.setBorrowTime(LocalDateTime.now());
        borrowMapper.insert(borrow);


        return borrow.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void giveBack(Long borrowId) {
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BizException("借阅记录不存在");
        }
        if (borrow.getStatus() == 1) {
            throw new BizException("该记录已归还");
        }

        borrow.setStatus(1);
        borrow.setReturnTime(LocalDateTime.now());
        borrowMapper.updateById(borrow);

        bookMapper.increaseStock(borrow.getBookId());
    }

    public IPage<Borrow> page(long page, long size) {
        return borrowMapper.selectBorrowPage(new Page<>(page, size));
    }
}