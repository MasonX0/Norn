# Исправление проблемы с сохранением загруженных данных

## Проблема

Конфигурация приложения сохранялась при выходе и загружалась при входе, но изменения, сделанные во
вкладке "Загруженные данные" (группы студентов и предприятия), не сохранялись между сессиями.

## Причина

Данные студентов и предприятий хранились только в памяти (`MutableStateFlow` в `NornRepository` и
`NornViewModel`), но не включались в модель настроек `AppSettings`, которая сохраняется в файл
`.norn-settings.json`.

## Решение

### 1. Обновлена модель настроек `AppSettings`

- Добавлены поля `loadedGroups: List<Group>` и `loadedEnterprises: List<Enterprise>`
- Это позволяет сохранять загруженные данные между сессиями

### 2. Добавлены аннотации сериализации

- `@Serializable` добавлены к классам:
    - `Group` и `GroupStatistics`
    - `Student`
    - `Enterprise` и `PracticeSupervisor`
- Это обеспечивает корректную сериализацию данных в JSON

### 3. Обновлен `AppSettingsService`

- Метод `fromCurrentData()` теперь принимает и сохраняет загруженные группы и предприятия
- Данные корректно преобразуются в JSON формат

### 4. Обновлен `NornViewModel`

- `loadSavedSettings()` восстанавливает группы и предприятия из сохраненных настроек
- `saveCurrentSettings()` включает текущие данные групп и предприятий в сохраняемые настройки
- `restoreDocumentFiles()` не загружает данные автоматически, если они уже восстановлены из настроек

### 5. Добавлено автоматическое сохранение

Вызов `saveCurrentSettings()` добавлен во все методы, изменяющие данные:

- `loadStudentsFromExcel()` - после загрузки студентов
- `removeGroup()` - после удаления группы
- `updateGroupData()` - после обновления группы
- `updateGroupStudents()` - после изменения списка студентов
- `updateStudentData()` - после редактирования данных студента
- `readEnterprisesFromTxt()` - после загрузки предприятий
- `clearEnterprisesList()` - после очистки списка предприятий
- `updateEnterpriseData()` - после редактирования предприятия

### 6. Обработка выхода из приложения

- В `main.kt` уже настроен вызов `viewModel.saveSettingsOnExit()` при закрытии окна
- Это обеспечивает сохранение всех изменений перед выходом

## Результат

Теперь все изменения, сделанные пользователем в интерфейсе:

- ✅ Редактирование данных студентов
- ✅ Добавление/удаление групп
- ✅ Загрузка предприятий
- ✅ Редактирование данных предприятий

Будут автоматически сохраняться и восстанавливаться при следующем запуске приложения.

## Файлы, которые были изменены

1. `composeApp/src/commonMain/kotlin/ru/bpo/norn/commonMain/models/AppSettings.kt`
2. `composeApp/src/commonMain/kotlin/ru/bpo/norn/commonMain/models/Group.kt`
3. `composeApp/src/commonMain/kotlin/ru/bpo/norn/commonMain/models/Student.kt`
4. `composeApp/src/commonMain/kotlin/ru/bpo/norn/commonMain/models/Enterprise.kt`
5. `composeApp/src/jvmMain/kotlin/ru/bpo/norn/jwmMain/services/AppSettingsService.kt`
6. `composeApp/src/jvmMain/kotlin/ru/bpo/norn/jwmMain/viewmodel/NornViewModel.kt`

## Тестирование

Для проверки исправления:

1. Запустите приложение
2. Загрузите Excel файл со студентами или TXT файл с предприятиями
3. Отредактируйте данные через интерфейс
4. Закройте приложение
5. Запустите приложение снова
6. Убедитесь, что все ваши изменения сохранились