package org.joint;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("org.joint.modules.**.mapper")
@EnableCaching
@EnableAsync
public class JointApplication {

    public static void main(String[] args) {
        SpringApplication.run(JointApplication.class, args);
    }

}
