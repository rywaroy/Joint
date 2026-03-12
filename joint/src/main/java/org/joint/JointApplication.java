package org.joint;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.joint.modules.**.mapper")
public class JointApplication {

    public static void main(String[] args) {
        SpringApplication.run(JointApplication.class, args);
    }

}
