package com.abyssdev.entertheabyss.mapas;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class Mapa {

    private String id;
    private ObjectMap<String, Sala> salas;
    private Sala salaActual;

    public Mapa(String id) {
        this.id = id;
        this.salas = new ObjectMap<String, Sala>();
    }

    public void agregarSala(Sala sala) {
        salas.put(sala.getId(), sala);
    }

    public void establecerSalaActual(String salaId) {
        if (salaActual != null) {

        }
        salaActual = salas.get(salaId);
    }

    public Sala getSalaActual() { return salaActual; }
    public Sala getSala(String id) { return salas.get(id); }
    public String getId() { return id; }

    public void dispose() {
        for (Sala sala : salas.values()) {
            sala.dispose();
        }
        salas.clear();
    }
}
