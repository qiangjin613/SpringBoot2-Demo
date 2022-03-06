package com.springboot.demo.conf;

import com.springboot.demo.bean.Lion;
import com.springboot.demo.bean.Pet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalTest {

    public ConditionalTest() {
        System.out.println("@Configuration 标注的类被初始化");
    }

    /**
     * 使用 @ConditionalOnBean 可能会因为 Bean 的加载顺序导致条件装配不生效，
     * 可使用 @ConditionalOnClass 注解进行代替
     */
    @Bean
    //@ConditionalOnBean(name = "fantong")
    @ConditionalOnClass(Pet.class)
    public Lion lion() {
        return new Lion();
    }
}
