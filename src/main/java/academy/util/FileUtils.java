package academy.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<Path> resolvePaths(List<String> patterns) throws IOException {
        List<Path> result = new ArrayList<>();

        for (String pattern : patterns) {

            //используем PathMatcher для glob целиком
            if (pattern.contains("*")) {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

                //определяем корень для обхода
                Path startDir = getSearchRoot(pattern);

                if (!Files.exists(startDir)) {
                    throw new IOException("Каталог не найден: " + startDir);
                }

                //метод walk для рекурсивного обхода директории
                Files.walk(startDir)
                    //оставляем только обычные файлы (не директории)
                    .filter(Files::isRegularFile)
                    //применяем шаблон к каждому пути
                    .filter(matcher::matches)
                    //каждый путь отправляем на проверку
                    .filter(FileUtils::isValidLogFile)
                    //тут преобразуем каждый путь в абсолютный + убираем из пути значения типа '.' и '..'
                    .forEach(p -> result.add(p.toAbsolutePath().normalize()));

            } else {
                Path path = Paths.get(pattern);

                if (!Files.exists(path)) {
                    throw new IOException("Файл не найден: " + path);
                }

                if (!isValidLogFile(path)) {
                    throw new IOException("Неподдерживаемое расширение: " + path);
                }

                result.add(path.toAbsolutePath().normalize());
            }
        }

        if (result.isEmpty()) {
            throw new IOException("Не найдено допустимых файлов по указанным путям");
        }

        return result;
    }

    //определяем директорию поиска
    private static Path getSearchRoot(String pattern) {
        int star = pattern.indexOf('*');
        if (star == -1) {
            return Paths.get(pattern).getParent();
        }

        String beforeStar = pattern.substring(0, star);
        Path path = Paths.get(beforeStar);

        return Files.isDirectory(path)
            ? path
            : path.getParent() != null ? path.getParent() : Paths.get(".");
    }

    private static boolean isValidLogFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();

        return name.endsWith(".log") || name.endsWith(".txt");
    }
}
