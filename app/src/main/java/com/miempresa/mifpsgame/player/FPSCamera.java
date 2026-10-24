// FPSCamera.java
package com.miempresa.mifpsgame.player;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/** Camara en primera persona sobre un CharacterControl (seccion 6 del manual). */
public class FPSCamera {

    /** Altura de los ojos respecto al centro de la capsula (capsula total = 1.8 m). */
    private static final float EYE_OFFSET = 0.7f;
    private static final float MAX_PITCH = 1.4f; // ~80 grados

    private final SimpleApplication app;
    private final BulletAppState bulletAppState;
    private CharacterControl player;

    private final Vector3f walkDirection = new Vector3f();
    private final Vector3f forward = new Vector3f();
    private final Vector3f left = new Vector3f();
    private final Vector3f tmp = new Vector3f();
    private final Quaternion rot = new Quaternion();

    private float yaw = 0f, pitch = 0f;
    /** Entrada de movimiento: x = derecha(+), y = adelante(+), rango -1..1. */
    private float moveX, moveY;
    private boolean jumpRequested;

    private float playerSpeed = 7f;   // m/s
    private float jumpSpeed = 9f;

    public FPSCamera(SimpleApplication app, BulletAppState bullet) {
        this.app = app;
        this.bulletAppState = bullet;
    }

    public void initialize(Vector3f spawn) {
        // Capsula de colision del jugador: radio 0.4 + cilindro 1.0 = 1.8 m
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(0.4f, 1.0f, 1);
        player = new CharacterControl(capsule, 0.1f);
        player.setJumpSpeed(jumpSpeed);
        player.setFallSpeed(30f);
        player.setGravity(30f);
        player.setPhysicsLocation(spawn);
        bulletAppState.getPhysicsSpace().add(player);
        applyCamera();
    }

    /** Joystick: desplazamiento (dx, dy) en pixeles, radio maximo en pixeles. */
    public void setMoveDirection(float dx, float dy, float radius) {
        float nx = dx / radius, ny = dy / radius;
        float len = FastMath.sqrt(nx * nx + ny * ny);
        if (len > 1f) { nx /= len; ny /= len; len = 1f; }
        if (len < 0.12f) { nx = 0; ny = 0; } // zona muerta
        moveX = nx;
        moveY = ny;
    }

    public void stopMoving() { moveX = 0; moveY = 0; }

    /** Suma incrementos (radianes) a la orientacion. +yaw = gira a la izquierda; +pitch = mira abajo. */
    public void rotateCamera(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch = FastMath.clamp(pitch + deltaPitch, -MAX_PITCH, MAX_PITCH);
    }

    public void requestJump() { jumpRequested = true; }

    public void update(float tpf) {
        // Direcciones horizontales a partir del yaw
        forward.set(FastMath.sin(yaw), 0, FastMath.cos(yaw));
        left.set(FastMath.cos(yaw), 0, -FastMath.sin(yaw));

        walkDirection.set(0, 0, 0);
        walkDirection.addLocal(tmp.set(forward).multLocal(moveY));
        walkDirection.addLocal(tmp.set(left).multLocal(-moveX));
        // walkDirection es el desplazamiento por paso de fisica (1/60 s)
        walkDirection.multLocal(playerSpeed / 60f);
        player.setWalkDirection(walkDirection);

        if (jumpRequested) {
            if (player.onGround()) player.jump();
            jumpRequested = false;
        }
        applyCamera();
    }

    private void applyCamera() {
        Camera cam = app.getCamera();
        cam.setLocation(getEyePosition(tmp));
        cam.setRotation(rot.fromAngles(pitch, yaw, 0));
    }

    public Vector3f getPosition() { return player.getPhysicsLocation(); }

    public Vector3f getEyePosition(Vector3f store) {
        store.set(player.getPhysicsLocation());
        store.y += EYE_OFFSET;
        return store;
    }

    public void teleport(Vector3f p) {
        player.setPhysicsLocation(p);
        yaw = 0;
        pitch = 0;
        stopMoving();
    }
}
