package com.abyssdev.entertheabyss.personajes;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Boss extends Enemigo {


    public Boss(float x, float y,float velocidadBoss,float coolDown,int danio) {
        super(x, y,velocidadBoss,coolDown,danio);
        this.vidaMaxima = 200;
        this.vida = 200;
        this.estado = Accion.ESTATICO;
        this.velocidad.set(velocidadBoss, velocidadBoss);
        cargarAnimaciones();
    }

    @Override
    public Rectangle getRectangulo() {
        return new Rectangle(posicion.x, posicion.y, 3f, 3f);
    }

    @Override
    public void renderizar(SpriteBatch batch) {
        TextureRegion frame = obtenerFrameActual();
        float width = 6f;
        float height = 6f;
        float drawX = this.posicion.x;
        float drawY = this.posicion.y;

        if (haciaIzquierda && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!haciaIzquierda && frame.isFlipX()) {
            frame.flip(true, false);
        }
        batch.draw(frame, drawX, drawY, width, height);
    }

    private TextureRegion obtenerFrameActual() {
        switch (estado) {
            case CAMINAR:
                return animCaminar.getKeyFrame(tiempoEstado);
            case ATAQUE:
                return animAtacar.getKeyFrame(tiempoEstado);
            case HIT:
                return animHit.getKeyFrame(tiempoEstado);
            case MUERTE:
                return animMuerte.getKeyFrame(tiempoEstado);
            case ESTATICO:
            default:
                return animIdle.getKeyFrame(tiempoEstado);
        }
    }

}
