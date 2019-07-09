package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @Title: DiscussPostController
 * @Description:
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/7/9 19:20
 */
 @Controller
 @RequestMapping("/discuss")
public class DiscussPostController {

     @Autowired
     private DiscussPostService discussPostService;

     @Autowired
     private UserService userService;

     @Autowired
     private HostHolder hostHolder;

     @RequestMapping(path = "/add", method = RequestMethod.POST)
     @ResponseBody
     public String addDiscussPost(String title, String content) {
         User user = hostHolder.getUser();
         if (user == null) {
             return CommunityUtil.getJSONString(403, "你还没有登录！");
         }
         DiscussPost discussPost = new DiscussPost();
         discussPost.setUserId(user.getId());
         discussPost.setTitle(title);
         discussPost.setContent(content);
         discussPost.setCreateTime(new Date());
         discussPostService.addDiscussPost(discussPost);

         // 报错情况，将来统一处理
         return CommunityUtil.getJSONString(0, "发布成功");
     }

     @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
     public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
         // 查询帖子
         DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
         model.addAttribute("post", discussPost);
         // 查询帖子作者
         User user = userService.findUserById(discussPost.getUserId());
         model.addAttribute("user", user);

         return "/site/discuss-detail";
     }
}
