package com.abyssdev.entertheabyss.habilidades;

import com.abyssdev.entertheabyss.personajes.Jugador;
import com.badlogic.gdx.graphics.Texture;

public abstract class Habilidad {
    protected String nombre;
    protected String descripcion;
    protected int costo;
    protected Texture icono;
    public boolean comprada = false;

    public Habilidad(String nombre, String descripcion, int costo, String rutaIcono) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.icono = new Texture(rutaIcono);
    }

    public abstract void aplicar(Jugador jugador);

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCosto() {
        return costo;
    }

    public Texture getIcono() {
        return icono;
    }
}
