package com.abyssdev.entertheabyss.logica;

import com.abyssdev.entertheabyss.EnterTheAbyssPrincipal;
import com.abyssdev.entertheabyss.pantallas.Pantalla;
import com.abyssdev.entertheabyss.pantallas.PantallaOpciones;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * InputHandler para la pantalla de opciones.
 * Maneja clicks y arrastre de mouse en sliders y botones.
 */
public class OpcionesInputHandler extends InputAdapter {

    private final PantallaOpciones pantalla;
    private final Game juego;
    private final Pantalla pantallaAnterior;
    private final Viewport viewport;

    private final Rectangle sliderMusicaBounds;
    private final Rectangle sliderEfectosBounds;
    private final Rectangle botonVolverBounds;

    private boolean arrastandoSliderMusica = false;
    private boolean arrastandoSliderEfectos = false;

    private static final float SLIDER_WIDTH = 300f;

    public OpcionesInputHandler(
        PantallaOpciones pantalla,
        Game juego,
        Pantalla pantallaAnterior,
        Viewport viewport,
        Rectangle sliderMusicaBounds,
        Rectangle sliderEfectosBounds,
        Rectangle botonVolverBounds) {

        this.pantalla = pantalla;
        this.juego = juego;
        this.pantallaAnterior = pantallaAnterior;
        this.viewport = viewport;
        this.sliderMusicaBounds = sliderMusicaBounds;
        this.sliderEfectosBounds = sliderEfectosBounds;
        this.botonVolverBounds = botonVolverBounds;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 worldCoords = convertirCoordenadas(screenX, screenY);

        if (sliderMusicaBounds.contains(worldCoords)) {
            arrastandoSliderMusica = true;
            pantalla.setOpcionSeleccionada(0);
            ajustarSlider(worldCoords.x, sliderMusicaBounds, true);
            return true;
        }

        if (sliderEfectosBounds.contains(worldCoords)) {
            arrastandoSliderEfectos = true;
            pantalla.setOpcionSeleccionada(1);
            ajustarSlider(worldCoords.x, sliderEfectosBounds, false);
            return true;
        }

        if (botonVolverBounds.contains(worldCoords)) {
            pantalla.guardarPreferencias();
            juego.setScreen(pantallaAnterior);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 worldCoords = convertirCoordenadas(screenX, screenY);

        if (arrastandoSliderMusica) {
            ajustarSlider(worldCoords.x, sliderMusicaBounds, true);
            return true;
        }

        if (arrastandoSliderEfectos) {
            ajustarSlider(worldCoords.x, sliderEfectosBounds, false);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        arrastandoSliderMusica = false;
        arrastandoSliderEfectos = false;
        return true;
    }

    private Vector2 convertirCoordenadas(int screenX, int screenY) {
        Vector2 coords = new Vector2(screenX, screenY);
        viewport.unproject(coords);
        return coords;
    }

    private void ajustarSlider(float mouseX, Rectangle sliderBounds, boolean esMusica) {
        float valorNormalizado = (mouseX - sliderBounds.x) / SLIDER_WIDTH;
        valorNormalizado = Math.max(0f, Math.min(1f, valorNormalizado));

        if (esMusica) {
            pantalla.setVolumenMusica(valorNormalizado);
        } else {
            pantalla.setVolumenEfectos(valorNormalizado);
        }
    }

    public boolean isArrastandoSliderMusica() {
        return arrastandoSliderMusica;
    }

    public boolean isArrastandoSliderEfectos() {
        return arrastandoSliderEfectos;
    }
}
