# Тестирование (Lite)

## Acceptance tests

Запуск локально:

```bash
chmod +x scripts/run_acceptance_tests.sh
CI_PROJECT_NAME=local CI_COMMIT_SHA=dev GITLAB_DOCKER_PROXY=docker.io ./scripts/run_acceptance_tests.sh
```

Тесты запускают вашу программу внутри Docker-контейнера и проверяют:
- основные негативные сценарии (нет файла, неверные расширения, отсутствуют параметры)
- один позитивный сценарий (подсчёт статистики)
