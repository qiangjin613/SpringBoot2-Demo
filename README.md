# SpringBoot2-Demo

## 1. 自动配置

用于代替繁琐的 xml 配置

### 1.1 基础：相关注解解析

#### 方式一：使用 @Configuration + @Bean 注册组件

> 代码位置：com.springboot.demo.conf.MyConfig、com.springboot.demo.MyApplication

1. 使用 @Configuration 标记类：告诉 SpringBoot 这是一个配置类（作用等同于 .xml 配置 Bean）
2. 在配置类中，使用 @Bean 给容器注册组件，默认是单实例的
3. 使用 @Configuration 修饰的类也是一个组件（查看 @Configuration 源码，发现其被 @Component 标注）
4. SpringBoot2 的最大特性：@Configuration 的 proxyBeanMethods 属性
   - 在 SpringBoot2 新增的一个参数（基于 Spring 5.2），用于解决组件依赖的场景
   - 作用：是否代理 Bean 的方法
     - 为 true 时（默认）：SpringBoot 会通过 CGLIB 创建标注类的代理对象，用于保持组件的单实例；无论外部对配置类中的这个组件注册方法调用多少次，获取的对象都是之前注册容器中的单实例
     - 为 false 时：注册到容器中的是配置类本身（不是代理对象），当获取到配置类的实例，然后调用方法时，如：本例中的 user()，会创建一个新的 User 对象
   - 基于以上属性值的不同，衍生了 @Configuration 在底层的两个配置模式：Full、Lite
     - Full 全配置（proxyBeanMethods = true）：SpringBoot为配置类生成代理对象，方法被调用时，每一次调用获取 Bean 都会在容器中检查该 Bean 存不存在，从而得到之前单例组件
     - Lite 轻量级配置（proxyBeanMethods = false）：不会为配置类生成代理对象，会跳过容器中 Bean 是否存在的检查，每一次调用配置类中的方法都会产生新的对象，启动运行速度快
   - 最佳实践
     - 如果配置类的组件之间没有依赖关系，使用 Lite 模式，减少判断，提高 SpringBoot启动加载速度
     - 如果配置的组件之间有依赖关机，使用 Full 模式，保证依赖的组件就是容器中的组件

#### 方式一：使用 @Component、@Controller、@Service、@Response + @Bean 注册组件



