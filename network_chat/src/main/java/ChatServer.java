import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static int port; //��� �������� ������ �����
    static List<Socket> clients = new ArrayList<>();//������ ��� �������� �������  ��������
    static Set<String> clientNames = new HashSet<>(); //�������� ��� �������� � ������������ ���������� ����
    static String logFile = "file.log";//��� �������� ����� ����� � ������� ������������ ���� ���������
    private static int threadPoolSize = 7;//���������� ������� � ����

    public static void main(String[] args) {

        loadSettings(); //��������� ��������� ������� �� ����� � ����������� �� ���������� PORT
        try {
            ServerSocket serverSocket = new ServerSocket(port);//��������� ����� , ������ ��������
            //��������� ��������� � �������
            System.out.println("������ ���� ������� �� ����� " + port + " � ������� ����������� ��������...");
            logMessage("������ ���� ������� �� ����� " + port + " � ������� ����������� ��������...");
            //��������� ��� �������
            ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
            while (true) {
                Socket socket = serverSocket.accept();//������� ����������� ��������
                synchronized (clients) {//�������������� ������ � ������
                    clients.add(socket);//���������� ����� ����� ������� � ������
                }
                //������� ��������� ����� �� ���� � ���������� � ���� ������ ClientHandler
                // ��� ��������� ����������� ���������� , ����� ����� �������� � ������� ������
                threadPool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace(); // �������������� ������ ����������� ������� � �����
        }
    }

    //��������� ��������� �� ����� � ��������� �� � ���������� PORT
    static void loadSettings() {

        //������������ �����  � ��������� ������ �� ����� ��������
        try (BufferedReader reader = new BufferedReader(new FileReader("settings.txt"))) {
            //������������ ������ �� ������ � ��������� �� � ����������
            port = Integer.parseInt(reader.readLine().split("=")[1].trim());
        } catch (IOException e) {
            e.printStackTrace();//������������ ������ � ������ ������� ����������
        }
    }

    //����� ���������� ��������� � ���� ������� (log file) � ��������� ��������� �����
    // � �������� ��������� ���������� ������ ������� ����� �������� � ��� ����
    static void logMessage(String message) {

        //��������� ������ PrintWriter ������� ������������ ��� ������ ������ � ����
        // ������ FileWriter �������� �� ������ ������ ,�������� true  �������� ��� ������ �����
        // ����� ��������� � �� ������������
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) {
            //��������� ������ � ������� ����������� ������� ����� � ���� � �������� �������
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //������������ ������ � ���� � ������� ���� + ����� + ������ ���������
            logWriter.println(timestamp + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();//������������ ������ ���� ����� ��� ��� ������������ ���� � ����
        }
    }

    //����� ������������ ����� �����
    public static int getPort() {
        return port;
    }
}