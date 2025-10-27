package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;

public class HabilidadAtaqueVeloz extends Habilidad {
    public HabilidadAtaqueVeloz() {
        super("Ataque Veloz", "Aumenta la velocidad de ataque.", 25, "imagenes/espadaDoble.PNG");
    }

    @Override
    public void aplicar(Jugador jugador) {
        jugador.reducirCooldownAtaque(0.3f);
    }
}
