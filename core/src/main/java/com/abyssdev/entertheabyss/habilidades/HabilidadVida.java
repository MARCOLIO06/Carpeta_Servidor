package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadVida extends Habilidad {
    public HabilidadVida() {
        super("Vida Extra", "Aumenta la salud máxima del jugador.", 15, "imagenes/corazon.png");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.aumentarVidaMaxima(20);
    }
}
