# HELP (Lite)

В lite-версии поддерживаются только параметры:

- `-p, --path` — путь/шаблон до локальных лог-файлов (`.txt`, `.log`)
- `-o, --output` — путь до выходного отчёта (`.json`, файл не должен существовать)

Пример:

```bash
java -jar log-analyzer.jar -p logs/*.txt -o report.json
```

Коды завершения:
- `0` — OK
- `1` — unexpected error
- `2` — invalid arguments / invalid files
