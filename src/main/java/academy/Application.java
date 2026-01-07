package academy;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import academy.model.LogEntry;
import academy.model.Report;
import academy.service.NginxLogParser;
import academy.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Lite-версия шаблона.
 * <p>
 * Реализуйте:
 * - поиск файлов по локальным путям/глобам
 * - чтение логов, парсинг строк, сбор статистики
 * - запись отчёта в JSON
 */
@Command(name = "log-analyzer", version = "lite", mixinStandardHelpOptions = true, exitCodeOnInvalidInput = 2,
        exitCodeOnExecutionException = 1)
public class Application implements Runnable {

    private static final String UNDEFINED_PARAMETER = "undefined";

    @Option(names = {"-p", "--path"}, required = true, arity = "1..*", description =
            "Path(s) or glob(s) to local " + "NGINX log files (.txt/.log).")
    private List<String> inputPaths;

    @Option(names = {"-o", "--output"}, required = true, description = "Output report path (.json). Must not exist.")
    private Path output;

    public static void main(String[] args) {
        // Логирование входных параметров для проверки работоспособности black-box тестов
        debugArgs(Arrays.asList(args));

        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        //валидация выходного файла
        validateOutputFile();

        //валидация входящих данных (путей файла или файлов)
        List<Path> logFiles = resolveLogFiles();

        //анализ
        List<LogEntry> entries = parseLogFiles(logFiles);
        Report report = Report.build(entries, logFiles);

        //запись
        writeReport(report);
    }

    private void validateOutputFile() {
        if (!output.toString().endsWith(".json")) {
            System.out.println("Выходной файл должен иметь .json расширение");
            System.exit(2);
        }

        if (Files.exists(output)) {
            System.out.println("Выходной файл уже существует: " + output);
            System.exit(2);
        }

        Path dir = output.getParent();

        if (dir != null && !Files.isWritable(dir)) {
            System.out.println("Выходной каталог недоступен для записи: " + dir);
            System.exit(2);
        }
    }

    private List<Path> resolveLogFiles() {
        try {
            return FileUtils.resolvePaths(inputPaths);
        } catch (Exception e) {
            System.out.println("Не удалось обработать входные пути: " + e);
            System.exit(2);

            return List.of();
        }
    }

    private List<LogEntry> parseLogFiles(List<Path> logFiles) {
        List<LogEntry> entries = new ArrayList<>();
        NginxLogParser parser = new NginxLogParser();

        for (Path file : logFiles) {
            try {
                List<String> list = Files.readAllLines(file);
                for (String str : list) {
                    //отправляяем строку на проверку
                    LogEntry entry = parser.parse(str);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения лог-файла: " + e);
                System.exit(1);
            }
        }

        return entries;
    }

    private void writeReport(Report report) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(output.toFile(), report);
            System.out.println("Отчет записан в: " + output);
        } catch (IOException e) {
            System.out.println("Ошибка записи отчета: " + e);
            System.exit(1);
        }
    }

    // Note: нужно только для отладки, удалить в случае ненадобности
    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        System.out.printf("Входные параметры программы: %s%n", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Путь к отчету", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;
        while (!queue.isEmpty()) {
            var element = queue.removeFirst();
            if (element.startsWith("-")) {
                currentParameter = element.startsWith("--") ? element.substring(2) : element.substring(1);
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());
            } else {
                argsPerParameter.get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER)).add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }
        System.out.printf("%s: %s%n", description, paths.stream().map(it -> it.contains("*") ? "glob: " + it : ("path" +
                ":" + " %s, exists: %s").formatted(it, Files.exists(Path.of(it)))).collect(Collectors.joining(";")));
    }
}
