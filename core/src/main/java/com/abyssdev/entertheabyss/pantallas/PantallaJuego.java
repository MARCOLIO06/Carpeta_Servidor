package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.mapas.Mapa;
import com.abyssdev.entertheabyss.mapas.Sala;
import com.abyssdev.entertheabyss.mapas.SpawnPoint;
import com.abyssdev.entertheabyss.mapas.ZonaTransicion;
import com.abyssdev.entertheabyss.logica.ManejoEntradas;
import com.abyssdev.entertheabyss.personajes.Accion;
import com.abyssdev.entertheabyss.personajes.Boss;
import com.abyssdev.entertheabyss.personajes.Enemigo;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.Hud;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.abyssdev.entertheabyss.ui.Sonidos;

import java.util.ArrayList;

public class PantallaJuego extends Pantalla {

    private OrthographicCamera camara;
    private Viewport viewport;
    private Jugador jugador;
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

    public PantallaJuego(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        if (!yaInicializado) {
            jugador = new Jugador();
            mapaActual = new Mapa("mazmorra1");
            mapaActual.agregarSala(new Sala("sala1", "maps/mapa1_sala1.tmx",5));
            mapaActual.agregarSala(new Sala("sala2", "maps/mapa1_sala2.tmx",1));
            mapaActual.agregarSala(new Sala("sala5", "maps/mapa2_posible.tmx",15));
            mapaActual.agregarSala(new Sala("sala4", "maps/mapa1_sala4.tmx",1));
            mapaActual.agregarSala(new Sala("sala3", "maps/mapa1_sala5.tmx",1));

            camara = new OrthographicCamera();
            viewport = new FitViewport(32, 32 * (9f / 16f), camara);
            texturaFade = generarTextura();
            cambiarSala("sala1");
            hud = new Hud(jugador, viewport);
            inputProcessor = new ManejoEntradas(jugador);
            yaInicializado = true;
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
                        jugador.setX(spawn.x);
                        jugador.setY(spawn.y);
                    } else {
                        if (!salaDestino.getSpawnPoints().isEmpty()) {
                            SpawnPoint fallback = salaDestino.getSpawnPoints().first();
                            jugador.setX(fallback.x);
                            jugador.setY(fallback.y);
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
                jugador.setX(defaultSpawn.x);
                jugador.setY(defaultSpawn.y);
            } else {
                centrarJugadorEnSala();
            }
        }

        camara.position.set(jugador.getX(), jugador.getY(), 0);
        camara.update();
        salaActual.getRenderer().setView(camara);

        if (salaActual.getEnemigos() == null || salaActual.getEnemigos().isEmpty()) {
            salaActual.generarEnemigos();
        }
    }

    private void centrarJugadorEnSala() {
        float centroX = salaActual.getAnchoMundo() / 2f;
        float centroY = salaActual.getAltoMundo() / 2f;
        jugador.setX(centroX);
        jugador.setY(centroY);
    }

    private void verificarTransiciones() {
        if (enTransicion) return;

        Rectangle hitboxJugador = new Rectangle(
            jugador.getX() + jugador.getAncho() / 4f,
            jugador.getY(),
            jugador.getAncho() / 2f,
            jugador.getAlto()
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

        salaActual.getRenderer().setView(camara);
        salaActual.getRenderer().render();

        ArrayList<Enemigo> enemigos = salaActual.getEnemigos();
        if (enemigos != null) {
            for (int i = enemigos.size() - 1; i >= 0; i--) {
                Enemigo enemigo = enemigos.get(i);
                if (enemigo.debeEliminarse()) {
                    jugador.modificarMonedas(10);
                    System.out.println("✅ Enemigo eliminado. Jugador recibe 10 monedas.");
                    enemigos.remove(i);
                    continue;
                }

                if (enemigo.actualizar(delta, jugador.getPosicion(), salaActual.getColisiones(), enemigos)) {
                    jugador.recibirDanio(enemigo.getDanio());
                    if (jugador.getVida() <= 0) {
                        juego.setScreen(new PantallaGameOver(juego,batch));
                        return;
                    }
                }
            }

            if (jugador.getHitboxAtaque().getWidth() > 0) {
                for (int i = enemigos.size() - 1; i >= 0; i--) {
                    Enemigo enemigo = enemigos.get(i);
                    if (!enemigo.debeEliminarse() && jugador.getHitboxAtaque().overlaps(enemigo.getRectangulo())) {
                        enemigo.recibirDanio(jugador.getDanio());
                    }
                }
            }
        }
        if (salaActual.getId().equalsIgnoreCase("sala5")) {
            if (salaActual.getBoss() == null) {
                salaActual.generarBoss();
            }
            Boss boss = salaActual.getBoss();
            if (boss != null && !boss.debeEliminarse()) {
                // Actualizar el Boss
                if (boss.actualizar(delta, jugador.getPosicion(), salaActual.getColisiones(), enemigos != null ? enemigos : new ArrayList<>())) {
                    jugador.recibirDanio(boss.getDanio());
                    if (jugador.getVida() <= 0) {
                        juego.setScreen(new PantallaGameOver(juego, batch));
                        return;
                    }
                }

                // Comprobar ataque del jugador al Boss
                if (jugador.getHitboxAtaque().getWidth() > 0) {
                    if (jugador.getHitboxAtaque().overlaps(boss.getRectangulo())) {
                        boss.recibirDanio(jugador.getDanio());
                    }
                }
            }

            // ✅ Verificación de victoria: MOVIDA FUERA del if (!boss.debeEliminarse())
            if (boss != null && boss.debeEliminarse() &&
                (enemigos == null || enemigos.isEmpty())) {
                jugador.modificarMonedas(50);
                Sonidos.detenerTodaMusica();
                System.out.println("✅ ¡JEFE DERROTADO! Jugador recibe 50 monedas.");
                juego.setScreen(new PantallaWin(juego, batch));
                return; // Evita más procesamiento innecesario
            }
        }

        try {
            salaActual.actualizarPuertas();
        } catch (Exception e) {
            System.out.println("ERROR AL ACTUALIZAR PUERTAS");
            e.printStackTrace();
        }


        jugador.update(delta, salaActual.getColisiones());
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
            Sonidos.pausarMusicaJuego(); // Pausar música del juego
            juego.setScreen(new PantallaTienda(juego, batch, jugador,this));
        }

        actualizarCamara();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        for (Enemigo enemigo : salaActual.getEnemigos()) {
            enemigo.renderizar(batch);
        }
       Boss boss = salaActual.getBoss();
        if (boss != null) {
            boss.renderizar(batch);
        }
        jugador.dibujar(batch);
        //aca se puede elegir si mostrar un mensaje de tienda para el jugador
        //otra idea es agregar un tutorial para que el jugador lea y sepa que
        //cuando se acerca a ogrini puede comprar
//        if (jugadorCercaDeOgrini) {
//            // Dibujar texto indicador sobre el jugador
//            com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
//            font.getData().setScale(0.05f);
//            font.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
//            font.draw(batch, "Presiona [T] para abrir tienda",
//                jugador.getX() - 2f, jugador.getY() + jugador.getAlto() + 1f);
//            font.dispose();
//        }
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
                juego.setScreen(new PantallaPausa(juego,batch, this));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                Sonidos.pausarMusicaJuego();
                // ✅ Ahora se obtienen las habilidades del jugador
                juego.setScreen(new PantallaArbolHabilidades(juego, batch, this, jugador, jugador.getHabilidades()));
            }
        }


    }
    private void verificarProximidadOgrini() {
        jugadorCercaDeOgrini = false;

        // Obtener los objetos de la capa de colisiones del mapa
        if (salaActual == null || salaActual.getMapa() == null) {
            return;
        }

        // Buscar la capa de objetos (en tu caso se llama "colisiones")
        com.badlogic.gdx.maps.MapLayer capaObjetos = salaActual.getMapa().getLayers().get("colisiones");

        if (capaObjetos == null) {
            return;
        }

        // Revisar todos los objetos de la capa
        com.badlogic.gdx.maps.MapObjects objetos = capaObjetos.getObjects();

        for (com.badlogic.gdx.maps.MapObject objeto : objetos) {
            // Solo procesar RectangleMapObject
            if (!(objeto instanceof com.badlogic.gdx.maps.objects.RectangleMapObject)) {
                continue;
            }

            // Verificar si tiene las propiedades "nombre" y "tipo"
            String nombre = objeto.getProperties().get("nombre", String.class);
            String tipo = objeto.getProperties().get("tipo", String.class);

            if (nombre != null && nombre.equalsIgnoreCase("ogrini") &&
                tipo != null && tipo.equalsIgnoreCase("tienda")) {

                // Obtener el rectángulo y su posición
                com.badlogic.gdx.maps.objects.RectangleMapObject rectObj =
                    (com.badlogic.gdx.maps.objects.RectangleMapObject) objeto;
                com.badlogic.gdx.math.Rectangle rect = rectObj.getRectangle();

                // Convertir a coordenadas del mundo (dividir por TILE_SIZE)
                float objX = (rect.x + rect.width / 2f) / 16f;  // Centro del rectángulo
                float objY = (rect.y + rect.height / 2f) / 16f; // Centro del rectángulo

                // Calcular distancia entre jugador y centro de Ogrini
                float distancia = (float) Math.sqrt(
                    Math.pow(jugador.getX() - objX, 2) +
                        Math.pow(jugador.getY() - objY, 2)
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

        float x = jugador.getX();
        float y = jugador.getY();

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
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        if (jugador != null) {
            jugador.dispose(); // ✅ Esto ahora libera también las texturas de habilidades
        }
        if (texturaFade != null) {
            texturaFade.dispose();
        }
    }
}
