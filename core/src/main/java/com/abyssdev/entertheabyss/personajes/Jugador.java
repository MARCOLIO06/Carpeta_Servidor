package com.abyssdev.entertheabyss.personajes;

import com.abyssdev.entertheabyss.habilidades.*;
import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class Jugador {
    private Vector2 posicion;
    private float ancho = 3f, alto = 3f;
    private float velocidad = 3.2f;

    private int vida = 100;
    private int vidaMaxima = 100;
    private int municionActual = 30;
    private int municionMaxima = 30;
    private int monedas = 0;
    private int danioBase = 5;

    // HITBOX
    private final float anchoHitbox = 1f;
    private final float altoHitbox = 1f;
    private final float offsetHitboxX = 1f;
    private final float offsetHitboxY = .5f;

    // MOVIMIENTO
    private boolean arriba, abajo, izquierda, derecha;
    private Texture hojaSprite;
    private Animation<TextureRegion>[][] animaciones;
    private float estadoTiempo;
    private Direccion direccionActual = Direccion.ABAJO;
    private Accion accionActual = Accion.ESTATICO;

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 48;
    private static final int FRAMES_PER_ANIMATION = 3;

    private int[][] mapaFilasAnimacion;

    private Rectangle hitboxAtaque;
    private boolean atacandoAplicado;
    private float duracionHitboxAtaque = 0.1f;
    private float tiempoHitboxActivo;

    private float tiempoUltimoAtaque = 0f;
    private float cooldownAtaque = 1f;
    private float porcentajeReduccionDanio = 0f;

    private boolean regeneracionActiva = false;
    private float tiempoRegeneracion = 0f;
    private float intervaloRegeneracion = 1f; // Cada 1 segundo
    private int cantidadRegeneracion = 1;

    // --- DASH / EVASIÓN ---
    private boolean evasionHabilitada = false;
    private boolean estaEvadendo = false;
    private float duracionEvasion = 0.3f; // dura 0.3 segundos
    private float tiempoEvasion = 0f;
    private float cooldownEvasion = 1.5f; // tiempo antes de poder volver a usar
    private float tiempoDesdeUltimaEvasion = 0f;
    private float velocidadBase = 3.2f;

    private float parpadeoEvasion = 0f; // tiempo acumulado para parpadeo
    private float intervaloParpadeo = 0.05f; // cambia cada 0.05s la visibilidad
    private boolean mostrarFrame = true; // si dibujar o no


    // 🔹 HABILIDADES DEL JUGADOR
    private Map<String, Habilidad> habilidades;

    public Jugador() {
        this.posicion = new Vector2(100, 100);
        hojaSprite = new Texture("personajes/player.png");
        inicializarMapaFilas();
        cargarAnimaciones();
        hitboxAtaque = new Rectangle(0, 0, 0, 0);
        // 🔹 Inicializar habilidades aquí
        inicializarHabilidades();
    }

    private void inicializarHabilidades() {
        habilidades = new HashMap<>();
        habilidades.put("Vida Extra", new HabilidadVida());
        habilidades.put("Defensa", new HabilidadDefensa());
        habilidades.put("Regeneración", new HabilidadRegeneracion());
        habilidades.put("Fuerza", new HabilidadFuerza());
        habilidades.put("Ataque Veloz", new HabilidadAtaqueVeloz());
        habilidades.put("Golpe Crítico", new HabilidadGolpeCritico());
        habilidades.put("Velocidad", new HabilidadVelocidad());
        habilidades.put("Velocidad II", new HabilidadVelocidad2());
        habilidades.put("Evasión", new HabilidadEvasion());
    }

    // 🔹 Getter para que otras pantallas accedan
    public Map<String, Habilidad> getHabilidades() {
        return habilidades;
    }

    private void inicializarMapaFilas() {
        mapaFilasAnimacion = new int[Accion.values().length][Direccion.values().length];
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.ABAJO.ordinal()] = 0;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.DERECHA.ordinal()] = 1;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.ARRIBA.ordinal()] = 2;
        mapaFilasAnimacion[Accion.ESTATICO.ordinal()][Direccion.IZQUIERDA.ordinal()] = 1;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.ABAJO.ordinal()] = 3;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.DERECHA.ordinal()] = 4;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.ARRIBA.ordinal()] = 5;
        mapaFilasAnimacion[Accion.CAMINAR.ordinal()][Direccion.IZQUIERDA.ordinal()] = 4;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.ABAJO.ordinal()] = 6;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.DERECHA.ordinal()] = 7;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.IZQUIERDA.ordinal()] = 7;
        mapaFilasAnimacion[Accion.ATAQUE.ordinal()][Direccion.ARRIBA.ordinal()] = 8;
        for (int dir = 0; dir < Direccion.values().length; dir++) {
            mapaFilasAnimacion[Accion.MUERTE.ordinal()][dir] = 9;
        }
    }

    private void cargarAnimaciones() {
        TextureRegion[][] regiones = TextureRegion.split(hojaSprite, FRAME_WIDTH, FRAME_HEIGHT);
        animaciones = new Animation[Accion.values().length][Direccion.values().length];

        for (Accion accion : Accion.values()) {
            for (Direccion dir : Direccion.values()) {
                int filaSpriteSheet = mapaFilasAnimacion[accion.ordinal()][dir.ordinal()];
                if (filaSpriteSheet >= regiones.length) {
                    Gdx.app.error("Jugador", "Error: La fila " + filaSpriteSheet +
                        " para la acción " + accion.name() +
                        " y dirección " + dir.name() +
                        " excede las filas disponibles en la hoja de sprites (" + regiones.length + ").");
                    animaciones[accion.ordinal()][dir.ordinal()] = new Animation<>(0.1f, new TextureRegion[1]);
                    continue;
                }

                TextureRegion[] frames = new TextureRegion[FRAMES_PER_ANIMATION];
                for (int i = 0; i < Math.min(FRAMES_PER_ANIMATION, regiones[filaSpriteSheet].length); i++) {
                    frames[i] = regiones[filaSpriteSheet][i];
                }

                float frameDuration = 0.2f;
                Animation.PlayMode playMode = Animation.PlayMode.LOOP;

                switch (accion) {
                    case ESTATICO:
                        frameDuration = 0.2f;
                        playMode = Animation.PlayMode.NORMAL;
                        if (frames.length > 1) {
                            playMode = Animation.PlayMode.LOOP;
                        } else {
                            frames = new TextureRegion[]{regiones[filaSpriteSheet][0]};
                        }
                        break;
                    case CAMINAR:
                        frameDuration = 0.15f;
                        playMode = Animation.PlayMode.LOOP;
                        break;
                    case ATAQUE:
                        frameDuration = 0.1f;
                        playMode = Animation.PlayMode.NORMAL;
                        break;
                    case MUERTE:
                        frameDuration = 0.15f;
                        playMode = Animation.PlayMode.NORMAL;
                        break;
                }
                animaciones[accion.ordinal()][dir.ordinal()] = new Animation<>(frameDuration, frames);
                animaciones[accion.ordinal()][dir.ordinal()].setPlayMode(playMode);
            }
        }
    }

    public void update(float delta, Array<Rectangle> colisiones) {
        float dx = 0, dy = 0;
        if (arriba) dy += 1;
        if (abajo) dy -= 1;
        if (izquierda) dx -= 1;
        if (derecha) dx += 1;

        if (dx != 0 && dy != 0) {
            dx *= 0.7071f;
            dy *= 0.7071f;
        }

        float nuevaX = posicion.x + dx * velocidad * delta;
        float nuevaY = posicion.y + dy * velocidad * delta;

        Rectangle hitboxX = new Rectangle(nuevaX + offsetHitboxX, posicion.y + offsetHitboxY, anchoHitbox, altoHitbox);
        Rectangle hitboxY = new Rectangle(posicion.x + offsetHitboxX, nuevaY + offsetHitboxY, anchoHitbox, altoHitbox);

        boolean colisionX = false;
        for (Rectangle r : colisiones) {
            if (r.overlaps(hitboxX)) {
                colisionX = true;
                break;
            }
        }
        if (!colisionX) {
            posicion.x = nuevaX;
        }

        boolean colisionY = false;
        for (Rectangle r : colisiones) {
            if (r.overlaps(hitboxY)) {
                colisionY = true;
                break;
            }
        }
        if (!colisionY) {
            posicion.y = nuevaY;
        }

        boolean estaMoviendo = (dx != 0 || dy != 0);

        if (accionActual != Accion.MUERTE) {
            if (accionActual == Accion.ATAQUE) {
                estadoTiempo += delta;
                if (!atacandoAplicado && estadoTiempo >= 0.05f) {
                    actualizarHitboxAtaque();
                    atacandoAplicado = true;
                    tiempoHitboxActivo = 0;
                }
                if (atacandoAplicado) {
                    tiempoHitboxActivo += delta;
                    if (tiempoHitboxActivo >= duracionHitboxAtaque) {
                        hitboxAtaque.setSize(0, 0);
                    }
                }

                if (animaciones[accionActual.ordinal()][direccionActual.ordinal()].isAnimationFinished(estadoTiempo)) {
                    accionActual = Accion.ESTATICO;
                    estadoTiempo = 0;
                    atacandoAplicado = false;
                    hitboxAtaque.setSize(0, 0);
                }
            } else if (estaMoviendo) {
                if (accionActual != Accion.CAMINAR) {
                    estadoTiempo = 0;
                }
                accionActual = Accion.CAMINAR;
                estadoTiempo += delta;

                if (Math.abs(dx) > Math.abs(dy)) {
                    direccionActual = dx > 0 ? Direccion.DERECHA : Direccion.IZQUIERDA;
                } else {
                    direccionActual = dy > 0 ? Direccion.ARRIBA : Direccion.ABAJO;
                }
            } else {
                accionActual = Accion.ESTATICO;
                estadoTiempo += delta;
            }
        } else {
            estadoTiempo += delta;
        }

        // --- TIEMPO ENTRE ATAQUES ---
        tiempoUltimoAtaque += delta;

        // --- REGENERACIÓN DE VIDA AUTOMÁTICA ---
        if (regeneracionActiva && vida < vidaMaxima) {
            tiempoRegeneracion += delta;
            if (tiempoRegeneracion >= intervaloRegeneracion) {
                tiempoRegeneracion = 0f;
                vida = Math.min(vida + cantidadRegeneracion, vidaMaxima);
                Gdx.app.log("Jugador", "Regenerando vida... (" + vida + "/" + vidaMaxima + ")");
            }
        }

        // --- EVASIÓN (DASH) ---
        if (estaEvadendo) {
            tiempoEvasion += delta;
            if (tiempoEvasion >= duracionEvasion) {
                estaEvadendo = false;
                velocidad = velocidadBase; // volver a velocidad normal
                mostrarFrame = true; // asegurarse que el personaje se dibuje normalmente
                parpadeoEvasion = 0f;
            }
        } else {
            tiempoDesdeUltimaEvasion += delta;
        }
    }


    public void dibujar(SpriteBatch batch) {
        Animation<TextureRegion> currentAnimation = animaciones[accionActual.ordinal()][direccionActual.ordinal()];
        TextureRegion frameADibujar = currentAnimation.getKeyFrame(estadoTiempo);

        boolean voltearX = false;
        if (direccionActual == Direccion.IZQUIERDA) {
            if (accionActual == Accion.ESTATICO || accionActual == Accion.CAMINAR || accionActual == Accion.ATAQUE) {
                voltearX = true;
            }
        }

        TextureRegion frameParaDibujar = new TextureRegion(frameADibujar);
        if (frameParaDibujar.isFlipX() && !voltearX) {
            frameParaDibujar.flip(true, false);
        } else if (!frameParaDibujar.isFlipX() && voltearX) {
            frameParaDibujar.flip(true, false);
        }

        // --- EFECTO DASH ---
        if (estaEvadendo) {
            parpadeoEvasion += Gdx.graphics.getDeltaTime();
            if (parpadeoEvasion >= intervaloParpadeo) {
                mostrarFrame = !mostrarFrame;
                parpadeoEvasion = 0f;
            }
            if (mostrarFrame) {
                batch.setColor(1f, 1f, 1f, 0.5f); // 50% transparencia
                batch.draw(frameParaDibujar, posicion.x, posicion.y, ancho, alto);
                batch.setColor(Color.WHITE); // volver a normal
            }
        } else {
            batch.draw(frameParaDibujar, posicion.x, posicion.y, ancho, alto);
        }
    }

    private void actualizarHitboxAtaque() {
        float hitboxWidth = 0.5f;
        float hitboxHeight = 0.5f;
        float offsetX = 0;
        float offsetY = 0;

        switch (direccionActual) {
            case ABAJO:
                offsetX = (ancho - hitboxWidth) / 2;
                offsetY = -0.5f;
                break;
            case ARRIBA:
                offsetX = (ancho - hitboxWidth) / 2;
                offsetY = alto;
                break;
            case IZQUIERDA:
                offsetX = -0.5f;
                offsetY = (alto - hitboxHeight) / 2;
                break;
            case DERECHA:
                offsetX = ancho;
                offsetY = (alto - hitboxHeight) / 2;
                break;
        }
        hitboxAtaque.set(posicion.x + offsetX, posicion.y + offsetY, hitboxWidth, hitboxHeight);
        System.out.println("[Jugador] Hitbox de ataque: " + hitboxAtaque.toString());
    }

    public void atacar() {
        if (tiempoUltimoAtaque >= cooldownAtaque && accionActual != Accion.MUERTE) {
            accionActual = Accion.ATAQUE;
            estadoTiempo = 0;
            atacandoAplicado = false;
            tiempoHitboxActivo = 0;
            tiempoUltimoAtaque = 0;
            Sonidos.reproducirAtaque();
        }
    }

    public void morir() {
        if (accionActual != Accion.MUERTE) {
            accionActual = Accion.MUERTE;
            estadoTiempo = 0;
            atacandoAplicado = false;
            hitboxAtaque.setSize(0, 0);
        }
    }

    // --- NUEVOS MÉTODOS PARA ÁRBOL DE HABILIDADES ---
    public void aumentarVelocidad(float incremento) {
        this.velocidad += incremento;
        Gdx.app.log("Jugador", "Nueva velocidad: " + this.velocidad);
    }

    public void reducirCooldownAtaque(float reduccion) {
        this.cooldownAtaque -= reduccion;
        Gdx.app.log("Jugador", "Nueva velocidad de ataque: " + this.cooldownAtaque);
    }

    public void aumentarDanio(int cantidad) {
        this.danioBase += cantidad;
        Gdx.app.log("Jugador", "Daño aumentado a: " + this.danioBase);
    }

    public void habilitarEvasion(boolean b) {
        this.evasionHabilitada = b;
        Gdx.app.log("Jugador", "Evasión " + (b ? "habilitada" : "deshabilitada"));
    }


    public float getVelocidad() {
        return this.velocidad;
    }

    public void aumentarVidaMaxima(int i) {
        this.vidaMaxima += i;
        this.vida = Math.min(this.vida, this.vidaMaxima);
        Gdx.app.log("Jugador", "Vida máxima aumentada a: " + this.vidaMaxima);
    }

    public void reducirDanioRecibido(float porcentaje) {
        this.porcentajeReduccionDanio += porcentaje;
        this.porcentajeReduccionDanio = Math.min(0.9f, this.porcentajeReduccionDanio);
        Gdx.app.log("Jugador", "Reducción de daño actual: " + (int)(this.porcentajeReduccionDanio * 100) + "%");
    }

    public void activarRegeneracion(int cantidadPorSegundo) {
        this.regeneracionActiva = true;
        this.cantidadRegeneracion = cantidadPorSegundo;
        this.tiempoRegeneracion = 0f;
        Gdx.app.log("Jugador", "Regeneración activada: +" + cantidadPorSegundo + " por segundo");
    }

    public void intentarEvasion() {
        if (!evasionHabilitada) return;
        if (estaEvadendo) return;
        if (tiempoDesdeUltimaEvasion < cooldownEvasion) return;

        estaEvadendo = true;
        tiempoEvasion = 0f;
        tiempoDesdeUltimaEvasion = 0f;

        // Aumenta temporalmente la velocidad
        velocidad = velocidadBase * 4f;

        // Reproducir sonido opcional
        Sonidos.reproducirEvasion(); // solo si querés agregarlo

        Gdx.app.log("Jugador", "¡Evasión activada!");
    }



    // --- GETTERS ---
    public int getVida() { return this.vida; }
    public int getVidaMaxima() { return this.vidaMaxima; }
    public int getMunicionActual() { return this.municionActual; }
    public int getMunicionMaxima() { return this.municionMaxima; }
    public int getMonedas() { return this.monedas; }
    public int getDanio() { return this.danioBase; }
    public float getX() { return this.posicion.x; }
    public float getY() { return this.posicion.y; }
    public Vector2 getPosicion() { return this.posicion; }
    public float getAncho() { return this.ancho; }
    public float getAlto() { return this.alto; }
    public Rectangle getHitboxAtaque() { return this.hitboxAtaque; }


    // --- SETTERS ---
    public void setVida(int vida) {
        this.vida = Math.max(0, Math.min(vida, vidaMaxima));
    }

    public void modificarVida(int cantidad) {
        setVida(this.vida + cantidad);
    }

    public void setMunicionActual(int municion) {
        this.municionActual = Math.max(0, Math.min(municion, municionMaxima));
    }

    public void modificarMunicion(int cantidad) {
        setMunicionActual(this.municionActual + cantidad);
    }

    public void setMonedas(int monedas) {
        this.monedas = Math.max(0, monedas);
    }

    public void modificarMonedas(int cantidad) {
        setMonedas(this.monedas + cantidad);
    }

    public void dispose() {
        if (hojaSprite != null) {
            hojaSprite.dispose();
        }
        // 🔹 Liberar texturas de habilidades
        if (habilidades != null) {
            for (Habilidad h : habilidades.values()) {
                if (h.getIcono() != null) {
                    h.getIcono().dispose();
                }
            }
        }
    }

    public void setX(float x) { this.posicion.x = x; }
    public void setY(float y) { this.posicion.y = y; }
    public void setPosicion(float x, float y) { this.posicion.set(x, y); }

    public void moverArriba(boolean activo) { this.arriba = activo; }
    public void moverAbajo(boolean activo) { this.abajo = activo; }
    public void moverIzquierda(boolean activo) { this.izquierda = activo; }
    public void moverDerecha(boolean activo) { this.derecha = activo; }


    public void recibirDanio(int danioBruto) {
        if (danioBruto <= 0) return;

        float danioReducido = danioBruto * (1f - porcentajeReduccionDanio);
        int danioFinal = Math.max(1, (int) Math.floor(danioReducido));

        this.vida -= danioFinal;
        Gdx.app.log("Jugador",
            "Daño recibido: " + danioFinal +
                " (original: " + danioBruto +
                ", reducción: " + (int)(porcentajeReduccionDanio * 100) + "%). " +
                "Vida actual: " + this.vida);
    }
}
