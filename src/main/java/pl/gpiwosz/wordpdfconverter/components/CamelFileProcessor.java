package pl.gpiwosz.wordpdfconverter.components;

import org.apache.camel.Headers;
import org.springframework.stereotype.Component;
import pl.gpiwosz.wordpdfconverter.repositories.RedisIdempotentRepository;
import pl.gpiwosz.wordpdfconverter.services.LibreOfficeConverterService;
import java.util.Map;

/**
 * Component responsible for processing files received by Apache Camel, converting them using LibreOffice.
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Component("camelFileProcessor")
public class CamelFileProcessor {

  private final LibreOfficeConverterService libreOfficeConverter;

  /**
   * Constructs a new CamelFileProcessor with the specified LibreOfficeConverterService.
   *
   * @param libreOfficeConverter The service used for converting files using LibreOffice.
   */
  public CamelFileProcessor(LibreOfficeConverterService libreOfficeConverter, RedisIdempotentRepository redisQueueIdempotentRepository) {
    this.libreOfficeConverter = libreOfficeConverter;
  }

  /**
   * Processes the incoming file content.
   * This method receives the file content as a byte array and extracts the filename from the Camel headers.
   * It then delegates the conversion to the {@link LibreOfficeConverterService}.
   *
   * @param files   The byte array containing the file content.
   * @param headers The Camel headers associated with the message, containing metadata such as the filename.
   * @throws Exception If an error occurs during file processing or conversion. This can include exceptions thrown by the underlying {@link LibreOfficeConverterService}.
   */
  public void process(byte[] files, @Headers Map<String, Object> headers) throws Exception {
    String fileName = (String) headers.get("CamelFileName");
    System.out.println("Processing file: " + fileName);
    libreOfficeConverter.convertFile(files, fileName);
  }
}
