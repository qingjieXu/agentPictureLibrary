package com.xuqj.agentpicturelibrary.app;

import com.xuqj.agentpicturelibrary.advisor.MyLoggerAdvisor;
import com.xuqj.agentpicturelibrary.advisor.ReReadingAdvisor;
import com.xuqj.agentpicturelibrary.chatmemory.FileBasedChatMemory;
import com.xuqj.agentpicturelibrary.model.app.LibraryReport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LibraryApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一位专业的智能图库助手，具备摄影美学、空间设计、数字资产管理等专业知识。" +
            "你的任务是帮助用户高效管理图库，" +
            "并通过多轮对话精准挖掘用户在「用户画像」「图片用途」「空间场景」三方面的需求，" +
            "最终提供定制化解决方案。";

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
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
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

    @Resource
    private VectorStore libraryAppVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(libraryAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}

