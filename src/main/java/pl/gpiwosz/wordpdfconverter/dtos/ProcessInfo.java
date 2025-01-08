package pl.gpiwosz.wordpdfconverter.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProcessInfo {
    private String pid;
    private String memUsage;
}
