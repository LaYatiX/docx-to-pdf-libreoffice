package pl.gpiwosz.wordpdfconverter.components;

import org.apache.camel.Headers;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Component responsible for sending files as messages to a JMS queue using Spring's {@link JmsTemplate}.
 *
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Component("camelFileSender")
public class CamelFileSender {

  private final JmsTemplate jmsTemplate;

  /**
   * Constructs a new CamelFileSender with the specified {@link JmsTemplate}.
   *
   * @param jmsTemplate The Spring JmsTemplate used for sending messages.
   */
  public CamelFileSender(final JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  /**
   * Processes the incoming file content and sends it as a message to the "fileQueue" JMS queue.
   * This method receives the file content as a byte array and extracts the filename from the Camel headers for logging purposes.
   * It then uses the {@link JmsTemplate} to convert and send the file content to the specified queue.
   *
   * @param file    The byte array containing the file content.
   * @param headers The Camel headers associated with the message, containing metadata such as the filename.
   */

  public void process(final byte[] file, @Headers final Map<String, Object> headers) {
    String fileName = (String) headers.get("CamelFileName");
    System.out.println("Processing file: " + fileName);
    jmsTemplate.convertAndSend("fileQueue", file);
  }
}
