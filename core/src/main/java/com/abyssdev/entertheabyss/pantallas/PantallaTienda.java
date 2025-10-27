package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.personajes.Jugador;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PantallaTienda extends Pantalla {

    private BitmapFont font;
    private Texture fondoTienda;
    private Texture heart100;

    private final String[] opciones = {"Comprar corazón (+20 vida)", "Volver al juego"};
    private int opcionSeleccionada = 0;

    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camara;

    private Jugador jugador;
    private int precioCorazon = 5;

    private PantallaJuego pantallaJuego;
    public PantallaTienda(Game juego, SpriteBatch batch, Jugador jugador, PantallaJuego pantallaJuego) {
        super(juego, batch);
        this.jugador = jugador;
        this.pantallaJuego = pantallaJuego;

    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(1.8f);

        camara = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camara);
        viewport.apply();
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();

        fondoTienda = new Texture("Fondos/fondotienda.jpg");
        heart100 = new Texture("imagenes/corazon100%.png");

        layout = new GlyphLayout();

        // ✅ Música o sonido opcional
       // Sonidos.reproducirMusicaTienda();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        manejarInput();

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float ancho = viewport.getWorldWidth();
        float alto = viewport.getWorldHeight();

        batch.begin();
        batch.draw(fondoTienda, 0, 0, ancho, alto);

        font.setColor(Color.WHITE);
        font.draw(batch, "Tienda de Ogrini", ancho / 2f - 160, alto - 50);

        // Dibujar ítem de corazón
        batch.draw(heart100, ancho / 2f - 200, alto / 2f + 40, 64, 64);
        font.draw(batch, "Precio: " + precioCorazon + " monedas", ancho / 2f - 120, alto / 2f + 60);

        // Mostrar monedas del jugador
        font.setColor(Color.YELLOW);
        font.draw(batch, "Tus monedas: " + jugador.getMonedas(), ancho / 2f - 80, 100);
        font.setColor(Color.WHITE);

        // Opciones
        float baseY = alto / 2f - 50;
        for (int i = 0; i < opciones.length; i++) {
            layout.setText(font, opciones[i]);
            float x = ancho / 2f - layout.width / 2f;
            float y = baseY - i * 60;

            if (i == opcionSeleccionada)
                font.setColor(Color.GOLD);
            else
                font.setColor(Color.WHITE);

            font.draw(batch, opciones[i], x, y);
        }

        batch.end();
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
                    comprarCorazon();
                    break;
                case 1:
                    // volver al juego
                    juego.setScreen(pantallaJuego);
                    break;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.setScreen(pantallaJuego);
            Sonidos.reanudarMusicaJuego();
        }
    }

    private void comprarCorazon() {
        if (jugador.getMonedas() >= precioCorazon) {
            if(jugador.getVida() >= jugador.getVidaMaxima()){
                Sonidos.reproducirCompraFallida();
                Gdx.app.log("TIENDA", "¡Ya tenés la vida al máximo!");
                return;
            }
            jugador.setMonedas(jugador.getMonedas() - precioCorazon);
            jugador.setVida(Math.min(jugador.getVidaMaxima(), jugador.getVida() + 20));
            Sonidos.reproducirCompraExitosa();
            Gdx.app.log("TIENDA", "Compraste un corazón! Vida actual: " + jugador.getVida());
        } else {
            Sonidos.reproducirCompraFallida();
            Gdx.app.log("TIENDA", "No tenés suficientes monedas!");
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        font.dispose();
        fondoTienda.dispose();
        heart100.dispose();
    }
}
