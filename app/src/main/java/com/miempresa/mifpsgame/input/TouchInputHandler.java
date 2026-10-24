// TouchInputHandler.java
package com.miempresa.mifpsgame.input;

import com.jme3.input.controls.TouchListener;
import com.jme3.input.event.TouchEvent;
import com.miempresa.mifpsgame.game.FPSGame;
import com.miempresa.mifpsgame.player.FPSCamera;
import com.miempresa.mifpsgame.player.PlayerControl;
import com.miempresa.mifpsgame.ui.TouchLayout;

/**
 * Controles tactiles multitouch (seccion 7 del manual):
 * joystick flotante a la izquierda, mirar arrastrando a la derecha,
 * y botones DISPARAR / SALTAR / RECARGAR.
 */
public class TouchInputHandler implements TouchListener {

    private final FPSGame game;
    private final TouchLayout layout;

    private static final int NONE = -1;
    private int moveTouchId = NONE, lookTouchId = NONE;
    private int fireTouchId = NONE, jumpTouchId = NONE, reloadTouchId = NONE;

    // Estado del joystick (lo lee el HUD para dibujarlo)
    private boolean joyActive;
    private float joystickCenterX, joystickCenterY, joyKnobX, joyKnobY;

    private float prevLookX, prevLookY;
    /** Radianes por pixel; el manual recomienda 0.002 - 0.005. */
    private float lookSensitivity = 0.0035f;
    private boolean invertY = false;

    public TouchInputHandler(FPSGame game, TouchLayout layout) {
        this.game = game;
        this.layout = layout;
    }

    @Override
    public void onTouch(String name, TouchEvent evt, float tpf) {
        switch (evt.getType()) {
            case DOWN:
                handleTouchDown(evt);
                break;
            case MOVE:
                handleTouchMove(evt);
                break;
            case UP:
                handleTouchUp(evt);
                break;
            default:
                break;
        }
        evt.setConsumed();
    }

    private void handleTouchDown(TouchEvent evt) {
        if (game.isGameOver()) {
            game.requestRestart();
            return;
        }
        PlayerControl player = game.getPlayerControl();
        if (player == null) return;
        float x = evt.getX(), y = evt.getY();
        int id = evt.getPointerId();

        if (fireTouchId == NONE && TouchLayout.inside(x, y, layout.fireX, layout.fireY, layout.fireR * 1.15f)) {
            fireTouchId = id;
            player.getWeapon().setFiring(true);
        } else if (jumpTouchId == NONE && TouchLayout.inside(x, y, layout.jumpX, layout.jumpY, layout.jumpR * 1.15f)) {
            jumpTouchId = id;
            player.getCamera().requestJump();
        } else if (reloadTouchId == NONE && TouchLayout.inside(x, y, layout.reloadX, layout.reloadY, layout.reloadR * 1.2f)) {
            reloadTouchId = id;
            player.getWeapon().reload();
        } else if (x < layout.splitX) {
            // Mitad izquierda: movimiento (joystick flotante)
            if (moveTouchId == NONE) {
                moveTouchId = id;
                joystickCenterX = x;
                joystickCenterY = y;
                joyKnobX = x;
                joyKnobY = y;
                joyActive = true;
            }
        } else if (lookTouchId == NONE) {
            // Mitad derecha: mirar
            lookTouchId = id;
            prevLookX = x;
            prevLookY = y;
        }
    }

    private void handleTouchMove(TouchEvent evt) {
        PlayerControl player = game.getPlayerControl();
        if (player == null || player.isDead()) return;
        FPSCamera cam = player.getCamera();
        int id = evt.getPointerId();

        if (id == moveTouchId) {
            float dx = evt.getX() - joystickCenterX;
            float dy = evt.getY() - joystickCenterY;
            cam.setMoveDirection(dx, dy, layout.joyRadius);
            // Posicion visual del pomo, limitada al radio
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            float k = len > layout.joyRadius ? layout.joyRadius / len : 1f;
            joyKnobX = joystickCenterX + dx * k;
            joyKnobY = joystickCenterY + dy * k;
        } else if (id == lookTouchId) {
            float dx = evt.getX() - prevLookX;
            float dy = evt.getY() - prevLookY;
            // Arrastrar a la derecha => mirar a la derecha (yaw negativo);
            // arrastrar hacia arriba => mirar arriba (pitch negativo).
            cam.rotateCamera(-dx * lookSensitivity, (invertY ? dy : -dy) * lookSensitivity);
            prevLookX = evt.getX();
            prevLookY = evt.getY();
        }
    }

    private void handleTouchUp(TouchEvent evt) {
        PlayerControl player = game.getPlayerControl();
        int id = evt.getPointerId();

        if (id == moveTouchId) {
            moveTouchId = NONE;
            joyActive = false;
            if (player != null) player.getCamera().stopMoving();
        } else if (id == lookTouchId) {
            lookTouchId = NONE;
        } else if (id == fireTouchId) {
            fireTouchId = NONE;
            if (player != null) player.getWeapon().setFiring(false);
        } else if (id == jumpTouchId) {
            jumpTouchId = NONE;
        } else if (id == reloadTouchId) {
            reloadTouchId = NONE;
        }
    }

    /** Libera todos los toques (al reiniciar la partida). */
    public void resetTouches() {
        moveTouchId = lookTouchId = fireTouchId = jumpTouchId = reloadTouchId = NONE;
        joyActive = false;
    }

    public boolean isJoyActive() { return joyActive; }
    public float getJoystickCenterX() { return joystickCenterX; }
    public float getJoystickCenterY() { return joystickCenterY; }
    public float getJoyKnobX() { return joyKnobX; }
    public float getJoyKnobY() { return joyKnobY; }
    public boolean isFireDown() { return fireTouchId != NONE; }
}
