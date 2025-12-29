package academy;

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
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Lite-версия шаблона.
 *
 * Реализуйте:
 * - поиск файлов по локальным путям/глобам
 * - чтение логов, парсинг строк, сбор статистики
 * - запись отчёта в JSON
 */
@Command(
        name = "log-analyzer",
        version = "lite",
        mixinStandardHelpOptions = true,
        exitCodeOnInvalidInput = 2,
        exitCodeOnExecutionException = 1
)
public class Application implements Runnable {

    private static final String UNDEFINED_PARAMETER = "undefined";

    @Option(
            names = {"-p", "--path"},
            required = true,
            arity = "1..*",
            description = "Path(s) or glob(s) to local NGINX log files (.txt/.log)."
    )
    private List<String> inputPaths;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Output report path (.json). Must not exist."
    )
    private Path output;

    public static void main(String[] args) {
        // Логирование входных параметров для проверки работоспособности black-box тестов
        debugArgs(Arrays.asList(args));

        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // TODO: реализуйте логику по парсингу лог-файлов и генерации отчёта (Lite)
        // Требования описаны в README.md
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
                argsPerParameter
                        .get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER))
                        .add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }
        System.out.printf(
                "%s: %s%n",
                description,
                paths.stream()
                        .map(it -> it.contains("*")
                                ? "glob: " + it
                                : "path: %s, exists: %s".formatted(it, Files.exists(Path.of(it))))
                        .collect(Collectors.joining(";")));
    }
}
