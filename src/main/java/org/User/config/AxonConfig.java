package org.User.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AxonConfig {
    @Bean
    @Primary
    public Serializer jacksonSerializer(ObjectMapper objectMapper) {
        // Cấu hình ObjectMapper để xử lý tốt các Java 8 Date/Time và các kiểu dữ liệu mới
        objectMapper.findAndRegisterModules();

        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .defaultTyping() // Giúp Axon hiểu đúng kiểu dữ liệu khi deserialize
                .build();
    }
}
