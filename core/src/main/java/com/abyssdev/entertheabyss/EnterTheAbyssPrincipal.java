package com.abyssdev.entertheabyss;

import com.abyssdev.entertheabyss.pantallas.MenuInicio;
import com.abyssdev.entertheabyss.pantallas.PantallaWin;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnterTheAbyssPrincipal extends Game {
    public SpriteBatch batch; // SpriteBatch usado por todas las pantallas que va a tener el juego
    private Preferences prefs;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Inicializar preferencias
        prefs = Gdx.app.getPreferences("EnterTheAbyss_Settings");

        float volumenMusica = prefs.getFloat("volumenMusica", .2f);
        float volumenEfectos = prefs.getFloat("volumenEfectos", .2f);

        // Inicializar sonidos y aplicar volúmenes
        Sonidos.cargar();
        Sonidos.setVolumenMusica(volumenMusica);
        Sonidos.setVolumenEfectos(volumenEfectos);

        // Arrancar en el menú
        setScreen(new MenuInicio(this,batch));
    }

    @Override
    public void dispose() {
        batch.dispose();
        Sonidos.dispose();
    }


    public Preferences getPreferencias() {
        return prefs;
    }
}
