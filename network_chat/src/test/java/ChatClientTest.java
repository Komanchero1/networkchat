import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatClientTest {

    //тест проверяет, что метод loadSettings() корректно загружает настройки из файла
    //и сохраняет их в соответствующие поля класса ChatClient
    @Test
    void testLoadSettings() throws IOException {
        //создается мок объект для считывания строки
        BufferedReader mockedReader = Mockito.mock(BufferedReader.class);
        //настраиваем поведение mockedReader , будет возвращать строку "port=8080"
        when(mockedReader.readLine()).thenReturn("port=8080");
        //устанавливается mockedReader в качестве считывания для класса ChatClient
        ChatClient.setReader(mockedReader);
        //считываются настройки из файла "settings.txt" и сохранять их в соответствующие поля класса
        ChatClient.loadSettings("settings.txt");
        //Проверяется, что значение порта, полученное с помощью метода getPort(), равно 8080
        assertEquals(8080, ChatClient.getPort());
    }

    //проверка что метод LogMessage корректно записывает  сообщение в файл, и что это
    // сообщение можно успешно прочитать из файла
    @Test
    void testLogMessage() throws IOException {
        //инициируем переменную message и сохраняем в нее тестовое сообщение
        String message = "Test log message";
        //инициируем переменную tempFileName и сохраняем в нее временный файл
        String tempFileName = "temp-file.log";
        //Устанавливается временный файл для логирования с помощью метода setLogFile
        ChatClient.setLogFile(tempFileName);
        //Вызывается метод logMessage() с тестовым сообщением
        ChatClient.logMessage(message);
        //считываем все строки с временного файла
        BufferedReader reader = new BufferedReader(new FileReader(tempFileName));
        //последнюю строку сохраняем сохраняем в переменную lastLine
        String lastLine = null, line;
        //считываем строку пока файле есть хоть одна строка
        while ((line = reader.readLine()) != null) {
            //присваиваем прочитанную строку переменной  lastLine
            lastLine = line;
        }

        assertNotNull(lastLine);//проверка что была считана строка
        //проверка что строка содержащаяся lastLine начинается с пдстроки указанной  message
        assertTrue(lastLine.contains(message));

        reader.close(); //закрываем поток
        new File(tempFileName).delete(); // Удаление временного файла
    }

    // тест проверяет, что метод listenForMessages() правильно обрабатывает входные сообщения
    // и записывает их в лог-файл
    @Test
    void testListenForMessages() throws IOException, InterruptedException {
        //Создается BufferedReader объект mockedInput, который будет имитировать ввод пользователя
        BufferedReader mockedInput = Mockito.mock(BufferedReader.class);
        //Эта строка создает экземпляр ExecutorService, который представляет собой пул потоков с одним
        //рабочим потоком для последовательного выполнения задач
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //задаем поведение mockedInput первым возврашает "exit" затем "Test message"
        when(mockedInput.readLine()).thenReturn("exit", "Test message");
        //инициируем переменную tempFileName и сохраняем в нее временный файл
        String tempFileName = "temp-file.log";
        //Устанавливается временный файл для логирования с помощью метода setLogFile
        ChatClient.setLogFile(tempFileName);
        //вызываем метод listenForMessages в качестве аргументов передаем ему мок объект
        // ввода пользователем и поток в котором происходит обработка ввода
        ChatClient.listenForMessages(mockedInput, executor);
        executor.shutdown();//завершаем работу потока в  executor

        // Ожидание завершения задачи
        executor.awaitTermination(1, TimeUnit.SECONDS);

        //Создается объект BufferedReader для чтения файла
        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        //последняя строка сохраняется в lastLine
        String lastLine = null, line;
        //в цикле считываем файл и сохраняем считанную строку в line
        while ((line = logReader.readLine()) != null) {
            //сохраняем считанную строку в lastLine
            lastLine = line;
        }


        assertNotNull(lastLine);//проверка что была считана строка
        //проверка что строка содержащаяся lastLine начинается с пдстроки указанной  message
        assertTrue(lastLine.contains("Test message"));

        logReader.close();//закрываем поток
        new File(tempFileName).delete(); // Удаление временного файла
    }

    @Test
    void testSendMessages() throws IOException, InterruptedException {
        //создается мок объект для эмитации вывода сообщений
        PrintWriter mockedOutput = Mockito.mock(PrintWriter.class);
        //создается мок объект для эмитации ввода сообщений
        BufferedReader mockedUserInput = Mockito.mock(BufferedReader.class);
        //создается мок объект который будет использоваться для выполнения асинхронных задач
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //задаем поведение мок объекта mockedUserInput который при вызове readLine возвращает
        //"test message"  затем "exit"
        when(mockedUserInput.readLine()).thenReturn("test message", "exit");
        //инициируем переменную tempFileName и сохраняем в нее временный файл
        String tempFileName = "temp-file.log";
        //Устанавливается временный файл для логирования с помощью метода setLogFile
        ChatClient.setLogFile(tempFileName);
        //вызываем метод sendMessages в качестве аргументов передаем ему мок объекты
        ChatClient.sendMessages(mockedOutput, mockedUserInput, executor);

        executor.shutdown(); //заканчиваем работу потока
        executor.awaitTermination(1, TimeUnit.SECONDS); // Ожидание завершения задач

        verify(mockedOutput).println("test message");//проверяем что метод println был вызван с "test message"
        verify(mockedOutput).println("exit");// и с "exit"
        //Создается объект BufferedReader для чтения файла
        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        //последнюю строку сохраняем сохраняем в переменную lastLine
        String lastLine = null, line;
        //в цикле считываем файл и сохраняем считанную строку в line
        while ((line = logReader.readLine()) != null) {
            lastLine = line; //сохраняем считанную строку в lastLine
        }

        assertNotNull(lastLine);//проверка что была считана строка
        //проверка что строка содержащаяся lastLine начинается с пдстроки exit
        assertTrue(lastLine.contains("Sent: exit"));

        logReader.close();//закрываем поток
        new File(tempFileName).delete(); // Удаление временного файла
    }
}