package pl.gpiwosz.wordpdfconverter.components;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.gpiwosz.wordpdfconverter.dtos.ProcessInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class monitors a specific process identified by a command and takes action
 * if its memory usage exceeds a certain threshold.
 *
 * @author Grzegorz Piwosz
 * @version 1.0
 * @since 2025-01-01
 */
@Component
public class ProcessMonitor {

  /**
   * Schedules a periodic check for the target process every 10 minutes.
   *
   * @throws IOException If there is an error reading from the process output.
   * @throws InterruptedException If the process is interrupted while waiting.
   */
  @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
  private void checkProcess() throws IOException, InterruptedException {
    List<String> command = Arrays.asList("/bin/bash", "-c", "ps aux | grep soffice.bin");

    ProcessBuilder builder = new ProcessBuilder(command);
    Process process = builder.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String inputLine;
      while ((inputLine = reader.readLine()) != null) {
        ProcessInfo processInfo = parseProcessLine(inputLine);
        if (processInfo != null) {
          handleProcess(processInfo);
        }
      }
  }

  /**
   * Handles a process based on its memory usage. If the memory usage exceeds 10.0%,
   * the process is terminated.
   *
   * @param processInfo The information about the process.
   * @throws IOException If there is an error during process termination.
   * @throws InterruptedException If the process is interrupted while waiting.
   */
  private void handleProcess(ProcessInfo processInfo) throws IOException, InterruptedException {
    if(Float.parseFloat(processInfo.getMemUsage()) > 10.0) {
        killProcess(processInfo.getPid());
    }
  }

  /**
   * Terminates a process identified by its PID using the `kill` command.
   *
   * @param pid The process ID of the process to terminate.
   * @throws IOException If there is an error during process termination.
   * @throws InterruptedException If the process is interrupted while waiting.
   */
  private void killProcess(String pid) throws IOException, InterruptedException {
    List<String> command = Arrays.asList("/bin/bash", "-c", "kill -9 " + pid);
    ProcessBuilder builder = new ProcessBuilder(command);
    Process process = builder.start();
    process.waitFor();
  }

  /**
   * Parses a line of output from the `ps aux` command and extracts information about a process.
   * The regular expression used for parsing might need adjustment for different systems.
   *
   * @param line A line of output from the `ps aux` command.
   * @return A {@link ProcessInfo} object containing information about the process,
   *         or null if the line doesn't match the expected format.
   */
  private ProcessInfo parseProcessLine(String line) {
    // Regular expression to parse 'ps aux' output (adjust as needed for your system)
    String regex = "^\\S+\\s+(\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(.*)$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);

    if (matcher.find()) {
      String pid = matcher.group(1);
      String cpuUsage = matcher.group(2);
      String memUsage = matcher.group(3);
      String command = matcher.group(4);

      System.out.println("PID: " + pid + ", CPU%: " + cpuUsage + ", MEM%: " + memUsage + ", Command: " + command);

      return ProcessInfo.builder()
        .memUsage(memUsage)
        .pid(pid)
        .build();

    } else {
      return null;
    }
  }

}
