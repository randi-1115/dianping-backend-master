package com.example.dianpingbackend.mapper;

import com.example.dianpingbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
@Mapper
public interface UserMapper {
    @Select("select * from user where phone = #{phone}")
    User selectByPhone(String phone);
    @Insert("insert into user(phone,nickname,create_time,update_time)"+"values(#{phone}, #{nickname}, #{createTime}, #{updateTime})")
    void insert(User user);
    @Select("select * from user where id = #{id}")
    User selectById(Long id);
}
