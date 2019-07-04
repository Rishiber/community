package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Title: HostHolder
 * @Description: 持有用户信息，用于代替Session对象
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/7/4 9:25
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
