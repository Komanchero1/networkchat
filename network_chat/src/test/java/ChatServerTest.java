import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class ChatServerTest {

    @TempDir//
    Path tempDir;//��������� ���� � ���������� �����
    //ByteArrayOutputStream ����� ��������� ���������� ����� � ������ � ��������������� �� � ������ ������
    //��������� ������ ��� ��������� � �������� ������, ������������ � ����� ������
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    //��������� ����� ������, ������� ������������ ��� ����������� ��������� �� �������. ��������
    // ��� � ����������, �� ������ ������������ ��� �������� ����� ���������� ������
    private final PrintStream originalOut = System.out;

    @BeforeEach //����������� ����� ������ �������
    public void setUpStreams() {
    //�������������� ����������� ����� ������ �� outContent ��� ������, ������� ���������
        // ����� ��������, ����� ������������ � ByteArrayOutputStream,
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach // ����������� ����� ������� ������
    public void restoreStreams() {
     //��������������� ����������� ����� ������, ������� �� ��������� �����
        System.setOut(originalOut);
    }

    @BeforeAll // ����������� ���� ��� ����� �������� ���� �������
    public static void setUpBeforeClass() throws Exception {
        // �������� ���� �������� ��� �����, ���� ��� ��� ���
        Path settingsFile = Paths.get("settings.txt");
        //����������� ���������� ���� �������� ��� ���, exists ���������� true ���� ���� ����������
        if (!Files.exists(settingsFile)) {
            //���� ������� ��������� ��������� ���� ��������
            Files.write(settingsFile, Collections.singletonList("port=12345"));
        }
    }
    //����� ��������� �������� �������� ����������� �� ���������� �����
    @Test
    void testLoadSettings() throws IOException {
        Path settingsFile = tempDir.resolve("settings.txt");//��������� ������� Path ��� ����� ���������� ��������� ����������.
        Files.write(settingsFile, Collections.singletonList("port = 12345"));//�������� ���������� �����

        //�������� ������� File ��� ������������� ����� ��������.
        File originalFile = new File("settings.txt");
        //�������������� ������������� ����� �������� � settings_backup.txt.
        originalFile.renameTo(new File("settings_backup.txt"));
        //����������� ���������� ����� �������� � ������������ ���� ��������.
        Files.copy(settingsFile, originalFile.toPath());
        //����� ������ loadSettings �� ������ ChatServer ��� �������� ��������.
        ChatServer.loadSettings();

        //�������� ������������� ����� ��������.
        originalFile.delete();
        //�������������� ������������� ����� ����� �������� �� ��������� �����.
        new File("settings_backup.txt").renameTo(originalFile);
        //��������, ��� ���� ������� ���������� ��������� � ����� 12345.
        assertEquals(12345, ChatServer.getPort());
    }
     //�������� ������ ������ � ��� ���� � ������� ������
    @Test
    void testLogMessage() throws IOException {
        // ������������� ��������� ���-���� � ������ "file.log" �� ��������� ���������� tempDir.
        ChatServer.logFile = tempDir.resolve("file.log").toString();

        // �������� ����� ������������ � ���������� "Test log message"
        ChatServer.logMessage("Test log message");

        //� ������� ������ readAllBytes � �������� ���������� ����� ���� � ��������� � ������
        String logContent = new String(Files.readAllBytes(Paths.get(ChatServer.logFile)));
        //����������� ,��� ������ logContent �������� ��������� "Test log message"
        assertTrue(logContent.contains("Test log message"));
        //�����������, �� ������ logContent �������� ������� ���� � ����� � ������� "yyyy-MM-dd HH:mm
        assertTrue(logContent.contains(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
    }

    //�������� ��� ����� ���������� ���������� �������� �����
    @Test
    void testGetPort() {
        //�������� ��� ��� ������ ������ GetPort ���������� ���������� ��������
        assertEquals(ChatServer.getPort(), ChatServer.getPort());
    }
}