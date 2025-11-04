package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.interfaces.GameController;
import com.abyssdev.entertheabyss.mapas.Mapa;
import com.abyssdev.entertheabyss.mapas.Sala;
import com.abyssdev.entertheabyss.mapas.SpawnPoint;
import com.abyssdev.entertheabyss.mapas.ZonaTransicion;
import com.abyssdev.entertheabyss.logica.ManejoEntradas;
import com.abyssdev.entertheabyss.network.ServerThread;
import com.abyssdev.entertheabyss.personajes.Boss;
import com.abyssdev.entertheabyss.personajes.Enemigo;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Hud;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashMap;

public class PantallaJuego extends Pantalla implements GameController {

    private OrthographicCamera camara;
    private Viewport viewport;

    // 🎮 Mapa de jugadores (para multijugador)
    private HashMap<Integer, Jugador> jugadores = new HashMap<>();
    private Jugador jugadorLocal; // El jugador de este servidor

    private Mapa mapaActual;
    private Sala salaActual;
    private ManejoEntradas inputProcessor;
    private boolean jugadorCercaDeOgrini = false;
    private static final float DISTANCIA_INTERACCION = 3f;

    // Transicion
    private boolean enTransicion = false;
    private boolean faseSubida = true;
    private float fadeAlpha = 0f;
    private float fadeSpeed = 2f;
    private String salaDestinoId = null;
    private Texture texturaFade;

    // HUD
    private Hud hud;
    private boolean yaInicializado = false;

    // 🌐 RED
    private ServerThread serverThread;
    private boolean juegoIniciado = false;

    public PantallaJuego(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        if (!yaInicializado) {
            // Inicializar jugador local (servidor)
            jugadorLocal = new Jugador();
            jugadores.put(1, jugadorLocal); // Jugador 1 es el servidor

            // Inicializar mapa
            mapaActual = new Mapa("mazmorra1");
            mapaActual.agregarSala(new Sala("sala1", "maps/mapa1_sala1.tmx", 5));
            mapaActual.agregarSala(new Sala("sala2", "maps/mapa1_sala2.tmx", 1));
            mapaActual.agregarSala(new Sala("sala5", "maps/mapa2_posible.tmx", 15));
            mapaActual.agregarSala(new Sala("sala4", "maps/mapa1_sala4.tmx", 1));
            mapaActual.agregarSala(new Sala("sala3", "maps/mapa1_sala5.tmx", 1));

            camara = new OrthographicCamera();
            viewport = new FitViewport(32, 32 * (9f / 16f), camara);
            texturaFade = generarTextura();
            cambiarSala("sala1");
            hud = new Hud(jugadorLocal, viewport);
            inputProcessor = new ManejoEntradas(jugadorLocal);

            // 🌐 Iniciar servidor
            serverThread = new ServerThread(this);
            serverThread.start();

            yaInicializado = true;

            System.out.println("🎮 Servidor de juego iniciado. Esperando jugadores...");
        } else {
            actualizarCamara();
        }
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void cambiarSala(String destinoId) {
        Sala salaDestino = mapaActual.getSala(destinoId);
        if (salaDestino == null) {
            Gdx.app.error("PantallaJuego", "Sala destino no encontrada: " + destinoId);
            return;
        }

        Sala salaAnterior = salaActual;
        salaActual = salaDestino;
        mapaActual.establecerSalaActual(destinoId);

        if (enTransicion && salaDestinoId != null) {
            for (ZonaTransicion zona : salaAnterior.getZonasTransicion()) {
                if (zona.destinoSalaId.equals(destinoId)) {
                    SpawnPoint spawn = null;
                    for (SpawnPoint sp : salaDestino.getSpawnPoints()) {
                        if (sp.name.equals(zona.spawnName) && sp.salaId.equals(destinoId)) {
                            spawn = sp;
                            break;
                        }
                    }

                    if (spawn != null) {
                        jugadorLocal.setX(spawn.x);
                        jugadorLocal.setY(spawn.y);
                    } else {
                        if (!salaDestino.getSpawnPoints().isEmpty()) {
                            SpawnPoint fallback = salaDestino.getSpawnPoints().first();
                            jugadorLocal.setX(fallback.x);
                            jugadorLocal.setY(fallback.y);
                        } else {
                            centrarJugadorEnSala();
                        }
                    }
                    break;
                }
            }
        } else {
            SpawnPoint defaultSpawn = null;
            for (SpawnPoint sp : salaDestino.getSpawnPoints()) {
                if (sp.name.equals("default") && sp.salaId.equals(destinoId)) {
                    defaultSpawn = sp;
                    break;
                }
            }
            if (defaultSpawn != null) {
                jugadorLocal.setX(defaultSpawn.x);
                jugadorLocal.setY(defaultSpawn.y);
            } else {
                centrarJugadorEnSala();
            }
        }

        camara.position.set(jugadorLocal.getX(), jugadorLocal.getY(), 0);
        camara.update();
        salaActual.getRenderer().setView(camara);

        if (salaActual.getEnemigos() == null || salaActual.getEnemigos().isEmpty()) {
            salaActual.generarEnemigos();
        }

        // 🌐 Notificar cambio de sala a todos los clientes
        serverThread.sendMessageToAll("RoomChange:" + destinoId);
    }

    private void centrarJugadorEnSala() {
        float centroX = salaActual.getAnchoMundo() / 2f;
        float centroY = salaActual.getAltoMundo() / 2f;
        jugadorLocal.setX(centroX);
        jugadorLocal.setY(centroY);
    }

    private void verificarTransiciones() {
        if (enTransicion) return;

        Rectangle hitboxJugador = new Rectangle(
            jugadorLocal.getX() + jugadorLocal.getAncho() / 4f,
            jugadorLocal.getY(),
            jugadorLocal.getAncho() / 2f,
            jugadorLocal.getAlto()
        );

        for (ZonaTransicion zona : salaActual.getZonasTransicion()) {
            if (hitboxJugador.overlaps(zona)) {
                if (salaActual.hayEnemigosVivos()) {
                    System.out.println("No se ha matado a todos los enemigos");
                    return;
                }
                String destinoId = zona.destinoSalaId;
                Sala salaDestino = mapaActual.getSala(destinoId);

                if (salaDestino != null) {
                    enTransicion = true;
                    faseSubida = true;
                    salaDestinoId = destinoId;
                    fadeAlpha = 0f;
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!juegoIniciado) {
            // Pantalla de espera
            batch.begin();
            // Aquí podrías dibujar un mensaje de "Esperando jugadores..."
            batch.end();
            return;
        }

        salaActual.getRenderer().setView(camara);
        salaActual.getRenderer().render();

        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
        if (enemigos != null) {
            for (int i = enemigos.size() - 1; i >= 0; i--) {
                Enemigo enemigo = enemigos.get(i);
                if (enemigo.debeEliminarse()) {
                    jugadorLocal.modificarMonedas(10);
                    System.out.println("✅ Enemigo eliminado. Jugador recibe 10 monedas.");

                    // 🌐 Notificar muerte de enemigo
                    serverThread.sendMessageToAll("EnemyDead:" + i);
                    serverThread.sendMessageToAll("UpdateCoins:1:" + jugadorLocal.getMonedas());

                    enemigos.remove(i);
                    continue;
                }

                if (enemigo.actualizar(delta, jugadorLocal.getPosicion(), salaActual.getColisiones(), enemigos)) {
                    jugadorLocal.recibirDanio(enemigo.getDanio());

                    // 🌐 Actualizar vida
                    serverThread.sendMessageToAll("UpdateHealth:1:" + jugadorLocal.getVida());

                    if (jugadorLocal.getVida() <= 0) {
                        serverThread.sendMessageToAll("EndGame:2"); // Gana el jugador 2
                        juego.setScreen(new PantallaGameOver(juego, batch));
                        return;
                    }
                }
            }

            if (jugadorLocal.getHitboxAtaque().getWidth() > 0) {
                for (int i = enemigos.size() - 1; i >= 0; i--) {
                    Enemigo enemigo = enemigos.get(i);
                    if (!enemigo.debeEliminarse() && jugadorLocal.getHitboxAtaque().overlaps(enemigo.getRectangulo())) {
                        enemigo.recibirDanio(jugadorLocal.getDanio());
                    }
                }
            }
        }

        // Boss logic
        if (salaActual.getId().equalsIgnoreCase("sala5")) {
            if (salaActual.getBoss() == null) {
                salaActual.generarBoss();
            }
            Boss boss = salaActual.getBoss();
            if (boss != null && !boss.debeEliminarse()) {
                if (boss.actualizar(delta, jugadorLocal.getPosicion(), salaActual.getColisiones(), enemigos != null ? enemigos : new ArrayList<>())) {
                    jugadorLocal.recibirDanio(boss.getDanio());

                    // 🌐 Actualizar vida
                    serverThread.sendMessageToAll("UpdateHealth:1:" + jugadorLocal.getVida());

                    if (jugadorLocal.getVida() <= 0) {
                        serverThread.sendMessageToAll("EndGame:2");
                        juego.setScreen(new PantallaGameOver(juego, batch));
                        return;
                    }
                }

                if (jugadorLocal.getHitboxAtaque().getWidth() > 0) {
                    if (jugadorLocal.getHitboxAtaque().overlaps(boss.getRectangulo())) {
                        boss.recibirDanio(jugadorLocal.getDanio());
                    }
                }
            }

            if (boss != null && boss.debeEliminarse() &&
                (enemigos == null || enemigos.isEmpty())) {
                jugadorLocal.modificarMonedas(50);
                Sonidos.detenerTodaMusica();
                System.out.println("✅ ¡JEFE DERROTADO! Jugador recibe 50 monedas.");

                // 🌐 Victoria
                serverThread.sendMessageToAll("BossDead");
                serverThread.sendMessageToAll("EndGame:1");

                juego.setScreen(new PantallaWin(juego, batch));
                return;
            }
        }

        try {
            salaActual.actualizarPuertas();
        } catch (Exception e) {
            System.out.println("ERROR AL ACTUALIZAR PUERTAS");
            e.printStackTrace();
        }

        jugadorLocal.update(delta, salaActual.getColisiones());

        // 🌐 Enviar posición del jugador local a los clientes
        if (juegoIniciado) {
            serverThread.sendMessageToAll("UpdatePosition:Player:1:" +
                jugadorLocal.getX() + ":" + jugadorLocal.getY());
        }

        verificarProximidadOgrini();
        verificarTransiciones();

        if (enTransicion) {
            if (faseSubida) {
                fadeAlpha += fadeSpeed * delta;
                if (fadeAlpha >= 1f) {
                    fadeAlpha = 1f;
                    cambiarSala(salaDestinoId);
                    faseSubida = false;
                }
            } else {
                fadeAlpha -= fadeSpeed * delta;
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    enTransicion = false;
                    salaDestinoId = null;
                }
            }
        }

        if (jugadorCercaDeOgrini && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            Sonidos.pausarMusicaJuego();
            juego.setScreen(new PantallaTienda(juego, batch, jugadorLocal, this));
        }

        actualizarCamara();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        // Dibujar enemigos
        for (Enemigo enemigo : salaActual.getEnemigos()) {
            enemigo.renderizar(batch);
        }

        // Dibujar boss
        Boss boss = salaActual.getBoss();
        if (boss != null) {
            boss.renderizar(batch);
        }

        // Dibujar todos los jugadores
        for (Jugador jugador : jugadores.values()) {
            jugador.dibujar(batch);
        }

        batch.end();

        if (hud != null) {
            hud.draw(batch);
        }

        if (fadeAlpha > 0f) {
            batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.begin();
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(texturaFade, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        if (!enTransicion) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                juego.setScreen(new PantallaPausa(juego, batch, this));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                Sonidos.pausarMusicaJuego();
                juego.setScreen(new PantallaArbolHabilidades(juego, batch, this, jugadorLocal, jugadorLocal.getHabilidades()));
            }
        }
    }

    private void verificarProximidadOgrini() {
        jugadorCercaDeOgrini = false;
        if (salaActual == null || salaActual.getMapa() == null) return;

        com.badlogic.gdx.maps.MapLayer capaObjetos = salaActual.getMapa().getLayers().get("colisiones");
        if (capaObjetos == null) return;

        com.badlogic.gdx.maps.MapObjects objetos = capaObjetos.getObjects();
        for (com.badlogic.gdx.maps.MapObject objeto : objetos) {
            if (!(objeto instanceof com.badlogic.gdx.maps.objects.RectangleMapObject)) continue;

            String nombre = objeto.getProperties().get("nombre", String.class);
            String tipo = objeto.getProperties().get("tipo", String.class);

            if (nombre != null && nombre.equalsIgnoreCase("ogrini") &&
                tipo != null && tipo.equalsIgnoreCase("tienda")) {

                com.badlogic.gdx.maps.objects.RectangleMapObject rectObj =
                    (com.badlogic.gdx.maps.objects.RectangleMapObject) objeto;
                com.badlogic.gdx.math.Rectangle rect = rectObj.getRectangle();

                float objX = (rect.x + rect.width / 2f) / 16f;
                float objY = (rect.y + rect.height / 2f) / 16f;

                float distancia = (float) Math.sqrt(
                    Math.pow(jugadorLocal.getX() - objX, 2) +
                        Math.pow(jugadorLocal.getY() - objY, 2)
                );

                if (distancia <= DISTANCIA_INTERACCION) {
                    jugadorCercaDeOgrini = true;
                    break;
                }
            }
        }
    }

    private void actualizarCamara() {
        float halfWidth = camara.viewportWidth / 2f;
        float halfHeight = camara.viewportHeight / 2f;

        float x = jugadorLocal.getX();
        float y = jugadorLocal.getY();

        float limiteIzquierdo = halfWidth;
        float limiteDerecho = Math.max(limiteIzquierdo, salaActual.getAnchoMundo() - halfWidth);
        float limiteInferior = halfHeight;
        float limiteSuperior = Math.max(limiteInferior, salaActual.getAltoMundo() - halfHeight);

        x = MathUtils.clamp(x, limiteIzquierdo, limiteDerecho);
        y = MathUtils.clamp(y, limiteInferior, limiteSuperior);

        camara.position.set(x, y, 0);
        camara.update();
    }

    public Texture generarTextura() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    // ========================================
    // 🎮 IMPLEMENTACIÓN DE GameController
    // ========================================

    private void enviarPosicionesEnemigos() {
        if (salaActual == null || salaActual.getEnemigos() == null) return;

        StringBuilder enemiesData = new StringBuilder();
        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();

        for (int i = 0; i < enemigos.size(); i++) {
            Enemigo e = enemigos.get(i);
            enemiesData.append(e.getPosicion().x).append(",").append(e.getPosicion().y);
            if (i < enemigos.size() - 1) {
                enemiesData.append(";");
            }
        }

        serverThread.sendMessageToAll("SyncEnemies:" + enemiesData.toString());
        System.out.println("📍 Sincronizando " + enemigos.size() + " enemigos");
    }

    @Override
    public void startGame() {
        System.out.println("🎮 ¡Juego iniciado con todos los jugadores conectados!");
        juegoIniciado = true;

        // Crear jugador 2 (cliente)
        Jugador jugador2 = new Jugador();
        jugador2.setX(jugadorLocal.getX() + 2);
        jugador2.setY(jugadorLocal.getY());
        jugadores.put(2, jugador2);
        enviarPosicionesEnemigos();
    }


    @Override
    public void move(int numPlayer, float x, float y) {
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.setX(x);
            jugador.setY(y);
        }
    }

    @Override
    public void attack(int numPlayer) {
        // El cliente atacó, procesar lógica si es necesario
        System.out.println("⚔️ Jugador " + numPlayer + " atacó");
    }

    @Override
    public void enemyKilled(int numPlayer, int enemyId) {
        System.out.println("💀 Jugador " + numPlayer + " mató enemigo " + enemyId);
        // Dar monedas al jugador
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.modificarMonedas(10);
            serverThread.sendMessageToAll("UpdateCoins:" + numPlayer + ":" + jugador.getMonedas());
        }
    }

    @Override
    public void bossKilled(int numPlayer) {
        System.out.println("👑 Jugador " + numPlayer + " mató al jefe");
        Jugador jugador = jugadores.get(numPlayer);
        if (jugador != null) {
            jugador.modificarMonedas(50);
            serverThread.sendMessageToAll("UpdateCoins:" + numPlayer + ":" + jugador.getMonedas());
        }
        serverThread.sendMessageToAll("EndGame:" + numPlayer);
    }

    @Override
    public void changeRoom(int numPlayer, String roomId) {
        System.out.println("🚪 Jugador " + numPlayer + " cambió a sala " + roomId);
    }

    @Override
    public void timeOut() {
        // Timeout después de finalizar el juego
        serverThread.disconnectClients();
    }

    // ========================================
    // 🧹 LIMPIEZA
    // ========================================

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        actualizarCamara();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (serverThread != null) {
            serverThread.terminate();
        }
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        for (Jugador jugador : jugadores.values()) {
            jugador.dispose();
        }
        if (texturaFade != null) {
            texturaFade.dispose();
        }
    }
}
