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

- @ConditionalOnBean、@ConditionalOnMissingBean：容器中存在指定类时加载
- @ConditionalOnClass、@ConditionalOnMissingClass：类路径中存在指定类时加载
- @ConditionalOnResource
- @ConditionalOnJava
- @ConditionalOnWebApplication、ConditionalOnNotWebApplication
- @ConditionalOnSingleCandidate
- @ConditionalOnProperty
- 等等

注：在使用 @ConditionalOnBean、@ConditionalOnMissingBean 时可能会因 Bean 的注入顺序造成条件装配失效的问题，这里提供两种解决：

- 可以根据 @ConditionalOnClass、@ConditionalOnMissingClass 进行替代；
- 像 DispatcherServletAutoConfiguration 中那样，使用 @AutoConfigureOrder + @AutoConfigureAfter 指定注册 Bean 的顺序和先前条件（在某个配置类之前/后进行注册）。

#### 导入 Spring 配置文件：@ImportResource

对于已经有 .xml 方式的配置文件的时候，我们可以使用 @ImportResource 将该配置文件中的内容导入进来（说白了，是对于已有的配置文件懒得改了。。。）。

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

- 从 SpringBoot 的核心注解 @SpringBootApplication 开始，该注解是由 @SpringBootConfiguration、@EnableAutoConfiguration 和 @ComponentScan 复合而成。

- 其中最值得关注的是 @EnableAutoConfiguration 注解，是 @AutoConfigurationPackage 和 @Import(AutoConfigurationImportSelector.class) 的合成。

  - @AutoConfigurationPackage：自动配置包的注解：***指定的默认包规则。***

    ```java
    @Import(AutoConfigurationPackages.Registrar.class)
    public @interface AutoConfigurationPackage
    ```

    可以看到，其实还是一个 @Import 注解，在 AutoConfigurationPackages.Registrar 中：

    ```java
    static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {
    
        /**
         * metadata 注解的原信息，这里是指主配置类：com.springboot.demo.MyApplication
         */
        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            // new PackageImports(metadata).getPackageNames().toArray(new String[0]) 相当于是一个字符串数组：{"com.springboot.demo"} 拿到了主配置类的包路径，相当于把主配置类包下的组件批量注册进来。
            register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
        }
    
        @Override
        public Set<Object> determineImports(AnnotationMetadata metadata) {
            return Collections.singleton(new PackageImports(metadata));
        }
    }
    ```

    由上述代码分析，@AutoConfigurationPackage 的作用是将指定的一个包下的所有组件导入进来，即把主配置类包（com.springboot.demo）下的组件批量注册进来。

    *这就解释了为什么默认所在的包路径是 MainApplication 所在的包。*

  - @Import(AutoConfigurationImportSelector.class)：在 AutoConfigurationImportSelector 类中，使用 getAutoConfigurationEntry() 给容器根据条件装配规则，按需批量导入组件

    1. getCandidateConfigurations() 方法获取到所有需要导入到容器中的组件（配置类）；
       1. 使用 Spring 的工厂加载器根据名字加载一系列的组件：通过 SpringFactoriesLoader.loadFactoryNames()，也就是在这个方法从 META-INF/spring.factories 中加载文件（默认扫描当前系统所有 META-INF/spring.factories 位置的文件）（核心的就是：spring-boot-autoconfigure 这个包里面的 spring.factories 文件，写死了 SpringBoot 一启动，就要给文件加载的所有配置（组件），在这些组件中，可以看到众多的 @Conditional 条件装配的注解的使用）
    2. getConfigurationClassFilter().filter(configurations) 方法按需过滤不需要的配置类。（条件装配中不需要加载的配置类）

    ```java
    /** 
     * 给容器中导入一些组件
     * annotationMetadata 值为 com.springboot.demo.MyApplication
     */
    protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
    	if (!isEnabled(annotationMetadata)) {
    		return EMPTY_ENTRY;
    	}
        AnnotationAttributes attributes = getAttributes(annotationMetadata);
        /* 在这个方法中获取所有的候选配置类（组件） */
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
        configurations = removeDuplicates(configurations);
        Set<String> exclusions = getExclusions(annotationMetadata, attributes);
        checkExcludedClasses(configurations, exclusions);
        configurations.removeAll(exclusions);
        /* 对候选的配置类进行过滤（按需加载） */
        configurations = getConfigurationClassFilter().filter(configurations);
        fireAutoConfigurationImportEvents(configurations, exclusions);
        return new AutoConfigurationEntry(configurations, exclusions);
    }
    
    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        /* 就是在这个方法中添加了 133 个组件 */
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
                                                                             getBeanClassLoader());
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
                        + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }
    ```

    有几个方法：getCandidateConfigurations() 中调用了 SpringFactoriesLoader.loadFactoryNames() 方法：

    ```java
    public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
        ClassLoader classLoaderToUse = classLoader;
        if (classLoader == null) {
            classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
        }
    
        String factoryTypeName = factoryType.getName();
        /* 重点！ */
        return (List)loadSpringFactories(classLoaderToUse).getOrDefault(factoryTypeName, Collections.emptyList());
    }
    
    private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
        Map<String, List<String>> result = (Map)cache.get(classLoader);
        if (result != null) {
            return result;
        } else {
            HashMap result = new HashMap();
    
            try {
                /* 这里是重点！！！绑定了配置文件中的值 */
                Enumeration urls = classLoader.getResources("META-INF/spring.factories");
        ... ...
    }
    ```

## 二、SpringBoot 启动流程

> Version：SpringBoot 2.6.4

SpringApplication.run(PxksStarter.class, args)

	|-- 使用构造器创建 SpringApplication
   
		|-- 推断 webApplicationType 类型，SERVLET
      
		|-- 查找 bootstrapRegistryInitializers（List）初始启动引导器列表
      
			|-- getSpringFactoriesInstances(Class<T> type) 获取初始化器
         
				|-- 使用 SpringFactoriesLoader.loadFactoryNames() 在所有的 META-INF/spring.factories 中查找 bootstrapRegistryInitializers
            
				|-- 在 createSpringFactoriesInstances() 中使用上一步查出来的全类名列表使用 Clss.forName() 逐一获取对应的 Class 对象
            
				|-- 使用 AnnotationAwareOrderComparator.sort() 对这些组件进行排序（底层使用了 List.sort()）
            
		|-- 查找 initializers（List）初始化器列表
      
			|-- getSpringFactoriesInstances(Class<T> type) 初始化器列表
         
		|-- 查找 listeners（List）监听器列表
      
			|-- getSpringFactoriesInstances(Class<T> type) 获取监听器
         
		|-- 推断 mainApplicationClass 类型，class cn.zjcdjk.fsws.pxks.start.PxksStarter
      
			|-- 使用 deduceMainApplicationClass() 推断主应用程序的类型：就是遍历其中所包含 main 方法的类去查找主运行类
         
	|-- 调用 SpringApplication 实例的 run() 运行 SpringApplication
   
		|-- 标记应用的启动时间
      
		|-- 创建引导上下文 bootstrapContext
      
			|-- createBootstrapContext() 中遍历 bootstrapRegistryInitializers，执行 intitialize() 来完成对引导启动器上下文环境设置
         
		|-- configureHeadlessProperty() 配置 headless 模式：java.awt.headless（自立更生模式）
      
		|-- 获取 SpringApplicationRunListeners 实例 listeners（运行监听器，与创建阶段的不一样）：为了方便所有 Listener 进行事件感知
      
			|-- getRunListeners()
         
				|-- getSpringFactoriesInstances() 获取运行监听器
            
		|-- 遍历 SpringApplicationRunListener 调用 starting 方法：相当于通知所有感兴趣 系统正在启动过程的人，项目正在启动中：listeners.starting(bootstrapContext, this.mainApplicationClass)
      
		|-- try {
      
		|-- 保存命令行参数 applicationArguments
      
		|-- 准备环境信息 environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
      
			|-- 返回或者创建基础环境信息对象：getOrCreateEnvironment()
         
				|-- 通过 webApplicationType switch-case 创建一个 ApplicationServletEnvironment 实例
            
			|-- 配置环境信息对象：configureEnvironment(environment, applicationArguments.getSourceArgs())
         
				|-- 给 environment 设置类型转换器：ConversionService
            
				|-- 加载外部的配置源：configurePropertySources()，相当于加载系统的所有配置信息
            
			|-- 绑定环境信息
         
			|-- （运行监听器）listeners 调用 environmentPrepared() 通知所有的运行监听器当前环境准备完成
         
		|-- configureIgnoreBeanInfo() 配置需要忽略的环境信息
      
		|-- printBanner() 打印 Banner
      
		|-- 创建 IoC 容器：context = createApplicationContext()
      
			|-- 根据 webApplicationType 当前项目类型创建容器类型：这里是 AnnotationConfigServletWebServerApplicationContext 的实例
         
		|-- 给容器设置 applicationStartup 信息
      
		|-- 准备 IoC 容器信息：prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner)
      
			|-- 保存环境信息：context.setEnvironment(environment)
         
			|-- IoC 容器的后置处理流程：postProcessApplicationContext(context)
         
				|-- ...
            
			|-- 应用初始化器：applyInitializers(context)
         
				|-- 使用准备环境中找到的 initializers 对 IoC 容器进行扩展初始化：for-each -> initializer.initialize(context)
            
			|-- 调用 listeners.contextPrepared(context) 通知所有的 listener（EventPublishingRunListener） 调用 contextPrepared() 通知 IoC 容器的上下文环境已经准备好了
         
			|-- 关闭 bootstrapContext：bootstrapContext.close(context)
         
			|-- 获取 beanFactory：ConfigurableListableBeanFactory beanFactory = context.getBeanFactory()
         
			|-- 将命令行参数、Banner以组件的形式注册到 IoC 容器中：beanFactory.registerSingleton("springApplicationArguments", applicationArguments)、beanFactory.registerSingleton("springBootBanner", printedBanner)
         
			|-- （运行监听器）listeners 通知所有的运行监听器 IoC 容器准备完成：listeners.contextLoaded(context)
         
		|-- 刷新 IoC 容器：refreshContext(context)
      
			|-- 里面有 IoC 容器经典的初始化代码：org.springframework.context.support.AbstractApplicationContext#refresh 创建容器中的所有组件
         
		|-- 容器刷新完成后工作：afterRefresh(context, applicationArguments)：在当前版本啥也没干
      
		|-- 计算应用的启动时间，并打印
      
		|-- （运行监听器）listeners 通知所有的运行监听器应用启动完成：listeners.started(context, timeTakenToStartup)
      
		|-- 调用所有 runners：callRunners(context, applicationArguments)
      
			|-- 在 IoC 容器中根据 Bean的类型获取所有的 ApplicationRunner、CommandLineRunner 的 runner
         
			|-- 对上述的 runner 进行排序：AnnotationAwareOrderComparator.sort(runners)（根据 @Order 注解进行排序）
         
			|-- 遍历所有的 runner，调用 run()
         
		|-- catch { 如果抛出异常：对所有的 listeners 调用 listeners.failed(context, exception)，在 finally 中关闭 监听器 }
      
		|-- try {
      
		|-- （运行监听器）listeners 通知所有的运行监听器启动：listeners.ready(context, timeTakenToReady)
      
		|-- catch { 如果抛出异常：对所有的 listeners 调用 listeners.failed(context, exception)，在 finally 中关闭 监听器 }
