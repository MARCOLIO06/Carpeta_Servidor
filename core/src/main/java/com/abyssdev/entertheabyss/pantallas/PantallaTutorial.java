package com.abyssdev.entertheabyss.pantallas;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PantallaTutorial extends Pantalla {

    private BitmapFont font;
    private BitmapFont fontTitulo;
    private Texture[] slides; // Array de imágenes del tutorial
    private String[] titulos; // Títulos de cada slide
    private String[] descripciones; // Descripciones de cada slide

    private int slideActual = 0;
    private float offsetY = 0; // Desplazamiento vertical para animación
    private float targetOffsetY = 0;
    private float velocidadTransicion = 8f;

    private Viewport viewport;
    private OrthographicCamera camara;
    private GlyphLayout layout;

    private Pantalla pantallaAnterior;

    // Animaciones
    private float tiempoParpadeo = 0;
    private float alphaIndicador = 1f;

    public PantallaTutorial(Game juego, SpriteBatch batch, Pantalla pantallaAnterior) {
        super(juego, batch);
        this.pantallaAnterior = pantallaAnterior;
    }

    @Override
    public void show() {
        font = FontManager.getInstance().getPequena();
        fontTitulo = FontManager.getInstance().getGrande();

        camara = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camara);
        viewport.apply();
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        camara.update();

        layout = new GlyphLayout();

        // ✅ Cargar las imágenes del tutorial
        cargarSlides();
    }

    private void cargarSlides() {
        // Define cuántos slides tenés
        slides = new Texture[] {
            new Texture("tutoriales/movimiento.png"),
            new Texture("tutoriales/combate.png"),
            new Texture("Tutoriales/ogrini.jpg"),
            new Texture("tutoriales/arbol.png"),
            new Texture("Fondos/Win1.jpg")
        };


        titulos = new String[] {
            "Movimiento",
            "Combate",
            "Tienda de Ogrini",
            "Árbol de Habilidades",
            "Objetivo del Juego"
        };


        descripciones = new String[] {
            "Para moverte utiliza las teclas WSAD",
            "Presiona ESPACIO para atacar a los enemigos",
            "Presiona T cerca de Ogrini para comprar mejoras",
            "Presiona TAB para abrir el árbol de habilidades",
            "Derrota enemigos y conquista el abismo"
        };
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        manejarInput();
        actualizarAnimaciones(delta);

        // Interpolar suavemente el desplazamiento
        offsetY = MathUtils.lerp(offsetY, targetOffsetY, delta * velocidadTransicion);

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        float ancho = viewport.getWorldWidth();
        float alto = viewport.getWorldHeight();

        batch.begin();

        // Dibujar fondo oscuro semitransparente
        dibujarFondo(ancho, alto);

        // Dibujar todos los slides con desplazamiento vertical
        for (int i = 0; i < slides.length; i++) {
            float y = alto / 2f - (i * alto) + offsetY;
            dibujarSlide(i, ancho, alto, y);
        }

        // Dibujar UI superior (título y contador)
        dibujarUI(ancho, alto);

        // Dibujar indicadores de navegación
        dibujarIndicadores(ancho, alto);

        batch.end();
    }

    private void dibujarFondo(float ancho, float alto) {
        // Puedes poner una textura de fondo o dejarlo con el color de clearColor
    }

    private void dibujarSlide(int index, float ancho, float alto, float posY) {
        // Solo dibujar slides que estén visibles
        if (posY < -alto || posY > alto * 2) return;

        Texture slide = slides[index];

        // Centrar la imagen
        float imgAncho = 600;
        float imgAlto = 400;
        float imgX = (ancho - imgAncho) / 2f;
        float imgY = posY - imgAlto / 2f;

        // Dibujar la imagen del slide
        batch.draw(slide, imgX, imgY, imgAncho, imgAlto);

        // Si es el slide actual, dibujar borde destacado
        if (index == slideActual) {
            batch.setColor(1, 0.8f, 0, 1);
            // Aquí podrías dibujar un borde o efecto
            batch.setColor(1, 1, 1, 1);
        }
    }

    private void dibujarUI(float ancho, float alto) {
        // Título del slide actual
        fontTitulo.setColor(Color.GOLD);
        layout.setText(fontTitulo, titulos[slideActual]);
        fontTitulo.draw(batch, titulos[slideActual],
            (ancho - layout.width) / 2f, alto - 20);

        // Descripción del slide actual
        font.setColor(Color.WHITE);
        layout.setText(font, descripciones[slideActual]);
        font.draw(batch, descripciones[slideActual],
            (ancho - layout.width) / 2f, 80);

        // Contador de slides
        font.setColor(Color.LIGHT_GRAY);
        String contador = (slideActual + 1) + " / " + slides.length;
        layout.setText(font, contador);
        font.draw(batch, contador, ancho - layout.width - 30, alto - 40);
    }

    private void dibujarIndicadores(float ancho, float alto) {
        font.setColor(1, 1, 1, alphaIndicador);

        // Indicador "Siguiente" (si no es el último)
        if (slideActual < slides.length - 1) {
            String textoAbajo = "Siguiente (S)";
            layout.setText(font, textoAbajo);
            font.draw(batch, textoAbajo, (ancho - layout.width) / 2f, 40);
        }

        // Indicador "Anterior" (si no es el primero)
        if (slideActual > 0) {
            String textoArriba = "Anterior (W)";
            layout.setText(font, textoArriba);
            font.draw(batch, textoArriba, (ancho - layout.width) / 2f, alto - 70);
        }

        // Indicador ESC para salir
        font.setColor(Color.RED);
        String textoSalir = "Volver (ESC)";
        layout.setText(font, textoSalir);
        font.draw(batch, textoSalir, 20, 30);

        font.setColor(Color.WHITE);
    }

    private void actualizarAnimaciones(float delta) {
        tiempoParpadeo += delta;
        alphaIndicador = 0.5f + 0.5f * (float) Math.sin(tiempoParpadeo * 3);
    }

    private void manejarInput() {
        // Navegar hacia abajo (siguiente slide)
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) ||
            Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (slideActual < slides.length - 1) {
                slideActual++;
                targetOffsetY += viewport.getWorldHeight();
            }
        }

        // Navegar hacia arriba (slide anterior)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
            Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (slideActual > 0) {
                slideActual--;
                targetOffsetY -= viewport.getWorldHeight();
            }
        }

        // Volver al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            juego.setScreen(pantallaAnterior);
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
        for (Texture slide : slides) {
            if (slide != null) slide.dispose();
        }
    }
}
