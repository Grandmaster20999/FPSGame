// PlayerControl.java
package com.miempresa.mifpsgame.player;

import com.jme3.math.Vector3f;
import com.miempresa.mifpsgame.game.FPSGame;

/** Controlador del jugador: camara, arma, salud (seccion 6 / 8 del manual). */
public class PlayerControl {

    public static final float MAX_HEALTH = 100f;
    private static final Vector3f SPAWN = new Vector3f(0f, 1.2f, 0f);

    private final FPSGame game;
    private FPSCamera fpsCamera;
    private WeaponControl weapon;

    private float health = MAX_HEALTH;
    private boolean dead;
    private float damageFlash;
    private final Vector3f tmp = new Vector3f();

    public PlayerControl(FPSGame game) {
        this.game = game;
    }

    public void initialize() {
        fpsCamera = new FPSCamera(game, game.getBulletAppState());
        fpsCamera.initialize(SPAWN.clone());
        weapon = new WeaponControl(game);
        weapon.initialize();
    }

    public void update(float tpf) {
        if (damageFlash > 0) damageFlash = Math.max(0f, damageFlash - tpf * 1.8f);
        if (dead) {
            fpsCamera.stopMoving();
            weapon.setFiring(false);
        }
        fpsCamera.update(tpf);
        weapon.update(tpf);
    }

    public void damage(float amount) {
        if (dead) return;
        health -= amount;
        damageFlash = 1f;
        game.getAudio().playHurt();
        if (health <= 0) {
            health = 0;
            dead = true;
        }
    }

    public void heal(float amount) {
        if (!dead) health = Math.min(MAX_HEALTH, health + amount);
    }

    public void reset() {
        health = MAX_HEALTH;
        dead = false;
        damageFlash = 0;
        weapon.reset();
        fpsCamera.teleport(SPAWN.clone());
    }

    public Vector3f getPosition() { return fpsCamera.getEyePosition(tmp); }
    public FPSCamera getCamera() { return fpsCamera; }
    public WeaponControl getWeapon() { return weapon; }
    public float getHealth() { return health; }
    public boolean isDead() { return dead; }
    public float getDamageFlash() { return damageFlash; }
}
