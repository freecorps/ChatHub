package ChatHub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Server {
    private int port;
    private CountDownLatch serverReadyLatch;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private ConcurrentHashMap<String, List<ClientHandler>> rooms;
    private ConcurrentHashMap<UUID, ClientHandler> clients;
    private List<Sala> salas;

    private static Server instance;
    private static int defaultPort = 12345;
    
    public static Server getInstance(Integer serverPort) {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);
        if (instance == null) {
            if (serverPort == null) {
                try {
                    instance = new Server(defaultPort, serverReadyLatch);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    instance = new Server(serverPort, serverReadyLatch);
                    defaultPort = serverPort; // Atualize a porta padrão
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // Agora o servidor deve ser iniciado em uma thread separada
            new Thread(() -> {
                instance.start(); // Executa o servidor em uma thread separada
            }).start();
        }
        return instance;
    }


    public List<Sala> getSalas() {
        return salas;
    }

    public void addSala(Sala sala) {
        salas.add(sala);
        sendRoomListToAllClients();
    }

    public void removeSala(Sala sala) {
        salas.remove(sala);
        sendRoomListToAllClients();
    }

    
    public void sendRoomListToAllClients() {
        Map<String, String> message = new HashMap<>();
        message.put("action", "updateRoomList");
        message.put("rooms", String.join(",", salas.stream().map(Sala::getNome).collect(Collectors.toList())));

        for (ClientHandler client : clients.values()) {
            client.out.println(Protocol.encodeMessage(message));
        }
    }
    
    public void joinRoom(UUID clientId, String roomName) {
        System.out.println("Entrada em sala requisitada: "+ clientId + " para a sala: " + roomName);
        ClientHandler clientHandler = clients.get(clientId);
        if (clientHandler != null) {
            clientHandler.joinRoom(roomName);
        }
    }
    
    public void leaveRoom(UUID clientId) {
        System.out.println("Saída da sala solicitada: " + clientId);
        ClientHandler clientHandler = clients.get(clientId);
        if (clientHandler != null) {
            clientHandler.leaveRoom();
        }
    }
    
    public Sala getSala(String nomeSala) {
        for (Sala sala : salas) {
            if (sala.getNome().equals(nomeSala)) {
                return sala;
            }
        }
        return null;
    }
    
    public ClientHandler getClientHandler(UUID clientId) {
        return clients.get(clientId);
    }
    
    public Server(int serverPort, CountDownLatch serverReadyLatch) throws IOException {
        this.serverReadyLatch = serverReadyLatch;
        this.port = serverPort;
        serverSocket = new ServerSocket(serverPort);
        threadPool = Executors.newCachedThreadPool();
        rooms = new ConcurrentHashMap<>();
        clients = new ConcurrentHashMap<>();
        salas = new ArrayList<>();
    }
     
        public void start() {
        System.out.println("Server started on port " + serverSocket.getLocalPort());
        serverReadyLatch.countDown(); // Signal that the server is ready

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.put(clientHandler.getId(), clientHandler);
                clientHandler.sendUUIDToClient();
                threadPool.submit(clientHandler);
            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientAddress;
        private String roomName;
        private final UUID id;
        
        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.id = UUID.randomUUID();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            clientAddress = clientSocket.getInetAddress().toString();
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + clientAddress);
            sendInitialRoomList();

            String encodedMessage;
            try {
                while ((encodedMessage = in.readLine()) != null) {
                    Map<String, String> message = Protocol.decodeMessage(encodedMessage);
                    System.out.println("Received message: " + message);

                    String action = message.get("action");

                    if (action != null) {
                        switch (action) {
                            case "createRoom":
                                createRoom(message.get("roomName"));
                                break;
                            case "joinRoom":
                                joinRoom(message.get("roomName"));
                                break;
                            case "leaveRoom":
                                leaveRoom();
                                break;
                            case "chat":
                                processChat(message);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                 System.err.println("Error handling client: " + e.getMessage());
            } finally {
                disconnect();
            }

            System.out.println("Client disconnected: " + clientAddress);
        }

        private void createRoom(String roomName) {
            if (!rooms.containsKey(roomName)) {
                rooms.put(roomName, new ArrayList<>());
                Sala sala = new Sala(roomName);
                Server.getInstance(null).addSala(sala);
            }
        }
        
        public void sendUUIDToClient() { 
            Map<String, String> message = new HashMap<>();
            message.put("action", "setUUID");
            message.put("UUID", id.toString());
            out.println(Protocol.encodeMessage(message));
        }

        public void joinRoom(String roomName) {
            if (roomName != null && rooms.containsKey(roomName)) {
                this.roomName = roomName;
                List<ClientHandler> clientsInRoom = rooms.get(roomName);
                clientsInRoom.add(this);
                rooms.put(roomName, clientsInRoom);
            }
        }

        private void leaveRoom() {
            if (roomName != null && rooms.containsKey(roomName)) {
                rooms.get(roomName).remove(this);
                if (rooms.get(roomName).isEmpty()) {
                    rooms.remove(roomName);
                    Sala sala = new Sala(roomName);
                    Server.getInstance(null).removeSala(sala);
                }
                roomName = null;
            }
        }

        private void processChat(Map<String, String> chatMessage) {
            if (roomName != null && rooms.containsKey(roomName)) {
                for (ClientHandler client : rooms.get(roomName)) {
                    client.out.println(Protocol.encodeMessage(chatMessage));
                }
            }
        }   
        
        private void disconnect() {
            leaveRoom();
            clients.remove(id);

            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
        
        public UUID getId() {
            return id;
        }
        
        public void sendInitialRoomList() {
            List<Sala> salas = Server.getInstance(null).getSalas();
            Map<String, String> message = new HashMap<>();
            message.put("action", "initialRoomList");

            // Aqui você precisa codificar a lista de salas de alguma forma que possa ser enviada como uma string.
            // A maneira específica de fazer isso depende de como você deseja que seja o formato da lista no cliente.
            String encodedRoomList = encodeRoomList(salas);
            message.put("roomList", encodedRoomList);

            out.println(Protocol.encodeMessage(message));
        }

        private String encodeRoomList(List<Sala> salas) {
            // Codifique a lista de salas aqui
            // Por exemplo, você pode juntar todos os nomes de sala em uma string com um delimitador específico
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < salas.size(); i++) {
                builder.append(salas.get(i).getNome());
                if (i < salas.size() - 1) { // Não adicione o delimitador após o último elemento
                    builder.append(","); // Use a vírgula como delimitador
                }
            }
            return builder.toString();
        }
    }

    public static void main(String[] args) {
        int serverPort = 12345;
        CountDownLatch serverReadyLatch = new CountDownLatch(1);

        try {
            instance = new Server(serverPort, serverReadyLatch); // Pass the latch to the Server
            new Thread(() -> {
                instance.start(); // Run the server in a separate thread
            }).start();

            try {
                // Create the client and pass the latch to it
                Client client = new Client("localhost", serverPort, serverReadyLatch);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for the server to start: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
