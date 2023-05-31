package ChatHub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private UUID clientUUID;
    private final CountDownLatch latch = new CountDownLatch(1);
    private String serverAddress;
    private int serverPort;

    public Client(String serverAddress, int serverPort) throws IOException, InterruptedException {
        this(serverAddress, serverPort, null);
    }

    public Client(String serverAddress, int serverPort, CountDownLatch serverReadyLatch) throws IOException, InterruptedException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        if(serverReadyLatch != null) {
            serverReadyLatch.await(); // Only wait if a latch was provided
        }
        new Thread(this::messageListener).start();
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public void sendMessage(Map<String, String> message) {
        out.println(Protocol.encodeMessage(message));
    }

    public Map<String, String> receiveMessage() throws IOException {
        String encodedMessage = in.readLine();
        return Protocol.decodeMessage(encodedMessage);
    }

    public void close() throws IOException {
        socket.close();
    }

    public void setClientUUID(UUID clientUUID) {
        this.clientUUID = clientUUID;
        latch.countDown();
    }

    public UUID getClientUUID() throws InterruptedException {
        latch.await();
        return this.clientUUID;
    }
    
    private void messageListener() {
        try {
            while (true) {
                Map<String, String> message = receiveMessage();
                processMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error reading message from server: " + e.getMessage());
        }
    }
    
    public void sendMessageToRoom(String roomName, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("action", "chat");
        message.put("room", roomName);
        message.put("UUID", clientUUID.toString());
        message.put("content", content);
        sendMessage(message);
    }
    
    private void processMessage(Map<String, String> message) {
        
        System.out.println("Message recived from the server: "+message);
        
        String action = message.get("action");

        if (action != null) {
            switch (action) {
                case "setUUID":
                    setClientUUID(UUID.fromString(message.get("UUID")));
                    break;
            }
        }
    }
    
}