package com.springboot.demo.conf;

import com.springboot.demo.bean.Panda;
import com.springboot.demo.bean.Tiger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import({Tiger.class})
@Component
public class ImportTest {

    public ImportTest() {
        System.out.println("@Component 标注的类被初始化");
    }

    @Bean
    public Panda panda() {
        System.out.println("@Component 中的 Bean 注入");
        return new Panda();
    }
}
