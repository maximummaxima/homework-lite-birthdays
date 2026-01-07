package academy.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Report {
    public List<String> files;
    public int totalRequestsCount;
    public ResponseSize responseSizeInBytes;
    public List<ResourceCount> resources;
    public List<StatusCodeCount> responseCodes;

    public static Report build(List<LogEntry> entries, List<Path> files) {
        Report report = new Report();
        report.files = files.stream()
            .map(Path::getFileName)
            .map(Path::toString)
            .toList();
        report.totalRequestsCount = entries.size();

        if (entries.isEmpty()) {
            report.responseSizeInBytes = new ResponseSize(0.0, 0L);
            report.resources = List.of();
            report.responseCodes = List.of();

            return report;
        }

        //средний и максимальный размер
        double avg = entries.stream()
            .mapToLong(e -> e.responseSize)
            .average()
            .orElse(0.0);
        double max = entries.stream()
            .mapToLong(e -> e.responseSize)
            .max()
            .orElse(0L);
        avg = Math.round(avg * 100.0) / 100.0;
        report.responseSizeInBytes = new ResponseSize(avg, max);

        //коды ответов
        Map<Integer, Long> codeCounts = entries.stream()
            .collect(Collectors.groupingBy(e -> e.statusCode, Collectors.counting()));
        report.responseCodes = codeCounts.entrySet().stream()
            .map(e -> new StatusCodeCount(e.getKey(), e.getValue().intValue()))
            .sorted((a, b) -> Integer.compare(b.totalResponsesCount, a.totalResponsesCount))
            .toList();

        //топ-5 ресурсов
        Map<String, Long> resourceCounts = entries.stream()
            .collect(Collectors.groupingBy(e -> e.resource, Collectors.counting()));
        report.resources = resourceCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(e -> new ResourceCount(e.getKey(), e.getValue().intValue()))
            .toList();

        return report;
    }

    public static class ResponseSize {
        public double average;
        public double max;

        public ResponseSize(double average, double max) {
            this.average = average;
            this.max = max;
        }
    }

    public static class ResourceCount {
        public String resource;
        public int totalRequestsCount;

        public ResourceCount(String resource, int totalRequestsCount) {
            this.resource = resource;
            this.totalRequestsCount = totalRequestsCount;
        }
    }

    public static class StatusCodeCount {
        public int code;
        public int totalResponsesCount;

        public StatusCodeCount(int code, int totalResponsesCount) {
            this.code = code;
            this.totalResponsesCount = totalResponsesCount;
        }
    }
}
