package com.abyssdev.entertheabyss.network;

import com.abyssdev.entertheabyss.interfaces.GameController;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ServerThread extends Thread {

    private DatagramSocket socket;
    private final int serverPort = 9999;
    private boolean end = false;
    private final int MAX_CLIENTS = 2;
    private int connectedClients = 0;
    private ArrayList<Client> clients = new ArrayList<Client>();
    private GameController gameController;

    public ServerThread(GameController gameController) {
        this.gameController = gameController;
        try {
            socket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            System.err.println("Error al crear socket del servidor: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("🟢 Servidor iniciado en puerto " + serverPort);
        do {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if(!end) {
                    System.err.println("Error al recibir paquete: " + e.getMessage());
                }
            }
        } while(!end);
        System.out.println("🔴 Servidor detenido");
    }

    private void processMessage(DatagramPacket packet) {
        String message = (new String(packet.getData())).trim();
        String[] parts = message.split(":");
        int index = findClientIndex(packet);
        System.out.println("📨 Mensaje recibido: " + message);

        if(parts[0].equals("Connect")){
            if(index != -1) {
                System.out.println("⚠️ Cliente ya conectado");
                this.sendMessage("AlreadyConnected", packet.getAddress(), packet.getPort());
                return;
            }

            if(connectedClients < MAX_CLIENTS) {
                connectedClients++;
                Client newClient = new Client(connectedClients, packet.getAddress(), packet.getPort());
                clients.add(newClient);
                sendMessage("Connected:"+connectedClients, packet.getAddress(), packet.getPort());
                System.out.println("✅ Cliente " + connectedClients + " conectado");

                if(connectedClients == MAX_CLIENTS) {
                    System.out.println("🎮 Iniciando juego con " + MAX_CLIENTS + " jugadores");
                    for(Client client : clients) {
                        sendMessage("Start", client.getIp(), client.getPort());
                    }
                    gameController.startGame();
                }
            } else {
                System.out.println("❌ Servidor lleno");
                sendMessage("Full", packet.getAddress(), packet.getPort());
            }
        } else if(index == -1){
            System.out.println("⚠️ Cliente no conectado intentando enviar mensaje");
            this.sendMessage("NotConnected", packet.getAddress(), packet.getPort());
            return;
        } else {
            Client client = clients.get(index);
            switch(parts[0]){
                case "Move":
                    // Move:x:y:action:direction
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    String action = parts.length > 3 ? parts[3] : "ESTATICO";
                    String direction = parts.length > 4 ? parts[4] : "ABAJO";

                    gameController.move(client.getNum(), x, y);

                    // Reenviar a todos los demás clientes CON animación
                    for(Client otherClient : clients) {
                        if(otherClient.getNum() != client.getNum()) {
                            sendMessage("UpdatePosition:Player:" + client.getNum() + ":" + x + ":" + y + ":" + action + ":" + direction,
                                otherClient.getIp(), otherClient.getPort());
                        }
                    }
                    break;

                case "Attack":
                    // Attack
                    gameController.attack(client.getNum());
                    // Notificar a otros jugadores
                    for(Client otherClient : clients) {
                        if(otherClient.getNum() != client.getNum()) {
                            sendMessage("PlayerAttack:" + client.getNum(),
                                otherClient.getIp(), otherClient.getPort());
                        }
                    }
                    break;

                case "EnemyKilled":
                    // EnemyKilled:enemyId
                    int enemyId = Integer.parseInt(parts[1]);
                    gameController.enemyKilled(client.getNum(), enemyId);
                    // Notificar a todos
                    sendMessageToAll("EnemyDead:" + enemyId);
                    break;

                case "BossKilled":
                    // BossKilled
                    gameController.bossKilled(client.getNum());
                    sendMessageToAll("BossDead");
                    break;

                case "ChangeRoom":
                    // ChangeRoom:roomId
                    String roomId = parts[1];
                    gameController.changeRoom(client.getNum(), roomId);
                    break;

                case "RequestEnemies":
                    // Cliente solicita lista de enemigos
                    // El servidor debe enviar la lista desde PantallaJuego
                    break;
            }
        }
    }

    private int findClientIndex(DatagramPacket packet) {
        int i = 0;
        int clientIndex = -1;
        while(i < clients.size() && clientIndex == -1) {
            Client client = clients.get(i);
            String id = packet.getAddress().toString() + ":" + packet.getPort();
            if(id.equals(client.getId())){
                clientIndex = i;
            }
            i++;
        }
        return clientIndex;
    }

    public void sendMessage(String message, InetAddress clientIp, int clientPort) {
        byte[] byteMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, clientIp, clientPort);
        try {
            socket.send(packet);
            System.out.println("📤 Enviado: " + message + " a " + clientIp + ":" + clientPort);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    public void terminate(){
        this.end = true;
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
        this.interrupt();
    }

    public void sendMessageToAll(String message) {
        System.out.println("📢 Broadcast: " + message);
        for (Client client : clients) {
            sendMessage(message, client.getIp(), client.getPort());
        }
    }

    public void disconnectClients() {
        System.out.println("🔌 Desconectando todos los clientes");
        for (Client client : clients) {
            sendMessage("Disconnect", client.getIp(), client.getPort());
        }
        this.clients.clear();
        this.connectedClients = 0;
    }

    public int getConnectedClients() {
        return connectedClients;
    }
}
