package pl.gpiwosz.wordpdfconverter.services;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.gpiwosz.wordpdfconverter.config.TempDirConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service responsible for converting files to PDF using LibreOffice via JODConverter.
 * This service uses an asynchronous approach to handle conversions.
 *
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Service
public class LibreOfficeConverterService {

  private final DocumentConverter converter;
  private final String outputPath;
  private final ExecutorService executor;
  private final TempDirConfig tempDirConfig;

  /**
   * Constructs a new LibreOfficeConverterService.
   *
   * @param outputPath    The output path for converted files, read from application properties.
   * @param converter     The JODConverter DocumentConverter instance.
   * @param tempDirConfig The TempDirConfig instance for managing temporary files.
   */
  public LibreOfficeConverterService(@Value("${output.path}") final String outputPath, final DocumentConverter converter, final TempDirConfig tempDirConfig) {
    this.converter = converter;
    this.outputPath = outputPath;
    this.tempDirConfig = tempDirConfig;
    this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  /**
   * Converts a file (represented as a byte array) to PDF asynchronously.
   * The conversion process happens in a separate thread using an ExecutorService.
   * The input file is first written to a temporary file, then converted, and finally the temporary file is deleted.
   *
   * @param fileBytes The byte array containing the file content.
   * @param fileName  The original file name.
   * @throws Exception If an error occurs during file processing or conversion. This includes exceptions thrown by the underlying JODConverter library.
   */
  @Async
  public void convertFile(final byte[] fileBytes, final String fileName) throws Exception {
    File inputFile = createFileFromByteArray(fileBytes, fileName);
    String outputFileName = outputPath + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".pdf";
    File outputFile = new File(outputFileName);
    executor.execute(() -> {
      try {
        converter.convert(inputFile).to(outputFile).execute();
        inputFile.delete();
      } catch (OfficeException e) {
        throw new RuntimeException(e); // Re-throw as RuntimeException so it is not necessary to handle it in caller method
      }
    });
  }

  /**
   * Creates a temporary file from a byte array.
   *
   * @param data     The byte array containing the file content.
   * @param filePath The desired file path (used for creating the temporary file name).
   * @return The created temporary File object.
   * @throws IOException          If an I/O error occurs during file creation.
   * @throws IllegalArgumentException If the input byte array or file path is null or empty.
   */
  private File createFileFromByteArray(final byte[] data, final String filePath) throws IOException {
    if (data == null) {
      throw new IllegalArgumentException("Byte array cannot be null.");
    }
    if (filePath == null || filePath.isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty.");
    }

    // Create temp file
    String temFilePath = Files.createTempFile(tempDirConfig.getTempDirPath(), "_temp", "_data").toString();

    File file = new File(temFilePath);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(data);
    }
    return file;
  }
}
