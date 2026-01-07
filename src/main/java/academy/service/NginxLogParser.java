package academy.service;

import academy.model.LogEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NginxLogParser {
    //регулярка согласно примерам логов
    // ^ - начало строки
    // (\\S+) - группа 1: IP-адрес (не-пробельные символы)
    // \\S+ \\S+ - пропускаем два поля (идентификатор, пользователь)
    // \\[[^]]+\\] - дата и время в квадратных скобках
    // \" - открывающая кавычка
    // (\\S+) - группа 2: HTTP-метод (GET, POST и т.д.)
    // ([^\\s]+) - группа 3: ресурс/URL (все до пробела)
    // (\\S+) - группа 4: HTTP-версия
    // \" - закрывающая кавычка
    // (\\d{3}) - группа 5: статус-код (ровно 3 цифры)
    // (\\d+|-) - группа 6: размер ответа (число или дефис)
    // \"[^\"]*\" \"[^\"]*\" - два поля в кавычках (referer, user-agent)
    // $ - конец строки
    private static final Pattern PATTERN = Pattern.compile(
        "^(\\S+) \\S+ \\S+ \\[[^]]+\\] \"(\\S+) ([^\\s]+) (\\S+)\" (\\d{3}) (\\d+|-) \"[^\"]*\" \"[^\"]*\"$"
    );

    public LogEntry parse(String line) {
        //проверяем на null или пустую строку
        if (line == null || line.isBlank()) {
            return null;
        }

        //создаём Matcher для применения регулярки к строке
        Matcher match = PATTERN.matcher(line);

        if (!match.matches()) {
            System.out.println("Пропущена недопустимая строка: " + line);

            return null;
        }

        try {
            //извлекаем данные из групп регулярного выражения:
            //группа 3: ресурс/URL
            String resource = match.group(3);
            //группа 5: HTTP-статус код
            int statusCode = Integer.parseInt(match.group(5));
            //группа 6: размер ответа
            String sizeStr = match.group(6);
            //если размер "-" (обычно означает 0 или не применимо), то 0
            //иначе преобразуем строку в long
            long size = "-".equals(sizeStr) ? 0L : Long.parseLong(sizeStr);

            return new LogEntry(resource, statusCode, size);
        } catch (Exception e) {
            System.out.println("Ошибка парсинга строки: " + line);

            return null;
        }
    }
}
