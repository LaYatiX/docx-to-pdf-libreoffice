package pl.gpiwosz.wordpdfconverter.components;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.FileIdempotentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Apache Camel route configuration for processing DOCX files from a file system and sending them to an ActiveMQ queue.
 * This route also implements idempotent consumption to prevent duplicate processing of files.
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Component
public class FileRoute extends RouteBuilder {

  /**
   * The input path for the file consumer, read from application properties.
   */
  @Value("${input.path}")
  String inputPath;

  /**
   * The path for the idempotent repository, read from application properties.
   */
  @Value("${idempotent.path}")
  String idempotentRepo;

  /**
   * Creates and configures a FileIdempotentRepository for tracking processed files.
   * This repository uses a file-based storage mechanism to persist the processed file names.
   *
   * @return An IdempotentRepository instance configured for file-based storage.
   */
  public IdempotentRepository fileIdempotencyRepository() {
    Map<String, Object> fileMap = new HashMap<>();
    return new FileIdempotentRepository(new File(idempotentRepo + "/idempotent_repo"), fileMap);
  }

  /**
   * Configures the Camel routes.
   * This method defines two routes:
   * <ol>
   *     <li>A route that consumes DOCX files from the specified input path, applies idempotent consumption using the {@link #fileIdempotencyRepository()}, and sends them to the "fileQueue" on ActiveMQ.</li>
   *     <li>A route that consumes messages from the "fileQueue" on ActiveMQ and sends them to the "camelFileProcessor" bean for further processing.</li>
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
      .idempotentConsumer(header("CamelFileName"), fileIdempotencyRepository())
      .to("activemq:queue:fileQueue");

    from("activemq:queue:fileQueue")
      .to("bean:camelFileProcessor");
  }
}
