package com.abyssdev.entertheabyss.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.abyssdev.entertheabyss.personajes.Jugador;

public class Hud {
    private Jugador jugador;
    private Texture heart100;
    private Texture heart75;
    private Texture heart50;
    private Texture heart25;
    private Texture moneda;
    private BitmapFont font;
    private GlyphLayout layout;
    private Viewport viewport; // Añadir viewport

    private float heartWidth = 32f;
    private float heartHeight = 32f;
    private float startX = 20f;
    private float startY = 0f; // Se ajustará en draw()
    private float monedaX = 0f;
    private float monedaY = 0f;

    public Hud(Jugador jugador, Viewport viewport) {
        this.jugador = jugador;
        this.viewport = viewport;
        this.heart100 = new Texture("imagenes/corazon100%.png");
        this.heart75 = new Texture("imagenes/corazon75%.png");
        this.heart50 = new Texture("imagenes/corazon50%.png");
        this.heart25 = new Texture("imagenes/corazon25%.png");
        this.moneda = new Texture("imagenes/moneda.png");
        this.font = FontManager.getInstance().getPequena();
        this.font.getData().setScale(0.9f);
        this.layout = new GlyphLayout();
    }


    public void draw(SpriteBatch batch) {
        // Guardar proyección original
        com.badlogic.gdx.math.Matrix4 originalProjection = batch.getProjectionMatrix().cpy();
        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight()));

        batch.begin();

        float screenWidth = viewport.getScreenWidth();
        float screenHeight = viewport.getScreenHeight();

        // --- DIBUJAR CORAZONES ---
        int vidaActual = jugador.getVida();
        int vidaMaxima = jugador.getVidaMaxima(); // Ejemplo: 100 o 120

        int corazonesTotales = (int)Math.ceil(vidaMaxima / 20f); // 🔹 1 corazón por cada 20 puntos
        startX = 20f;
        startY = screenHeight - 60f;

        float x = startX;

        for (int i = 0; i < corazonesTotales; i++) {
            int vidaPorCorazon = 20;
            int vidaRestante = vidaActual - (i * vidaPorCorazon);

            if (vidaRestante <= 0) {
                // 🔹 Si el jugador tiene menos vida que el inicio de este corazón, se dibuja vacío o nada
                continue;
            }

            float porcentaje = (float) vidaRestante / vidaPorCorazon * 100f;

            if (porcentaje >= 100) {
                batch.draw(heart100, x, startY);
            } else if (porcentaje >= 75) {
                batch.draw(heart75, x, startY);
            } else if (porcentaje >= 50) {
                batch.draw(heart50, x, startY);
            } else if (porcentaje >= 25) {
                batch.draw(heart25, x, startY);
            }
            x += heartWidth + 5f;
        }

        // --- DIBUJAR MONEDAS ---
        int monedas = jugador.getMonedas();

        monedaX = screenWidth - 120f;
        monedaY = 20f;

        batch.draw(moneda, monedaX, monedaY);

        String textoMonedas = "x" + monedas;
        layout.setText(font, textoMonedas);
        float textoX = monedaX + moneda.getWidth() + 5f;
        float textoY = monedaY + moneda.getHeight() / 2f + font.getLineHeight() / 2f;

        font.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
        font.draw(batch, textoMonedas, textoX, textoY);

        batch.end();
        batch.setProjectionMatrix(originalProjection);
    }

    public void dispose() {
        if (heart100 != null) {
            heart100.dispose();
        }
        if (heart75 != null) {
            heart75.dispose();
        }
        if (heart50 != null) {
            heart50.dispose();
        }
        if (heart25 != null) {
            heart25.dispose();
        }
        if (moneda != null) {
            moneda.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        // ✅ GlyphLayout no necesita dispose()
    }
}
