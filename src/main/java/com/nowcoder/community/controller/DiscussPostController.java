package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.util.*;

/**
 * @Title: DiscussPostController
 * @Description:
 * @Author: Rishiber
 * @Version: 1.0
 * @create: 2019/7/9 19:20
 */
 @Controller
 @RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

     @Autowired
     private DiscussPostService discussPostService;

     @Autowired
     private UserService userService;

     @Autowired
     private CommentService commentService;

     @Autowired
     private LikeService likeService;

     @Autowired
     private HostHolder hostHolder;

     @Autowired
     private EventProducer eventProducer;

     @Autowired
     private RedisTemplate redisTemplate;

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

         // 触发发帖事件
         Event event = new Event()
                 .setTopic(TOPIC_PUBLISH)
                 .setUserId(user.getId())
                 .setEntityType(ENTITY_TYPE_POST)
                 .setEntityId(discussPost.getId());
         eventProducer.fireEvent(event);

         // 计算帖子分数
         String redisKey = RedisKeyUtil.getPostScoreKey();
         redisTemplate.opsForSet().add(redisKey, discussPost.getId());

         // 报错情况，将来统一处理
         return CommunityUtil.getJSONString(0, "发布成功");
     }

     @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
     public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
         // 查询帖子
         DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
         model.addAttribute("post", discussPost);
         // 查询帖子作者
         User user = userService.findUserById(discussPost.getUserId());
         model.addAttribute("user", user);
         // 点赞数量
         long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
         model.addAttribute("likeCount", likeCount);
         // 点赞状态
         int likeStatus = hostHolder.getUser() == null ? 0 :
                 likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
         model.addAttribute("likeStatus", likeStatus);

         // 评论分页信息
         page.setLimit(5);
         page.setPath("/discuss/detail/" + discussPostId);
         page.setRows(discussPost.getCommentCount());

         // 评论：给帖子的评论
         // 回复：给评论的评论
         // 评论列表
         List<Comment> commentList = commentService.findCommentsByEntity(
                 ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
         // 评论VO列表
         List<Map<String, Object>> commentVOList = new ArrayList<>();
         if (commentList != null) {
             for (Comment comment : commentList) {
                 // 评论VO
                 Map<String, Object> commentVO = new HashMap<>();
                 // 评论
                 commentVO.put("comment", comment);
                 // 作者
                 commentVO.put("user", userService.findUserById(comment.getUserId()));
                 // 点赞数量
                 likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                 commentVO.put("likeCount", likeCount);
                 // 点赞状态
                 likeStatus = hostHolder.getUser() == null ? 0 :
                         likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                 commentVO.put("likeStatus", likeStatus);

                 // 回复列表
                 List<Comment> replyList = commentService.findCommentsByEntity(
                         ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                 // 回复VO列表
                 List<Map<String, Object>> replyVOList = new ArrayList<>();
                 if (replyList != null) {
                     for (Comment reply : replyList) {
                         Map<String, Object> replyVO = new HashMap<>();
                         // 回复
                         replyVO.put("reply", reply);
                         // 作者
                         replyVO.put("user", userService.findUserById(reply.getUserId()));
                         // 回复目标
                         User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                         replyVO.put("target", target);
                         // 点赞数量
                         likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                         replyVO.put("likeCount", likeCount);
                         // 点赞状态
                         likeStatus = hostHolder.getUser() == null ? 0 :
                                 likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                         replyVO .put("likeStatus", likeStatus);

                         replyVOList.add(replyVO);
                     }
                 }
                 commentVO.put("replys", replyVOList);

                 // 回复数量
                 int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                 commentVO.put("replyCount", replyCount);

                 commentVOList.add(commentVO);
             }

         }

         model.addAttribute("comments", commentVOList);


         return "/site/discuss-detail";
     }

     // 置顶
     @RequestMapping(path = "/top", method = RequestMethod.POST)
     @ResponseBody
     public String setTop(int id) {
         discussPostService.updateType(id, 1);

         // 触发发帖事件
         Event event = new Event()
                 .setTopic(TOPIC_PUBLISH)
                 .setUserId(hostHolder.getUser().getId())
                 .setEntityType(ENTITY_TYPE_POST)
                 .setEntityId(id);
         eventProducer.fireEvent(event);

         return CommunityUtil.getJSONString(0);
     }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }



}
