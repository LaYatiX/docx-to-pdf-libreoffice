package pl.gpiwosz.wordpdfconverter.services;

import java.util.Arrays;

/**
 * Utility class providing helper methods for common tasks.
 *
 *  @author Grzegorz Piwosz
 *  @version 1.0
 *  @since 2025-01-01
 */
public class UtilsService {
  /**
   * Converts a comma-separated string of numbers into an array of integers.
   * This method handles potential parsing errors and empty strings.
   *
   * @param numbersString The string containing comma-separated numbers (e.g., "8100, 8200").
   * @return An array of integers parsed from the string, or an empty array if the string is null, empty, or contains invalid formats.
   */
  public static int[] getNumbersArray(final String numbersString) {
    int[] numbersArray = new int[0];
    if (numbersString != null) {
      try {
        numbersArray = Arrays.stream(numbersString.split(","))
          .map(String::trim) // Remove whitespace
          .mapToInt(Integer::parseInt)
          .toArray();
      } catch (NumberFormatException e) {
        // Handle parsing errors (e.g., log, throw exception, set default value)
        System.err.println("Invalid number format in my.numbers: " + numbersString);
        numbersArray = new int[0]; // Or set a default array
      }
    }
    return numbersArray;
  }
}
