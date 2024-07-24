import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;//для установки и поддержания связи с сервером
    private BufferedReader in;//для чтения входящих сообщений
    private PrintWriter out;//для вывода данных в выходной поток
    String name;//для хранения имени
    private ChatServer chatServer;//дает возможность обращаться к методам класса ChatServer

    //конструктор в качестве параметра сокет
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    //реализуем метод ран интерфейса Runnable
    @Override
    public void run() {
        try {
            //создается поток для чтения информации из входящего потока и буферезируем его
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //создается объект для записи данных в выходной поток
            out = new PrintWriter(socket.getOutputStream(), true);

            // запрос на ввод имени
            while (true) {
                //отправка сообщения клиенту
                out.println("Введите ваше имя:");
                //считываем строку и сохраняем в переменную
                name = in.readLine();
                // проверка имени на совпадение
                if (isNameValid(name)) {
                    //отправка сообщения всем клиентам
                    broadcastMessage(name + " присоединился к чату.");
                    //запись сообщения в лог файл
                    ChatServer.logMessage(name + " подключился к серверу.");
                    break;

                } else {
                    //отправка сообщения клиенту
                    out.println("Имя уже занято или неверное. Попробуйте снова.");
                }
            }

            // прослушивание сообщений если exit отключаемся от чата если нет
            // отправляем сообщения всем пользователям
            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //отключение от сервера
            disconnect();
        }
    }

    //метод для отправки и получения сообщений
    void listenForMessages() {

        try {
            String message;//для хранения и обработки сообщений
            while ((message = in.readLine()) != null) {
                //если сообщение от клиента exit
                if (message.equalsIgnoreCase("exit")) {
                    out.println("Вы отключены от чата.");//отправляем сообщение клиенту
                    break;//выходим из цикла
                }
                //если нет отправляем сообщение в чат
                broadcastMessage(name + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //отключаемся от сервера
            disconnect();
        }
    }

    //метод отправляет сообщение всем клиентам подключеным к серверу,
    // синхронизируется с коллекцией клиентов чтобы сообщение гарантировано
    // было отправлено каждому клиенту
    void broadcastMessage(String message) {

        //записываем сообщение в лог файл
        ChatServer.logMessage(message);
        synchronized (ChatServer.clients) {//синхронизируемся с коллекцией клиентов
            //в цикле перебираем клиентов
            for (Socket client : ChatServer.clients) {
                try {
                    //создается объект  PrintWriter связанный с выходным потоком клиентского сокета
                    // параметр true указывает что необходимо сбрасывать буфер после каждой записи
                    PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                    //записываем сообщение в выходной поток клиента
                    clientOut.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //метод проверяет есть уже такое имя клиента или нет ,
    //в качестве аргумента принимает имя слиента
    public boolean isNameValid(String name) {

        //проверяем имя пустое или содержит пробелы
        if (name.isEmpty() || name.contains(" ")) {
            return false;
        }
        //синхронизируем доступ к коллекции
        synchronized (ChatServer.clientNames) {
            //провека имени на совпадение
            if (ChatServer.clientNames.contains(name)) {
                return false;
            }
            //записывается имя  в коллекцию
            ChatServer.clientNames.add(name);
            return true;
        }
    }

    //метод отключает клиента отсервера
    void disconnect() {

        try {
            //закрывается сокет
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (ChatServer.clients) { //синхронизируем доступ к списку
            ChatServer.clients.remove(socket);//удаляем сокет клиента из списка
        }
        synchronized (ChatServer.clientNames) { //синхронизируем доступ к коллекции
            ChatServer.clientNames.remove(name);//удаляем имя клиента из коллекции
        }
        broadcastMessage(name + " покинул чат.");//отправляем сообщение всем клиентам
        ChatServer.logMessage(name + " отключился от сервера.");//записываем лог
    }

    //устанавливает значение in для упрощения тестирования
    public void setIn(BufferedReader in) {
        this.in = in;
    }

    //устанавливает значение out используем для тестирования класса
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    //устанавливает значение chatServer  для тестирования класса
    public void setChatServer(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
}
