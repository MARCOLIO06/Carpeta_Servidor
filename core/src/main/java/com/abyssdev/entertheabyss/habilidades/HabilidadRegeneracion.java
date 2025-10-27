package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadRegeneracion extends Habilidad {
    public HabilidadRegeneracion() {
        super("Regeneración", "Regenera salud lentamente.", 35, "imagenes/corazonDorado.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.activarRegeneracion(1); // método que regenere 1 punto por segundo
    }
}
