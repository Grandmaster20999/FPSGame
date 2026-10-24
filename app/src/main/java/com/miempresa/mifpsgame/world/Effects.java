// Effects.java
package com.miempresa.mifpsgame.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;

/** Marcas de impacto y destellos (no participan en el raycast del arma). */
public class Effects {

    private static final int MAX_HOLES = 40;

    private final Node effectsNode;
    private final Sphere holeMesh = new Sphere(6, 6, 0.06f);
    private final Sphere puffMesh = new Sphere(6, 6, 0.12f);
    private final Material holeMat, hitMat, sparkMat;
    private final Geometry[] holes = new Geometry[MAX_HOLES];
    private int nextHole = 0;

    public Effects(AssetManager am, Node effectsNode) {
        this.effectsNode = effectsNode;
        holeMat = unshaded(am, new ColorRGBA(0.05f, 0.05f, 0.05f, 1f));
        hitMat = unshaded(am, new ColorRGBA(0.9f, 0.1f, 0.1f, 1f));
        sparkMat = unshaded(am, new ColorRGBA(1f, 0.85f, 0.3f, 1f));
    }

    private static Material unshaded(AssetManager am, ColorRGBA c) {
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", c);
        return m;
    }

    /** Marca de bala en pared/suelo; recicla las mas antiguas. */
    public void spawnBulletHole(Vector3f point, Vector3f normal) {
        Geometry g = holes[nextHole];
        if (g == null) {
            g = new Geometry("BulletHole", holeMesh);
            g.setMaterial(holeMat);
            holes[nextHole] = g;
        }
        g.removeFromParent();
        g.setLocalTranslation(point.add(normal.mult(0.02f)));
        effectsNode.attachChild(g);
        nextHole = (nextHole + 1) % MAX_HOLES;

        spawnTimed(sparkMat, point.add(normal.mult(0.05f)), 0.06f, 0.6f);
    }

    /** Destello de sangre al golpear un enemigo. */
    public void spawnHit(Vector3f point) {
        spawnTimed(hitMat, point, 0.15f, 1.3f);
    }

    private void spawnTimed(Material mat, Vector3f pos, float life, float scale) {
        Geometry g = new Geometry("Puff", puffMesh);
        g.setMaterial(mat);
        g.setLocalTranslation(pos);
        g.setLocalScale(scale);
        g.addControl(new TimedRemove(life));
        effectsNode.attachChild(g);
    }

    private static class TimedRemove extends AbstractControl {
        private float left;
        TimedRemove(float life) { left = life; }

        @Override
        protected void controlUpdate(float tpf) {
            left -= tpf;
            if (left <= 0f) spatial.removeFromParent();
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) { }
    }
}
