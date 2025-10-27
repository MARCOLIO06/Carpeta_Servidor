package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadGolpeCritico extends Habilidad {
    public HabilidadGolpeCritico() {
        super("Golpe Crítico", "Aumenta más el daño de ataque.", 35, "imagenes/espadaRoja.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.aumentarDanio(2);
    }
}
