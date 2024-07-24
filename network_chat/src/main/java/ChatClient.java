import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    //централизованная переменная для управления потоком ввода/вывода в классе
    private static BufferedReader reader;
    private static final String HOST = "localhost";//переменная для обращение к локальному компьютеру
    public static int PORT;//для хранения номера порта
    private static String LOG_FILE = "file.log";//для хранения файла журнала
    public static String file = "settings.txt";//для хранения фала настроек

    public static void main(String[] args) {

        //загружаем настройки сервера из файла и присваиваем их переменной PORT
        loadSettings(file);

        try {
            //стартуем сервер , в качестве параметров подключения  передаем HOST, PORT
            Socket socket = new Socket(HOST, PORT);
            //для более эфективногосчитывания данных создается выходной поток и буферезируется
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            //создается объект PrintWriter для отправки исходящих сообщений через сокет
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // Создание пула потоков
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // Ожидание запроса на ввод имени
            String message;//переменная для хранения сообщений
            while ((message = in.readLine()) != null) {
                System.out.println(message);//сообщение полученное от сервера
                // создается объект  BufferedReader который считывает ввод пользователя с консоли
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in));
                //сохраняем данные с консоли в переменную
                String name = userInput.readLine();
                //отправляем значение переменной name в выходной поток
                out.println(name);

                // Ожидание подтверждения проверки имени
                //считываем ответ сервера
                message = in.readLine();
                //проверяем начинается ли полученное сообщение от сервера с фразы "Имя уже занято или неверное"
                if (message.startsWith("Имя уже занято или неверное")) {
                    //если да выводим сообщение в консоль
                    System.out.println(message);//сообщение сервера выводим в консоль

                } else {
                    //считываем входящие сообщения и выводим в консоль в отдельном потоке
                    listenForMessages(in, executor);
                    //отправляем сообщения в  PrintWriter в отдельном потоке , таким образом
                    // за ввод сообщений и вывод отвечают разные потоки
                    sendMessages(out, userInput, executor);
                    System.out.println(message);//сообщение сервера выводим в консоль
                    break;
                }
            }

            // Завершение пула потоков
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод загружает значение порта из файла настроек и сохраняет его в статической переменной PORT
    public static void loadSettings(String file) {//в качестве параметра принимает имя файла

        //проверяется если переменная reader не равна нулю то используется она иначе создается новая
        try (BufferedReader bufferedReader = reader != null ? reader :
                new BufferedReader(new FileReader(file))) {
            //считываем 1ю строку из файла которая содержин настройку в формате PORT = <значение>,
            //строка разбивается по знаку = и второй элемент массива берется как значение настройки
            PORT = Integer.parseInt(bufferedReader.readLine().split("=")[1].trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод для записи сообщений в лог файл
    public static void logMessage(String message) {

        //создается объект PrintWriter который используется для записи данных в файл
        // обьект FileWriter отвечает за запись данных ,параметр true  означает что данные вфайл
        // будут добавлены а не перезаписаны
        try (PrintWriter logWriter = new PrintWriter(
                new FileWriter(LOG_FILE, true))) {
            //создается строка в которой формируется текущее время и дата в заданном формате
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //записываются данные в файл в формате дата + время + строка сообщения
            logWriter.println(timeStamp + " - " + message);//формат записи логов
            logWriter.flush();//очистка буфера,если в нем что то оставалось записывается в лог файл
            logWriter.close();//закрытие записывающего потока
        } catch (IOException e) {
            e.printStackTrace();//обрабатывает ошибку если файла нет или неправильный путь к нему
        }

    }

    //метод для считывания входящих сообщений из BufferedReader
    // в качестве параметров получает входной поток и пул потоков
    static void listenForMessages(BufferedReader in, ExecutorService executor) {

        //создается задача с логикой считывания сообщений
        executor.execute(() -> {
            try {
                //в цикле считываем сообщения от сервера и выводим их в консоль и добавляем в лог файл
                String message;//для хранения полученных сообщений
                while ((message = in.readLine()) != null) {
                    System.out.println(message);//вывод сообщения в консоль
                    logMessage(message);//запись сообщения в лог файл
                }
            } catch (IOException e) {
                e.printStackTrace();//обрабатываем ошибку ввода вывода
            }
        });
    }

    //метод для отправки сообщений из пользовательского ввода на сервер в качестве параметров
    //принимает out- для записи сообщений в выходной поток, userInput - для чтения с консоли
    // executor - пул потоков
    static void sendMessages(PrintWriter out, BufferedReader userInput, ExecutorService executor) {

        //создается задача с логикой отправки сообщений
        executor.execute(() -> {
            try {
                String message;//для работы сообщений отправляемых
                //в цикле считываем сообщения от спользователя  добавляем в лог файл
                while ((message = userInput.readLine()) != null) {
                    out.println(message);//вывод сообщения в консоль
                    logMessage("Sent: " + message);//записываем сообщение в лог файл
                    //если сообщение начинается с фразы "exit"
                    if (message.equalsIgnoreCase("exit")) {
                        break; //выходим из цикла - отключаемся от сервера
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //возвращает номер порта
    public static int getPort() {
        return PORT;
    }

    //Метод принимает в качестве аргумента объект BufferedReader и сохраняет его
    // в статическом поле reader класса ChatClient,дает возможность другим методам
    //получать доступ к этим данным
    public static void setReader(BufferedReader reader) {
        ChatClient.reader = reader;
    }

    //Этот метод  для установки пути к файлу лога, в качестве аргумнта принимает строку
    // которая содержит путь к файлу
    public static void setLogFile(String logFile) {
        LOG_FILE = logFile;
    }
}