package com.sha.client_service.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "grpc.endpoints")
public class GrpcProperties {

    private String address;
    private int port;
}
