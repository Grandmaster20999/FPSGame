// GameAudio.java
package com.miempresa.mifpsgame.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agrupa los sonidos del juego. Todo esta protegido con try/catch:
 * si un archivo de audio falla, el juego sigue funcionando sin sonido.
 */
public class GameAudio {

    private static final Logger LOG = Logger.getLogger(GameAudio.class.getName());

    private AudioNode music, shoot, hit, hurt, reload;

    public GameAudio(AssetManager assets, Node attachTo) {
        music = load(assets, attachTo, "Audio/background_music.wav", true);
        shoot = load(assets, attachTo, "Audio/gunshot.wav", false);
        hit = load(assets, attachTo, "Audio/enemy_hit.wav", false);
        hurt = load(assets, attachTo, "Audio/player_hurt.wav", false);
        reload = load(assets, attachTo, "Audio/reload.wav", false);
        if (shoot != null) shoot.setVolume(0.8f);
    }

    private AudioNode load(AssetManager assets, Node parent, String path, boolean ambient) {
        try {
            AudioNode n = ambient ? AudioManager.createAmbientSound(assets, path)
                                  : AudioManager.createSFX(assets, path, false);
            parent.attachChild(n);
            return n;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo cargar el audio " + path, e);
            return null;
        }
    }

    public void startMusic() { play(music, false); }
    public void playShoot()  { play(shoot, true); }
    public void playHit()    { play(hit, true); }
    public void playHurt()   { play(hurt, true); }
    public void playReload() { play(reload, true); }

    private void play(AudioNode n, boolean instance) {
        if (n == null) return;
        try {
            if (instance) n.playInstance(); else n.play();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error reproduciendo audio", e);
        }
    }
}
