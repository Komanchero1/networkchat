import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatClientTest {

    //���� ���������, ��� ����� loadSettings() ��������� ��������� ��������� �� �����
    //� ��������� �� � ��������������� ���� ������ ChatClient
    @Test
    void testLoadSettings() throws IOException {
        //��������� ��� ������ ��� ���������� ������
        BufferedReader mockedReader = Mockito.mock(BufferedReader.class);
        //����������� ��������� mockedReader , ����� ���������� ������ "port=8080"
        when(mockedReader.readLine()).thenReturn("port=8080");
        //��������������� mockedReader � �������� ���������� ��� ������ ChatClient
        ChatClient.setReader(mockedReader);
        //����������� ��������� �� ����� "settings.txt" � ��������� �� � ��������������� ���� ������
        ChatClient.loadSettings("settings.txt");
        //�����������, ��� �������� �����, ���������� � ������� ������ getPort(), ����� 8080
        assertEquals(8080, ChatClient.getPort());
    }

    //�������� ��� ����� LogMessage ��������� ����������  ��������� � ����, � ��� ���
    // ��������� ����� ������� ��������� �� �����
    @Test
    void testLogMessage() throws IOException {
        //���������� ���������� message � ��������� � ��� �������� ���������
        String message = "Test log message";
        //���������� ���������� tempFileName � ��������� � ��� ��������� ����
        String tempFileName = "temp-file.log";
        //��������������� ��������� ���� ��� ����������� � ������� ������ setLogFile
        ChatClient.setLogFile(tempFileName);
        //���������� ����� logMessage() � �������� ����������
        ChatClient.logMessage(message);
        //��������� ��� ������ � ���������� �����
        BufferedReader reader = new BufferedReader(new FileReader(tempFileName));
        //��������� ������ ��������� ��������� � ���������� lastLine
        String lastLine = null, line;
        //��������� ������ ���� ����� ���� ���� ���� ������
        while ((line = reader.readLine()) != null) {
            //����������� ����������� ������ ����������  lastLine
            lastLine = line;
        }

        assertNotNull(lastLine);//�������� ��� ���� ������� ������
        //�������� ��� ������ ������������ lastLine ���������� � �������� ���������  message
        assertTrue(lastLine.contains(message));

        reader.close(); //��������� �����
        new File(tempFileName).delete(); // �������� ���������� �����
    }

    // ���� ���������, ��� ����� listenForMessages() ��������� ������������ ������� ���������
    // � ���������� �� � ���-����
    @Test
    void testListenForMessages() throws IOException, InterruptedException {
        //��������� BufferedReader ������ mockedInput, ������� ����� ����������� ���� ������������
        BufferedReader mockedInput = Mockito.mock(BufferedReader.class);
        //��� ������ ������� ��������� ExecutorService, ������� ������������ ����� ��� ������� � �����
        //������� ������� ��� ����������������� ���������� �����
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //������ ��������� mockedInput ������ ���������� "exit" ����� "Test message"
        when(mockedInput.readLine()).thenReturn("exit", "Test message");
        //���������� ���������� tempFileName � ��������� � ��� ��������� ����
        String tempFileName = "temp-file.log";
        //��������������� ��������� ���� ��� ����������� � ������� ������ setLogFile
        ChatClient.setLogFile(tempFileName);
        //�������� ����� listenForMessages � �������� ���������� �������� ��� ��� ������
        // ����� ������������� � ����� � ������� ���������� ��������� �����
        ChatClient.listenForMessages(mockedInput, executor);
        executor.shutdown();//��������� ������ ������ �  executor

        // �������� ���������� ������
        executor.awaitTermination(1, TimeUnit.SECONDS);

        //��������� ������ BufferedReader ��� ������ �����
        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        //��������� ������ ����������� � lastLine
        String lastLine = null, line;
        //� ����� ��������� ���� � ��������� ��������� ������ � line
        while ((line = logReader.readLine()) != null) {
            //��������� ��������� ������ � lastLine
            lastLine = line;
        }


        assertNotNull(lastLine);//�������� ��� ���� ������� ������
        //�������� ��� ������ ������������ lastLine ���������� � �������� ���������  message
        assertTrue(lastLine.contains("Test message"));

        logReader.close();//��������� �����
        new File(tempFileName).delete(); // �������� ���������� �����
    }

    @Test
    void testSendMessages() throws IOException, InterruptedException {
        //��������� ��� ������ ��� �������� ������ ���������
        PrintWriter mockedOutput = Mockito.mock(PrintWriter.class);
        //��������� ��� ������ ��� �������� ����� ���������
        BufferedReader mockedUserInput = Mockito.mock(BufferedReader.class);
        //��������� ��� ������ ������� ����� �������������� ��� ���������� ����������� �����
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //������ ��������� ��� ������� mockedUserInput ������� ��� ������ readLine ����������
        //"test message"  ����� "exit"
        when(mockedUserInput.readLine()).thenReturn("test message", "exit");
        //���������� ���������� tempFileName � ��������� � ��� ��������� ����
        String tempFileName = "temp-file.log";
        //��������������� ��������� ���� ��� ����������� � ������� ������ setLogFile
        ChatClient.setLogFile(tempFileName);
        //�������� ����� sendMessages � �������� ���������� �������� ��� ��� �������
        ChatClient.sendMessages(mockedOutput, mockedUserInput, executor);

        executor.shutdown(); //����������� ������ ������
        executor.awaitTermination(1, TimeUnit.SECONDS); // �������� ���������� �����

        verify(mockedOutput).println("test message");//��������� ��� ����� println ��� ������ � "test message"
        verify(mockedOutput).println("exit");// � � "exit"
        //��������� ������ BufferedReader ��� ������ �����
        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        //��������� ������ ��������� ��������� � ���������� lastLine
        String lastLine = null, line;
        //� ����� ��������� ���� � ��������� ��������� ������ � line
        while ((line = logReader.readLine()) != null) {
            lastLine = line; //��������� ��������� ������ � lastLine
        }

        assertNotNull(lastLine);//�������� ��� ���� ������� ������
        //�������� ��� ������ ������������ lastLine ���������� � �������� exit
        assertTrue(lastLine.contains("Sent: exit"));

        logReader.close();//��������� �����
        new File(tempFileName).delete(); // �������� ���������� �����
    }
}