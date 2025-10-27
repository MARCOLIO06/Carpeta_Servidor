package com.abyssdev.entertheabyss.mapas;

import com.abyssdev.entertheabyss.ui.Sonidos;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class Puerta {

    private Rectangle colision;
    private Array<TileInfo> tiles;
    private boolean abierta;
    private TiledMap mapa;

    // Cache de tiles por nombre
    private final ObjectMap<String, TiledMapTile> cacheTilesPorNombre = new ObjectMap<>();

    public static class TileInfo {
        public int x, y;
        public String nombreCerrado;
        public String nombreAbierto;
        public String nombreCapa;

        public TileInfo(int x, int y, String nombreCerrado, String nombreAbierto, String nombreCapa) {
            this.x = x;
            this.y = y;
            this.nombreCerrado = nombreCerrado;
            this.nombreAbierto = nombreAbierto;
            this.nombreCapa = nombreCapa;
        }
    }

    public Puerta(int tileX, int tileY, int tileWidth, int tileHeight,
                  TiledMap mapa, String nombreCapa, Rectangle colisionFisica) {
        this.colision = colisionFisica;
        this.mapa = mapa;
        this.tiles = new Array<TileInfo>();
        this.abierta = false;

        detectarTiles(nombreCapa, tileX, tileY, tileWidth, tileHeight);
    }

    private void detectarTiles(String nombreCapa, int startX, int startY, int width, int height) {
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(nombreCapa);
        if (capa == null) {
            System.err.println("Capa de tiles no encontrada: " + nombreCapa);
            return;
        }

        int endX = startX + width;
        int endY = startY + height;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                TiledMapTileLayer.Cell cell = capa.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                String tileName = cell.getTile().getProperties().get("name", String.class);
                if (tileName != null && tileName.startsWith("puerta_cerrada_")) {
                    String nombreAbierto = tileName.replace("cerrada", "abierta");
                    if (getTileByName(nombreAbierto) != null) {
                        tiles.add(new TileInfo(x, y, tileName, nombreAbierto, nombreCapa));
                    } else {
                        System.err.println("No se encontró tile abierto: " + nombreAbierto);
                    }
                }
            }
        }

        if (tiles.size == 0) {
            System.err.println("No se detectaron tiles de puerta en el área especificada.");
        }
    }

    public void abrir() {
        if (abierta) return;
        abierta = true;
        Sonidos.reproducirSonidoPuerta();
        System.out.println("Abriendo puerta con " + tiles.size + " tiles...");

        for (TileInfo tileInfo : tiles) {
            cambiarTile(tileInfo.x, tileInfo.y, tileInfo.nombreAbierto, tileInfo.nombreCapa);
        }
    }

    public void cerrar() {
        if (!abierta) return;
        abierta = false;
        System.out.println("Cerrando puerta...");

        for (TileInfo tileInfo : tiles) {
            cambiarTile(tileInfo.x, tileInfo.y, tileInfo.nombreCerrado, tileInfo.nombreCapa);
        }
    }

    private boolean cambiarTile(int x, int y, String nuevoNombre, String nombreCapa) {
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(nombreCapa);
        if (capa == null) {
            System.err.println("Capa no encontrada: " + nombreCapa);
            return false;
        }

        TiledMapTile nuevoTile = getTileByName(nuevoNombre);
        if (nuevoTile == null) {
            System.err.println("No se encontró tile con nombre: " + nuevoNombre);
            return false;
        }

        TiledMapTileLayer.Cell cell = capa.getCell(x, y);
        if (cell == null) {
            cell = new TiledMapTileLayer.Cell();
            capa.setCell(x, y, cell);
        }

        cell.setTile(nuevoTile);
        return true;
    }

    private TiledMapTile getTileByName(String nombre) {
        if (cacheTilesPorNombre.containsKey(nombre)) {
            return cacheTilesPorNombre.get(nombre);
        }

        for (TiledMapTileSet tileset : mapa.getTileSets()) {
            for (TiledMapTile tile : tileset) {
                if (tile == null) continue;
                String tileName = tile.getProperties().get("name", String.class);
                if (tileName != null) {
                    cacheTilesPorNombre.put(tileName, tile);
                    if (tileName.equals(nombre)) {
                        return tile;
                    }
                }
            }
        }

        System.err.println("No se encontró tile con nombre: " + nombre);
        return null;
    }

    public boolean estaAbierta() {
        return abierta;
    }

    public Rectangle getColision() {
        return abierta ? null : colision;
    }
}
