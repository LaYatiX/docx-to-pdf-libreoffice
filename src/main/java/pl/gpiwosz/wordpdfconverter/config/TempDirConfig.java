package pl.gpiwosz.wordpdfconverter.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration class responsible for managing a temporary directory.
 * This component creates the temporary directory on startup and deletes it on shutdown.
 * The location of the temporary directory is determined by the "output.path" property.
 *
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
@Component
public class TempDirConfig {
  private final Path tempDirPath;

  /**
   * Constructs a new TempDirConfig.
   * The temporary directory path is constructed by appending "/temp" to the path specified by the "output.path" property.
   *
   * @param outputPath The base output path read from application properties.
   */
  public TempDirConfig(@Value("${output.path}") final String outputPath) {
    this.tempDirPath = Path.of(outputPath + "/temp");
  }

  /**
   * Creates the temporary directory if it does not already exist.
   * This method is executed after the app is constructed.
   *
   * @throws Exception If an error occurs during directory creation.
   */
  @PostConstruct
  public void run() throws Exception {
    if (!Files.exists(this.tempDirPath)) {
      Files.createDirectory(this.tempDirPath);
    }
  }

  /**
   * Deletes the temporary directory and all its contents recursively.
   * This method is executed before the app is destroyed.
   *
   * @throws IOException If an I/O error occurs during deletion.
   */
  @PreDestroy
  public void cleanup() throws IOException {
    deleteRecursively(this.tempDirPath);
  }

  /**
   * Recursively deletes a directory and its contents.
   *
   * @param path The path to the directory or file to delete.
   * @throws IOException If an I/O error occurs during deletion.
   */
  private void deleteRecursively(final Path path) throws IOException {
    if (Files.isDirectory(path)) {
      try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
        for (Path entry : entries) {
          deleteRecursively(entry);
        }
      }
    }
    Files.deleteIfExists(path);
  }

  public Path getTempDirPath() {
    return this.tempDirPath;
  }
}
