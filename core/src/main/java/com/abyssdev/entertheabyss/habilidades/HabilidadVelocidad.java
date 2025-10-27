package com.abyssdev.entertheabyss.habilidades;
import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadVelocidad extends Habilidad {
    public HabilidadVelocidad() {
        super("Velocidad",
            "Aumenta la velocidad del jugador.",
            15 ,
            "imagenes/botas.png");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.aumentarVelocidad(0.8f);
    }
}
