package pentago.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import pentago.networking.server.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private Server server;

    @BeforeEach
    public void setUp() throws IOException {
        server = new Server(0);
    }

    @Test
    void testPingPong() throws IOException {
        String s;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);) {
            printWriter1.println("PING");
            s = bufferedReader1.readLine();
            assertEquals(s, "PONG");
        } finally {
            socket1.close();
            server.stop();
        }

    }

    @Test
    void testWrongInput() throws IOException {
        String s;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);) {
            // wrong input
            printWriter1.println("HELLO-Server's test");

            s = bufferedReader1.readLine().split("~")[0];
            assertEquals(s, "ERROR");

            // testing missing description
            printWriter1.println("LOGIN~");
            assertEquals(s, "ERROR");

            printWriter1.println("LOGIN~Ha~n");
            assertEquals(s, "ERROR");
        } finally {
            socket1.close();
            server.stop();
        }
    }

    @Test
    void testUserName() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true)) {

            printWriter1.println("HELLO~Server's test");
            printWriter1.println("LOGIN~Han123");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "HELLO");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "LOGIN");

            printWriter1.println("LOGIN~Han123");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "ALREADYLOGGEDIN");

            printWriter2.println("HELLO~Server's test");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "HELLO");

            printWriter2.println("LOGIN~Han123");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "ALREADYLOGGEDIN");

        } finally {
            socket1.close();
            server.stop();
        }
    }

    @Test
    void testWrongMoves() throws IOException {
        String[] ss;
        server.start();
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);) {
            // first client logging in
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();

            // second client logging in
            printWriter2.println("HELLO~Server's test");
            printWriter2.println("LOGIN~server_test_2");

            printWriter1.println("QUEUE");
            printWriter2.println("QUEUE");
            bufferedReader1.readLine();

            printWriter1.println("MOVE~37~0");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "ERROR");

            printWriter1.println("MOVE~-1~0");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "ERROR");
        } finally {
            socket1.close();
            socket2.close();
            server.stop();
        }
    }

    @Test
    void TestWrongTurn() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);) {
            // first client logging in
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();

            // second client logging in
            printWriter2.println("HELLO~Server's test");
            bufferedReader2.readLine();
            printWriter2.println("LOGIN~server_test_2");
            bufferedReader2.readLine();

            printWriter1.println("QUEUE");
            printWriter2.println("QUEUE");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter1.println("MOVE~7~0");
            assertEquals(bufferedReader1.readLine(), "MOVE~7~0");
            assertEquals(bufferedReader2.readLine(), "MOVE~7~0");

            printWriter2.println("MOVE~10~2");
            assertEquals(bufferedReader1.readLine(), "MOVE~10~2");
            assertEquals(bufferedReader2.readLine(), "MOVE~10~2");
            printWriter2.println("MOVE~13~2");
            assertEquals(bufferedReader2.readLine().split("~")[0], "ERROR");
        } finally {
            socket1.close();
            socket2.close();
            server.stop();
        }
    }

    @Test
    void TestPentagoCommands() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);) {

            // first client logging in
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();

            // second client logging in
            printWriter2.println("HELLO~Server's test");
            bufferedReader2.readLine();
            printWriter2.println("LOGIN~server_test_2");
            bufferedReader2.readLine();

            printWriter1.println("LIST");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "LIST");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            printWriter2.println("LIST");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "LIST");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            printWriter1.println("QUEUE");
            printWriter2.println("QUEUE");
            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "NEWGAME");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "NEWGAME");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            // test move wrong move (writer is the first player)
            printWriter2.println("MOVE~7~0");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "ERROR");

            printWriter1.println("MOVE~7~0");
            assertEquals(bufferedReader1.readLine(), "MOVE~7~0");
            assertEquals(bufferedReader2.readLine(), "MOVE~7~0");

            printWriter2.println("MOVE~10~2");
            assertEquals(bufferedReader1.readLine(), "MOVE~10~2");
            assertEquals(bufferedReader2.readLine(), "MOVE~10~2");

            // testing winning
            printWriter1.println("MOVE~14~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter2.println("MOVE~11~2");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter1.println("MOVE~21~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter2.println("MOVE~17~2");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter1.println("MOVE~28~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter2.println("MOVE~16~2");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            printWriter1.println("MOVE~35~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            assertEquals(bufferedReader1.readLine(), "GAMEOVER~VICTORY~server_test_1");
            assertEquals(bufferedReader2.readLine(), "GAMEOVER~VICTORY~server_test_1");
        } finally {
            socket1.close();
            socket2.close();
            server.stop();
        }
    }

    @Test
    public void testNoOneInTheQueue() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());
        Socket socket3 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);
             BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
             PrintWriter printWriter3 = new PrintWriter(new OutputStreamWriter(socket3.getOutputStream()), true);) {
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();
            printWriter1.println("QUEUE");
            printWriter1.println("MOVE~7~0");
            assertEquals(bufferedReader1.readLine().split("~")[0], "ERROR");

            printWriter2.println("HELLO~Server's test");
            bufferedReader2.readLine();
            printWriter2.println("LOGIN~server_test_2");
            bufferedReader2.readLine();
            printWriter2.println("QUEUE");

            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "NEWGAME");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            // queue is free, test new player can queue
            printWriter3.println("HELLO~Server's test");
            bufferedReader3.readLine();
            printWriter3.println("LOGIN~server_test_3");
            bufferedReader3.readLine();
            printWriter3.println("QUEUE");
            printWriter3.println("MOVE~14~4");
            assertEquals(bufferedReader3.readLine().split("~")[0], "ERROR");

        } finally {
            socket1.close();
            socket2.close();
            socket3.close();
            server.stop();
        }


    }

    @Test
    public void testClientLostConnection() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());  // connect to the server
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);) {
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();
            printWriter1.println("QUEUE");

            printWriter2.println("HELLO~Server's test");
            bufferedReader2.readLine();
            printWriter2.println("LOGIN~server_test_2");
            bufferedReader2.readLine();
            printWriter2.println("QUEUE");

            ss = bufferedReader1.readLine().split("~");
            assertEquals(ss[0], "NEWGAME");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "NEWGAME");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            printWriter1.println("MOVE~14~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();
            printWriter2.println("MOVE~11~2");
            bufferedReader1.readLine();
            bufferedReader2.readLine();
            printWriter1.println("MOVE~21~4");
            bufferedReader1.readLine();
            bufferedReader2.readLine();
            printWriter2.println("MOVE~17~2");
            bufferedReader1.readLine();
            bufferedReader2.readLine();

            socket2.close(); // close connection
            assertEquals(bufferedReader1.readLine(), "GAMEOVER~DISCONNECT~server_test_1");
        } finally {
            socket1.close();
            server.stop();
        }

    }

    @Test
    public void testQuit() throws IOException {
        String[] ss;
        server.start();  // start the server
        Socket socket1 = new Socket(InetAddress.getLocalHost(), server.getPort());
        Socket socket2 = new Socket(InetAddress.getLocalHost(), server.getPort());

        try (BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
             PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()), true);
             BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
             PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()), true);) {
            printWriter1.println("HELLO~Server's test");
            bufferedReader1.readLine();
            printWriter1.println("LOGIN~server_test_1");
            bufferedReader1.readLine();

            printWriter2.println("HELLO~Server's test");
            bufferedReader2.readLine();
            printWriter2.println("LOGIN~server_test_2");
            bufferedReader2.readLine();

            printWriter2.println("LIST");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "LIST");
            assertTrue(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));

            printWriter1.println("QUIT");
            bufferedReader1.readLine();

            printWriter2.println("LIST");
            ss = bufferedReader2.readLine().split("~");
            assertEquals(ss[0], "LIST");
            assertFalse(Arrays.asList(ss).contains("server_test_1"));
            assertTrue(Arrays.asList(ss).contains("server_test_2"));
        } finally {
            socket2.close();
            server.stop();
        }
    }

}
