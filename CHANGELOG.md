## [Unreleased]

## [1.1.0] - 2025-08-03

### Added 

- Implementación del movimiento del personaje.
- Animaciones del jugador mediante spritesheets. 
- Carga y renderizado de mapas con Tiled.
- Implementación de distintos viewports para adaptar resolución en todas las pantallas.
- Implementación de camara que sigue el movimiento del jugador.
- Pantallas múltiples: Menú, Juego, Pausa y Árbol de habilidades.
- Gestion de estados como pausar, reanudar y salir del juego.
- Colisiones del jugador con los objetos del mapa.

### Changed

- La clase principal del juego ahora extiende de `Game` en lugar de `ApplicationAdapter`, permitiendo una mejor gestión de pantallas.
- Se estructuró el código para compartir una única instancia de `SpriteBatch` entre todas las pantallas del juego.
- Se aplicó escalado correcto a los mapas, colisiones y a la hitbox del jugador para mantener coherencia visual con el entorno.
- Refactorización general del código para una mejor organización modular (entrada, render, lógica de colisión).

### Fixed
- Corrección del estiramiento del fondo en el menú al cambiar resolución.
- Solución al desfasaje entre el tamaño visual del jugador y su rectángulo de colisión.
- Eliminación de múltiples instancias innecesarias de `SpriteBatch` que causaban inconsistencias gráficas.

## [1.0.0] - 2025-05-17

### Added

- Creación inicial del proyecto **Enter The Abyss**.
- Estructura base del proyecto con configuración de LibGDX y Gradle.
- Inclusión de dependencias básicas: core y desktop.

