package com.xjh.atcrowdfunding.manager.dao;

import com.xjh.atcrowdfunding.bean.Tag;
import java.util.List;

public interface TagMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Tag record);

    Tag selectByPrimaryKey(Integer id);

    List<Tag> selectAll();

    int updateByPrimaryKey(Tag record);
}