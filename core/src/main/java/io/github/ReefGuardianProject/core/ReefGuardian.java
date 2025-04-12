package io.github.ReefGuardianProject.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.ReefGuardianProject.objects.Checkpoint;
import io.github.ReefGuardianProject.objects.GameObjects;
import io.github.ReefGuardianProject.objects.LiveCollectible;
import io.github.ReefGuardianProject.objects.RockBlock;
import io.github.ReefGuardianProject.objects.player.Honu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ReefGuardian implements ApplicationListener {
    private OrthographicCamera camera;
    private Sprite sprite;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture texture;
    private Honu honu;
    private ArrayList<GameObjects> gameObjectsList = new ArrayList<GameObjects>();
    private int level = 1;
    private Texture backgroundLevel1;
    /**
     * State of the game: 1. Main menu; 2. Main Game; 3. Next Level; 4. Game Over
     */
    private int gameState = 2;
    @Override
    public void create() {
        camera = new OrthographicCamera();

        viewport = new FitViewport(1280, 1024, camera);
        viewport.apply();

        //camera.position.set(640, 412, 0);
        camera.setToOrtho(false, 1280, 1024);

        batch = new SpriteBatch();

        //Create Honu
        honu = new Honu();
        honu.setPosition(0, 200);

        //Call the load level
        if (level == 1) {
            loadLevel("map\\level1.txt");
        }
        if (level == 2) {
            loadLevel("map\\level2.txt");
        }


    }

    @Override
    public void resize(int width, int height) {
        // Makes sure screen of the game doesn't stretch when the window changes
        viewport.update(width, height);
    }

    @Override
    public void render() {
        switch (this.gameState) {
            case 1:
                this.mainMenu();
                break;
            case 2:
                this.mainGame();
                break;
            case 3:
                this.nextLevel();
                break;
            case 4:
                this.gameOver();
                break;
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public void updateCamera() {
        camera.position.x = honu.getHitBox().x;
        camera.update();;
    }
    //Load the level
    public void loadLevel(String level) {
        //Clear the list before loading the level
        gameObjectsList.clear();
        //Loading level .txt files

        FileHandle file = Gdx.files.internal(level); //"map\\level1.txt"
        StringTokenizer tokens = new StringTokenizer(file.readString());
        while (tokens.hasMoreTokens()) {
            String type = tokens.nextToken();

            //Render the map from txt files:
            if (type.equals("Background_Level1")) {
                // Get background file path
                String bgPath1 = tokens.nextToken();
                if (backgroundLevel1 != null) backgroundLevel1.dispose(); // clean old texture

                backgroundLevel1 = new Texture(Gdx.files.internal(bgPath1));
            }
            if (type.equals("RockBlock")) {
                gameObjectsList.add(new RockBlock(
                    Integer.parseInt(tokens.nextToken()), //x value
                    Integer.parseInt(tokens.nextToken()))); //y value
            }
            if (type.equals("Checkpoint")) {
                gameObjectsList.add(new Checkpoint(
                    Integer.parseInt(tokens.nextToken()), //x value
                    Integer.parseInt(tokens.nextToken()))); //y value
            }
            if (type.equals("Live")) {
                gameObjectsList.add(new LiveCollectible(
                    Integer.parseInt(tokens.nextToken()), //x value
                    Integer.parseInt(tokens.nextToken()))); //y value
            }
            if (type.equals("Coral1")) {
                return;
            }
        }
    }
    public void mainMenu() {

    }
    public void mainGame() {
        //Clear screen before rendering
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        //Begin the rendering
        batch.begin();
        //Update render here

            if (backgroundLevel1 != null) {
                //Scroll the background based on the camera position
                float bgX = camera.position.x - camera.viewportWidth / 2f;
                // match the image size
                batch.draw(backgroundLevel1, bgX, 0, 1280, 1024);
            }
            //Draw Honu
            honu.draw(batch);

            //object
            for (GameObjects o : gameObjectsList) {
                o.draw(batch);
            }
        batch.end();

        //Updates

        //Honu
        honu.update(Gdx.graphics.getDeltaTime());

        //Check for collision
        boolean changeLevel = false; //Changing level check

        for (GameObjects o : gameObjectsList) {
            int honuCollision = honu.hit(o.getHitBox());
            //if (honuCollision != -1 ) {continue;} // No collision

            int collisionType = o.hitAction(); // 1 = normal block, 2 = die, 3 = collectible, 4 = checkpoint

            // Handle object type behavior
            switch (collisionType) {
                case 1:
                    switch (honuCollision) {
                        case 1:
                            //Collide top
                            honu.action(1, 0, o.getHitBox().y + o.getHitBox().height);
                            break;
                        case 2:
                            //Collide right
                            honu.action(2, o.getHitBox().x + o.getHitBox().width + 1, 0);
                            break;
                        case 3:
                            //Collide left
                            honu.action(3, o.getHitBox().x - honu.getHitBox().width - 1, 0);
                            break;
                        case 4:
                            //Collide Bottom
                            honu.action(4, honu.getHitBox().x, o.getHitBox().y - honu.getHitBox().height);
                            break;
                    }
                case 2: // Character dies
                    System.out.println("Honu died!");
                    break;

                case 3: // Collect item
                    break;

                case 4: // Checkpoint
                    level++;
                    changeLevel = true;
                    break;
            }
        }
        updateCamera();

        //Handling Input Controls
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            honu.moveUp(Gdx.graphics.getDeltaTime());    //move Up = W
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            honu.moveLeft(Gdx.graphics.getDeltaTime());    //move left = A
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            honu.moveDown(Gdx.graphics.getDeltaTime());    //move down = S
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            honu.moveRight(Gdx.graphics.getDeltaTime());    //move right = D
        }

    }
    public void nextLevel() {

    }
    public void gameOver() {

    }
    @Override
    public void dispose() {

    }
}
