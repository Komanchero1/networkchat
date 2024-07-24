import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.Socket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ClientHandlerTest {

    @Mock
    private Socket socket;//создается mock-объект для класса Socket

    @Mock
    private BufferedReader in;//создается mock - объект для эмитации BufferedReader чтобы управлять вводом данных

    @Mock
    private PrintWriter out;//создается mock - объект для эмитации PrintWriter чтобы управлять выводом данных

    @Mock
    private ChatServer chatServer;//создается mock-объект для класса ChatServer

    private ClientHandler clientHandler;//Объявление экземпляра ClientHandler, который будет тестироваться

    //метод настраивает тестовое окружение, создавая мок-объекты для различных зависимостей ClientHandler
    @BeforeEach
    public void setUp() throws IOException { //метод который выполняется перед каждым тестом
        //вызов метода openMocks который инициализирует все mock- объекты помеченные анотациями @Mock
        MockitoAnnotations.openMocks(this);

        // Создание объекта ClientHandler и передаеи его в spy чтобы контролировать его поведение
        clientHandler = spy(new ClientHandler(socket));

        // Установка мок-объектов
        clientHandler.setIn(in);//устанавливается mock- объект in в качестве входного потока
        clientHandler.setOut(out);//устанавливается mock- объект out в качестве выходного потока
        clientHandler.setChatServer(chatServer);//устанавливается mock- объект chatServer в качестве сервера чата
    }

    //после каждого теста все обьекты будут обнулены
    @AfterEach
    public void tearDown() {
        // Сбросить состояние тестируемого объекта
        clientHandler = null;

        // Сбросить состояние mock-объектов
        Mockito.reset(socket, in, out, chatServer);
    }


    //проверка имени клиента на валидность,вывод сообщений, логирование и рассылка сообщения о подключении
    @Test
    public void testRunValidName() throws IOException, InterruptedException {
        // Настраиваем поведения мок объекта in, сначало объект вернет "Вася"
        when(in.readLine()).thenReturn("Вася");

        // Настройка поведения для мок объекта clientHandler метод isNameValid
        // с аргументом "Вася" будет возвращать true
        doReturn(true).when(clientHandler).isNameValid("Вася");

        // создается отдельный поток и внего передается объект clientHandler
        Thread clientThread = new Thread(clientHandler);
        clientThread.start();//стартует поток

        // Дожидаемся завершения потока
        clientThread.join(1000);

        // Теперь выполняем проверки
        //Проверяется, что метод println() мок-объекта out был вызван 1 раз с аргументом "Введите Ваше имя:"
        verify(out, times(1)).println(eq("Введите ваше имя:"));
        //Проверяется, что метод println() мок-объекта out был вызван 1 раз с аргументом "Вася присоединился к чату."
        verify(out, times(1)).println(eq("Вася присоединился к чату."));
        //Проверяется, что метод logMessage() мок-объекта chatServer был вызван 1 раз с
        // аргументом "Вася подключился к серверу."
        verify(chatServer).logMessage(eq("Вася подключился к серверу."));
        //Проверяется, что метод broadcastMessage() мок-объекта clientHandler был вызван 1 раз
        // с аргументом "Вася присоединился к чату.".
        verify(clientHandler).broadcastMessage(eq("Вася присоединился к чату."));
        //Проверяется, что метод listenForMessages() мок-объекта clientHandler был вызван.
        verify(clientHandler).listenForMessages();
    }

    //проверка имени клиента на невалидность,вывод сообщений, логирование и рассылка сообщения о подключении
    @Test
    public void testRunInvalidName() throws IOException, InterruptedException {
        // Настраиваем поведения мок объекта in, сначало объект вернет "" затем "exit"
        when(in.readLine()).thenReturn("", "exit");

        // Настройка поведения для мок объекта clientHandler метод isNameValid
        // с аргументом "" и "exit" будет возвращать false
        doReturn(false).when(clientHandler).isNameValid(any());

        // создается отдельный поток и внего передается объект clientHandler
        Thread clientThread = new Thread(clientHandler);
        clientThread.start();//стартует поток

        // Дожидаемся завершения потока
        clientThread.join(1000);

        //Проверяется, что метод println() мок-объекта out был вызван 1 раз с аргументом "Введите Ваше имя:"
        verify(out, times(1)).println(eq("Введите Ваше имя:"));
        //Проверяется, что метод println() мок-объекта out был вызван 1 раз с аргументом "Неверное имя. Попробуйте еще раз."
        verify(out, times(1)).println(eq("Неверное имя. Попробуйте еще раз."));
        //Проверяется, что метод logMessage() мок-объекта chatServer ни разу не был вызван во время выполнения теста
        verify(chatServer, never()).logMessage(any());
        //Проверяет, что метод broadcastMessage() мок-объекта clientHandler ни разу не был вызван во время выполнения теста
        verify(clientHandler, never()).broadcastMessage(any());
        //Проверяет, что метод listenForMessages() мок-объекта clientHandler ни разу не был вызван во время выполнения теста
        verify(clientHandler, never()).listenForMessages();
    }

    //проверяется,что когда пользователь вводит "exit", то клиент отключается от чата и выводится соответствующее сообщение
    @Test
    public void testListenForMessagesExitKeyword() throws IOException {
        // Настраиваем поведения мок объекта in,объект будет возвращать "exit"
        when(in.readLine()).thenReturn("exit");

        // Вызов тестируемого метода
        clientHandler.listenForMessages();

        //Проверяется, что метод println() мок-объекта out был вызван 1 раз с аргументом "Вы отключены от чата."
        verify(out, times(1)).println(eq("Вы отключены от чата."));
    }

    //проверка имени клиента на валидность
    @Test
    public void testIsNameValid() {
        String name = "John";//инициализируется переменная с именем
        //у объекта clientHandler вызывается метод isNameValid которому в качестве аргумента передаем name
        // возвращенный результат типа boolean сохраняем в result
        boolean result = clientHandler.isNameValid(name);
        //если тест не пройдет в консоль выведется сообщение "возвращает true для действительного имени name"
        assertTrue(result, "возвращает true для действительного имени name");
    }


    @Test
    public void testDisconnect() throws IOException {
        // настраивается поведение у метода disconnect в данном случае вызов метода ничего не должен делать
        doNothing().when(clientHandler).disconnect();
        //вызываем метод disconnect
        clientHandler.disconnect();
        // проверяется, что метод disconnect() был вызван ровно один раз
        verify(clientHandler, times(1)).disconnect();
    }
}