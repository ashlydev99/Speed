# Speed - Prueba de velocidad de internet

Aplicación Android para medir la velocidad de descarga de internet con una interfaz moderna tipo aguja. Desarrollada con Kotlin y Material Design.

## Características

- Pantalla de bienvenida (splash) animada.
- Medición de velocidad en Mbps mediante descarga de un archivo de prueba.
- Indicador analógico tipo aguja.
- Botón de actualización manual.
- Detección de falta de conexión a internet.
- Menú con funciones, acerca de y salida.
- Icono vectorial adaptativo (anydpi).
- Compatible con Android 9 (API 28) hasta Android 15 (API 35).

## Capturas

*(Añade aquí capturas de la aplicación si deseas)*

## Construcción

### Requisitos

- Android Studio Hedgehog o superior.
- JDK 11 o superior.

### Compilar desde Android Studio

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Selecciona `Build > Build APK(s)`.

### Compilar con GitHub Actions

El repositorio incluye un workflow que genera la APK automáticamente en cada push o pull request a la rama `main`. Puedes descargar los artefactos desde la pestaña `Actions`.

## Personalización

Puedes cambiar la URL de prueba en `SpeedTester.kt` para usar tu propio servidor.

## Licencia

Este proyecto es de código abierto bajo la licencia MIT.