// SceneLoader.java
package com.miempresa.mifpsgame.world;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import jme3tools.optimize.GeometryBatchFactory;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carga del escenario (seccion 5 del manual).
 * Si existe Models/Level/nivel1.j3o se usa; si no, se construye una arena
 * procedural con muros y cajas para que el proyecto funcione sin assets externos.
 */
public class SceneLoader {

    private static final Logger LOG = Logger.getLogger(SceneLoader.class.getName());
    public static final float ARENA_HALF = 30f;

    private final SimpleApplication app;
    private final BulletAppState bulletAppState;
    private final Node worldNode;
    private final AssetManager assets;
    private final List<Vector3f> spawnPoints = new ArrayList<>();

    public SceneLoader(SimpleApplication app, BulletAppState bullet, Node worldNode) {
        this.app = app;
        this.bulletAppState = bullet;
        this.worldNode = worldNode;
        this.assets = app.getAssetManager();
    }

    public List<Vector3f> getSpawnPoints() { return spawnPoints; }

    public void loadScene() {
        buildFloor();

        boolean loaded = false;
        try {
            // Cargar modelo del nivel (.j3o) si existe
            Spatial scene = assets.loadModel("Models/Level/nivel1.j3o");
            RigidBodyControl landscape = new RigidBodyControl(0.0f); // masa 0 = estatico
            scene.addControl(landscape);
            worldNode.attachChild(scene);
            bulletAppState.getPhysicsSpace().add(landscape);
            loaded = true;
        } catch (RuntimeException e) {
            LOG.log(Level.INFO, "No hay Models/Level/nivel1.j3o, se usa la arena procedural");
        }
        if (!loaded) buildArena();

        // Puntos de aparicion de enemigos (lejos del centro)
        float s = ARENA_HALF - 5f;
        float y = 1.6f;
        spawnPoints.add(new Vector3f(s, y, s));
        spawnPoints.add(new Vector3f(-s, y, s));
        spawnPoints.add(new Vector3f(s, y, -s));
        spawnPoints.add(new Vector3f(-s, y, -s));
        spawnPoints.add(new Vector3f(0, y, s + 1));
        spawnPoints.add(new Vector3f(0, y, -s - 1));
        spawnPoints.add(new Vector3f(s + 1, y, 0));
        spawnPoints.add(new Vector3f(-s - 1, y, 0));
    }

    /** Suelo de emergencia (plano) con textura repetida. */
    private void buildFloor() {
        float half = ARENA_HALF + 2f;
        Box floorBox = new Box(half, 0.5f, half);
        floorBox.scaleTextureCoordinates(new Vector2f(half / 2f, half / 2f));
        Geometry floor = new Geometry("Floor", floorBox);
        floor.setMaterial(texturedMaterial("Textures/terrain_diffuse.png"));
        floor.setLocalTranslation(0f, -0.5f, 0f); // cara superior en y = 0
        worldNode.attachChild(floor);

        PhysicsRigidBody body = new PhysicsRigidBody(
                new BoxCollisionShape(new Vector3f(half, 0.5f, half)), 0f);
        body.setPhysicsLocation(new Vector3f(0f, -0.5f, 0f));
        physics().add(body);
    }

    private void buildArena() {
        Node level = new Node("Level");
        Material wallMat = texturedMaterial("Textures/wall_diffuse.png");
        Material crateMat = texturedMaterial("Textures/crate_diffuse.png");

        // Muros perimetrales (alto 4 m)
        float h = 2f, t = 1f, a = ARENA_HALF;
        addBox(level, "WallN", new Vector3f(0, h, -a - t), new Vector3f(a + 2 * t, h, t), wallMat);
        addBox(level, "WallS", new Vector3f(0, h, a + t), new Vector3f(a + 2 * t, h, t), wallMat);
        addBox(level, "WallE", new Vector3f(a + t, h, 0), new Vector3f(t, h, a), wallMat);
        addBox(level, "WallW", new Vector3f(-a - t, h, 0), new Vector3f(t, h, a), wallMat);

        // Cajas y pilares como cobertura: {x, z, mitadX, mitadY, mitadZ}
        float[][] crates = {
                {8, 6, 1.5f, 1.5f, 1.5f}, {-9, 8, 1, 1, 1}, {12, -8, 2, 1.25f, 1},
                {-12, -10, 1.5f, 1.5f, 1.5f}, {0, 14, 3, 1, 0.75f}, {-18, 2, 1, 2, 3},
                {18, 4, 1, 2, 3}, {5, -16, 1, 1, 1}, {-6, -18, 2, 1, 1},
                {20, -17, 1.5f, 1.5f, 1.5f}, {-20, 18, 2, 2, 2}, {19, 18, 1.5f, 1, 1.5f},
                {-4, -6, 0.75f, 2, 0.75f}, {4, 20, 0.75f, 2, 0.75f}
        };
        int i = 0;
        for (float[] c : crates) {
            addBox(level, "Crate" + (i++), new Vector3f(c[0], c[3], c[1]),
                    new Vector3f(c[2], c[3], c[4]), crateMat);
        }

        // Optimizacion (seccion 12): unir mallas estaticas con el mismo material
        try {
            GeometryBatchFactory.optimize(level);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo optimizar el nivel", e);
        }
        worldNode.attachChild(level);
    }

    private void addBox(Node parent, String name, Vector3f pos, Vector3f half, Material mat) {
        Box box = new Box(half.x, half.y, half.z);
        float s = Math.max(1f, Math.max(half.x, half.z) / 2f);
        box.scaleTextureCoordinates(new Vector2f(s, Math.max(1f, half.y / 2f)));
        Geometry g = new Geometry(name, box);
        g.setMaterial(mat);
        g.setLocalTranslation(pos);
        parent.attachChild(g);

        PhysicsRigidBody body = new PhysicsRigidBody(new BoxCollisionShape(half.clone()), 0f);
        body.setPhysicsLocation(pos.clone());
        physics().add(body);
    }

    private Material texturedMaterial(String texturePath) {
        Material m = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
        Texture tex = assets.loadTexture(texturePath);
        tex.setWrap(Texture.WrapMode.Repeat);
        m.setTexture("DiffuseMap", tex);
        return m;
    }

    /** Material de color solido con iluminacion (usado por jugador/enemigos). */
    public static Material coloredMaterial(AssetManager am, ColorRGBA color) {
        Material m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", color);
        m.setColor("Ambient", color);
        return m;
    }

    private PhysicsSpace physics() { return bulletAppState.getPhysicsSpace(); }
}
