package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.ui.FontManager;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuInicio extends Pantalla {

    private BitmapFont fontOpciones;
    private Texture fondo;

    private final String[] opciones = {
        "Iniciar Servidor",
        "Opciones",
        "Tutorial",
        "Salir"
    };
    private int opcionSeleccionada = 0;

    private float tiempoParpadeo = 0;
    private float alphaParpadeo = 1f;
    private float tiempoTotal = 0;

    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camara;

    public MenuInicio(Game juego, SpriteBatch batch) {
        super(juego, batch);
    }

    @Override
    public void show() {
        fontOpciones = FontManager.getInstance().getGrande();

        camara = new OrthographicCamera();

        viewport = new ScreenViewport(camara);
        viewport.apply();
        camara.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camara.update();

        fondo = new Texture(Gdx.files.internal("Fondos/fondoMenu.png"));
        fondo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        layout = new GlyphLayout();
        Sonidos.reproducirMusicaMenu();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiempoTotal += delta;
        actualizarAnimaciones(delta);
        manejarInput();

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float ancho = Gdx.graphics.getWidth();
        float alto = Gdx.graphics.getHeight();
        float centerX = ancho / 2f;
        float centerY = alto / 2f;

        batch.begin();

        batch.draw(fondo, 0, 0, ancho, alto);

        float escalaUI = Math.min(ancho / 640f, alto / 480f);
        float startY = centerY + (80 * escalaUI);
        float espacioOpciones = 70 * escalaUI;

        for (int i = 0; i < opciones.length; i++) {
            String texto = opciones[i];
            layout.setText(fontOpciones, texto);

            float x = centerX - layout.width / 2f;
            float y = startY - (i * espacioOpciones);

            if (i == opcionSeleccionada) {
                fontOpciones.setColor(1, 0, 0, alphaParpadeo);

                float indicadorOffset = 2f * (float) Math.sin(tiempoTotal * 4);
                layout.setText(fontOpciones, "[");
                fontOpciones.draw(batch, "[", x - (50 * escalaUI) - indicadorOffset, y);

                layout.setText(fontOpciones, texto);
                fontOpciones.draw(batch, "]", x + layout.width + (30 * escalaUI) + indicadorOffset, y);
            } else {
                fontOpciones.setColor(1, 1, 1, 0.6f);
            }

            layout.setText(fontOpciones, texto);
            fontOpciones.draw(batch, texto, centerX - layout.width / 2f, y);
        }

        batch.end();
    }

    private void actualizarAnimaciones(float delta) {
        tiempoParpadeo += delta;
        alphaParpadeo = 0.7f + 0.3f * (float) Math.sin(tiempoParpadeo * 4);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camara.position.set(width / 2f, height / 2f, 0);
        camara.update();
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
            tiempoParpadeo = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
            tiempoParpadeo = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (opcionSeleccionada) {
                case 0: // Iniciar Servidor
                    System.out.println("🖥️ Iniciando SERVIDOR DEDICADO...");
                    Sonidos.detenerTodaMusica();
                    juego.setScreen(new PantallaServidor(juego, batch));
                    break;
                case 1:
                    juego.setScreen(new PantallaOpciones(juego, batch, this));
                    break;
                case 2: // Tutorial
                    juego.setScreen(new PantallaTutorial(juego, batch, this));
                    break;
                case 3: // Salir
                    Gdx.app.exit();
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        if (fondo != null) fondo.dispose();
    }
}
