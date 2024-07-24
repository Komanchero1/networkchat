import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    //���������������� ���������� ��� ���������� ������� �����/������ � ������
    private static BufferedReader reader;
    private static final String HOST = "localhost";//���������� ��� ��������� � ���������� ����������
    public static int PORT;//��� �������� ������ �����
    private static String LOG_FILE = "file.log";//��� �������� ����� �������
    public static String file = "settings.txt";//��� �������� ���� ��������

    public static void main(String[] args) {

        //��������� ��������� ������� �� ����� � ����������� �� ���������� PORT
        loadSettings(file);

        try {
            //�������� ������ , � �������� ���������� �����������  �������� HOST, PORT
            Socket socket = new Socket(HOST, PORT);
            //��� ����� ��������������������� ������ ��������� �������� ����� � ��������������
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            //��������� ������ PrintWriter ��� �������� ��������� ��������� ����� �����
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // �������� ���� �������
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // �������� ������� �� ���� �����
            String message;//���������� ��� �������� ���������
            while ((message = in.readLine()) != null) {
                System.out.println(message);//��������� ���������� �� �������
                // ��������� ������  BufferedReader ������� ��������� ���� ������������ � �������
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in));
                //��������� ������ � ������� � ����������
                String name = userInput.readLine();
                //���������� �������� ���������� name � �������� �����
                out.println(name);

                // �������� ������������� �������� �����
                //��������� ����� �������
                message = in.readLine();
                //��������� ���������� �� ���������� ��������� �� ������� � ����� "��� ��� ������ ��� ��������"
                if (message.startsWith("��� ��� ������ ��� ��������")) {
                    //���� �� ������� ��������� � �������
                    System.out.println(message);//��������� ������� ������� � �������

                } else {
                    //��������� �������� ��������� � ������� � ������� � ��������� ������
                    listenForMessages(in, executor);
                    //���������� ��������� �  PrintWriter � ��������� ������ , ����� �������
                    // �� ���� ��������� � ����� �������� ������ ������
                    sendMessages(out, userInput, executor);
                    System.out.println(message);//��������� ������� ������� � �������
                    break;
                }
            }

            // ���������� ���� �������
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ����� ��������� �������� ����� �� ����� �������� � ��������� ��� � ����������� ���������� PORT
    public static void loadSettings(String file) {//� �������� ��������� ��������� ��� �����

        //����������� ���� ���������� reader �� ����� ���� �� ������������ ��� ����� ��������� �����
        try (BufferedReader bufferedReader = reader != null ? reader :
                new BufferedReader(new FileReader(file))) {
            //��������� 1� ������ �� ����� ������� �������� ��������� � ������� PORT = <��������>,
            //������ ����������� �� ����� = � ������ ������� ������� ������� ��� �������� ���������
            PORT = Integer.parseInt(bufferedReader.readLine().split("=")[1].trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //����� ��� ������ ��������� � ��� ����
    public static void logMessage(String message) {

        //��������� ������ PrintWriter ������� ������������ ��� ������ ������ � ����
        // ������ FileWriter �������� �� ������ ������ ,�������� true  �������� ��� ������ �����
        // ����� ��������� � �� ������������
        try (PrintWriter logWriter = new PrintWriter(
                new FileWriter(LOG_FILE, true))) {
            //��������� ������ � ������� ����������� ������� ����� � ���� � �������� �������
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //������������ ������ � ���� � ������� ���� + ����� + ������ ���������
            logWriter.println(timeStamp + " - " + message);//������ ������ �����
            logWriter.flush();//������� ������,���� � ��� ��� �� ���������� ������������ � ��� ����
            logWriter.close();//�������� ������������� ������
        } catch (IOException e) {
            e.printStackTrace();//������������ ������ ���� ����� ��� ��� ������������ ���� � ����
        }

    }

    //����� ��� ���������� �������� ��������� �� BufferedReader
    // � �������� ���������� �������� ������� ����� � ��� �������
    static void listenForMessages(BufferedReader in, ExecutorService executor) {

        //��������� ������ � ������� ���������� ���������
        executor.execute(() -> {
            try {
                //� ����� ��������� ��������� �� ������� � ������� �� � ������� � ��������� � ��� ����
                String message;//��� �������� ���������� ���������
                while ((message = in.readLine()) != null) {
                    System.out.println(message);//����� ��������� � �������
                    logMessage(message);//������ ��������� � ��� ����
                }
            } catch (IOException e) {
                e.printStackTrace();//������������ ������ ����� ������
            }
        });
    }

    //����� ��� �������� ��������� �� ����������������� ����� �� ������ � �������� ����������
    //��������� out- ��� ������ ��������� � �������� �����, userInput - ��� ������ � �������
    // executor - ��� �������
    static void sendMessages(PrintWriter out, BufferedReader userInput, ExecutorService executor) {

        //��������� ������ � ������� �������� ���������
        executor.execute(() -> {
            try {
                String message;//��� ������ ��������� ������������
                //� ����� ��������� ��������� �� �������������  ��������� � ��� ����
                while ((message = userInput.readLine()) != null) {
                    out.println(message);//����� ��������� � �������
                    logMessage("Sent: " + message);//���������� ��������� � ��� ����
                    //���� ��������� ���������� � ����� "exit"
                    if (message.equalsIgnoreCase("exit")) {
                        break; //������� �� ����� - ����������� �� �������
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //���������� ����� �����
    public static int getPort() {
        return PORT;
    }

    //����� ��������� � �������� ��������� ������ BufferedReader � ��������� ���
    // � ����������� ���� reader ������ ChatClient,���� ����������� ������ �������
    //�������� ������ � ���� ������
    public static void setReader(BufferedReader reader) {
        ChatClient.reader = reader;
    }

    //���� �����  ��� ��������� ���� � ����� ����, � �������� �������� ��������� ������
    // ������� �������� ���� � �����
    public static void setLogFile(String logFile) {
        LOG_FILE = logFile;
    }
}