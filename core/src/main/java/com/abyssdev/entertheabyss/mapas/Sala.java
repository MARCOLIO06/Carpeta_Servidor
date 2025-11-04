package com.abyssdev.entertheabyss.mapas;

import com.abyssdev.entertheabyss.personajes.Boss;
import com.abyssdev.entertheabyss.personajes.Enemigo;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

public class Sala {

    private String id;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderer;
    private Array<Rectangle> colisionesEstaticas;
    private Array<Rectangle> colisionesDinamicas;
    private Array<Rectangle> colisiones;
    private Array<ZonaTransicion> zonasTransicion;
    private ArrayList<Enemigo> enemigos;
    private Boss bossFinal;
    private boolean bossGenerado = false;
    private Array<SpawnPoint> spawnPoints;
    private Array<Puerta> puertas;
    private int cantidadEnemigos;
    private boolean enemigosGenerados = false;
    private float anchoTiles, altoTiles;
    private static final float TILE_SIZE = 16f;

    public Sala(String id, String rutaTmx, int cantidadEnemigos) {
        this.id = id;
        this.cantidadEnemigos = cantidadEnemigos;
        cargarMapa(rutaTmx);
        cargarColisiones();
        cargarPuertas();
        cargarZonasTransicion();
        cargarSpawnPoints();
    }

    private void cargarMapa(String ruta) {
        TmxMapLoader loader = new TmxMapLoader();
        mapa = loader.load(ruta);
        renderer = new OrthogonalTiledMapRenderer(mapa, 1f / TILE_SIZE);

        com.badlogic.gdx.maps.tiled.TiledMapTileLayer capaBase =
            (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) mapa.getLayers().get(0);
        anchoTiles = capaBase.getWidth();
        altoTiles = capaBase.getHeight();
    }

    private void cargarColisiones() {
        colisionesEstaticas = new Array<>();
        colisionesDinamicas = new Array<>();
        colisiones = new Array<>();

        if (mapa.getLayers().get("colisiones") == null) {
            System.err.println("⚠️ No hay capa 'colisiones' en el mapa: " + id);
            return;
        }

        MapObjects objetos = mapa.getLayers().get("colisiones").getObjects();

        for (MapObject objeto : objetos) {
            if (!(objeto instanceof RectangleMapObject)) continue;

            RectangleMapObject rectObj = (RectangleMapObject) objeto;
            Rectangle rect = rectObj.getRectangle();
            Rectangle colisionRect = new Rectangle(
                rect.x / TILE_SIZE,
                rect.y / TILE_SIZE,
                rect.width / TILE_SIZE,
                rect.height / TILE_SIZE
            );

            // ✅ Separar colisiones estáticas y dinámicas
            if (objeto.getProperties().containsKey("colision")) {
                colisionesEstaticas.add(colisionRect);
                colisiones.add(colisionRect);
            } else if (objeto.getProperties().containsKey("colisionPuerta")) {
                colisionesDinamicas.add(colisionRect);
                colisiones.add(colisionRect); // Inicialmente cerrada
            }
        }

        System.out.println("✅ Sala " + id + " - Colisiones estáticas: " + colisionesEstaticas.size);
        System.out.println("✅ Sala " + id + " - Colisiones dinámicas (puertas): " + colisionesDinamicas.size);
    }

    private void cargarPuertas() {
        puertas = new Array<>();

        if (mapa.getLayers().get("puertas") == null) {
            System.out.println("⚠️ No hay capa 'puertas' en el mapa " + id);
            return;
        }

        MapObjects objetos = mapa.getLayers().get("puertas").getObjects();

        for (MapObject objeto : objetos) {
            if (!(objeto instanceof RectangleMapObject)) continue;
            if (!"puerta".equals(objeto.getProperties().get("tipo", String.class))) continue;

            RectangleMapObject rectObj = (RectangleMapObject) objeto;
            Rectangle rect = rectObj.getRectangle();

            // ✅ Leer la capa de tiles desde Tiled
            String capaTiles = objeto.getProperties().get("capa", "Pared", String.class);

            // ✅ Buscar la colisión física asociada
            Rectangle colisionFisica = buscarColisionPuerta();

            // ✅ Convertir coordenadas de píxeles a tiles
            int tileX = (int) Math.floor(rect.x / TILE_SIZE);
            int tileY = (int) Math.floor(rect.y / TILE_SIZE);
            int tileW = (int) Math.ceil(rect.width / TILE_SIZE);
            int tileH = (int) Math.ceil(rect.height / TILE_SIZE);

            System.out.println("📦 Cargando puerta en sala " + id + ":");
            System.out.println("   Área píxeles: x=" + rect.x + " y=" + rect.y + " w=" + rect.width + " h=" + rect.height);
            System.out.println("   Área tiles: x=" + tileX + " y=" + tileY + " w=" + tileW + " h=" + tileH);
            System.out.println("   Capa: " + capaTiles);

            Puerta puerta = new Puerta(
                tileX, tileY, tileW, tileH,
                mapa, capaTiles, colisionFisica
            );

            puertas.add(puerta);
        }

        System.out.println("✅ Se cargaron " + puertas.size + " puertas en la sala " + id);
    }

    /**
     * ✅ Busca la colisión física de la puerta en la capa "colisiones"
     */
    private Rectangle buscarColisionPuerta() {
        MapObjects objetos = mapa.getLayers().get("colisiones").getObjects();

        for (MapObject objeto : objetos) {
            if (!(objeto instanceof RectangleMapObject)) continue;
            if (!objeto.getProperties().containsKey("colisionPuerta")) continue;

            RectangleMapObject rectObj = (RectangleMapObject) objeto;
            Rectangle rect = rectObj.getRectangle();

            return new Rectangle(
                rect.x / TILE_SIZE,
                rect.y / TILE_SIZE,
                rect.width / TILE_SIZE,
                rect.height / TILE_SIZE
            );
        }

        return null;
    }

    private void cargarZonasTransicion() {
        zonasTransicion = new Array<>();
        if (mapa.getLayers().get("transiciones") == null) return;

        MapObjects objetos = mapa.getLayers().get("transiciones").getObjects();
        for (MapObject objeto : objetos) {
            if (!(objeto instanceof RectangleMapObject)) continue;
            if (!objeto.getProperties().containsKey("destino")) continue;

            RectangleMapObject rectObj = (RectangleMapObject) objeto;
            Rectangle rect = rectObj.getRectangle();
            String destino = objeto.getProperties().get("destino", String.class);
            String spawnName = objeto.getProperties().get("spawn_centro", "default", String.class);

            zonasTransicion.add(new ZonaTransicion(
                rect.x / TILE_SIZE,
                rect.y / TILE_SIZE,
                rect.width / TILE_SIZE,
                rect.height / TILE_SIZE,
                destino,
                spawnName
            ));
        }
    }

    public void generarEnemigos() {
        enemigos = new ArrayList<>();

        for (int i = 0; i < cantidadEnemigos; i++) {
            final int delay = i;

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Enemigo nuevoEnemigo = null;
                    int intentos = 0;
                    final int MAX_INTENTOS = 50; // Evitar bucle infinito

                    while (nuevoEnemigo == null && intentos < MAX_INTENTOS) {
                        float x = MathUtils.random(2f, getAnchoMundo() - 2f);
                        float y = MathUtils.random(2f, getAltoMundo() - 2f);

                        // Crear rectángulo temporal para verificar colisión
                        Rectangle rectTemp = new Rectangle(x, y, Enemigo.getTamaño(), Enemigo.getTamaño());

                        boolean colisiona = false;

                        // Verificar colisiones con paredes
                        for (Rectangle r : colisiones) {
                            if (rectTemp.overlaps(r)) {
                                colisiona = true;
                                break;
                            }
                        }

                        // Verificar colisiones con otros enemigos ya generados
                        if (!colisiona) {
                            for (Enemigo e : enemigos) {
                                if (rectTemp.overlaps(e.getRectangulo())) {
                                    colisiona = true;
                                    break;
                                }
                            }
                        }

                        if (!colisiona) {
                            nuevoEnemigo = new Enemigo(x, y,3f,2f,10);
                        }

                        intentos++;
                    }

                    if (nuevoEnemigo != null) {
                        enemigos.add(nuevoEnemigo);
                        System.out.println("✅ Enemigo generado en (" + nuevoEnemigo.getPosicion().x + ", " + nuevoEnemigo.getPosicion().y + ")");
                    } else {
                        System.err.println("❌ No se pudo generar enemigo después de " + MAX_INTENTOS + " intentos.");
                    }
                }
            }, delay * 1.5f);
        }
    }

    public void generarBoss() {
        Boss nuevoBoss = null;
        int intentos = 0;
        final int MAX_INTENTOS = 50;
        final float TAMANO_BOSS = 6f; // ✅ Tamaño visual del Boss

        while (nuevoBoss == null && intentos < MAX_INTENTOS) {
            float x = MathUtils.random(2f, getAnchoMundo() - 2f);
            float y = MathUtils.random(2f, getAltoMundo() - 2f);

            // ✅ Crear rectángulo temporal para verificar colisión
            Rectangle rectTemp = new Rectangle(x, y, TAMANO_BOSS, TAMANO_BOSS);

            boolean colisiona = false;

            // Verificar colisiones con paredes
            for (Rectangle r : colisiones) {
                if (rectTemp.overlaps(r)) {
                    colisiona = true;
                    break;
                }
            }

            // Verificar colisiones con enemigos (si ya hay enemigos generados)
            if (!colisiona && enemigos != null) {
                for (Enemigo e : enemigos) {
                    if (rectTemp.overlaps(e.getRectangulo())) {
                        colisiona = true;
                        break;
                    }
                }
            }

            if (!colisiona) {
                nuevoBoss = new Boss(x, y, 1.5f,4f,30);
            }

            intentos++;
        }

        if (nuevoBoss != null) {
            bossFinal = nuevoBoss;
            System.out.println("✅ Boss generado en (" + nuevoBoss.getPosicion().x + ", " + nuevoBoss.getPosicion().y + ")");
        } else {
            System.err.println("❌ No se pudo generar el Boss después de " + MAX_INTENTOS + " intentos.");
        }
    }

    private void cargarSpawnPoints() {
        spawnPoints = new Array<>();
        if (mapa.getLayers().get("spawns") == null) return;

        MapObjects objetos = mapa.getLayers().get("spawns").getObjects();
        for (MapObject objeto : objetos) {
            if (!(objeto instanceof PointMapObject)) continue;

            PointMapObject pointObj = (PointMapObject) objeto;
            Vector2 point = pointObj.getPoint();

            float x = point.x / TILE_SIZE;
            float y = point.y / TILE_SIZE;

            String name = objeto.getProperties().get("name", "default", String.class);
            String salaId = objeto.getProperties().get("sala_id", id, String.class);

            spawnPoints.add(new SpawnPoint(x, y, name, salaId));
        }
    }

    public boolean hayEnemigosVivos() {
        if (this.enemigos == null) return false;
        for (Enemigo e : this.enemigos) {
            if (!e.debeEliminarse()) return true;
        }
        return false;
    }

    /**
     * ✅ Actualiza el estado de las puertas y las colisiones
     */
    public void actualizarPuertas() {
        if (puertas == null || puertas.size == 0) return;

        boolean algunaPuertaSeAbrio = false;

        // ✅ Abrir puertas si no hay enemigos ni boss
        if (!hayEnemigosVivos() && (bossFinal == null || bossFinal.debeEliminarse())) {
            for (int i = 0; i < puertas.size; i++) {
                Puerta puerta = puertas.get(i);
                if (!puerta.estaAbierta()) {
                    puerta.abrir();
                    algunaPuertaSeAbrio = true;
                }
            }
        }

        // ✅ Solo actualizar colisiones si cambió algo
        if (algunaPuertaSeAbrio) {
            actualizarColisiones();
        }
    }

    /**
     * ✅ Reconstruye la lista de colisiones combinando estáticas y dinámicas
     */
    private void actualizarColisiones() {
        colisiones.clear();

        // Agregar todas las estáticas
        for (int i = 0; i < colisionesEstaticas.size; i++) {
            colisiones.add(colisionesEstaticas.get(i));
        }

        // Agregar solo colisiones de puertas cerradas
        for (int i = 0; i < puertas.size; i++) {
            Puerta puerta = puertas.get(i);
            Rectangle colPuerta = puerta.getColision();
            if (colPuerta != null) {
                colisiones.add(colPuerta);
            }
        }

        System.out.println("🔄 Colisiones actualizadas: " + colisiones.size + " totales");
    }

    // GETTERS
    public String getId() { return this.id; }
    public TiledMap getMapa() { return this.mapa; }
    public OrthogonalTiledMapRenderer getRenderer() { return this.renderer; }
    public Array<Rectangle> getColisiones() { return this.colisiones; }
    public Array<ZonaTransicion> getZonasTransicion() { return this.zonasTransicion; }
    public ArrayList<Enemigo> getEnemigos() { return this.enemigos; }
    public float getAnchoMundo() { return this.anchoTiles; }
    public float getAltoMundo() { return this.altoTiles; }
    public Array<SpawnPoint> getSpawnPoints() { return this.spawnPoints; }
    public Boss getBoss() { return this.bossFinal; }
    public void setBoss(Boss boss) { this.bossFinal = boss; }
    public boolean getBossGenerado() { return this.bossGenerado; }



    public void dispose() {
        if (mapa != null) mapa.dispose();
        if (renderer != null) renderer.dispose();
    }
}
