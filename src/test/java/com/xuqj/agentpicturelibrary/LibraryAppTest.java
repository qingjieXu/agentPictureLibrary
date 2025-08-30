package com.xuqj.agentpicturelibrary;

import com.xuqj.agentpicturelibrary.app.LibraryApp;
import com.xuqj.agentpicturelibrary.model.app.LibraryReport;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LibraryApp libraryApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是xqj。我显示器的分辨率是2560*1440。";
        String answer = libraryApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想找一些游戏壁纸，我该怎么找适合我显示器的壁纸呢？";
        answer = libraryApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "能给我一些有上述壁纸的网站么？";
        answer = libraryApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是xqj，我想找一些壁纸网站，有哪些推荐么";
        LibraryReport libraryReport = libraryApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(libraryReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "为什么上传的图片变模糊了？";
        String answer =  libraryApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

}

