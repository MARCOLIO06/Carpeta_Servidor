package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.EnterTheAbyssPrincipal;
import com.abyssdev.entertheabyss.habilidades.*;
import com.abyssdev.entertheabyss.personajes.Jugador;
import com.abyssdev.entertheabyss.ui.FontManager;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Map;

public class PantallaArbolHabilidades extends Pantalla {

    private final PantallaJuego pantallaJuego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Texture fondo;
    private Jugador jugador;
    private Map<String, Habilidad> habilidades;

    private int filaSeleccionada = 0;
    private int columnaSeleccionada = 0;
    private Habilidad habilidadSeleccionada = null;
    private final float anchoNodo = 80f;

    private float tiempoParpadeo = 0;
    private boolean mostrarColor = true;
    private GlyphLayout layout;

    private String mensaje = "";
    private float tiempoMensaje = 0;

    private Viewport viewport;
    private OrthographicCamera camara;

    private final String[][] NODOS = {
        {"Vida Extra"  , "Fuerza"       , "Velocidad"   },
        {"Defensa"     , "Ataque Veloz" , "Velocidad II"},
        {"Regeneración", "Golpe Crítico", "Evasión"     }
    };

    public PantallaArbolHabilidades(Game juego, SpriteBatch batch, PantallaJuego pantallaJuego, Jugador jugador, Map<String, Habilidad> habilidades) {
        super(juego, batch);
        this.pantallaJuego = pantallaJuego;
        this.jugador = jugador;
        this.habilidades = habilidades;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontManager.getInstance().getPequena();
        layout = new GlyphLayout();

        fondo = new Texture("Fondos/FondoArbol.PNG");

        camara = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camara);
        viewport.apply();
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiempoParpadeo += delta;
        if (tiempoParpadeo > 0.5f) {
            mostrarColor = !mostrarColor;
            tiempoParpadeo = 0;
        }

        manejarInput();

        viewport.apply();
        batch.setProjectionMatrix(camara.combined);

        // Dibuja Fondo
        batch.begin();
        batch.draw(fondo, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        // Dibuja TODO el árbol (marcos, líneas y nodos)
        dibujarArbolHabilidades();

        // Dibuja la UI Superior/Panel de Monedas
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "Árbol de Habilidades");
        font.draw(batch, layout, (viewport.getWorldWidth() - layout.width) / 2, viewport.getWorldHeight() - 40);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Monedas: " + jugador.getMonedas(), 50, 50);

        // Dibuja el panel de detalle de la habilidad seleccionada
        if (habilidadSeleccionada != null) {
            dibujarPanelDetalles(habilidadSeleccionada);
        }

        // Dibuja el mensaje temporal
        if (!mensaje.isEmpty()) {
            font.getData().setScale(1.5f); // Puedes aumentar un poco la escala si quieres que destaque más
            font.setColor(Color.CYAN); // Mantener el color o cambiarlo a Color.RED para errores

            // Calcula el ancho del texto para centrarlo
            layout.setText(font, mensaje);
            float mensajeX = (viewport.getWorldWidth() - layout.width) / 2;
            float mensajeY = 60; // Ahora más abajo (antes 100)

            font.draw(batch, layout, mensajeX, mensajeY);
        }
        batch.end();

        if (tiempoMensaje > 0) {
            tiempoMensaje -= delta;
            if (tiempoMensaje <= 0) mensaje = "";
        }
    }

    private void dibujarArbolHabilidades() {
        float anchoPantalla = viewport.getWorldWidth();
        float altoPantalla = viewport.getWorldHeight();
        float margen = 50f;
        float anchoColumna = (anchoPantalla - margen * 4) / 3;
        float altoNodo = anchoNodo;
        float espacioVertical = (altoPantalla - 200 - altoNodo * 3) / 2;

        shapeRenderer.setProjectionMatrix(camara.combined);

        // 1. Dibuja las líneas de conexión
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int fila = 0; fila < NODOS.length - 1; fila++) {
            for (int columna = 0; columna < NODOS[fila].length; columna++) {
                // Coordenadas del nodo superior e inferior
                float x1 = margen + columna * (anchoColumna + margen) + anchoColumna / 2;
                float y1 = altoPantalla - 100 - fila * (altoNodo + espacioVertical) - altoNodo / 2;

                float x2 = margen + columna * (anchoColumna + margen) + anchoColumna / 2;
                float y2 = altoPantalla - 100 - (fila + 1) * (altoNodo + espacioVertical) - altoNodo / 2;

                // Determinar color de la línea
                String idSuperior = NODOS[fila][columna];
                Habilidad superior = habilidades.get(idSuperior);

                if (superior != null && superior.comprada) {
                    shapeRenderer.setColor(Color.GREEN);
                } else {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                }

                // Dibuja la línea de conexión
                shapeRenderer.rectLine(x1, y1, x2, y2, 5);
            }
        }
        shapeRenderer.end();

        // 2. Dibuja los nodos (marcos y relleno) con shapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int fila = 0; fila < NODOS.length; fila++) {
            for (int columna = 0; columna < NODOS[fila].length; columna++) {
                dibujarNodo(columna, fila, NODOS[fila][columna], true); // soloFondo = true
            }
        }
        shapeRenderer.end();

        // 3. Dibuja los íconos y textos con batch
        batch.begin();
        for (int fila = 0; fila < NODOS.length; fila++) {
            for (int columna = 0; columna < NODOS[fila].length; columna++) {
                dibujarNodo(columna, fila, NODOS[fila][columna], false); // soloFondo = false
            }
        }
        batch.end();
    }

    private void dibujarNodo(int columna, int fila, String habilidadId, boolean soloFondo) {
        Habilidad habilidad = habilidades.get(habilidadId);
        if (habilidad == null) return;

        float anchoPantalla = viewport.getWorldWidth();
        float altoPantalla = viewport.getWorldHeight();
        float margen = 50f;
        float anchoColumna = (anchoPantalla - margen * 4) / 3;
        float altoNodo = anchoNodo;
        float espacioVertical = (altoPantalla - 200 - altoNodo * 3) / 2;

        float x = margen + columna * (anchoColumna + margen) + (anchoColumna - altoNodo) / 2;
        float y = altoPantalla - 100 - fila * (altoNodo + espacioVertical) - altoNodo;

        // Determinar si está disponible (solo si la de arriba está comprada)
        boolean disponible = true;
        if (fila > 0) {
            String idSuperior = NODOS[fila - 1][columna];
            Habilidad superior = habilidades.get(idSuperior);
            if (superior != null && !superior.comprada) disponible = false;
        }

        Color marcoColor;
        Color rellenoColor;
        Color iconoColor;

        if (habilidad.comprada) {
            marcoColor = Color.GREEN;
            rellenoColor = Color.GREEN.cpy().lerp(Color.BLACK, 0.7f);
            iconoColor = Color.WHITE;
        } else if (!disponible) {
            marcoColor = Color.RED.cpy().lerp(Color.BLACK, 0.5f);
            rellenoColor = Color.BLACK.cpy().lerp(Color.DARK_GRAY, 0.5f);
            iconoColor = new Color(0.3f, 0.3f, 0.3f, 0.8f);
        } else {
            marcoColor = Color.YELLOW;
            rellenoColor = Color.BLACK.cpy().lerp(Color.BLUE, 0.2f);
            iconoColor = Color.WHITE;
        }

        // Si está seleccionada, ajustamos el marco para el parpadeo
        if (columna == columnaSeleccionada && fila == filaSeleccionada) {
            marcoColor = mostrarColor ? Color.WHITE : Color.YELLOW;
        }

        // --- Parte 1: Dibujo del marco/relleno (ShapeRenderer) ---
        if (soloFondo) {
            // Relleno interior
            shapeRenderer.setColor(rellenoColor.r, rellenoColor.g, rellenoColor.b, 0.8f);
            shapeRenderer.rect(x - 5, y - 5, altoNodo + 10, altoNodo + 10);

            // Marco de color (doble marco para efecto)
            shapeRenderer.setColor(marcoColor.r, marcoColor.g, marcoColor.b, 1f);
            shapeRenderer.rect(x - 10, y - 10, altoNodo + 20, altoNodo + 20); // Marco exterior
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(x - 8, y - 8, altoNodo + 16, altoNodo + 16); // Marco interior
        }
        // --- Parte 2: Dibujo del ícono y texto (SpriteBatch) ---
        else {
            // Dibujo del ícono
            batch.setColor(iconoColor);
            batch.draw(habilidad.getIcono(), x, y, altoNodo, altoNodo);
            batch.setColor(Color.WHITE);

            // Texto del nombre (debajo)
            font.getData().setScale(0.9f);
            font.setColor(Color.CYAN);
            layout.setText(font, habilidad.getNombre());
            font.draw(batch, layout, x + (altoNodo - layout.width) / 2, y - 15);
        }
    }


    private void dibujarPanelDetalles(Habilidad habilidad) {
        float panelAncho = 350;
        float panelAlto = 300;
        float panelY = viewport.getWorldHeight() / 2 - panelAlto / 2; // Centrado verticalmente
        float padding = 15;

        // Calcular la posición X del panel dinámicamente
        float panelX;
        // Si la columna seleccionada es de la mitad derecha (columna 1 o 2), el panel va a la izquierda.
        // Si es de la izquierda (columna 0), el panel va a la derecha.
        if (columnaSeleccionada >= NODOS[0].length / 2) { // Habilidad en columna 1 o 2 (derecha)
            panelX = 50; // Posiciona el panel en el lado izquierdo, con un margen de 50px
        } else { // Habilidad en columna 0 (izquierda)
            panelX = viewport.getWorldWidth() - panelAncho - 50; // Posiciona el panel en el lado derecho
        }

        // Obtener estado de disponibilidad (misma lógica que antes)
        boolean disponible = true;
        int fila = filaSeleccionada;
        int columna = columnaSeleccionada;
        if (fila > 0) {
            String idSuperior = NODOS[fila - 1][columna];
            Habilidad superior = habilidades.get(idSuperior);
            if (superior != null && !superior.comprada) disponible = false;
        }

        // 1. Dibujar el fondo del panel (ShapeRenderer)
        batch.end(); // Aseguramos que el batch esté cerrado
        shapeRenderer.setProjectionMatrix(camara.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.0f, 0.0f, 0.1f, 0.8f); // Fondo azul muy oscuro y opaco
        shapeRenderer.rect(panelX, panelY, panelAncho, panelAlto);
        shapeRenderer.end();

        // Dibujar el marco con ShapeRenderer (efecto de borde)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(panelX, panelY, panelAncho, panelAlto);
        shapeRenderer.end();

        batch.begin(); // Volver a abrir el batch

        // 2. Título de la Habilidad
        font.getData().setScale(1.3f); // Escala un poco más pequeña para el título
        font.setColor(Color.WHITE);
        font.draw(batch, habilidad.getNombre(), panelX + padding, panelY + panelAlto - padding);

        // 3. Estado
        font.getData().setScale(0.9f); // Escala más pequeña para el texto
        float yPos = panelY + panelAlto - padding - 40;
        if (habilidad.comprada) {
            font.setColor(Color.GREEN);
            font.draw(batch, "ESTADO: COMPRADA", panelX + padding, yPos);
        } else if (!disponible) {
            font.setColor(Color.RED);
            font.draw(batch, "ESTADO: BLOQUEADA", panelX + padding, yPos);
        } else {
            font.setColor(Color.YELLOW);
            font.draw(batch, "ESTADO: DISPONIBLE", panelX + padding, yPos);
        }

        // 4. Requisito (si está bloqueada)
        if (!habilidad.comprada && !disponible) {
            String idSuperior = NODOS[fila - 1][columna];
            font.setColor(Color.RED);
            font.draw(batch, "REQUISITOS: " + habilidades.get(idSuperior).getNombre(), panelX + padding, yPos - 25); // Ajuste el desplazamiento Y
            yPos -= 25;
        }

        // 5. Costo
        font.setColor(Color.YELLOW);
        font.draw(batch, "COSTO: " + habilidad.getCosto() + " Monedas", panelX + padding, yPos - 25); // Ajuste el desplazamiento Y

        // 6. Descripción
        font.getData().setScale(1.0f); // Escala para la descripción
        font.setColor(Color.LIGHT_GRAY);
        // Ajuste el yPos de inicio para la descripción y el espacio vertical
        font.draw(batch, habilidad.getDescripcion(), panelX + padding, panelY + panelAlto - 180, panelAncho - padding * 2, Align.topLeft, true);
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            filaSeleccionada = Math.max(0, filaSeleccionada - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            filaSeleccionada = Math.min(NODOS.length - 1, filaSeleccionada + 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            columnaSeleccionada = Math.max(0, columnaSeleccionada - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            columnaSeleccionada = Math.min(NODOS[0].length - 1, columnaSeleccionada + 1);
        }

        // Lógica de actualización de habilidad seleccionada al mover la selección
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) ||
            Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {

            filaSeleccionada = Math.max(0, Math.min(NODOS.length - 1, filaSeleccionada));
            columnaSeleccionada = Math.max(0, Math.min(NODOS[0].length - 1, columnaSeleccionada));

            String habilidadId = NODOS[filaSeleccionada][columnaSeleccionada];
            habilidadSeleccionada = habilidades.get(habilidadId);

            mensaje = "";
            tiempoMensaje = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (filaSeleccionada >= 0 && filaSeleccionada < NODOS.length &&
                columnaSeleccionada >= 0 && columnaSeleccionada < NODOS[0].length) {

                String habilidadId = NODOS[filaSeleccionada][columnaSeleccionada];
                Habilidad habilidad = habilidades.get(habilidadId);
                if (habilidad != null) {
                    intentarCompra(habilidad);
                } else {
                    mostrarMensaje("Habilidad no disponible: " + habilidadId);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {

            juego.setScreen(pantallaJuego);
            Sonidos.reanudarMusicaJuego();
        }
    }

    private void intentarCompra(Habilidad habilidad) {
        if (habilidad.comprada) {

            mostrarMensaje("Ya has comprado esta habilidad.");
            Sonidos.reproducirCompraFallida();
            return;
        }

        // Buscar la posición de la habilidad en la matriz NODOS
        int fila = -1, columna = -1;
        for (int f = 0; f < NODOS.length; f++) {
            for (int c = 0; c < NODOS[f].length; c++) {
                if (NODOS[f][c].equals(habilidad.getNombre())) {
                    fila = f;
                    columna = c;
                    break;
                }
            }
        }

        // Si la habilidad no está en la matriz, no seguimos
        if (fila == -1 || columna == -1) {
            mostrarMensaje("Error: habilidad no encontrada en el árbol.");
            return;
        }

        // Verificar si tiene una habilidad superior (fila anterior)
        if (fila > 0) {
            String idSuperior = NODOS[fila - 1][columna];
            Habilidad habilidadSuperior = habilidades.get(idSuperior);
            if (habilidadSuperior != null && !habilidadSuperior.comprada) {
                mostrarMensaje("Primero debes comprar " + habilidadSuperior.getNombre() + ".");
                Sonidos.reproducirCompraFallida();
                return;
            }
        }

        // Verificar monedas
        if (jugador.getMonedas() < habilidad.getCosto()) {
            mostrarMensaje("Monedas insuficientes.");
            Sonidos.reproducirCompraFallida();
            return;
        }

        // Compra válida
        jugador.modificarMonedas(-habilidad.getCosto());
        habilidad.comprada = true;
        habilidad.aplicar(jugador);
        mostrarMensaje("¡Compra exitosa! " + habilidad.getNombre() + " mejorada.");
        Sonidos.reproducirCompraExitosa();
    }


    private void mostrarMensaje(String msg) {
        mensaje = msg;
        tiempoMensaje = 3.0f; // Aumentado a 3 segundos (antes 2f)
        Gdx.app.log("ÁrbolHabilidades", msg);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        fondo.dispose();
    }
}
