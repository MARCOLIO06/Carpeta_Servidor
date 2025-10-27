# Enter The Abyss

## Integrantes
 - Marco Caputo 
 - Luca Fontan
 - Ramiro Ridolfi

## Descripción

Enter the Abyss es un juego de acción y supervivencia con elementos roguelike, ambientado en un mundo post-apocalíptico donde ha colapsado tras un evento conocido como El Gran Silencio. Los jugadores asumen el papel de sobrevivientes que se adentran en El Abismo, una mazmorra  misteriosa, cambiante y llena de secretos, con el objetivo de reconstruir lo que queda de la humanidad.

## Tecnologías

Este videojuego será programado en Java, utilizando la versión 8, e integrará el framework LibGDX en su versión 1.13.1 para escritorio.

## Características implementadas (Prototipo jugable)

- Movimiento del personaje con teclado usando `InputProcessor`.
- Animaciones del jugador mediante spritesheets.
- Carga y renderizado de mapas desde Tiled (`.tmx`).
- Escalado de mapa y cámara con Viewport, adaptable a múltiples resoluciones.
- Cámara que sigue al jugador dentro de los límites del mapa.
- Detección de colisiones entre el jugador y los objetos del mapa.
- Estructura multiventana (`Screen`): Menú principal, juego, pausa y árbol de habilidades.
- Lógica de pausa/reanudación y cambio de pantallas con teclas.
- Reutilización eficiente de `SpriteBatch` entre pantallas.

> El proyecto sigue una estructura modular orientada a facilitar la expansión con nuevas salas, personajes y mecánicas.

## Video desmostrativo del juego
- https://drive.google.com/file/d/1gN7TS5AoHX75s-pRf-d_Zs6uzK27-uly/view?usp=drive_link

## Clonar el repositorio

Primero en tu terminal vas a tener que seleccionar la carpeta donde queres clonar el repositorio, con el siguente comando:

```bash
cd nombre-carpeta-ejemplo
```

Una vez en la carpeta utiliza el siguente comando:

```bash
git clone https://github.com/Ridooo11/enter-the-abyss-libgdx
```

Luego en tu IDE deberas abrirlo como un proyecto de Gradle:

- `IntelliJ IDEA`: <br>
 o Anda a "File" > "Open..." (o "Open" en la pantalla de bienvenida).<br>
 o Navega hasta la carpeta del proyecto que Liftoff acaba de crear. <br>
 o Selecciona el archivo build.gradle (o la carpeta que lo contiene) y elegí "Open as Project". El IDE debería reconocerlo como un proyecto Gradle.

 - `Eclipse`: <br>
 o Anda a "File" > "Import...".<br>
 o Selecciona "Gradle" > "Existing Gradle Project".<br>
 o Navega hasta la carpeta raíz del proyecto generado y seguí los pasos del asistente.

## Wiki

Podes encontrar informacion mas detallada en la [wiki](https://github.com/Ridooo11/enter-the-abyss-libgdx/wiki) 


