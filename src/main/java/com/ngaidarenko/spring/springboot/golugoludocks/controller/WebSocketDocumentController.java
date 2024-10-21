package com.ngaidarenko.spring.springboot.golugoludocks.controller;

import com.ngaidarenko.spring.springboot.golugoludocks.dto.LockRequest;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.LockResponse;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.TextUpdateRequest;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.TextUpdateResponse;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketDocumentController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketDocumentController.class);

    private final RedissonClient redissonClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> lockOwners = new ConcurrentHashMap<>();

    public WebSocketDocumentController(RedissonClient redissonClient, SimpMessagingTemplate messagingTemplate) {
        this.redissonClient = redissonClient;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lock")
    @SendTo("/topic/locks")
    public LockResponse lockParagraph(LockRequest request) {
        log.info("Sending message: {}", request);
        RLock lock = redissonClient.getLock("lock:" + request.getParagraphId());
        try {
            boolean isLocked = lock.tryLock(10, 10, TimeUnit.SECONDS);
            if (isLocked) {
                // Запоминаем владельца блокировки
                lockOwners.put(request.getParagraphId(), request.getOwnerId());
                messagingTemplate.convertAndSend("/topic/locks",
                        new LockResponse(request.getParagraphId(), true, request.getOwnerId()));
                return new LockResponse(request.getParagraphId(), true, request.getOwnerId());
            } else {
                return new LockResponse(request.getParagraphId(), false, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new LockResponse(request.getParagraphId(), false, null);
        }
    }

    @MessageMapping("/unlock")
    @SendTo("/topic/locks")
    public LockResponse unlockParagraph(LockRequest request) {
        log.info("Unlocking paragraph with request: {}", request);
        String ownerId = lockOwners.get(request.getParagraphId());
        if (ownerId != null && ownerId.equals(request.getOwnerId())) {
            log.info("Pass a test: ownerId.equals(request.getOwnerId())");
            RLock lock = redissonClient.getLock("lock:" + request.getParagraphId());
            if (lock.isLocked()) {
                lock.unlock();
                log.info("Unlocked");
                lockOwners.remove(request.getParagraphId());  // Удаляем владельца после разблокировки
                messagingTemplate.convertAndSend("/topic/locks",
                        new LockResponse(request.getParagraphId(), false, null));
                return new LockResponse(request.getParagraphId(), false, null);
            }
        }
        return new LockResponse(request.getParagraphId(), true, ownerId);  // Не разрешаем другим разблокировать
    }

    @MessageMapping("/updateText")
    @SendTo("/topic/textUpdates")
    public TextUpdateResponse updateText(TextUpdateRequest request) {
        messagingTemplate.convertAndSend("/topic/textUpdates",
                new TextUpdateResponse(request.getParagraphId(), request.getText()));
        return new TextUpdateResponse(request.getParagraphId(), request.getText());
    }
}
