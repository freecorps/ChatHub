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
    private List<ClientHandler> allClients = new ArrayList<>();
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

    // Quando um ClientHandler se conecta, adicione-o à lista
    public void addClient(ClientHandler client) {
        allClients.add(client);
    }

    // Quando um ClientHandler se desconecta, remova-o da lista
    public void removeClient(ClientHandler client) {
        allClients.remove(client);
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
                    addClient(clientHandler); // Adicione esta linha
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
                                System.out.println("enviando chat");
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
            for (ClientHandler client : allClients) {
                client.out.println(Protocol.encodeMessage(chatMessage));
            }
        }
  
        
        private void disconnect() {
            leaveRoom();
            clients.remove(id);
            Server.getInstance(null).removeClient(this); // Adicione esta linha

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
            String encodedRoomList = encodeRoomList(salas);
            message.put("roomList", encodedRoomList);

            out.println(Protocol.encodeMessage(message));
        }

        private String encodeRoomList(List<Sala> salas) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < salas.size(); i++) {
                builder.append(salas.get(i).getNome());
                if (i < salas.size() - 1) {
                    builder.append(",");
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

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
