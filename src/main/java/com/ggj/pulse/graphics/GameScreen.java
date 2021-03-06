package com.ggj.pulse.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.ggj.pulse.ApplicationContainer;
import com.ggj.pulse.entities.AbstractEntity;
import com.ggj.pulse.entities.PlayerEntity;
import com.ggj.pulse.utils.AssetManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Modris Vekmanis
 */
public class GameScreen extends AbstractScreen {
    private Sprite endGame;
    private ApplicationContainer applicationContainer;
    private AssetManager assetManager;
    private World world;
    private Camera camera;
    private SpriteBatch batch;
    private Box2DDebugRenderer box2DDebugRenderer;
    private List<AbstractEntity> visibleEntities = new ArrayList<>();
    private long timeAlive;
    private long previousUpdate;

    private Comparator<AbstractEntity> comparator = new Comparator<AbstractEntity>() {
        @Override
        public int compare(AbstractEntity o1, AbstractEntity o2) {
            if (o1.getRenderOrder() == o2.getRenderOrder()) {
                return 0;
            } else if (o1.getRenderOrder() > o2.getRenderOrder()) {
                return -1;
            }
            return 1;
        }
    };

    public GameScreen() {

        camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        box2DDebugRenderer = new Box2DDebugRenderer();
        camera.translate(50, 50, 200);
        camera.lookAt(50, 50, 0);
        camera.near = 200f;
        camera.far = 201f;
    }

    public void initialize() {
        world = (World) applicationContainer.get("physicsWorld");
        applicationContainer.put("gameCam", camera);
        batch = new SpriteBatch();
        previousUpdate = System.currentTimeMillis();
        endGame = new Sprite(assetManager.get(AssetManager.GAME_OVER, Texture.class));
    }


    private static Vector3 coords = new Vector3();

    @Override
    public void render(float delta) {
        PlayerEntity player = (PlayerEntity) applicationContainer.get("player");

        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        if (player != null) {

            camera.update();
            //  box2DDebugRenderer.render(world, camera.combined);

            coords.set(1, 1, 0);
            camera.project(coords);
            float x1 = coords.x;
            float y1 = coords.y;

            coords.set(2, 2, 0);
            camera.project(coords);
            float x2 = coords.x;
            float y2 = coords.y;

            float scaleX = x2 - x1;
            float scaleY = y2 - y1;

            Collections.sort(visibleEntities, comparator);

            batch.begin();
            for (AbstractEntity entity : visibleEntities) {
                entity.render(batch, camera, assetManager, scaleX, scaleY);

            }
            BitmapFont font = new BitmapFont();
            font.scale(1);
            font.draw(batch, "Time alive: " + TimeUnit.MILLISECONDS.toSeconds(timeAlive), 50, Gdx.graphics.getHeight() - 20);

            if (player.isDead()) {
                endGame.setSize(400, 200);
                endGame.setPosition(Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() / 2);
                endGame.draw(batch);
                font = new BitmapFont();
                font.scale(1);
                String text = "Survived for : " + TimeUnit.MILLISECONDS.toSeconds(timeAlive) + " seconds!!";
                font.draw(batch, text, Gdx.graphics.getWidth() / 2 - font.getBounds(text).width / 2, Gdx.graphics.getHeight() / 2);

                font = new BitmapFont();
                font.scale(2);
                text = "Press space for new game!";
                font.draw(batch, "Press space for new game!", Gdx.graphics.getWidth() / 2 - font.getBounds(text).width / 2, Gdx.graphics.getHeight() / 2 - 100);


            } else {
                timeAlive += applicationContainer.getCurrTime() - previousUpdate;
                previousUpdate = applicationContainer.getCurrTime();
            }

            batch.end();
        }

    }

    public ApplicationContainer getApplicationContainer() {
        return applicationContainer;
    }

    public void setApplicationContainer(ApplicationContainer applicationContainer) {
        this.applicationContainer = applicationContainer;
    }

    public List<AbstractEntity> getVisibleEntities() {
        return visibleEntities;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
}
