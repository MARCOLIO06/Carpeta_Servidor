package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadVelocidad2 extends Habilidad {
    public HabilidadVelocidad2() {
        super("Velocidad II",
            "Aumenta aún más la velocidad de movimiento.",
            25,
            "imagenes/botas2.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.aumentarVelocidad(1.2f);
    }
}
