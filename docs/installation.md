# Установка и сборка проекта

## Системные требования
- **Операционная система**: Windows 10/11, Linux или macOS.
- **JDK**: Java Development Kit 17 или 21 (проверено на OpenJDK 21.0.9 из комплекта Android Studio JBR).
- **Android SDK**: установленный компонент `platforms;android-34` и `build-tools;34.0.0` (или выше).
- **Gradle**: Gradle 8.14.3 (включен в проект через `gradlew`).
- **Android Gradle Plugin (AGP)**: 8.11.1.

## Сборка из командной строки

### 1. Клонирование репозитория и переход в каталог
```powershell
cd D:\projects\3187350
```

### 2. Настройка пути к SDK
Убедитесь, что файл `local.properties` содержит корректный путь к вашему Android SDK:
```properties
sdk.dir=C\:/Android/Sdk
```

### 3. Чистая сборка проекта
```powershell
.\gradlew.bat clean assembleDebug
```
Собранный файл APK будет сгенерирован по пути:
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Запуск автоматизированных тестов
```powershell
.\gradlew.bat testDebugUnitTest
```

### 5. Установка на эмулятор или физическое устройство
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Открытие в Android Studio
1. Запустите Android Studio.
2. Выберите пункт **Open** и укажите корневой каталог `D:\projects\3187350`.
3. Дождитесь синхронизации проекта через Gradle Sync.
4. Выберите конфигурацию запуска `app` и нажмите **Run**.
