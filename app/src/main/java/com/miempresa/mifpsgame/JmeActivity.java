// JmeActivity.java
package com.miempresa.mifpsgame;

import com.jme3.app.AndroidHarness;

/** Puente entre Android y jMonkeyEngine (seccion 4.1 del manual). */
public class JmeActivity extends AndroidHarness {

    public JmeActivity() {
        // Clase principal del juego jME
        appClass = "com.miempresa.mifpsgame.game.FPSGame";

        // Configuracion de pantalla
        eglBitsPerPixel = 24;
        eglAlphaBits = 0;
        eglDepthBits = 16;
        eglSamples = 0;
        eglStencilBits = 0;

        // Pantalla completa sin titulo
        screenFullScreen = true;
        screenShowTitle = false;

        // Usamos eventos tactiles reales (TouchListener), no el raton simulado
        mouseEventsEnabled = false;
        joystickEventsEnabled = false;
        keyEventsEnabled = true;
    }
}
