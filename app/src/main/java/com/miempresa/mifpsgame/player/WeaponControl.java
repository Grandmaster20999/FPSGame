// WeaponControl.java
package com.miempresa.mifpsgame.player;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.miempresa.mifpsgame.enemies.EnemyControl;
import com.miempresa.mifpsgame.game.FPSGame;
import com.miempresa.mifpsgame.world.SceneLoader;

/** Arma: disparo por raycast, municion, recarga y modelo en pantalla (seccion 8.2). */
public class WeaponControl {

    private final FPSGame game;
    private final Camera cam;

    private final int magSize = 30;
    private int ammo = magSize, reserve = 90;
    private final float weaponDamage = 34f;
    private final float fireInterval = 0.14f;
    private final float reloadTime = 1.6f;
    private final float range = 100f;

    private float cooldown, reloadTimer, recoil, muzzleTimer;
    private boolean firing, reloading;

    private Node gunNode;
    private Geometry muzzleFlash;

    private final Vector3f tmpPos = new Vector3f();
    private final Vector3f tmpLeft = new Vector3f();
    private final Vector3f tmpUp = new Vector3f();

    public WeaponControl(FPSGame game) {
        this.game = game;
        this.cam = game.getCamera();
    }

    public void initialize() {
        gunNode = new Node("Gun");
        gunNode.attachChild(box("Body", 0.03f, 0.045f, 0.16f, 0f, 0f, 0.10f, new ColorRGBA(0.15f, 0.15f, 0.17f, 1f)));
        gunNode.attachChild(box("Barrel", 0.015f, 0.015f, 0.12f, 0f, 0.015f, 0.34f, new ColorRGBA(0.08f, 0.08f, 0.08f, 1f)));
        gunNode.attachChild(box("Grip", 0.025f, 0.07f, 0.03f, 0f, -0.10f, 0.03f, new ColorRGBA(0.3f, 0.2f, 0.1f, 1f)));

        muzzleFlash = box("Muzzle", 0.04f, 0.04f, 0.04f, 0f, 0.015f, 0.48f, new ColorRGBA(1f, 0.85f, 0.2f, 1f));
        muzzleFlash.setCullHint(Spatial.CullHint.Always);
        gunNode.attachChild(muzzleFlash);

        game.getRootNode().attachChild(gunNode); // fuera de worldNode: no recibe disparos
    }

    private Geometry box(String name, float hx, float hy, float hz, float x, float y, float z, ColorRGBA c) {
        Geometry g = new Geometry(name, new Box(hx, hy, hz));
        g.setMaterial(SceneLoader.coloredMaterial(game.getAssetManager(), c));
        g.setLocalTranslation(x, y, z);
        return g;
    }

    public void setFiring(boolean f) { firing = f; }

    public void update(float tpf) {
        if (cooldown > 0) cooldown -= tpf;
        if (muzzleTimer > 0) {
            muzzleTimer -= tpf;
            if (muzzleTimer <= 0) muzzleFlash.setCullHint(Spatial.CullHint.Always);
        }
        recoil = Math.max(0f, recoil - tpf * 6f);

        if (reloading) {
            reloadTimer -= tpf;
            if (reloadTimer <= 0) finishReload();
        } else if (firing && cooldown <= 0) {
            if (ammo > 0) shoot();
            else reload(); // sin balas: recarga automatica
        }
        updateViewModel();
    }

    /** Disparo por raycast desde el centro de la camara. */
    private void shoot() {
        ammo--;
        cooldown = fireInterval;
        recoil = 1f;
        muzzleTimer = 0.05f;
        muzzleFlash.setCullHint(Spatial.CullHint.Never);

        Vector3f origin = cam.getLocation();
        Vector3f direction = cam.getDirection().normalizeLocal();
        Ray ray = new Ray(origin, direction);
        ray.setLimit(range);

        CollisionResults results = new CollisionResults();
        game.getWorldNode().collideWith(ray, results);

        for (int i = 0; i < results.size(); i++) {
            CollisionResult r = results.getCollision(i);
            EnemyControl enemy = findEnemy(r.getGeometry());
            if (enemy != null) {
                if (enemy.isDead()) continue; // los cadaveres no bloquean
                enemy.takeDamage(weaponDamage);
                game.getEffects().spawnHit(r.getContactPoint());
                game.getAudio().playHit();
            } else {
                game.getEffects().spawnBulletHole(r.getContactPoint(), r.getContactNormal());
            }
            break;
        }
        game.getAudio().playShoot();
    }

    private EnemyControl findEnemy(Spatial s) {
        while (s != null) {
            EnemyControl ec = s.getControl(EnemyControl.class);
            if (ec != null) return ec;
            s = s.getParent();
        }
        return null;
    }

    public void reload() {
        if (reloading || ammo >= magSize || reserve <= 0) return;
        reloading = true;
        reloadTimer = reloadTime;
        game.getAudio().playReload();
    }

    private void finishReload() {
        int take = Math.min(magSize - ammo, reserve);
        ammo += take;
        reserve -= take;
        reloading = false;
    }

    public void addReserve(int n) { reserve += n; }

    public void reset() {
        ammo = magSize;
        reserve = 90;
        reloading = false;
        firing = false;
        cooldown = 0;
    }

    /** Coloca el arma abajo a la derecha de la vista, con retroceso. */
    private void updateViewModel() {
        Vector3f dir = cam.getDirection();
        cam.getLeft(tmpLeft);
        cam.getUp(tmpUp);
        tmpPos.set(cam.getLocation())
              .addLocal(dir.multLocal(0.55f - recoil * 0.08f))
              .addLocal(tmpLeft.multLocal(-0.24f))
              .addLocal(tmpUp.multLocal(-0.2f + (reloading ? -0.08f : 0f)));
        gunNode.setLocalTranslation(tmpPos);
        gunNode.setLocalRotation(cam.getRotation());
    }

    public int getAmmo() { return ammo; }
    public int getReserve() { return reserve; }
    public boolean isReloading() { return reloading; }
}
