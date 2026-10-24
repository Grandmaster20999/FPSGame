// AudioManager.java
package com.miempresa.mifpsgame.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;

/** Fabricas de AudioNode (seccion 11 del manual). */
public class AudioManager {

    /** Musica / ambiente: streaming, en bucle, no posicional. */
    public static AudioNode createAmbientSound(AssetManager assets, String path) {
        AudioNode audio = new AudioNode(assets, path, AudioData.DataType.Stream);
        audio.setLooping(true);
        audio.setVolume(0.35f);
        audio.setPositional(false);
        return audio;
    }

    /** Efecto corto: en memoria (Buffer). */
    public static AudioNode createSFX(AssetManager assets, String path, boolean positional) {
        AudioNode audio = new AudioNode(assets, path, AudioData.DataType.Buffer);
        audio.setLooping(false);
        audio.setVolume(1.0f);
        audio.setPositional(positional);
        if (positional) {
            audio.setReverbEnabled(false);
            audio.setMaxDistance(50f);
            audio.setRefDistance(5f);
        }
        return audio;
    }
}
