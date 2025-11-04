package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.interfaces.GameController;
import com.abyssdev.entertheabyss.network.ServerThread;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pantalla SOLO para el servidor.
 * No renderiza el juego, solo maneja la lógica de red y estado del juego.
 */
public class PantallaServidor extends Pantalla implements GameController {

    private ServerThread serverThread;
    private boolean juegoIniciado = false;

    // Estado del juego (sin renderizado)
    private HashMap<Integer, PosicionJugador> posicionesJugadores = new HashMap<>();
    private ArrayList<Integer> enemigosVivos = new ArrayList<>();
    private String salaActual = "sala1";
    private boolean bossVivo = true;

    // UI del servidor
    private BitmapFont font;
    private ArrayList<String> logs = new ArrayList<>();
    private final int MAX_LOGS = 20;

    // Clase interna para guardar posiciones
    private static class PosicionJugador {
        float x, y;
        int vida = 100;
        int monedas = 0;

        PosicionJugador(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public PantallaServidor(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        agregarLog("=================================");
        agregarLog("🖥️  SERVIDOR DEDICADO");
        agregarLog("=================================");

        // Iniciar servidor
        serverThread = new ServerThread(this);
        serverThread.start();

        agregarLog("🟢 Servidor iniciado en puerto 9999");
        agregarLog("👥 Esperando 2 jugadores...");
        agregarLog("");
        agregarLog("Presiona ESC para detener servidor");

        // Inicializar enemigos para sala1 (ejemplo: IDs del 0 al 4)
        for (int i = 0; i < 5; i++) {
            enemigosVivos.add(i);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Título
        font.getData().setScale(2f);
        font.setColor(Color.CYAN);
        font.draw(batch, "SERVIDOR DEDICADO", 20, Gdx.graphics.getHeight() - 20);

        // Estado
        font.getData().setScale(1.5f);
        font.setColor(Color.GREEN);
        String estado = juegoIniciado ? "🎮 JUEGO EN CURSO" : "⏳ ESPERANDO JUGADORES";
        font.draw(batch, estado, 20, Gdx.graphics.getHeight() - 60);

        font.setColor(Color.YELLOW);
        font.draw(batch, "👥 Jugadores: " + serverThread.getConnectedClients() + "/2",
            20, Gdx.graphics.getHeight() - 90);

        if (juegoIniciado) {
            font.setColor(Color.WHITE);
            font.draw(batch, "🗺️  Sala: " + salaActual, 20, Gdx.graphics.getHeight() - 120);
            font.draw(batch, "👾 Enemigos vivos: " + enemigosVivos.size(),
                20, Gdx.graphics.getHeight() - 150);
            font.draw(batch, "👑 Boss: " + (bossVivo ? "Vivo" : "Muerto"),
                20, Gdx.graphics.getHeight() - 180);
        }

        // Logs
        font.getData().setScale(1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "--- LOGS ---", 20, Gdx.graphics.getHeight() - 220);

        float y = Gdx.graphics.getHeight() - 250;
        for (int i = Math.max(0, logs.size() - MAX_LOGS); i < logs.size(); i++) {
            font.draw(batch, logs.get(i), 20, y);
            y -= 20;
        }

        batch.end();

        // Manejo de input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            agregarLog("🔴 Deteniendo servidor...");
            serverThread.disconnectClients();
            serverThread.terminate();
            juego.setScreen(new MenuInicio(juego, batch));
        }
    }

    private void agregarLog(String mensaje) {
        logs.add(mensaje);
        System.out.println(mensaje);

        // Limitar logs
        if (logs.size() > 100) {
            logs.remove(0);
        }
    }

    // ========================================
    // 🎮 IMPLEMENTACIÓN DE GameController
    // ========================================

    @Override
    public void startGame() {
        juegoIniciado = true;
        agregarLog("🎮 ¡Juego iniciado con 2 jugadores!");

        // Inicializar posiciones de jugadores
        posicionesJugadores.put(1, new PosicionJugador(10f, 10f));
        posicionesJugadores.put(2, new PosicionJugador(12f, 10f));

        // Enviar mensaje de inicio a todos
        serverThread.sendMessageToAll("Start");
    }

    @Override
    public void move(int numPlayer, float x, float y) {
        PosicionJugador jugador = posicionesJugadores.get(numPlayer);
        if (jugador != null) {
            jugador.x = x;
            jugador.y = y;
            // El ServerThread ya reenvía la posición a los otros clientes
        }
    }

    @Override
    public void attack(int numPlayer) {
        agregarLog("⚔️ Jugador " + numPlayer + " atacó");
        // El ServerThread ya notifica a otros clientes
    }

    @Override
    public void enemyKilled(int numPlayer, int enemyId) {
        agregarLog("💀 Jugador " + numPlayer + " mató enemigo " + enemyId);

        // Eliminar enemigo de la lista
        enemigosVivos.remove(Integer.valueOf(enemyId));

        // Dar monedas al jugador
        PosicionJugador jugador = posicionesJugadores.get(numPlayer);
        if (jugador != null) {
            jugador.monedas += 10;
            serverThread.sendMessageToAll("UpdateCoins:" + numPlayer + ":" + jugador.monedas);
            agregarLog("💰 Jugador " + numPlayer + " ahora tiene " + jugador.monedas + " monedas");
        }

        // El ServerThread ya envió EnemyDead a todos
    }

    @Override
    public void bossKilled(int numPlayer) {
        agregarLog("👑 ¡Jugador " + numPlayer + " mató al JEFE!");
        bossVivo = false;

        // Dar monedas al jugador
        PosicionJugador jugador = posicionesJugadores.get(numPlayer);
        if (jugador != null) {
            jugador.monedas += 50;
            serverThread.sendMessageToAll("UpdateCoins:" + numPlayer + ":" + jugador.monedas);
        }

        // Verificar victoria
        if (enemigosVivos.isEmpty()) {
            agregarLog("🏆 ¡VICTORIA! Jugador " + numPlayer + " ganó");
            serverThread.sendMessageToAll("EndGame:" + numPlayer);
        }
    }

    @Override
    public void changeRoom(int numPlayer, String roomId) {
        agregarLog("🚪 Jugador " + numPlayer + " cambió a sala " + roomId);
        salaActual = roomId;

        // Notificar a todos los clientes
        serverThread.sendMessageToAll("RoomChange:" + roomId);

        // Reiniciar enemigos según la sala
        enemigosVivos.clear();
        if (roomId.equals("sala5")) {
            // Sala del boss
            for (int i = 0; i < 15; i++) {
                enemigosVivos.add(i);
            }
            bossVivo = true;
        } else if (roomId.equals("sala1")) {
            for (int i = 0; i < 5; i++) {
                enemigosVivos.add(i);
            }
        }
    }

    @Override
    public void timeOut() {
        agregarLog("⏰ Tiempo agotado");
        serverThread.disconnectClients();
    }

    // Métodos no usados por el servidor puro
     public void connect(int numPlayer) {}
     public void start() {}
     public void updatePlayerPosition(int numPlayer, float x, float y) {}
     public void updateEnemyDead(int enemyId) {}
     public void updateBossDead() {}
     public void updateCoins(int numPlayer, int coins) {}
     public void updateHealth(int numPlayer, int health) {}
     public void updateRoomChange(String roomId) {}
     public void playerAttack(int numPlayer) {}
     public void endGame(int winner) {}
     public void backToMenu() {}




    @Override
    public void resize(int width, int height) {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (serverThread != null) {
            serverThread.terminate();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
