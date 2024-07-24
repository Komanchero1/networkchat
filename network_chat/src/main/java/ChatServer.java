import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static int port; //для хранения номера порта
    static List<Socket> clients = new ArrayList<>();//список для хранения сокетов  клиентов
    static Set<String> clientNames = new HashSet<>(); //колекция для хранения и отслеживания уникальных имен
    static String logFile = "file.log";//для хранения имени файла в который записываются логи сообщений
    private static int threadPoolSize = 7;//количество потоков в пуле

    public static void main(String[] args) {

        loadSettings(); //загружаем настройки сервера из файла и присваиваем их переменной PORT
        try {
            ServerSocket serverSocket = new ServerSocket(port);//создается сокет , сервер стартует
            //добавляем сообщение в логфайл
            System.out.println("Сервер чата запущен на порту " + port + " и ожидаем подключения клиентов...");
            logMessage("Сервер чата запущен на порту " + port + " и ожидаем подключения клиентов...");
            //создается пул потоков
            ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
            while (true) {
                Socket socket = serverSocket.accept();//ожидает подключения клиентов
                synchronized (clients) {//синхронизируем доступ к списку
                    clients.add(socket);//добавление новго сокет клиента в список
                }
                //берется свободный поток из пула и передается в него обьект ClientHandler
                // для обработки клиентского соединения , далее поток работает в фоновом режиме
                threadPool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace(); // обрабатывается ошибка подключения сервера к порту
        }
    }

    //считываем настройки из файла и сохраняем их в переменную PORT
    static void loadSettings() {

        //буферизируем поток  и считываем данные из файла настроек
        try (BufferedReader reader = new BufferedReader(new FileReader("settings.txt"))) {
            //обрабатываем данные из буфера и сохраняем их в переменную
            port = Integer.parseInt(reader.readLine().split("=")[1].trim());
        } catch (IOException e) {
            e.printStackTrace();//обрабатываем ошибку в случае неудачи считывания
        }
    }

    //метод записывает сообщение в файл журнала (log file) с указанием временной метки
    // в качестве параметра передается строка которую нужно записать в лог фвйл
    static void logMessage(String message) {

        //создается объект PrintWriter который используется для записи данных в файл
        // обьект FileWriter отвечает за запись данных ,параметр true  означает что данные вфайл
        // будут добавлены а не перезаписаны
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) {
            //создается строка в которой формируется текущее время и дата в заданном формате
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //записываются данные в файл в формате дата + время + строка сообщения
            logWriter.println(timestamp + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();//обрабатывает ошибку если файла нет или неправильный путь к нему
        }
    }

    //метод возвращающий номер порта
    public static int getPort() {
        return port;
    }
}