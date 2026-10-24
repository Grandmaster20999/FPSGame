# MiFPSGame – Práctica FPS con jMonkeyEngine en Android Studio

Proyecto completo que sigue el *Manual de Desarrollo FPS con jMonkeyEngine* (secciones 2–12).

## Cómo abrirlo
1. Android Studio (Hedgehog o superior) → **File > Open** → carpeta `MiFPSGame`.
2. Espera la sincronización de Gradle (necesita internet; las dependencias jME 3.6.1-stable salen de Maven Central).
   Si Studio sugiere actualizar el plugin de Android/Gradle, acepta.
3. Conecta un móvil (API 24+, depuración USB) o usa un emulador con GLES 3.0 y pulsa **Run ▶**.

## Cómo se juega
| Control | Acción |
|---|---|
| Arrastrar en la mitad izquierda | Joystick flotante (mover) |
| Arrastrar en la mitad derecha | Mirar |
| Botón rojo | Disparar (mantener = ráfaga) |
| Botón azul ▲ | Saltar |
| Botón amarillo ↻ | Recargar (también automática con 0 balas) |

Oleadas de enemigos cada vez más numerosos y rápidos; al limpiar una oleada recuperas 25 HP y 30 balas.
Con 0 HP → *GAME OVER*, toca la pantalla para reiniciar.

## Qué corresponde a cada sección del manual
| Sección | Archivo |
|---|---|
| 2.3 Gradle | `app/build.gradle` |
| 3.2 Manifest | `app/src/main/AndroidManifest.xml` |
| 4.1 Actividad | `JmeActivity.java` |
| 4.2 Clase principal | `game/FPSGame.java` |
| 5 Escenario | `world/SceneLoader.java` (+ `Effects.java`) |
| 6 Cámara FPS | `player/FPSCamera.java`, `player/PlayerControl.java` |
| 7 Controles táctiles | `input/TouchInputHandler.java`, `ui/TouchLayout.java` |
| 8 Físicas y disparo | `FPSGame` (Bullet), `player/WeaponControl.java` (raycast) |
| 9 Enemigos | `enemies/EnemyControl.java`, `enemies/EnemyManager.java` |
| 10 HUD | `ui/HUDState.java` |
| 11 Audio | `audio/AudioManager.java`, `audio/GameAudio.java` |
| 12 Optimización | `GeometryBatchFactory.optimize`, 2 luces, texturas potencia de 2, sin SSAO |
| 13 Firma / ProGuard | `app/build.gradle` (bloque `signingConfigs` comentado), `proguard-rules.pro` |

## Correcciones respecto al código del manual
El código literal del manual no compila / no funciona tal cual; esto se ajustó:
- **`jme3-bullet` y `jme3-bullet-native-android`**: dejaron de publicarse en la 3.3.2. Se usa **Minie 7.8.1+droid** (misma API `com.jme3.bullet`).
- **Dependencia `jme3-android-audio`**: no existe; el audio ya está en `jme3-android`. Se quitó.
- **`signingConfig signingConfigs.release`** referenciaba una config inexistente → bloque comentado con plantilla.
- **Imports faltantes** (AmbientLight, ColorRGBA, Vector3f, Geometry, Box, etc.) y desajuste `FPSCamera` vs `PlayerControl`: se completaron.
- **Velocidad**: el manual multiplicaba `walkDirection` por 8 sin tiempo → velocidad enorme. Ahora es m/s ÷ 60 (paso de física).
- **`CharacterControl` añadido al `rootNode`** (error del manual): ya no se asocia a ningún nodo (el jugador); los enemigos sí lo llevan en su propio nodo.
- **`mouseEventsEnabled = true`** simulaba ratón y chocaba con el `TouchListener`; se pone en `false`.
- **Optimización**: `getRenderer().setCaps(...)` y `SSAOFilter` no son apropiados en móvil; se sustituyeron por batching de geometría y luces limitadas.
- **`noCompress`**: se añadieron `wav/ogg/mp3/png/jpg` (el audio en Android necesita archivos sin comprimir).
- `AndroidManifest`: `android:exported="true"` (obligatorio desde Android 12) y `namespace` en Gradle (AGP 8).
- Se omitió `MainActivity.java`: el manual lo lista pero el lanzador es `JmeActivity`.

## Assets
El manual no incluye modelos ni sonidos, así que se generaron por script (texturas de suelo/ladrillo/caja, iconos del HUD, 5 sonidos WAV) y el nivel se construye por código.
Si colocas un modelo en `app/src/main/assets/Models/Level/nivel1.j3o`, se usa automáticamente en vez de la arena procedural.

## Pendiente de tu lado
Este entorno no tiene Android SDK ni acceso a Maven, por lo que **no pude compilar ni ejecutar el APK**; solo verifiqué la sintaxis Java.
Si algún nombre de la API de jME 3.6.1 da error en Studio, pásame el mensaje y lo corrijo.
