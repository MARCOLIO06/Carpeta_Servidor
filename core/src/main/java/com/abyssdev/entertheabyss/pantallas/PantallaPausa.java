package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.EnterTheAbyssPrincipal;
import com.abyssdev.entertheabyss.ui.FontManager;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PantallaPausa extends Pantalla {

    private final PantallaJuego pantallaJuego;
    private BitmapFont font;
    private Texture fondoPausa;

    private final String[] opciones = {"Continuar", "Opciones", "Tutorial", "Salir"};
    private int opcionSeleccionada = 0;

    private float tiempoParpadeo = 0;
    private boolean mostrarColor = true;

    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camara;

    public PantallaPausa(Game juego, SpriteBatch batch, PantallaJuego pantallaJuego) {
        super(juego, batch);
        this.pantallaJuego = pantallaJuego;
    }

    @Override
    public void show() {
        font = FontManager.getInstance().getMediana();

        camara = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camara);
        fondoPausa = new Texture("Fondos/pausa2.PNG");
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiempoParpadeo += delta;
        if (tiempoParpadeo > 0.5f) {
            mostrarColor = !mostrarColor;
            tiempoParpadeo = 0;
        }

        manejarInput();

        // Actualizar cámara y viewport
        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float mundoAncho = viewport.getWorldWidth();
        float mundoAlto = viewport.getWorldHeight();

        batch.begin();
        batch.draw(fondoPausa, 0, 0, mundoAncho, mundoAlto);

        // Centrar el bloque de opciones verticalmente
        float espacioEntreOpciones = 50f;
        float alturaTotalOpciones = (opciones.length - 1) * espacioEntreOpciones;
        float startY = mundoAlto / 2f + alturaTotalOpciones / 2f + 30f;

        for (int i = 0; i < opciones.length; i++) {
            String texto = opciones[i];
            layout.setText(font, texto);

            float x = mundoAncho / 2f - layout.width / 2f;
            float y = startY - i * espacioEntreOpciones;

            if (i == opcionSeleccionada && mostrarColor) {
                font.setColor(Color.YELLOW);
            } else {
                font.setColor(Color.WHITE);
            }

            font.draw(batch, texto, x, y);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true para centrar
        camara.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);
        camara.update();
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (opcionSeleccionada) {
                case 0:
                    juego.setScreen(pantallaJuego);
                    break;
                case 1:
                    juego.setScreen(new PantallaOpciones(juego, batch, this));
                    break;
                case 2:
                    juego.setScreen(new PantallaTutorial(juego, batch, this));
                    break;
                case 3:
                    juego.setScreen(new MenuInicio(juego, batch));
                    break;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.setScreen(pantallaJuego);
        }
    }

    @Override
    public void dispose() {
        if (fondoPausa != null) {
            fondoPausa.dispose();
        }
    }
}
