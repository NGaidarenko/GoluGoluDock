package com.ngaidarenko.spring.springboot.golugoludocks.controller;

import com.ngaidarenko.spring.springboot.golugoludocks.dto.LockRequest;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.LockResponse;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.TextUpdateRequest;
import com.ngaidarenko.spring.springboot.golugoludocks.dto.TextUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WebSocketDocumentControllerTest {

    @Autowired
    private WebSocketDocumentController documentController;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private RLock rLock;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void testLockParagraph() throws Exception {
        LockRequest lockRequest = new LockRequest("paragraph-1", "user123");

        when(redissonClient.getLock("lock:paragraph-1")).thenReturn(rLock);
        when(rLock.tryLock()).thenReturn(true);
        LockResponse lockResponse = documentController.lockParagraph(lockRequest);

        // Проверяем, что блокировка прошла успешно
        assertTrue(lockResponse.isLocked());
        assertEquals("user123", lockResponse.getOwnerId());

        verify(messagingTemplate, times(1)).convertAndSend("/topic/locks", lockResponse);
    }

    @Test
    public void testUnlockParagraph() throws Exception {
        LockRequest unlockRequest = new LockRequest("paragraph-1", "user123");

        when(redissonClient.getLock("lock:paragraph-1")).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        Field lockOwnersField = documentController.getClass().getDeclaredField("lockOwners");
        lockOwnersField.setAccessible(true);
        Map<String, String> mockLockOwners = new HashMap<>();
        mockLockOwners.put("paragraph-1", "user123");  // Устанавливаем нужного владельца
        lockOwnersField.set(documentController, mockLockOwners);


        LockResponse unlockResponse = documentController.unlockParagraph(unlockRequest);

        assertFalse(unlockResponse.isLocked());  // Ожидаем, что блокировка была снята

        verify(messagingTemplate, times(1)).convertAndSend("/topic/locks", unlockResponse);
        verify(rLock, times(1)).unlock();
    }

    @Test
    public void testUpdateText() throws Exception {
        // Мок данных для тестирования обновления текста
        TextUpdateRequest textUpdateRequest = new TextUpdateRequest("paragraph-1", "Updated text");

        // Выполнение метода обновления текста
        TextUpdateResponse textUpdateResponse = documentController.updateText(textUpdateRequest);

        // Проверяем, что текст был обновлен корректно
        assertEquals("Updated text", textUpdateResponse.getText());

        // Проверка, что сообщение было отправлено по WebSocket
        verify(messagingTemplate, times(1)).convertAndSend("/topic/textUpdates", textUpdateResponse);
    }
}
