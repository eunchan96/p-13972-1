package com.back.domain.post.postComment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("댓글 단건조회")
    public void t1() throws Exception {
        int postId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                ).andDo(print());

        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postComment.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.authorId").value(postComment.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value(postComment.getAuthor().getName()))
                .andExpect(jsonPath("$.postId").value(postComment.getPost().getId()))
                .andExpect(jsonPath("$.content").value(postComment.getContent()));
    }

    @Test
    @DisplayName("댓글 다건조회")
    public void t2() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                ).andDo(print());

        Post post = postService.findById(postId).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk());

        for (int i = 0; i < post.getComments().size(); i++) {
            PostComment postComment = post.getComments().get(i);
            resultActions
                .andExpect(jsonPath("$[%d].id".formatted(i)).value(postComment.getId()))
                .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$[%d].authorId".formatted(i)).value(postComment.getAuthor().getId()))
                .andExpect(jsonPath("$[%d].authorName".formatted(i)).value(postComment.getAuthor().getName()))
                .andExpect(jsonPath("$[%d].postId".formatted(i)).value(postComment.getPost().getId()))
                .andExpect(jsonPath("$[%d].content".formatted(i)).value(postComment.getContent()));
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    public void t3() throws Exception {
        int postId = 1;
        int id = 1;
        Post post = postService.findById(postId).get();
        Member author = post.getAuthor();
        String apiKey = author.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                                .header("Authorization", "Bearer " + apiKey)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("댓글 수정")
    public void t4() throws Exception {
        int postId = 1;
        int id = 1;
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();
        Member author = post.getAuthor();
        String apiKey = author.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 new"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 수정되었습니다.".formatted(id)));

        assertThat(postComment.getContent()).isEqualTo("댓글 new");
    }

    @Test
    @DisplayName("댓글 작성")
    public void t5() throws Exception {
        int postId = 1;
        Post post = postService.findById(postId).get();
        Member author = post.getAuthor();
        String apiKey = author.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글"
                                        }
                                        """)
                ).andDo(print());

        PostComment postComment = post.getComments().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 작성되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.authorId").value(postComment.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(postComment.getAuthor().getName()))
                .andExpect(jsonPath("$.data.postId").value(postComment.getPost().getId()))
                .andExpect(jsonPath("$.data.content").value("댓글"));
    }

    @Test
    @DisplayName("댓글 작성 - without Authorization Header")
    public void t6() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("Authorization 헤더가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 작성 - Bearer 형식이 아닌 Authorization Header")
    public void t7() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .header("Authorization", "B")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."));
    }

    @Test
    @DisplayName("댓글 작성 - 로그인 하지 않은 경우")
    public void t8() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .header("Authorization", "Bearer ")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-3"))
                .andExpect(jsonPath("$.msg").value("API Key가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 수정 - 본인 글이 아닌 글 수정 시도")
    public void t9() throws Exception {
        int postId = 1;
        int id = 1;
        int errorId = 2;
        Post post = postService.findById(errorId).get();
        Member author = post.getAuthor();
        String apiKey = author.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 new",
                                            "content": "내용 new"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글 수정 권한이 없습니다.".formatted(id)));
    }

    @Test
    @DisplayName("댓글 삭제 - 본인 글이 아닌 글 삭제 시도")
    public void t10() throws Exception {
        int postId = 1;
        int id = 1;
        int errorId = 2;
        Post post = postService.findById(errorId).get();
        Member author = post.getAuthor();
        String apiKey = author.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                                .header("Authorization", "Bearer " + apiKey)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글 삭제 권한이 없습니다.".formatted(id)));
    }
}
