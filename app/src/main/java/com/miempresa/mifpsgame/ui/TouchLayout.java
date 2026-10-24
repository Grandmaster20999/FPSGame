// TouchLayout.java
package com.miempresa.mifpsgame.ui;

/**
 * Geometria (en pixeles) de los controles tactiles. La comparten el manejador de
 * toques y el HUD. El origen es la esquina inferior izquierda, como en jME.
 */
public class TouchLayout {

    public final float width, height;
    public final float joyRadius, knobRadius;
    public final float fireX, fireY, fireR;
    public final float jumpX, jumpY, jumpR;
    public final float reloadX, reloadY, reloadR;
    /** Tocar a la izquierda de esta X mueve; a la derecha mira. */
    public final float splitX;

    public TouchLayout(float w, float h) {
        width = w;
        height = h;
        joyRadius = h * 0.17f;
        knobRadius = h * 0.075f;
        fireR = h * 0.12f;
        fireX = w - h * 0.20f;
        fireY = h * 0.26f;
        jumpR = h * 0.09f;
        jumpX = w - h * 0.46f;
        jumpY = h * 0.16f;
        reloadR = h * 0.075f;
        reloadX = w - h * 0.12f;
        reloadY = h * 0.55f;
        splitX = w * 0.45f;
    }

    public static boolean inside(float x, float y, float cx, float cy, float r) {
        float dx = x - cx, dy = y - cy;
        return dx * dx + dy * dy <= r * r;
    }
}
