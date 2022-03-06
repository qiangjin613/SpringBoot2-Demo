package com.springboot.demo.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 只有在容器中的组件才能拥有 SpringBoot 提供的强大功能，
 * 所有这个可选择使用 @Component 将 MyCar 放到容器中。
 *
 * 需要注意的是：要为这些字段添加 Getter 和 Setter 方法
 */
@Component
@ConfigurationProperties(prefix = "car")
public class MyCar {

    private String brand;
    private Integer price;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
