package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.logica.OpcionesInputHandler;
import com.abyssdev.entertheabyss.ui.FontManager;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PantallaOpciones extends Pantalla {

    private Pantalla pantallaAnterior;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camara;
    private ShapeRenderer shapeRenderer;

    private int opcionSeleccionada = 0;
    private float volumenMusica;
    private float volumenEfectos;

    private Preferences prefs;
    private Texture fondo;

    private OpcionesInputHandler inputHandler;

    private float tiempoParpadeo = 0;
    private float alphaParpadeo = 1f;

    private Rectangle sliderMusicaBounds;
    private Rectangle sliderEfectosBounds;
    private Rectangle botonVolverBounds;

    private static final float SLIDER_WIDTH = 300f;
    private static final float SLIDER_HEIGHT = 10f;

    private boolean pantallaCompleta;
    private String[] resolucionesDisponibles = {
        "Nativo",
        "1280x720",
        "1366x768",
        "1600x900",
        "1920x1080"
    };
    private int resolucionSeleccionada; // índice en resolucionesDisponibles

    private final String[] opcionesMenu = {
        "Volumen Musica",
        "Volumen Efectos",
        "Pantalla Completa",
        "Resolucion",
        "Volver"
    };

    public PantallaOpciones(Game juego, SpriteBatch batch, Pantalla pantallaAnterior) {
        super(juego, batch);
        this.pantallaAnterior = pantallaAnterior;

        sliderMusicaBounds = new Rectangle();
        sliderEfectosBounds = new Rectangle();
        botonVolverBounds = new Rectangle();
    }

    @Override
    public void show() {
        font = FontManager.getInstance().getMediana();
        titleFont = FontManager.getInstance().getTitulo();
        layout = new GlyphLayout();

        camara = new OrthographicCamera();
        viewport = new ScreenViewport(camara);
        viewport.apply();
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();

        shapeRenderer = new ShapeRenderer();


        prefs = Gdx.app.getPreferences("EnterTheAbyss_Settings");
        volumenMusica = prefs.getFloat("volumenMusica", 0.2f);
        volumenEfectos = prefs.getFloat("volumenEfectos", 0.2f);


        Sonidos.setVolumenMusica(volumenMusica);
        Sonidos.setVolumenEfectos(volumenEfectos);

        // Cargar preferencias de video
        pantallaCompleta = prefs.getBoolean("pantallaCompleta", true);
        String resGuardada = prefs.getString("resolucion", "Nativo");
        resolucionSeleccionada = 0; // por defecto "Nativo"


        for (int i = 0; i < resolucionesDisponibles.length; i++) {
            if (resolucionesDisponibles[i].equals(resGuardada)) {
                resolucionSeleccionada = i;
                break;
            }
        }



        fondo = new Texture(Gdx.files.internal("Fondos/fondoMenu.png"));
        fondo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        inputHandler = new OpcionesInputHandler(
            this,
            juego,
            pantallaAnterior,
            viewport,
            sliderMusicaBounds,
            sliderEfectosBounds,
            botonVolverBounds
        );

        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiempoParpadeo += delta;
        alphaParpadeo = 0.6f + 0.4f * (float) Math.sin(tiempoParpadeo * 5);

        manejarInputTeclado();

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float ancho = viewport.getWorldWidth();
        float alto = viewport.getWorldHeight();
        float centerX = ancho / 2f;
        float centerY = alto / 2f;

        batch.begin();
        batch.draw(fondo, 0, 0, ancho, alto);

        // Título
        String titulo = "OPCIONES";
        layout.setText(titleFont, titulo);
        titleFont.draw(batch, titulo, centerX - layout.width / 2f, alto - 50);

        // Altura base para las opciones
        float startY = centerY + 100;
        float espacio = 60f;

        for (int i = 0; i < opcionesMenu.length; i++) {
            String texto = "";
            float y = startY - i * espacio;

            switch (i) {
                case 0:
                    texto = "Volumen Musica";
                    break;
                case 1:
                    texto = "Volumen Efectos";
                    break;
                case 2:
                    texto = "Pantalla Completa: " + (pantallaCompleta ? "ON" : "OFF");
                    break;
                case 3:
                    texto = "Resolucion: " + resolucionesDisponibles[resolucionSeleccionada];
                    break;
                case 4:
                    texto = "Volver";
                    break;
            }

            layout.setText(font, texto);

            if (i == opcionSeleccionada) {
                font.setColor(1, 0, 0, alphaParpadeo);
            } else {
                font.setColor(1, 1, 1, 0.8f);
            }
            font.draw(batch, texto, centerX - layout.width / 2f, y);

            // Guardar bounds del botón "Volver" para input táctil
            if (i == 4) {
                botonVolverBounds.set(
                    centerX - layout.width / 2f - 10,
                    y - layout.height - 5,
                    layout.width + 20,
                    layout.height + 10
                );
            }
        }

        batch.end();

        // Dibujar sliders solo si están visibles
        if (opcionSeleccionada == 0 || opcionSeleccionada == 1) {
            shapeRenderer.setProjectionMatrix(camara.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            float yMusica = startY;
            float yEfectos = startY - espacio;

            float sliderX = centerX - SLIDER_WIDTH / 2f;
            float sliderMusicaY = yMusica - 40;
            float sliderEfectosY = yEfectos - 40;

            dibujarSlider(sliderX, sliderMusicaY, volumenMusica,
                opcionSeleccionada == 0 || inputHandler.isArrastandoSliderMusica());
            sliderMusicaBounds.set(sliderX, sliderMusicaY, SLIDER_WIDTH, SLIDER_HEIGHT);

            dibujarSlider(sliderX, sliderEfectosY, volumenEfectos,
                opcionSeleccionada == 1 || inputHandler.isArrastandoSliderEfectos());
            sliderEfectosBounds.set(sliderX, sliderEfectosY, SLIDER_WIDTH, SLIDER_HEIGHT);

            shapeRenderer.end();

            batch.begin();
            dibujarPorcentaje(sliderX + SLIDER_WIDTH + 20, sliderMusicaY + 15, volumenMusica);
            dibujarPorcentaje(sliderX + SLIDER_WIDTH + 20, sliderEfectosY + 15, volumenEfectos);
            batch.end();
        }
    }

    private void aplicarConfiguracionVideo() {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) return;

        Graphics.DisplayMode modoActual = Gdx.graphics.getDisplayMode();
        int ancho = modoActual.width;
        int alto = modoActual.height;

        // Si no es "Nativo", parsear resolución
        if (resolucionSeleccionada > 0) {
            String[] partes = resolucionesDisponibles[resolucionSeleccionada].split("x");
            try {
                ancho = Integer.parseInt(partes[0]);
                alto = Integer.parseInt(partes[1]);
            } catch (Exception e) {
                // fallback a nativo
                ancho = modoActual.width;
                alto = modoActual.height;
            }
        }

        Gdx.graphics.setWindowedMode(ancho, alto);
        if (pantallaCompleta) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            // Asegurar modo ventana con la resolución elegida
            Gdx.graphics.setWindowedMode(ancho, alto);
        }
    }

    public void guardarPreferencias() {
        prefs.putFloat("volumenMusica", volumenMusica);
        prefs.putFloat("volumenEfectos", volumenEfectos);
        prefs.putBoolean("pantallaCompleta", pantallaCompleta);
        prefs.putString("resolucion", resolucionesDisponibles[resolucionSeleccionada]);
        prefs.flush();
    }

    private void dibujarSlider(float x, float y, float valor, boolean seleccionado) {
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(x, y, SLIDER_WIDTH, SLIDER_HEIGHT);

        if (seleccionado) {
            shapeRenderer.setColor(1f, 0f, 0f, 1f);
        } else {
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        shapeRenderer.rect(x, y, SLIDER_WIDTH * valor, SLIDER_HEIGHT);

        float indicadorX = x + SLIDER_WIDTH * valor;
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(indicadorX, y + SLIDER_HEIGHT / 2f, 8f, 20);
    }

    private void dibujarPorcentaje(float x, float y, float valor) {
        String porcentaje = (int)(valor * 100) + "%";
        layout.setText(font, porcentaje);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, porcentaje, x, y);
    }

    private void manejarInputTeclado() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % opcionesMenu.length;
            tiempoParpadeo = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + opcionesMenu.length) % opcionesMenu.length;
            tiempoParpadeo = 0;
        }

        // Acciones según opción seleccionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            switch (opcionSeleccionada) {
                case 2: // Pantalla completa
                    pantallaCompleta = !pantallaCompleta;
                    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                        aplicarConfiguracionVideo();
                    }
                    break;
                case 3:
                    resolucionSeleccionada = (resolucionSeleccionada + 1) % resolucionesDisponibles.length;
                    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                        aplicarConfiguracionVideo();
                    }
                    break;
                case 4:
                    guardarPreferencias();
                    juego.setScreen(pantallaAnterior);
                    break;
            }
        }

        // Controles de volumen (solo si están seleccionados)
        if (opcionSeleccionada == 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                volumenMusica = Math.max(0f, volumenMusica - 0.02f);
                Sonidos.setVolumenMusica(volumenMusica);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                volumenMusica = Math.min(1f, volumenMusica + 0.02f);
                Sonidos.setVolumenMusica(volumenMusica);
            }
        } else if (opcionSeleccionada == 1) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                volumenEfectos = Math.max(0f, volumenEfectos - 0.02f);
                Sonidos.setVolumenEfectos(volumenEfectos);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                volumenEfectos = Math.min(1f, volumenEfectos + 0.02f);
                Sonidos.setVolumenEfectos(volumenEfectos);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            guardarPreferencias();
            juego.setScreen(pantallaAnterior);
        }
    }


    public void setVolumenMusica(float volumen) {
        this.volumenMusica = volumen;
        Sonidos.setVolumenMusica(volumen);
    }

    public void setVolumenEfectos(float volumen) {
        this.volumenEfectos = volumen;
        Sonidos.setVolumenEfectos(volumen);
    }

    public void setOpcionSeleccionada(int opcion) {
        this.opcionSeleccionada = opcion;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fondo != null) fondo.dispose();
    }
}
