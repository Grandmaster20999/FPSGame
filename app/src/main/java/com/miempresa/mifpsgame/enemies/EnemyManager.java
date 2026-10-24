// EnemyManager.java
package com.miempresa.mifpsgame.enemies;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.miempresa.mifpsgame.game.FPSGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Gestiona oleadas de enemigos y puntuacion. */
public class EnemyManager {

    private final FPSGame game;
    private final List<Vector3f> spawnPoints;
    private final List<Node> enemies = new ArrayList<>();
    private final Random rnd = new Random();

    private int wave = 0, kills = 0, alive = 0;
    private float nextWaveTimer = 2f;
    private float bannerTimer = 0f;

    public EnemyManager(FPSGame game, List<Vector3f> spawnPoints) {
        this.game = game;
        this.spawnPoints = spawnPoints;
    }

    public void update(float tpf) {
        if (bannerTimer > 0) bannerTimer -= tpf;
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (enemies.get(i).getParent() == null) enemies.remove(i); // cadaver ya retirado
        }
        if (game.getPlayerControl().isDead()) return;

        if (alive <= 0) {
            nextWaveTimer -= tpf;
            if (nextWaveTimer <= 0) startNextWave();
        }
    }

    private void startNextWave() {
        wave++;
        int count = Math.min(2 + wave, 12);
        float speed = Math.min(3.0f + wave * 0.25f, 6f);
        Vector3f playerPos = game.getPlayerControl().getPosition();

        for (int i = 0; i < count; i++) {
            // elegir un punto de aparicion alejado del jugador
            Vector3f sp = spawnPoints.get(rnd.nextInt(spawnPoints.size()));
            for (int tries = 0; tries < 6 && sp.distance(playerPos) < 15f; tries++) {
                sp = spawnPoints.get(rnd.nextInt(spawnPoints.size()));
            }
            Vector3f p = sp.add((rnd.nextFloat() - 0.5f) * 3f, 0f, (rnd.nextFloat() - 0.5f) * 3f);
            Node enemy = EnemyControl.create(game, p, speed, rnd);
            game.getWorldNode().attachChild(enemy);
            enemies.add(enemy);
            alive++;
        }
        bannerTimer = 2.5f;
        nextWaveTimer = 3f;
    }

    /** Llamado por EnemyControl al morir. */
    public void onEnemyKilled() {
        alive--;
        kills++;
        if (alive <= 0) {
            // recompensa por limpiar la oleada
            game.getPlayerControl().heal(25f);
            game.getPlayerControl().getWeapon().addReserve(30);
        }
    }

    public void reset() {
        for (Node n : enemies) {
            EnemyControl ec = n.getControl(EnemyControl.class);
            if (ec != null) ec.dispose(); else n.removeFromParent();
        }
        enemies.clear();
        wave = 0;
        kills = 0;
        alive = 0;
        nextWaveTimer = 2f;
        bannerTimer = 0f;
    }

    public int getWave() { return wave; }
    public int getKills() { return kills; }
    public int getAlive() { return alive; }
    public float getBannerTimer() { return bannerTimer; }
}
