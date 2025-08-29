package com.xuqj.agentpicturelibrary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.xuqj.agentpicturelibrary.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AgentPictureLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentPictureLibraryApplication.class, args);
    }

}
