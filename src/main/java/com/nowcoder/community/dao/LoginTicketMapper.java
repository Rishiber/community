package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @Title: LoginTicketMapper
 * @Description:
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/7/3 19:30
 */
@Mapper
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket (user_id, ticket, status, expired) ",
            "values (#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
