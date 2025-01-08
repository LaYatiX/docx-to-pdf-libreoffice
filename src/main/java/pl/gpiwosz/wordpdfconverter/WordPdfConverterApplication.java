package pl.gpiwosz.wordpdfconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WordPdfConverterApplication {

  public static void main(String[] args) {
    SpringApplication.run(WordPdfConverterApplication.class, args);
  }
}
