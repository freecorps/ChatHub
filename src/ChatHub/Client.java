package ChatHub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;
import java.util.List;

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private UUID clientUUID;
    private final CountDownLatch latch = new CountDownLatch(1);
    private String serverAddress;
    private int serverPort;
    private List<String> salas;
    private String salaAtual;

    public Client(String serverAddress, int serverPort) throws IOException, InterruptedException {
        this(serverAddress, serverPort, null);
    }

    public Client(String serverAddress, int serverPort, CountDownLatch serverReadyLatch) throws IOException, InterruptedException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        if(serverReadyLatch != null) {
            serverReadyLatch.await();
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
    
    public void setSalaAtual(String salaLegal) {
        this.salaAtual = salaLegal;
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
    
    private void processInitialRoomList(String encodedRoomList) {
        String[] roomNames = encodedRoomList.split(","); 
        for (String roomName : roomNames) {
            System.out.println("Room: " + roomName);
        }
        this.salas = Arrays.asList(roomNames);
    }

    
    public void sendMessageToRoom(String roomName, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("action", "chat");
        message.put("room", roomName);
        message.put("UUID", clientUUID.toString());
        message.put("content", content);
        sendMessage(message);
    }
    
    public void atualizarListaDeSalas(List<String> salas) {
        this.salas = salas;
    }
    
    public List<String> getSalas() {
        return salas;
    }
    
    public interface MessageListener {
        void messageReceived(String message);
    }

    private MessageListener messageListener;

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    private void processMessage(Map<String, String> message) {
        System.out.println("Message recived from the server: "+message);

        String action = message.get("action");
        
        if ("chat".equals(action) && messageListener != null) {
            String sala = message.get("room");
            UUID cleber = UUID.fromString(message.get("UUID"));
            if(this.salaAtual != sala) {
                return;
            }
            String content = message.get("content");
            String mensagem = cleber+ ": " + content;
            messageListener.messageReceived(mensagem);
        }

        if (action != null) {
            switch (action) {
                case "setUUID":
                    setClientUUID(UUID.fromString(message.get("UUID")));
                    break;
                case "initialRoomList":
                    processInitialRoomList(message.get("roomList"));
                    break;
                case "updateRoomList":
                    String roomsString = message.get("rooms");
                    List<String> rooms = Arrays.asList(roomsString.split(","));
                    atualizarListaDeSalas(rooms);
                    break;
            }
        }
    }
}