// HUDState.java
package com.miempresa.mifpsgame.ui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import com.miempresa.mifpsgame.enemies.EnemyManager;
import com.miempresa.mifpsgame.game.FPSGame;
import com.miempresa.mifpsgame.input.TouchInputHandler;
import com.miempresa.mifpsgame.player.PlayerControl;
import com.miempresa.mifpsgame.player.WeaponControl;

/**
 * HUD (seccion 10 del manual): salud, municion, oleada, mira, controles tactiles
 * y destello rojo al recibir dano. Lee el estado del juego en cada frame.
 */
public class HUDState extends BaseAppState {

    private final FPSGame game;
    private final TouchLayout layout;
    private final Node hudNode = new Node("HUD");

    private BitmapText healthText, ammoText, waveText, centerText;
    private Geometry healthBarBg, healthBar, damageFlash;
    private Picture crosshair, joyBase, joyKnob, fireBtn, jumpBtn, reloadBtn;
    private Material flashMat;
    private float barWidth, barHeight;

    private int lastHp = -1, lastAmmo = -1, lastReserve = -1, lastWave = -1, lastKills = -1;
    private boolean lastReloading;
    private String lastCenter = null;

    public HUDState(FPSGame game, TouchLayout layout) {
        this.game = game;
        this.layout = layout;
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication sa = (SimpleApplication) app;
        AssetManager am = sa.getAssetManager();
        float W = layout.width, H = layout.height;
        float pad = H * 0.03f;

        BitmapFont font = am.loadFont("Interface/Fonts/Default.fnt");
        float fontSize = H * 0.05f;

        // Destello rojo de dano (pantalla completa, debajo de todo)
        flashMat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        flashMat.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0f));
        flashMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        damageFlash = new Geometry("DamageFlash", new Quad(W, H));
        damageFlash.setMaterial(flashMat);
        damageFlash.setLocalTranslation(0, 0, 0);
        hudNode.attachChild(damageFlash);

        // Texto de salud + barra
        healthText = text(font, fontSize, ColorRGBA.White);
        healthText.setText("HP: 100");
        healthText.setLocalTranslation(pad, H - pad, 1);
        hudNode.attachChild(healthText);

        barWidth = W * 0.22f;
        barHeight = H * 0.035f;
        float barY = H - pad - fontSize - barHeight * 0.6f;
        healthBarBg = quad(am, barWidth, barHeight, new ColorRGBA(0f, 0f, 0f, 0.55f));
        healthBarBg.setLocalTranslation(pad, barY, 1);
        hudNode.attachChild(healthBarBg);
        healthBar = quad(am, barWidth, barHeight, new ColorRGBA(0.2f, 0.85f, 0.25f, 1f));
        healthBar.setLocalTranslation(pad, barY, 2);
        hudNode.attachChild(healthBar);

        // Municion (arriba a la derecha)
        ammoText = text(font, fontSize, ColorRGBA.White);
        ammoText.setText("30 / 90");
        ammoText.setLocalTranslation(W - pad - fontSize * 6f, H - pad, 1);
        hudNode.attachChild(ammoText);

        // Oleada / bajas (arriba al centro)
        waveText = text(font, fontSize * 0.8f, ColorRGBA.Yellow);
        waveText.setText("Oleada 0   Bajas 0");
        waveText.setLocalTranslation(W * 0.5f - fontSize * 4f, H - pad, 1);
        hudNode.attachChild(waveText);

        // Mensaje central
        centerText = text(font, fontSize * 1.4f, ColorRGBA.White);
        centerText.setBox(new Rectangle(0, 0, W, H * 0.4f));
        centerText.setAlignment(BitmapFont.Align.Center);
        centerText.setLocalTranslation(0, H * 0.72f, 3);
        centerText.setText("");
        hudNode.attachChild(centerText);

        // Mira central
        float cs = H * 0.07f;
        crosshair = picture(am, "HUD/Crosshair", "Interface/crosshair.png", cs, cs);
        crosshair.setPosition(W / 2f - cs / 2f, H / 2f - cs / 2f);
        hudNode.attachChild(crosshair);

        // Controles tactiles
        joyBase = picture(am, "HUD/JoyBase", "Interface/joy_base.png", layout.joyRadius * 2, layout.joyRadius * 2);
        joyKnob = picture(am, "HUD/JoyKnob", "Interface/joy_knob.png", layout.knobRadius * 2, layout.knobRadius * 2);
        joyBase.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        joyKnob.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        hudNode.attachChild(joyBase);
        hudNode.attachChild(joyKnob);

        fireBtn = button(am, "HUD/Fire", "Interface/btn_fire.png", layout.fireX, layout.fireY, layout.fireR);
        jumpBtn = button(am, "HUD/Jump", "Interface/btn_jump.png", layout.jumpX, layout.jumpY, layout.jumpR);
        reloadBtn = button(am, "HUD/Reload", "Interface/btn_reload.png", layout.reloadX, layout.reloadY, layout.reloadR);
    }

    private BitmapText text(BitmapFont font, float size, ColorRGBA color) {
        BitmapText t = new BitmapText(font, false);
        t.setSize(size);
        t.setColor(color);
        return t;
    }

    private Geometry quad(AssetManager am, float w, float h, ColorRGBA color) {
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        Geometry g = new Geometry("Quad", new Quad(w, h));
        g.setMaterial(m);
        return g;
    }

    private Picture picture(AssetManager am, String name, String path, float w, float h) {
        Picture p = new Picture(name);
        p.setImage(am, path, true);
        p.setWidth(w);
        p.setHeight(h);
        return p;
    }

    private Picture button(AssetManager am, String name, String path, float cx, float cy, float r) {
        Picture p = picture(am, name, path, r * 2, r * 2);
        p.setPosition(cx - r, cy - r);
        hudNode.attachChild(p);
        return p;
    }

    @Override
    public void update(float tpf) {
        PlayerControl player = game.getPlayerControl();
        EnemyManager em = game.getEnemyManager();
        TouchInputHandler touch = game.getTouchHandler();
        if (player == null || em == null || touch == null) return;
        WeaponControl weapon = player.getWeapon();

        // Salud
        int hp = Math.round(player.getHealth());
        if (hp != lastHp) {
            lastHp = hp;
            healthText.setText("HP: " + hp);
            float ratio = Math.max(0.0001f, hp / PlayerControl.MAX_HEALTH);
            healthBar.setLocalScale(ratio, 1, 1);
            healthBar.getMaterial().setColor("Color", ratio > 0.5f ? new ColorRGBA(0.2f, 0.85f, 0.25f, 1f)
                    : ratio > 0.25f ? new ColorRGBA(0.95f, 0.75f, 0.1f, 1f) : new ColorRGBA(0.9f, 0.15f, 0.15f, 1f));
        }

        // Municion
        int ammo = weapon.getAmmo(), reserve = weapon.getReserve();
        boolean reloading = weapon.isReloading();
        if (ammo != lastAmmo || reserve != lastReserve || reloading != lastReloading) {
            lastAmmo = ammo;
            lastReserve = reserve;
            lastReloading = reloading;
            ammoText.setText(reloading ? "Recargando..." : ammo + " / " + reserve);
        }

        // Oleada / bajas
        if (em.getWave() != lastWave || em.getKills() != lastKills) {
            lastWave = em.getWave();
            lastKills = em.getKills();
            waveText.setText("Oleada " + lastWave + "   Bajas " + lastKills);
        }

        // Mensaje central
        String center = "";
        if (player.isDead()) {
            center = "GAME OVER\nOleada " + em.getWave() + " - Bajas " + em.getKills() + "\nToca para reiniciar";
        } else if (em.getBannerTimer() > 0) {
            center = "OLEADA " + em.getWave();
        }
        if (!center.equals(lastCenter)) {
            lastCenter = center;
            centerText.setText(center);
        }

        // Destello de dano
        flashMat.setColor("Color", new ColorRGBA(1f, 0f, 0f, player.getDamageFlash() * 0.45f
                + (player.isDead() ? 0.25f : 0f)));

        // Joystick flotante
        if (touch.isJoyActive()) {
            joyBase.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
            joyKnob.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
            joyBase.setPosition(touch.getJoystickCenterX() - layout.joyRadius,
                                touch.getJoystickCenterY() - layout.joyRadius);
            joyKnob.setPosition(touch.getJoyKnobX() - layout.knobRadius,
                                touch.getJoyKnobY() - layout.knobRadius);
        } else {
            joyBase.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            joyKnob.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }
    }

    @Override
    protected void cleanup(Application app) { }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(hudNode);
    }

    @Override
    protected void onDisable() {
        hudNode.removeFromParent();
    }
}
