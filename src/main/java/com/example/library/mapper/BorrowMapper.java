package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.library.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {
    @Select("SELECT b.*, bk.title AS book_title " +
            "FROM borrow b LEFT JOIN book bk ON b.book_id = bk.id " +
            "ORDER BY b.id DESC")
    IPage<Borrow> selectBorrowPage(IPage<Borrow> page);
}
