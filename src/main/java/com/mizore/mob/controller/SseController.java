package com.mizore.mob.controller;

import com.mizore.mob.dto.Result;
import com.mizore.mob.message.SseServer;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@AllArgsConstructor
public class SseController {

    SseServer sseServer;

    /**
     * 客户端创建与服务器的sse连接
     * @return
     */
    @GetMapping("/connect")
    public SseEmitter connect() {
        return sseServer.connect(sseServer.getKey());
    }

    /**
     * 客户端关闭与服务器的sse连接
     * @return
     */
    @GetMapping("/close")
    public Result close() {
        boolean res = sseServer.removeUser(sseServer.getKey());
        return res ? Result.ok() : Result.error("移除连接失败，连接可能尚未建立。");
    }
}
