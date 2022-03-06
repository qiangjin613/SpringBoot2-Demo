package com.springboot.demo.conf;

import com.springboot.demo.bean.Pet;
import com.springboot.demo.bean.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. 使用 @Configuration 告诉 SpringBoot 这是一个配置类（作用等同于 .xml 配置 Bean）
 * 2. 在配置类中，使用 @Bean 给容器注册组件，默认是单实例的
 * 3. 使用 @Configuration 修饰的类也是一个组件（查看 @Configuration 源码，发现其被 @Component 标注）
 *
 * 4. proxyBeanMethods 属性
 *      （1）在 SpringBoot2 新增的一个参数（基于 Spring 5.2），用于解决组件依赖的场景
 *      （2）作用：是否代理 Bean 的方法
 *          i. 为 true 时（默认）
 *              SpringBoot 会通过 CGLIB 创建标注类的代理对象，用于保持组件的单实例；
 *              无论外部对配置类中的这个组件注册方法调用多少次，获取的对象都是之前注册容器中的单实例
 *          ii. 为 false 时
 *              注册到容器中的是配置类本身（不是代理对象），
 *              当获取到配置类的实例，然后调用方法时，如：本例中的 user()，会创建一个新的 User 对象
 *      （3）基于以上属性值的不同，衍生了 @Configuration 在底层的两个配置
 *          i. Full 全配置（proxyBeanMethods = true）
 *              SpringBoot 为配置类生成代理对象，
 *              方法被调用时，每一次调用获取 Bean 都会在容器中检查该 Bean 是否，从而得到之前单例组件
 *          ii. Lite 轻量级配置（proxyBeanMethods = false）
 *              不会为配置类生成代理对象，
 *              方法被调用时，会跳过容器中 Bean 是否存在的检查，每一次调用配置类中的方法都会产生新的对象，启动运行速度快
 *      （4）最佳实践
 *          i. 如果配置类的组件之间没有依赖关系，使用 Lite 模式，减少判断，提高 SpringBoot启动加载速度
 *          ii. 如果配置的组件之间有依赖关机，使用 Full 模式，保证依赖的组件就是容器中的组件
 */
@Configuration(proxyBeanMethods = true)
public class MyConfig {

    /**
     * 使用 @Bean 给容器中添加组件。
     * 默认以方法名作为组件的 id，以返回类型作为组件的 class 属性
     *
     * 在本例中，相当于：
     * <bean id="user" class="com.springboot.demo.bean.User">
     *     <property name="name" value="zhangsan" />
     *     <property name="age" value="18" />
     * </bean>
     */
    @Bean
    public User user() {
        User zhangsan = new User("zhangsan", 18);
        /* 这里观察 proxyBeanMethods 值对注入 Pet 属性值的不同：当为 true 时，用户中的 Pet 实例与容器中的实例是相同的（即，组件依赖）  */
        zhangsan.setPet(pet());
        return zhangsan;
    }

    /**
     * 也可以在 @Bean 中显式指定 id
     *
     * 在本例中，相当于：
     * <bean id="fantong" class="com.springboot.demo.bean.User">
     *     <property name="name" value="fantong" />
     * </bean>
     */
    @Bean("fantong")
    public Pet pet() {
        System.out.println("@Configuration 中的 Bean 注入");
        return new Pet("fantong");
    }
}
