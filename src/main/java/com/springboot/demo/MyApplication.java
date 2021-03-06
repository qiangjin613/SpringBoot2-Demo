package com.springboot.demo;

import com.springboot.demo.bean.Lion;
import com.springboot.demo.bean.Pet;
import com.springboot.demo.bean.Tiger;
import com.springboot.demo.bean.User;
import com.springboot.demo.conf.MyCar;
import com.springboot.demo.conf.MyConfig;
import com.springboot.demo.conf.MyHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @SpringBootApplication(scanBasePackages = "com.springboot.demo") 复合注解，
 * 等价于以下 3 个注解作用之和：
 *      @SpringBootConfiguration
 *      @EnableAutoConfiguration
 *      @ComponentScan("com.springboot.demo")
 *
 * MyApplication 即是主启动类，也是主配置类
 */
@EnableConfigurationProperties(MyHouse.class)
@RestController
@SpringBootApplication(scanBasePackages = "com.springboot.demo")
public class MyApplication {

    @Autowired
    MyCar car;

    /**
     * 对读取配置的类进行测试
     */
    @RequestMapping("/mycar")
    MyCar car() {
        return car;
    }

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        // 返回 IoC 容器
        ConfigurableApplicationContext iocContext = SpringApplication.run(MyApplication.class, args);

        //testConfiguration(iocContext);
        //testImport(iocContext);
        testConditional(iocContext);
    }

    /**
     * 使用 @Configuration + @Bean 注册组件的相关测试
     */
    public static void testConfiguration(ApplicationContext iocContext) {
        // 从容器中获取组件
        User user = iocContext.getBean("user", User.class);
        Pet pet = iocContext.getBean("fantong", Pet.class);
        System.out.println(user + "\n" + pet);
        /* 查看注册的组件是否是单实例的（答案：是） */
        System.out.println(pet == iocContext.getBean("fantong", Pet.class));

        // 对 @Configuration(proxyBeanMethods = true) 的测试
        /* 获取配置类的 Bean 实例 */
        MyConfig bean = iocContext.getBean(MyConfig.class);
        /* 从这里可以看出，配置类是被 SpringBoot 中 CGLIB 增强了的代理对象：com.springboot.demo.conf.MyConfig$$EnhancerBySpringCGLIB$$7dbf2ea9@23eee4b8 */
        System.out.println(bean);
        Pet pet1 = bean.pet();
        System.out.println(pet == pet1); /* true */
    }

    /**
     * 查看使用 @Import 导入到 IoC 容器中的 Tiger 类
     */
    public static void testImport(ApplicationContext iocContext) {
        String[] beanNames = iocContext.getBeanNamesForType(Tiger.class);
        System.out.println(Arrays.toString(beanNames));
    }

    /**
     * 对 @Conditional 的测试
     */
    public static void testConditional(ApplicationContext iocContext) {
        System.out.println(iocContext.containsBean("fantong"));
        System.out.println(iocContext.containsBean("lion"));
        String[] beanNames2 = iocContext.getBeanNamesForType(Lion.class);
        System.out.println(Arrays.toString(beanNames2));
    }
}
