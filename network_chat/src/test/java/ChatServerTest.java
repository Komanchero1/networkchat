import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class ChatServerTest {

    @TempDir//
    Path tempDir;//создается путь к временному файлу
    //ByteArrayOutputStream класс позволяет записывать байты в памяти и преобразовывать их в массив байтов
    //создается объект для перехвата и хранения данных, отправленных в поток вывода
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    //создается поток вывода, который используется для отображения сообщений на консоли. Сохранив
    // его в переменной, мы сможем восстановить его значение после выполнения тестов
    private final PrintStream originalOut = System.out;

    @BeforeEach //выполняется перед каждым методом
    public void setUpStreams() {
    //перенаправляем стандартный поток вывода на outContent все данные, которые программа
        // будет выводить, будут записываться в ByteArrayOutputStream,
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach // выполняется после каждого метода
    public void restoreStreams() {
     //восстанавливаем стандартный поток вывода, который мы сохранили ранее
        System.setOut(originalOut);
    }

    @BeforeAll // выполняется один раз перед запуском всех методов
    public static void setUpBeforeClass() throws Exception {
        // Создадим файл настроек для теста, если его еще нет
        Path settingsFile = Paths.get("settings.txt");
        //проверяется существует файл настроек или нет, exists возвращает true если файл существует
        if (!Files.exists(settingsFile)) {
            //если условие выполнено создается файл настроек
            Files.write(settingsFile, Collections.singletonList("port=12345"));
        }
    }
    //метод тестирует загрузку настроек подключения из текстового файла
    @Test
    void testLoadSettings() throws IOException {
        Path settingsFile = tempDir.resolve("settings.txt");//создается объекта Path для файла настроекво временной директории.
        Files.write(settingsFile, Collections.singletonList("port = 12345"));//задается содержимое файла

        //Создание объекта File для оригинального файла настроек.
        File originalFile = new File("settings.txt");
        //Переименование оригинального файла настроек в settings_backup.txt.
        originalFile.renameTo(new File("settings_backup.txt"));
        //Копирование временного файла настроек в оригинальный файл настроек.
        Files.copy(settingsFile, originalFile.toPath());
        //Вызов метода loadSettings из класса ChatServer для загрузки настроек.
        ChatServer.loadSettings();

        //Удаление оригинального файла настроек.
        originalFile.delete();
        //Восстановление оригинального имени файла настроек из резервной копии.
        new File("settings_backup.txt").renameTo(originalFile);
        //Проверка, что порт сервера установлен корректно и равен 12345.
        assertEquals(12345, ChatServer.getPort());
    }
     //проверка записи строки в лог файл и формата записи
    @Test
    void testLogMessage() throws IOException {
        // Устанавливаем временный лог-файл с именем "file.log" во временной директории tempDir.
        ChatServer.logFile = tempDir.resolve("file.log").toString();

        // Вызываем метод логгирования с аргументом "Test log message"
        ChatServer.logMessage("Test log message");

        //с помощью метода readAllBytes с читываем содержимое файла лога и сохраняем в строку
        String logContent = new String(Files.readAllBytes(Paths.get(ChatServer.logFile)));
        //проверяется ,что строка logContent содержит подстроку "Test log message"
        assertTrue(logContent.contains("Test log message"));
        //проверяется, то строка logContent содержит текущую дату и время в формате "yyyy-MM-dd HH:mm
        assertTrue(logContent.contains(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
    }

    //проверка что метод возвращает устойчивое значение порта
    @Test
    void testGetPort() {
        //проверка что два вызова метода GetPort возвращают одинаковое значение
        assertEquals(ChatServer.getPort(), ChatServer.getPort());
    }
}