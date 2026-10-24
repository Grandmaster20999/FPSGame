// FPSGame.java
package com.miempresa.mifpsgame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.miempresa.mifpsgame.audio.GameAudio;
import com.miempresa.mifpsgame.enemies.EnemyManager;
import com.miempresa.mifpsgame.input.TouchInputHandler;
import com.miempresa.mifpsgame.player.PlayerControl;
import com.miempresa.mifpsgame.ui.HUDState;
import com.miempresa.mifpsgame.ui.TouchLayout;
import com.miempresa.mifpsgame.world.Effects;
import com.miempresa.mifpsgame.world.SceneLoader;

/** Logica principal del juego (seccion 4.2 del manual). */
public class FPSGame extends SimpleApplication {

    private static final boolean SHOW_FPS = false;

    private BulletAppState bulletAppState;
    private PlayerControl playerControl;
    private SceneLoader sceneLoader;
    private EnemyManager enemyManager;
    private TouchInputHandler touchHandler;
    private TouchLayout touchLayout;
    private HUDState hud;
    private GameAudio audio;
    private Effects effects;

    /** Nodo con todo lo "disparable" (escenario + enemigos). */
    private Node worldNode;
    /** Marcas de impacto y destellos (no reciben raycast). */
    private Node effectsNode;

    private boolean restartRequested;

    /** AndroidHarness necesita un constructor sin argumentos. */
    public FPSGame() {
        // Sin FlyCam ni teclas de depuracion: solo estadisticas y audio
        super(new StatsAppState(), new AudioListenerState());
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(SHOW_FPS);
        setDisplayStatView(false);

        viewPort.setBackgroundColor(new ColorRGBA(0.53f, 0.75f, 0.95f, 1f));
        cam.setFrustumPerspective(70f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 150f);

        // Inicializar motor de fisicas (seccion 8.1)
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -30f, 0));
        bulletAppState.getPhysicsSpace().setAccuracy(1f / 60f);
        // bulletAppState.setDebugEnabled(true);   // solo en desarrollo

        worldNode = new Node("World");
        effectsNode = new Node("Effects");
        rootNode.attachChild(worldNode);
        rootNode.attachChild(effectsNode);

        setupLighting();

        // Cargar escenario
        sceneLoader = new SceneLoader(this, bulletAppState, worldNode);
        sceneLoader.loadScene();

        effects = new Effects(assetManager, effectsNode);
        audio = new GameAudio(assetManager, rootNode);
        audio.startMusic();

        // Jugador con camara FPS y arma
        playerControl = new PlayerControl(this);
        playerControl.initialize();

        enemyManager = new EnemyManager(this, sceneLoader.getSpawnPoints());

        // HUD y controles tactiles
        touchLayout = new TouchLayout(cam.getWidth(), cam.getHeight());
        touchHandler = new TouchInputHandler(this, touchLayout);
        inputManager.addMapping("Touch", new TouchTrigger(TouchInput.ALL));
        inputManager.addListener(touchHandler, "Touch");

        hud = new HUDState(this, touchLayout);
        stateManager.attach(hud);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (restartRequested) {
            restartRequested = false;
            doRestart();
        }
        playerControl.update(tpf);
        enemyManager.update(tpf);
    }

    private void setupLighting() {
        // Luz ambiental
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.45f));
        rootNode.addLight(ambient);

        // Luz direccional (sol)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.9f));
        rootNode.addLight(sun);
    }

    // ---- API usada por el resto de clases ----

    public void damagePlayer(float amount) { playerControl.damage(amount); }

    public boolean isGameOver() { return playerControl != null && playerControl.isDead(); }

    /** Se aplica al inicio del siguiente frame (hilo de render). */
    public void requestRestart() { restartRequested = true; }

    private void doRestart() {
        enemyManager.reset();
        playerControl.reset();
        touchHandler.resetTouches();
    }

    public BulletAppState getBulletAppState() { return bulletAppState; }
    public PlayerControl getPlayerControl() { return playerControl; }
    public EnemyManager getEnemyManager() { return enemyManager; }
    public TouchInputHandler getTouchHandler() { return touchHandler; }
    public GameAudio getAudio() { return audio; }
    public Effects getEffects() { return effects; }
    public Node getWorldNode() { return worldNode; }
}
