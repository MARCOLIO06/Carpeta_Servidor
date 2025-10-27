package com.abyssdev.entertheabyss.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class Enemigo {

    private float velocidadEnemigo;
    private static final float TAMANO = 3f;
    private static final float DISTANCIA_ATAQUE = 0.8f;
    private float coolDownAtaque;
    private static final float COOLDOWN_GOLPE = 0.1f;
    private int danio;
    protected float tiempoDesdeUltimoAtaque = 0;
    protected float tiempoDesdeUltimoGolpe = 0f;

    protected Texture hojaSprite;
    protected Vector2 posicion;
    protected Vector2 velocidad;
    protected Accion estado;
    protected float tiempoEstado;

    protected boolean eliminar;
    protected boolean haciaIzquierda;
    protected int vida = 30;
    protected int vidaMaxima = 30;

    protected Animation<TextureRegion> animIdle;
    protected Animation<TextureRegion> animCaminar;
    protected Animation<TextureRegion> animAtacar;
    protected Animation<TextureRegion> animHit;
    protected Animation<TextureRegion> animMuerte;

    public Enemigo(float x, float y, float velocidadEnemigo, float coldown, int danio) {
        this.hojaSprite = new Texture("personajes/esqueletoEnemigo.png");
        this.posicion = new Vector2(x, y);
        this.velocidad = new Vector2(0, 0);
        this.danio = danio;
        this.coolDownAtaque = coldown;
        this.estado = Accion.ESTATICO;
        this.tiempoEstado = 0;
        this.eliminar = false;
        this.velocidadEnemigo = velocidadEnemigo;
        this.haciaIzquierda = false;
        this.tiempoDesdeUltimoGolpe = 0;
        this.tiempoDesdeUltimoAtaque = 0;
        cargarAnimaciones();
    }

    protected void cargarAnimaciones() {
        TextureRegion[][] regiones = TextureRegion.split(hojaSprite, 64, 64);
        this.animAtacar = crearAnimacion(regiones[0], 13, 0.07f, Animation.PlayMode.NORMAL);
        this.animMuerte = crearAnimacion(regiones[1], 13, 0.1f, Animation.PlayMode.NORMAL);
        this.animCaminar = crearAnimacion(regiones[2], 12, 0.1f, Animation.PlayMode.LOOP);
        this.animIdle = crearAnimacion(regiones[3], 4, 0.2f, Animation.PlayMode.LOOP);
        this.animHit = crearAnimacion(regiones[4], 3, 0.1f, Animation.PlayMode.NORMAL);
    }

    Animation<TextureRegion> crearAnimacion(TextureRegion[] frames, int cantidad, float duracion, Animation.PlayMode modo) {
        TextureRegion[] usados = new TextureRegion[cantidad];
        System.arraycopy(frames, 0, usados, 0, cantidad);
        Animation<TextureRegion> anim = new Animation<>(duracion, usados);
        anim.setPlayMode(modo);
        return anim;
    }

    public boolean actualizar(float delta, Vector2 posicionJugador, Array<Rectangle> colisionesMapa, ArrayList<Enemigo> otrosEnemigos) {
        tiempoEstado += delta;
        tiempoDesdeUltimoAtaque += delta;
        tiempoDesdeUltimoGolpe += delta;

        if (estado == Accion.MUERTE && animMuerte.isAnimationFinished(tiempoEstado)) {
            eliminar = true;
            return false;
        }

        if (estado == Accion.HIT && animHit.isAnimationFinished(tiempoEstado)) {
            cambiarEstado(Accion.ESTATICO);
        }

        // Si está atacando, esperar a que termine la animación
        if (estado == Accion.ATAQUE) {
            if (animAtacar.isAnimationFinished(tiempoEstado)) {
                cambiarEstado(Accion.ESTATICO);
            }
            return false;
        }

        if (estado != Accion.HIT && estado != Accion.MUERTE) {
            Vector2 direccion = new Vector2(posicionJugador).sub(posicion);
            float distancia = direccion.len();

            if (distancia < DISTANCIA_ATAQUE) {
                velocidad.setZero();
                if (tiempoDesdeUltimoAtaque >= this.coolDownAtaque) {
                    cambiarEstado(Accion.ATAQUE);
                    tiempoDesdeUltimoAtaque = 0;
                    Gdx.app.log("Enemigo", "¡Jugador ha sido atacado!");
                    return true;
                }
            } else {
                // Calcular dirección normalizada y velocidad
                if (direccion.len() > 0.1f) {
                    direccion.nor();
                    velocidad.set(direccion.x * velocidadEnemigo, direccion.y * velocidadEnemigo);
                } else {
                    velocidad.setZero();
                }

                haciaIzquierda = velocidad.x < 0;

                // Guardar posición actual
                Vector2 posAnterior = new Vector2(posicion);

                // Intentar moverse paso a paso
                boolean movioX = false, movioY = false;

                // Mover en X
                posicion.x += velocidad.x * delta;
                if (hayColision(getRectangulo(), colisionesMapa, otrosEnemigos)) {
                    posicion.x = posAnterior.x;
                    velocidad.x = 0;
                } else {
                    movioX = true;
                }

                // Mover en Y
                posicion.y += velocidad.y * delta;
                if (hayColision(getRectangulo(), colisionesMapa, otrosEnemigos)) {
                    posicion.y = posAnterior.y;
                    velocidad.y = 0;
                } else {
                    movioY = true;
                }

                if (movioX || movioY) {
                    cambiarEstado(Accion.CAMINAR);
                } else {
                    cambiarEstado(Accion.ESTATICO);
                }
            }
        }
        return false;
    }

    private boolean hayColision(Rectangle rect, Array<Rectangle> colisionesMapa, ArrayList<Enemigo> otrosEnemigos) {
        for (Rectangle r : colisionesMapa) {
            if (rect.overlaps(r)) {
                return true;
            }
        }
        for (Enemigo otro : otrosEnemigos) {
            if (otro != this && rect.overlaps(otro.getRectangulo())) {
                return true;
            }
        }
        return false;
    }

    public void renderizar(SpriteBatch batch) {
        TextureRegion frame = obtenerFrameActual();
        float width = TAMANO;
        float height = TAMANO;
        float drawX = posicion.x;
        float drawY = posicion.y;

        TextureRegion frameRender = new TextureRegion(frame);

        if (haciaIzquierda && !frameRender.isFlipX()) {
            frameRender.flip(true, false);
        } else if (!haciaIzquierda && frameRender.isFlipX()) {
            frameRender.flip(true, false);
        }

        batch.draw(frameRender, drawX, drawY, width, height);
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

    public void recibirDanio(int danio) {
        if (tiempoDesdeUltimoGolpe < COOLDOWN_GOLPE || estado == Accion.MUERTE) return;

        this.vida -= danio;
        Gdx.app.log("Enemigo", "Recibió " + danio + " de daño. Vida restante: " + this.vida);

        cambiarEstado(Accion.HIT);
        tiempoDesdeUltimoGolpe = 0f;

        if (this.vida <= 0) {
            morir();
        }
    }

    public void morir() {
        cambiarEstado(Accion.MUERTE);
    }

    protected void cambiarEstado(Accion nuevo) {
        if (estado != nuevo) {
            estado = nuevo;
            tiempoEstado = 0;
            if (nuevo != Accion.CAMINAR) {
                velocidad.setZero();
            }
        }
    }

    public Rectangle getRectangulo() {
        return new Rectangle(posicion.x, posicion.y, TAMANO, TAMANO);
    }

    public boolean debeEliminarse() {
        return eliminar;
    }

    public Vector2 getPosicion() {
        return posicion;
    }

    public int getVida() {
        return this.vida;
    }

    public int getVidaMaxima() {
        return this.vidaMaxima;
    }

    public static float getTamaño() {
        return TAMANO;
    }

    public int getDanio() {
        return this.danio;
    }
}
