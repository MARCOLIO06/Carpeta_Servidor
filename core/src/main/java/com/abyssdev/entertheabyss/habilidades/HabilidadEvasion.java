package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadEvasion extends Habilidad {
    public HabilidadEvasion() {
        super("Evasión", "Permite esquivar ataques rodando.", 35, "imagenes/botasDoradas.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.habilitarEvasion(true);
    }
}
