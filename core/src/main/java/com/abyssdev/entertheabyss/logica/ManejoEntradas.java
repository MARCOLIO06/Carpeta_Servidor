package com.abyssdev.entertheabyss.logica;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.badlogic.gdx.Input; // Importar la clase Input
import com.badlogic.gdx.InputProcessor; // Importar la interfaz InputProcessor

public class ManejoEntradas implements InputProcessor {
    private Jugador jugador;

    public ManejoEntradas(Jugador jugador) {
        this.jugador = jugador;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            // Controles de movimiento
            case Input.Keys.W:
                jugador.moverArriba(true);
                break;
            case Input.Keys.S:
                jugador.moverAbajo(true);
                break;
            case Input.Keys.A:
                jugador.moverIzquierda(true);
                break;
            case Input.Keys.D:
                jugador.moverDerecha(true);
                break;
            // Tecla de ataque (ESPACIO)
            case Input.Keys.SPACE:
                jugador.atacar(); // Llama al método atacar() del jugador
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                jugador.intentarEvasion();
                break;
        }
        return true; // Indica que el evento ha sido manejado
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            // Controles de movimiento
            case Input.Keys.W:
                jugador.moverArriba(false);
                break;
            case Input.Keys.S:
                jugador.moverAbajo(false);
                break;
            case Input.Keys.A:
                jugador.moverIzquierda(false);
                break;
            case Input.Keys.D:
                jugador.moverDerecha(false);
                break;
            // Para la tecla ESPACIO, no necesitas hacer nada en keyUp
            // porque la acción de ataque se inicia en keyDown y es gestionada
            // por la propia clase Jugador (animación, hitbox).
        }
        return true; // Indica que el evento ha sido manejado
    }

    // Métodos de InputProcessor que no se utilizan en este contexto
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
