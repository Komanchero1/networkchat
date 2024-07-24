import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;//��� ��������� � ����������� ����� � ��������
    private BufferedReader in;//��� ������ �������� ���������
    private PrintWriter out;//��� ������ ������ � �������� �����
    String name;//��� �������� �����
    private ChatServer chatServer;//���� ����������� ���������� � ������� ������ ChatServer

    //����������� � �������� ��������� �����
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    //��������� ����� ��� ���������� Runnable
    @Override
    public void run() {
        try {
            //��������� ����� ��� ������ ���������� �� ��������� ������ � ������������ ���
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //��������� ������ ��� ������ ������ � �������� �����
            out = new PrintWriter(socket.getOutputStream(), true);

            // ������ �� ���� �����
            while (true) {
                //�������� ��������� �������
                out.println("������� ���� ���:");
                //��������� ������ � ��������� � ����������
                name = in.readLine();
                // �������� ����� �� ����������
                if (isNameValid(name)) {
                    //�������� ��������� ���� ��������
                    broadcastMessage(name + " ������������� � ����.");
                    //������ ��������� � ��� ����
                    ChatServer.logMessage(name + " ����������� � �������.");
                    break;

                } else {
                    //�������� ��������� �������
                    out.println("��� ��� ������ ��� ��������. ���������� �����.");
                }
            }

            // ������������� ��������� ���� exit ����������� �� ���� ���� ���
            // ���������� ��������� ���� �������������
            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //���������� �� �������
            disconnect();
        }
    }

    //����� ��� �������� � ��������� ���������
    void listenForMessages() {

        try {
            String message;//��� �������� � ��������� ���������
            while ((message = in.readLine()) != null) {
                //���� ��������� �� ������� exit
                if (message.equalsIgnoreCase("exit")) {
                    out.println("�� ��������� �� ����.");//���������� ��������� �������
                    break;//������� �� �����
                }
                //���� ��� ���������� ��������� � ���
                broadcastMessage(name + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //����������� �� �������
            disconnect();
        }
    }

    //����� ���������� ��������� ���� �������� ����������� � �������,
    // ���������������� � ���������� �������� ����� ��������� �������������
    // ���� ���������� ������� �������
    void broadcastMessage(String message) {

        //���������� ��������� � ��� ����
        ChatServer.logMessage(message);
        synchronized (ChatServer.clients) {//���������������� � ���������� ��������
            //� ����� ���������� ��������
            for (Socket client : ChatServer.clients) {
                try {
                    //��������� ������  PrintWriter ��������� � �������� ������� ����������� ������
                    // �������� true ��������� ��� ���������� ���������� ����� ����� ������ ������
                    PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                    //���������� ��������� � �������� ����� �������
                    clientOut.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //����� ��������� ���� ��� ����� ��� ������� ��� ��� ,
    //� �������� ��������� ��������� ��� �������
    public boolean isNameValid(String name) {

        //��������� ��� ������ ��� �������� �������
        if (name.isEmpty() || name.contains(" ")) {
            return false;
        }
        //�������������� ������ � ���������
        synchronized (ChatServer.clientNames) {
            //������� ����� �� ����������
            if (ChatServer.clientNames.contains(name)) {
                return false;
            }
            //������������ ���  � ���������
            ChatServer.clientNames.add(name);
            return true;
        }
    }

    //����� ��������� ������� ���������
    void disconnect() {

        try {
            //����������� �����
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (ChatServer.clients) { //�������������� ������ � ������
            ChatServer.clients.remove(socket);//������� ����� ������� �� ������
        }
        synchronized (ChatServer.clientNames) { //�������������� ������ � ���������
            ChatServer.clientNames.remove(name);//������� ��� ������� �� ���������
        }
        broadcastMessage(name + " ������� ���.");//���������� ��������� ���� ��������
        ChatServer.logMessage(name + " ���������� �� �������.");//���������� ���
    }

    //������������� �������� in ��� ��������� ������������
    public void setIn(BufferedReader in) {
        this.in = in;
    }

    //������������� �������� out ���������� ��� ������������ ������
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    //������������� �������� chatServer  ��� ������������ ������
    public void setChatServer(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
}
