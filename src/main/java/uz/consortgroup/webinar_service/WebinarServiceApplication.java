package uz.consortgroup.webinar_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WebinarServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebinarServiceApplication.class, args);
    }

}
