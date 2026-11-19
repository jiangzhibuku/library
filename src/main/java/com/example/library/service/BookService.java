package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public Page<Book> page(long page, long size, String keyword) {
        Page<Book> p = new Page<>(page, size);

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or()
                    .like(Book::getAuthor, keyword));
        }
        wrapper.orderByDesc(Book::getId);

        return bookMapper.selectPage(p, wrapper);
    }
}