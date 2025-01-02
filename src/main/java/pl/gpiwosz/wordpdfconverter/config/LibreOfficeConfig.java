package pl.gpiwosz.wordpdfconverter.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.gpiwosz.wordpdfconverter.services.UtilsService;


/**
 * Configuration class for setting up the LibreOffice integration using JODConverter.
 * This class defines Spring beans for the {@link OfficeManager} and {@link DocumentConverter}.
 *
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Configuration
public class LibreOfficeConfig {
  /**
   * Creates and starts a {@link LocalOfficeManager} instance.
   * This manager is responsible for managing the LibreOffice processes.
   * The port numbers used by LibreOffice are read from the "libreoffice.ports" property.
   *
   * @param ports A comma-separated string of port numbers for LibreOffice, read from application properties.
   * @return A started {@link OfficeManager} instance.
   * @throws Exception If an error occurs during the OfficeManager startup. This can include exceptions thrown by the underlying JODConverter library.
   */
  @Bean
  public OfficeManager officeManager(@Value("${libreoffice.ports}") final String ports) throws Exception {
    LocalOfficeManager officeManager = LocalOfficeManager.builder()
      .portNumbers(UtilsService.getNumbersArray(ports))
      .maxTasksPerProcess(0) // Set to 0 to allow unlimited tasks per process
      .build();
    officeManager.start();
    return officeManager;
  }

  /**
   * Creates a {@link DocumentConverter} instance using the provided {@link OfficeManager}.
   * This converter is used to perform document conversions using LibreOffice.
   *
   * @param officeManager The {@link OfficeManager} instance to be used by the converter.
   * @return A {@link DocumentConverter} instance.
   */
  @Bean
  public DocumentConverter documentConverter(final OfficeManager officeManager) {
    return LocalConverter.builder().officeManager(officeManager).build();
  }
}