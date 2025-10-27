package com.abyssdev.entertheabyss.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.HashMap;


public class FontManager {

    private static FontManager instance;


    private HashMap<String, BitmapFont> fuentes;


    private static final String RUTA_FUENTE = "fuentes/PixeloidSans.ttf";


    private boolean usarFreeType = true;

    private FontManager() {
        fuentes = new HashMap<>();
        cargarFuentes();
    }


    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }


    private void cargarFuentes() {
        try {
            cargarConFreeType();
        } catch (Exception e) {
            Gdx.app.error("FontManager", "Error al cargar con FreeType, usando fallback: " + e.getMessage());
            usarFreeType = false;
            cargarFallback();
        }
    }

    /**
     * Carga fuentes usando FreeTypeFontGenerator (mejor calidad)
     */
    private void cargarConFreeType() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal(RUTA_FUENTE)
        );

        // FUENTE TÍTULO GRANDE (para títulos principales)
        FreeTypeFontParameter paramTitulo = new FreeTypeFontParameter();
        paramTitulo.size = 72;
        paramTitulo.color = Color.WHITE;
        paramTitulo.borderWidth = 3;
        paramTitulo.borderColor = Color.BLACK;
        paramTitulo.shadowOffsetX = 3;
        paramTitulo.shadowOffsetY = 3;
        paramTitulo.shadowColor = new Color(0, 0, 0, 0.6f);
        paramTitulo.minFilter = Texture.TextureFilter.Linear;
        paramTitulo.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("titulo", generator.generateFont(paramTitulo));

        // FUENTE GRANDE (para subtítulos, opciones de menú)
        FreeTypeFontParameter paramGrande = new FreeTypeFontParameter();
        paramGrande.size = 48;
        paramGrande.color = Color.WHITE;
        paramGrande.borderWidth = 2;
        paramGrande.borderColor = Color.BLACK;
        paramGrande.shadowOffsetX = 2;
        paramGrande.shadowOffsetY = 2;
        paramGrande.shadowColor = new Color(0, 0, 0, 0.5f);
        paramGrande.minFilter = Texture.TextureFilter.Linear;
        paramGrande.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("grande", generator.generateFont(paramGrande));

        // FUENTE MEDIANA (para texto general, diálogos)
        FreeTypeFontParameter paramMediana = new FreeTypeFontParameter();
        paramMediana.size = 32;
        paramMediana.color = Color.WHITE;
        paramMediana.borderWidth = 1.5f;
        paramMediana.borderColor = Color.BLACK;
        paramMediana.shadowOffsetX = 1;
        paramMediana.shadowOffsetY = 1;
        paramMediana.shadowColor = new Color(0, 0, 0, 0.4f);
        paramMediana.minFilter = Texture.TextureFilter.Linear;
        paramMediana.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("mediana", generator.generateFont(paramMediana));

        // FUENTE PEQUEÑA (para HUD, stats, números)
        FreeTypeFontParameter paramPequena = new FreeTypeFontParameter();
        paramPequena.size = 24;
        paramPequena.color = Color.WHITE;
        paramPequena.borderWidth = 1;
        paramPequena.borderColor = Color.BLACK;
        paramPequena.minFilter = Texture.TextureFilter.Linear;
        paramPequena.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("pequena", generator.generateFont(paramPequena));

        // FUENTE PEQUEÑA SIN BORDE (para texto secundario)
        FreeTypeFontParameter paramPequenaSinBorde = new FreeTypeFontParameter();
        paramPequenaSinBorde.size = 20;
        paramPequenaSinBorde.color = Color.WHITE;
        paramPequenaSinBorde.minFilter = Texture.TextureFilter.Linear;
        paramPequenaSinBorde.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("pequena_sin_borde", generator.generateFont(paramPequenaSinBorde));

        // FUENTE PARA DAÑO/NÚMEROS (amarilla, más gruesa)
        FreeTypeFontParameter paramDanio = new FreeTypeFontParameter();
        paramDanio.size = 36;
        paramDanio.color = Color.YELLOW;
        paramDanio.borderWidth = 2;
        paramDanio.borderColor = Color.RED;
        paramDanio.shadowOffsetX = 2;
        paramDanio.shadowOffsetY = 2;
        paramDanio.shadowColor = new Color(0, 0, 0, 0.7f);
        paramDanio.minFilter = Texture.TextureFilter.Linear;
        paramDanio.magFilter = Texture.TextureFilter.Linear;
        fuentes.put("danio", generator.generateFont(paramDanio));

        generator.dispose();
        Gdx.app.log("FontManager", "Fuentes cargadas exitosamente con FreeType");
    }

    /**
     * Carga fuentes usando BitmapFont por defecto (fallback)
     */
    private void cargarFallback() {
        // Título
        BitmapFont titulo = new BitmapFont();
        titulo.getData().setScale(4f);
        titulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("titulo", titulo);

        // Grande
        BitmapFont grande = new BitmapFont();
        grande.getData().setScale(3f);
        grande.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("grande", grande);

        // Mediana
        BitmapFont mediana = new BitmapFont();
        mediana.getData().setScale(2f);
        mediana.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("mediana", mediana);

        // Pequeña
        BitmapFont pequena = new BitmapFont();
        pequena.getData().setScale(1.5f);
        pequena.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("pequena", pequena);

        // Pequeña sin borde
        BitmapFont pequenaSinBorde = new BitmapFont();
        pequenaSinBorde.getData().setScale(1.2f);
        pequenaSinBorde.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("pequena_sin_borde", pequenaSinBorde);

        // Daño
        BitmapFont danio = new BitmapFont();
        danio.getData().setScale(2.5f);
        danio.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fuentes.put("danio", danio);

        Gdx.app.log("FontManager", "Fuentes cargadas con fallback (BitmapFont)");
    }

    /**
     * Obtiene una fuente por su nombre
     * @param nombre Nombre de la fuente: "titulo", "grande", "mediana", "pequena", "pequena_sin_borde", "danio"
     * @return BitmapFont solicitada, o fuente mediana por defecto si no existe
     */
    public BitmapFont getFuente(String nombre) {
        if (fuentes.containsKey(nombre)) {
            return fuentes.get(nombre);
        }
        Gdx.app.error("FontManager", "Fuente no encontrada: " + nombre + ", usando 'mediana' por defecto");
        return fuentes.get("mediana");
    }

    /**
     * Métodos convenientes para obtener fuentes específicas
     */
    public BitmapFont getTitulo() {
        return getFuente("titulo");
    }

    public BitmapFont getGrande() {
        return getFuente("grande");
    }

    public BitmapFont getMediana() {
        return getFuente("mediana");
    }

    public BitmapFont getPequena() {
        return getFuente("pequena");
    }

    public BitmapFont getPequenaSinBorde() {
        return getFuente("pequena_sin_borde");
    }

    public BitmapFont getDanio() {
        return getFuente("danio");
    }

    /**
     * Libera todos los recursos de las fuentes
     * IMPORTANTE: Llamar solo al cerrar el juego
     */
    public void dispose() {
        for (BitmapFont fuente : fuentes.values()) {
            if (fuente != null) {
                fuente.dispose();
            }
        }
        fuentes.clear();
        instance = null;
        Gdx.app.log("FontManager", "Fuentes liberadas");
    }

    /**
     * Recarga todas las fuentes (útil si cambias de idioma o configuración)
     */
    public void recargar() {
        dispose();
        instance = new FontManager();
    }
}
