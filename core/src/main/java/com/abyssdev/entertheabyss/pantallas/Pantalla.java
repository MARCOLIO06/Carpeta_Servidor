package com.abyssdev.entertheabyss.pantallas;

import com.abyssdev.entertheabyss.EnterTheAbyssPrincipal;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Pantalla extends ScreenAdapter {
    protected Game juego;
    protected SpriteBatch batch;

    public Pantalla(Game juego, SpriteBatch batch) {
        this.juego = juego;
        this.batch = batch;
    }
    //algo de tipo game para tener juego.setscreen
}
