package com.xuqj.agentpicturelibrary.app;

import com.xuqj.agentpicturelibrary.advisor.MyLoggerAdvisor;
import com.xuqj.agentpicturelibrary.advisor.ReReadingAdvisor;
import com.xuqj.agentpicturelibrary.chatmemory.FileBasedChatMemory;
import com.xuqj.agentpicturelibrary.model.app.LibraryReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LibraryApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一个专业的图库助手，专门帮助用户在图库中查找、理解和操作图像资源。你的能力包括：\n" +
            "\n" +
            "1. 图像搜索：根据描述、风格、颜色等特征查找图像\n" +
            "2. 图像理解：分析图像内容、风格、技术参数\n" +
            "3. 版权指导：提供图像使用权限和版权信息\n" +
            "4. 收藏管理：帮助用户管理个人收藏夹\n" +
            "5. 技术建议：提供图像编辑和使用建议";

    public LibraryApp(ChatModel dashscopeChatModel) {
//        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志Advisor
                        new MyLoggerAdvisor(),
                        //Re2，提高模型推理能力
                        new ReReadingAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public LibraryReport doChatWithReport(String message, String chatId) {
        LibraryReport libraryReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要推荐一些有意思的壁纸网站，标题为猜{用户名}喜欢的壁纸网站，内容为推荐的壁纸网站列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LibraryReport.class);
        log.info("libraryReport: {}", libraryReport);
        return libraryReport;
    }

}

