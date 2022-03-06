# SpringBoot2-Demo

## 一、自动配置

用于代替繁琐的 xml 配置，对于这些注解的使用要使用 @ComponentScan  添加到包扫描的范围内。一般在 SpringBoot 主程序类（也称主配置类）中进行标注。

### 1. 相关注解解析

#### 注册组件（方式一）：使用 @Configuration + @Bean 注册组件

> 代码位置：com.springboot.demo.conf.MyConfig、com.springboot.demo.MyApplication#testConfiguration

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

#### 注册组件（方式二）： 使用 @Component、@Controller、@Service、@Response + @Bean 注册组件

对于之前在 Spring 中的操作也是可以的：

- @Component：标注一个组件
- @Controller：标注一个控制器
- @Service：标注一个业务逻辑组件
- @Response：标注一个数据库层组件

在 Spring IoC 的过程中，先对被注册注解标记的类进行初始化。然后进行 Bean 的注入，在注入 Bean 时，先注入 @Component、@Controller、@Service、@Response 中的 Bean，然后注入 @Configuration 中的 Bean。

示例如下：

```
@Configuration 标注的类被初始化
@Component 标注的类被初始化
@Component 中的 Bean 注入
@Configuration 中的 Bean 注入
```

#### 导入组件：@Import

> 代码位置：com.springboot.demo.conf.ImportTest、com.springboot.demo.MyApplication#testImport

可以使用在任何一个配置类或组件中，将指定的类型导入到容器中，组件的名字默认为导入类型的全类名。

#### 条件装配：@Conditional

> 代码位置：com.springboot.demo.conf.ConditionalTest、com.springboot.demo.MyApplication#testConditional

满足 @Conditional 指定的条件后，进行组件注入。可以使用在 类、方法上。

@Conditional 是一个根注解，有很多的派生注解，常用的有：

- @ConditionalOnBean、@ConditionalOnMissingBean
- @ConditionalOnClass、@ConditionalOnMissingClass
- @ConditionalOnResource
- @ConditionalOnJava
- @ConditionalOnWebApplication、ConditionalOnNotWebApplication
- @ConditionalOnSingleCandidate
- @ConditionalOnProperty
- 等等

注：在使用 @ConditionalOnBean、@ConditionalOnMissingBean 时可能会因 Bean 的注入顺序造成条件装配失效的问题，这里的简单解决方法：可以根据 @ConditionalOnClass、@ConditionalOnMissingClass 进行代替，便可解决。

#### 导入 Spring 配置文件：@ImportResource

对于已经有 .xml 方式的配置文件的时候，我们可以使用 @ImportResource 将该配置文件中的内容导入进来（说白了，是对于已有的配置文件懒得该了。。。）。

#### 配置绑定（方式一）：使用 @ConfigurationProperties + @Component 绑定配置文件

>代码位置：com.springboot.demo.conf.MyCar、com.springboot.demo.MyApplication#car

对于之前使用 Java + Properties 的方式读取配置文件的做法，太过于繁琐、复杂了。SpringBoot 提供了更简洁的方式供我们读取配置文件（application.properties）中的内容。

#### 配置绑定（方式二）：使用 @ConfigurationProperties + @EnableConfigurationProperties + @Component 绑定配置文件

> 代码位置：com.springboot.demo.conf.MyHouse、com.springboot.demo.MyApplication

在使用 @ConfigurationProperties 将配置与类绑定后，要在配置类上使用 @EnableConfigurationProperties 将该配置进行“激活”（这里选则的是在主配置类 MyApplication 上进行激活）。

@EnableConfigurationProperties 的两个功能：

- 开启指定类的属性配置功能
- 把指定类的组件自动注册到容器中

### 2. 自动配置原理解析

