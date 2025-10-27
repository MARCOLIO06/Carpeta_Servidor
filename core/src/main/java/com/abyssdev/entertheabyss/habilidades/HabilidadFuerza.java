package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadFuerza extends Habilidad {
    public HabilidadFuerza() {
        super("Fuerza", "Aumenta el daño de ataque.", 15, "imagenes/espada.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.aumentarDanio(1);
    }
}
