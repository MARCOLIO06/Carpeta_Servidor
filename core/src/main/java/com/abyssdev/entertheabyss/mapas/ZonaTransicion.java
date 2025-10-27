package com.abyssdev.entertheabyss.mapas;


import com.badlogic.gdx.math.Rectangle;

public class ZonaTransicion extends Rectangle {

    public String destinoSalaId;
    public String spawnName;

    public ZonaTransicion(float x, float y, float w, float h, String destino, String spawnName) {
        super(x, y, w, h);
        this.destinoSalaId = destino;
        this.spawnName = spawnName;
    }
}
