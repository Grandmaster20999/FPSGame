// EnemyControl.java
package com.miempresa.mifpsgame.enemies;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;
import com.miempresa.mifpsgame.game.FPSGame;
import com.miempresa.mifpsgame.world.SceneLoader;

import java.util.Random;

/** IA basica: PATROL -> CHASE -> ATTACK -> DEAD (seccion 9 del manual). */
public class EnemyControl extends AbstractControl {

    private enum State { PATROL, CHASE, ATTACK, DEAD }

    private final FPSGame game;
    private final CharacterControl physics;
    private final Random rnd;

    private State currentState = State.PATROL;
    private float health = 100f;
    private final float detectionRange = 22f;
    private final float attackRange = 2.0f;
    private final float patrolSpeed = 2f;
    private final float chaseSpeed;
    private final float attackDamage = 10f;
    private float attackCooldown;
    private float deathTimer;

    private final Vector3f pos = new Vector3f();
    private final Vector3f target = new Vector3f();
    private final Vector3f dir = new Vector3f();
    private final Vector3f view = new Vector3f();
    private boolean hasPatrolTarget;

    private EnemyControl(FPSGame game, CharacterControl physics, float chaseSpeed, Random rnd) {
        this.game = game;
        this.physics = physics;
        this.chaseSpeed = chaseSpeed;
        this.rnd = rnd;
    }

    /** Construye un enemigo (nodo con cuerpo, fisica e IA) en la posicion dada. */
    public static Node create(FPSGame game, Vector3f position, float chaseSpeed, Random rnd) {
        Node enemy = new Node("Enemy");
        enemy.attachChild(part(game, "Legs", 0.25f, 0.4f, 0.18f, 0f, -0.5f, 0f, new ColorRGBA(0.15f, 0.15f, 0.25f, 1f)));
        enemy.attachChild(part(game, "Torso", 0.32f, 0.3f, 0.2f, 0f, 0.2f, 0f, new ColorRGBA(0.75f, 0.12f, 0.12f, 1f)));
        enemy.attachChild(part(game, "Head", 0.2f, 0.2f, 0.2f, 0f, 0.7f, 0f, new ColorRGBA(0.85f, 0.7f, 0.55f, 1f)));
        enemy.attachChild(part(game, "Arms", 0.5f, 0.08f, 0.1f, 0f, 0.3f, 0.2f, new ColorRGBA(0.75f, 0.12f, 0.12f, 1f)));

        CharacterControl character = new CharacterControl(new CapsuleCollisionShape(0.4f, 1.0f, 1), 0.1f);
        character.setGravity(-30f);
        character.setPhysicsLocation(position);

        EnemyControl ai = new EnemyControl(game, character, chaseSpeed, rnd);
        enemy.addControl(character);
        enemy.addControl(ai);
        game.getBulletAppState().getPhysicsSpace().add(character);
        return enemy;
    }

    private static Geometry part(FPSGame g, String n, float hx, float hy, float hz,
                                 float x, float y, float z, ColorRGBA c) {
        Geometry geo = new Geometry(n, new Box(hx, hy, hz));
        geo.setMaterial(SceneLoader.coloredMaterial(g.getAssetManager(), c));
        geo.setLocalTranslation(x, y, z);
        return geo;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (currentState == State.DEAD) {
            deathTimer += tpf;
            if (deathTimer > 2.5f) spatial.removeFromParent();
            return;
        }

        pos.set(physics.getPhysicsLocation());
        Vector3f playerPos = game.getPlayerControl().getPosition();
        float dx = playerPos.x - pos.x, dz = playerPos.z - pos.z;
        float distToPlayer = FastMath.sqrt(dx * dx + dz * dz);
        boolean playerAlive = !game.getPlayerControl().isDead();
        if (attackCooldown > 0) attackCooldown -= tpf;

        switch (currentState) {
            case PATROL:
                if (playerAlive && distToPlayer < detectionRange) currentState = State.CHASE;
                patrol();
                break;
            case CHASE:
                if (!playerAlive) currentState = State.PATROL;
                else if (distToPlayer < attackRange) currentState = State.ATTACK;
                else if (distToPlayer > detectionRange * 1.5f) currentState = State.PATROL;
                chasePlayer(playerPos);
                break;
            case ATTACK:
                if (!playerAlive || distToPlayer > attackRange + 0.4f) currentState = State.CHASE;
                attackPlayer(playerPos, distToPlayer);
                break;
            default:
                break;
        }
    }

    private void patrol() {
        if (!hasPatrolTarget || horizontalDistance(pos, target) < 1.5f) {
            float r = SceneLoader.ARENA_HALF - 4f;
            target.set((rnd.nextFloat() * 2f - 1f) * r, pos.y, (rnd.nextFloat() * 2f - 1f) * r);
            hasPatrolTarget = true;
        }
        moveToward(target, patrolSpeed);
    }

    private void chasePlayer(Vector3f playerPos) {
        moveToward(playerPos, chaseSpeed);
    }

    private void attackPlayer(Vector3f playerPos, float dist) {
        physics.setWalkDirection(Vector3f.ZERO);
        face(playerPos);
        if (attackCooldown <= 0f && dist < attackRange + 0.4f) {
            game.damagePlayer(attackDamage);
            attackCooldown = 0.9f;
        }
    }

    private void moveToward(Vector3f to, float speed) {
        dir.set(to.x - pos.x, 0f, to.z - pos.z);
        float len = dir.length();
        if (len < 0.01f) { physics.setWalkDirection(Vector3f.ZERO); return; }
        dir.divideLocal(len);
        view.set(dir);
        physics.setViewDirection(view);
        dir.multLocal(speed / 60f);   // desplazamiento por paso de fisica
        physics.setWalkDirection(dir);
    }

    private void face(Vector3f to) {
        view.set(to.x - pos.x, 0f, to.z - pos.z);
        if (view.lengthSquared() > 0.0001f) {
            view.normalizeLocal();
            physics.setViewDirection(view);
        }
    }

    private static float horizontalDistance(Vector3f a, Vector3f b) {
        float dx = a.x - b.x, dz = a.z - b.z;
        return FastMath.sqrt(dx * dx + dz * dz);
    }

    public void takeDamage(float amount) {
        if (currentState == State.DEAD) return;
        health -= amount;
        if (currentState == State.PATROL) currentState = State.CHASE; // alerta al recibir un disparo
        if (health <= 0) die();
    }

    private void die() {
        currentState = State.DEAD;
        // Eliminar de fisicas
        PhysicsSpace space = game.getBulletAppState().getPhysicsSpace();
        space.remove(physics);
        spatial.removeControl(physics);
        // "Animacion" de muerte: el cuerpo cae al suelo
        spatial.rotate(-FastMath.HALF_PI, 0f, 0f);
        spatial.move(0f, -0.5f, 0f);
        game.getEnemyManager().onEnemyKilled();
    }

    public boolean isDead() { return currentState == State.DEAD; }

    /** Retira de las fisicas a un enemigo vivo (al reiniciar la partida). */
    public void dispose() {
        if (currentState != State.DEAD) {
            game.getBulletAppState().getPhysicsSpace().remove(physics);
            spatial.removeControl(physics);
        }
        spatial.removeFromParent();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) { }
}
