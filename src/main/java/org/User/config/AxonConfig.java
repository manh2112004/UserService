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
        // Tạo một ObjectMapper mới hoàn toàn, không ảnh hưởng đến Controller/Postman
        ObjectMapper axonObjectMapper = new ObjectMapper();
        axonObjectMapper.findAndRegisterModules();
        return JacksonSerializer.builder()
                .objectMapper(axonObjectMapper)
                .defaultTyping() // Giữ nguyên để Axon hoạt động đúng logic Aggregate/Event
                .build();
    }
}
