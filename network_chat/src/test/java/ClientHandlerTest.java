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
    private Socket socket;//��������� mock-������ ��� ������ Socket

    @Mock
    private BufferedReader in;//��������� mock - ������ ��� �������� BufferedReader ����� ��������� ������ ������

    @Mock
    private PrintWriter out;//��������� mock - ������ ��� �������� PrintWriter ����� ��������� ������� ������

    @Mock
    private ChatServer chatServer;//��������� mock-������ ��� ������ ChatServer

    private ClientHandler clientHandler;//���������� ���������� ClientHandler, ������� ����� �������������

    //����� ����������� �������� ���������, �������� ���-������� ��� ��������� ������������ ClientHandler
    @BeforeEach
    public void setUp() throws IOException { //����� ������� ����������� ����� ������ ������
        //����� ������ openMocks ������� �������������� ��� mock- ������� ���������� ���������� @Mock
        MockitoAnnotations.openMocks(this);

        // �������� ������� ClientHandler � �������� ��� � spy ����� �������������� ��� ���������
        clientHandler = spy(new ClientHandler(socket));

        // ��������� ���-��������
        clientHandler.setIn(in);//��������������� mock- ������ in � �������� �������� ������
        clientHandler.setOut(out);//��������������� mock- ������ out � �������� ��������� ������
        clientHandler.setChatServer(chatServer);//��������������� mock- ������ chatServer � �������� ������� ����
    }

    //����� ������� ����� ��� ������� ����� ��������
    @AfterEach
    public void tearDown() {
        // �������� ��������� ������������ �������
        clientHandler = null;

        // �������� ��������� mock-��������
        Mockito.reset(socket, in, out, chatServer);
    }


    //�������� ����� ������� �� ����������,����� ���������, ����������� � �������� ��������� � �����������
    @Test
    public void testRunValidName() throws IOException, InterruptedException {
        // ����������� ��������� ��� ������� in, ������� ������ ������ "����"
        when(in.readLine()).thenReturn("����");

        // ��������� ��������� ��� ��� ������� clientHandler ����� isNameValid
        // � ���������� "����" ����� ���������� true
        doReturn(true).when(clientHandler).isNameValid("����");

        // ��������� ��������� ����� � ����� ���������� ������ clientHandler
        Thread clientThread = new Thread(clientHandler);
        clientThread.start();//�������� �����

        // ���������� ���������� ������
        clientThread.join(1000);

        // ������ ��������� ��������
        //�����������, ��� ����� println() ���-������� out ��� ������ 1 ��� � ���������� "������� ���� ���:"
        verify(out, times(1)).println(eq("������� ���� ���:"));
        //�����������, ��� ����� println() ���-������� out ��� ������ 1 ��� � ���������� "���� ������������� � ����."
        verify(out, times(1)).println(eq("���� ������������� � ����."));
        //�����������, ��� ����� logMessage() ���-������� chatServer ��� ������ 1 ��� �
        // ���������� "���� ����������� � �������."
        verify(chatServer).logMessage(eq("���� ����������� � �������."));
        //�����������, ��� ����� broadcastMessage() ���-������� clientHandler ��� ������ 1 ���
        // � ���������� "���� ������������� � ����.".
        verify(clientHandler).broadcastMessage(eq("���� ������������� � ����."));
        //�����������, ��� ����� listenForMessages() ���-������� clientHandler ��� ������.
        verify(clientHandler).listenForMessages();
    }

    //�������� ����� ������� �� ������������,����� ���������, ����������� � �������� ��������� � �����������
    @Test
    public void testRunInvalidName() throws IOException, InterruptedException {
        // ����������� ��������� ��� ������� in, ������� ������ ������ "" ����� "exit"
        when(in.readLine()).thenReturn("", "exit");

        // ��������� ��������� ��� ��� ������� clientHandler ����� isNameValid
        // � ���������� "" � "exit" ����� ���������� false
        doReturn(false).when(clientHandler).isNameValid(any());

        // ��������� ��������� ����� � ����� ���������� ������ clientHandler
        Thread clientThread = new Thread(clientHandler);
        clientThread.start();//�������� �����

        // ���������� ���������� ������
        clientThread.join(1000);

        //�����������, ��� ����� println() ���-������� out ��� ������ 1 ��� � ���������� "������� ���� ���:"
        verify(out, times(1)).println(eq("������� ���� ���:"));
        //�����������, ��� ����� println() ���-������� out ��� ������ 1 ��� � ���������� "�������� ���. ���������� ��� ���."
        verify(out, times(1)).println(eq("�������� ���. ���������� ��� ���."));
        //�����������, ��� ����� logMessage() ���-������� chatServer �� ���� �� ��� ������ �� ����� ���������� �����
        verify(chatServer, never()).logMessage(any());
        //���������, ��� ����� broadcastMessage() ���-������� clientHandler �� ���� �� ��� ������ �� ����� ���������� �����
        verify(clientHandler, never()).broadcastMessage(any());
        //���������, ��� ����� listenForMessages() ���-������� clientHandler �� ���� �� ��� ������ �� ����� ���������� �����
        verify(clientHandler, never()).listenForMessages();
    }

    //�����������,��� ����� ������������ ������ "exit", �� ������ ����������� �� ���� � ��������� ��������������� ���������
    @Test
    public void testListenForMessagesExitKeyword() throws IOException {
        // ����������� ��������� ��� ������� in,������ ����� ���������� "exit"
        when(in.readLine()).thenReturn("exit");

        // ����� ������������ ������
        clientHandler.listenForMessages();

        //�����������, ��� ����� println() ���-������� out ��� ������ 1 ��� � ���������� "�� ��������� �� ����."
        verify(out, times(1)).println(eq("�� ��������� �� ����."));
    }

    //�������� ����� ������� �� ����������
    @Test
    public void testIsNameValid() {
        String name = "John";//���������������� ���������� � ������
        //� ������� clientHandler ���������� ����� isNameValid �������� � �������� ��������� �������� name
        // ������������ ��������� ���� boolean ��������� � result
        boolean result = clientHandler.isNameValid(name);
        //���� ���� �� ������� � ������� ��������� ��������� "���������� true ��� ��������������� ����� name"
        assertTrue(result, "���������� true ��� ��������������� ����� name");
    }


    @Test
    public void testDisconnect() throws IOException {
        // ������������� ��������� � ������ disconnect � ������ ������ ����� ������ ������ �� ������ ������
        doNothing().when(clientHandler).disconnect();
        //�������� ����� disconnect
        clientHandler.disconnect();
        // �����������, ��� ����� disconnect() ��� ������ ����� ���� ���
        verify(clientHandler, times(1)).disconnect();
    }
}