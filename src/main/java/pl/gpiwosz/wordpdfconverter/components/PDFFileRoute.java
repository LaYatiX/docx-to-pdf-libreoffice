package pl.gpiwosz.wordpdfconverter.components;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gpiwosz.wordpdfconverter.enums.FileStatusEnum;
import pl.gpiwosz.wordpdfconverter.repositories.RedisIdempotentRepository;

/**
 * Apache Camel route configuration for processing DOCX files from a file system and sending them to an ActiveMQ queue.
 * This route also implements idempotent consumption to prevent duplicate processing of files.
 *
 * @author Grzegorz Piwosz
 * @version 1.0
 * @since 2025-01-01
 */
@Component()
public class PDFFileRoute extends RouteBuilder {

  private final RedisIdempotentRepository redisFileIdempotentRepository;
  private final RedisIdempotentRepository redisQueueIdempotentRepository;

  /**
   * The input path for the file consumer, read from application properties.
   */
  @Value("${input.path}")
  String inputPath;

  /**
   * Creates and configures a RedisIdempotentRepository for tracking processed files.
   * This repository uses a redis-based storage mechanism to persist the processed file names.
   */
  public PDFFileRoute(RedisIdempotentRepository redisFileIdempotentRepository, RedisIdempotentRepository redisQueueIdempotentRepository) {
    this.redisFileIdempotentRepository = redisFileIdempotentRepository;
    this.redisQueueIdempotentRepository = redisQueueIdempotentRepository;
  }

  /**
   * Configures the Camel routes.
   * This method defines two routes:
   * <ol>
   *     <li>A route that consumes DOCX files from the specified input path, applies idempotent consumption using the {@link #redisFileIdempotentRepository}, and sends them to the "fileQueue" on ActiveMQ.</li>
   *     <li>A route that consumes messages from the "fileQueue", applies idempotent consumption using the {@link #redisQueueIdempotentRepository} on ActiveMQ and sends them to the "camelFileProcessor" bean for further processing.</li>
   * </ol>
   * The file consumer is configured to:
   * <ul>
   *     <li>Only include files ending with ".docx".</li>
   *     <li>Not move the processed files (noop=true).</li>
   *     <li>Use idempotent consumption to prevent duplicate processing.</li>
   * </ul>
   */
  @Override
  public void configure() {
    from("file:" + inputPath + "?include=.*.docx&noop=true&idempotent=true")
      .idempotentConsumer(header("CamelFileName"), this.redisFileIdempotentRepository)
      .to("activemq:queue:fileQueue");

    from("activemq:queue:fileQueue")
      // forward only not PROCESSED or currently not PROCESSING files
      .filter(exchange -> {
        String key = exchange.getIn().getHeader("CamelFileName", String.class);
        return !redisQueueIdempotentRepository.containsValue(key, FileStatusEnum.PROCESSED.toString())
          && !redisQueueIdempotentRepository.containsValue(key, FileStatusEnum.PROCESSING.toString());
      })
      .to("bean:camelFileProcessor");


    /*
    * POTENTIAL IMPROVEMENTS
    *
    * Additionally, if machine is busy, we can log a message and send the message back to the queue
    * */
//    from("activemq:queue:fileQueue")
//      .filter(exchange -> {
//        String key = exchange.getIn().getHeader("CamelFileName", String.class);
//        return !redisQueueIdempotentRepository.containsValue(key, FileStatusEnum.PROCESSED.toString())
//          && !redisQueueIdempotentRepository.containsValue(key, FileStatusEnum.PROCESSING.toString());
//      })
//      .choice()
//        .when(exchange -> libreOfficeConverter.getNumberOfCurrentTasks().get() <= Runtime.getRuntime().availableProcessors()) // check if machine is busy
//        .to("bean:camelFileProcessor")
//      .otherwise()
//        .log("Message cannot be processed at the moment")
//        .to("activemq:queue:fileQueue").
//      endChoice();
  }
}