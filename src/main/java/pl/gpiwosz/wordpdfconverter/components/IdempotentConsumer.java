package pl.gpiwosz.wordpdfconverter.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.gpiwosz.wordpdfconverter.enums.FileStatusEnum;
import pl.gpiwosz.wordpdfconverter.repositories.RedisIdempotentRepository;

import java.net.URISyntaxException;
import java.time.Duration;

@Component
public class IdempotentConsumer {

  @Value("${spring.data.redis.host}")
  String redisHost;

  @Bean
  public RedisIdempotentRepository redisFileIdempotentRepository() throws URISyntaxException {
    return RedisIdempotentRepository.redisIdempotentRepository("redis://"+redisHost+":6379", "docx-pdf-idempotent", FileStatusEnum.READ, Duration.ofDays(5));
  };

  @Bean
  public RedisIdempotentRepository redisQueueIdempotentRepository() throws URISyntaxException {
    return RedisIdempotentRepository.redisIdempotentRepository("redis://"+redisHost+":6379", "docx-pdf-idempotent", FileStatusEnum.PROCESSED, Duration.ofDays(5));
  };
}
