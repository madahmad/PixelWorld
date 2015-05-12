package com.mygdx.pixelworld.data;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.pixelworld.GUI.Logger;
import com.mygdx.pixelworld.Game;
import com.mygdx.pixelworld.data.background.Chest;
import com.mygdx.pixelworld.data.background.SavePillar;
import com.mygdx.pixelworld.data.draw.Bullet;
import com.mygdx.pixelworld.data.draw.DrawHitValue;
import com.mygdx.pixelworld.data.draw.ScreenWriter;
import com.mygdx.pixelworld.data.entities.characters.Player;
import com.mygdx.pixelworld.data.entities.enemies.Blocker;
import com.mygdx.pixelworld.data.entities.enemies.Enemy;
import com.mygdx.pixelworld.data.items.Item;
import com.mygdx.pixelworld.data.items.weapons.WeaponStats;
import com.mygdx.pixelworld.data.story.Story;
import com.mygdx.pixelworld.data.utilities.Constants;
import com.mygdx.pixelworld.data.utilities.EntityStats;
import com.mygdx.pixelworld.data.utilities.Utils;
import com.mygdx.pixelworld.data.utilities.bounding.BoundingRect;
import com.mygdx.pixelworld.data.utilities.bounding.BoundingShape;
import com.mygdx.pixelworld.data.utilities.bounding.ExitBoundingRect;
import com.mygdx.pixelworld.debug.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * Class for managing map loading / world update and draw.
 */
public class World implements Disposable {

    private static TiledMapRenderer tiledMapRenderer;
    private static TiledMap tiledMap;
    private static String currentMap;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Chest> chests = new ArrayList<>();
    private final List<BoundingRect> mapObstacles = new ArrayList<>();
    private final List<ExitBoundingRect> exits = new ArrayList<>();
    private final List<BoundingRect> spawnPoints = new ArrayList<>();
    private final List<SavePillar> savePillars = new ArrayList<>();
    private final int[] backgroundLayers = {0};
    private final int[] foregroundLayers = {1};
    private final Player player;
    private Story story;

    public World() {
        player = new Player();
        Game.assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        loadNewMap("core/assets/maps/marco/castello.tmx");
        new Blocker(0, 0);
        new SavePillar(new Rectangle());
    }

    /**
     * @return Width of current map.
     */
    public static int getWidth() {
        if (tiledMap == null) return (int) Constants.gameWidth;
        int tileNumber = tiledMap.getProperties().get("width", Integer.class);
        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
        return tileNumber * tileWidth;
    }

    /**
     * @return Height of current map.
     */
    public static int getHeight() {
        if (tiledMap == null) return (int) Constants.gameHeight;
        int tileNumber = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        return tileNumber * tileHeight;
    }

    public static String getCurrentMap() {
        return currentMap;
    }

    /**
     * Loads new map's obstacles, exits, spawn monsters etc.
     *
     * @param mapPath File path of map file
     */
    private void loadNewMap(String mapPath) {
        Logger.log("World.loadNewMap()", "Loading " + mapPath + "...");
        enemies.clear();
        bullets.clear();
        chests.clear();
        mapObstacles.clear();
        exits.clear();
        savePillars.clear();
        spawnPoints.clear();
        story = null;
        if (tiledMap != null) {
            Game.assetManager.unload(currentMap);
            tiledMap.dispose();
        }
        tiledMap = null;
        currentMap = mapPath;
        Game.assetManager.load(currentMap, TiledMap.class);
    }

    /**
     * When map is loaded reads all data and initializes the map.
     */
    private void initMap(SpriteBatch batch) {
        tiledMap = Game.assetManager.get(currentMap);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

        int x = 100;
        int y = 100;
        try {
            x = Integer.parseInt(tiledMap.getProperties().get("PlayerPositionX", String.class)) * tiledMap.getProperties().get("tilewidth", Integer.class);
            y = Integer.parseInt(tiledMap.getProperties().get("PlayerPositionY", String.class)) * tiledMap.getProperties().get("tileheight", Integer.class);
        } catch (NumberFormatException ignored) {
            Logger.log("World.initMap()", "Player position not found.");
        }
        player.setInitialPos(x, y);
        CameraManager.setOffset(player.getPos());

        if (tiledMap.getLayers().get("Collisions") != null) {
            for (MapObject object : tiledMap.getLayers().get("Collisions").getObjects()) {
                if (!(object instanceof RectangleMapObject)) continue;
                mapObstacles.add(new BoundingRect(((RectangleMapObject) object).getRectangle()));
            }
        } else Logger.log("World.initMap()", "Collisions layer not found! Skipping.");

        if (tiledMap.getLayers().get("Exits") != null) {
            for (MapObject object : tiledMap.getLayers().get("Exits").getObjects()) {
                if (!(object instanceof RectangleMapObject)) continue;
                exits.add(new ExitBoundingRect(((RectangleMapObject) object).getRectangle(), (String) object.getProperties().get("NextMap")));
            }
        } else Logger.log("World.initMap()", "Exits layer not found! Skipping.");

        if (tiledMap.getLayers().get("Spawn") != null) {
            for (MapObject object : tiledMap.getLayers().get("Spawn").getObjects()) {
                if (!(object instanceof RectangleMapObject)) continue;
                String type = object.getProperties().get("type", String.class);
                int number = Integer.parseInt(object.getProperties().get("Number", String.class));
                generateEnemies(((RectangleMapObject) object).getRectangle(), type, number);
                spawnPoints.add(new BoundingRect(((RectangleMapObject) object).getRectangle()));
            }
        } else Logger.log("World.initMap()", "Spawn layer not found! Skipping.");

        story = new Story(tiledMap.getLayers().get("NPC"), "core/assets/maps/" + tiledMap.getProperties().get("Story", String.class));

        if (tiledMap.getLayers().get("Interactions") != null) {
            for (MapObject object : tiledMap.getLayers().get("Interactions").getObjects()) {
                if (!(object instanceof RectangleMapObject)) continue;
                switch (object.getProperties().get("type", String.class)) {
                    case "savePillar":
                        savePillars.add(new SavePillar(((RectangleMapObject) object).getRectangle()));
                        mapObstacles.add(new BoundingRect(((RectangleMapObject) object).getRectangle()));
                        break;
                    default:
                        Logger.log("World.initMap()", "Interaction of type " + object.getProperties().get("type") + " not yet implemented.");
                }
            }
        } else Logger.log("World.initMap()", "Interactions layer not found! Skipping.");

        Logger.log("World.initMap()", String.format("Loaded %d layers, %d obstacles, %d exits, %d pillars, %d spawn points (%d total enemies).",
                tiledMap.getLayers().getCount(), mapObstacles.size(), exits.size(), savePillars.size(), spawnPoints.size(), enemies.size()));


    }

    /**
     * Adds an enemy to the map.
     * @param enemyType Type of enemy
     */
    private void addEnemy(String enemyType, float x, float y) {
        Enemy e;
        switch (enemyType.toLowerCase()) {
            case "blocker":
                e = new Blocker(x, y);
                break;
            default:
                e = new Blocker(x, y);
                break;
        }
        enemies.add(e);
    }

    /**
     * Adds chest to current map.
     * @param items Items inside the chest
     * @param pos Position of the chest
     */
    private void addChest(List<Item> items, Vector2 pos) {
        Chest c = new Chest(items, pos);
        for (Chest old : chests)
            if (BoundingShape.intersect(c.getBoundingShape(), old.getBoundingShape())) {
                if (old.move(c)) return;
            }
        chests.add(new Chest(items, pos));
    }

    /**
     * Updates all map component and checks collisions.
     */
    public void update(SpriteBatch batch) {
        player.update(this);
        if (tiledMap == null) initMap(batch);
        BoundingShape playerBoundingShape = player.getBoundingShape();

        for (ExitBoundingRect e : exits)
            if (BoundingShape.intersect(playerBoundingShape, e)) {
                loadNewMap(e.getNextMap());
                return;
            }

        story.update();
        for (SavePillar savePillar : savePillars) savePillar.update();
        updateEnemies();
        updateChests();
        updateBullets(playerBoundingShape);
        playerBoundingShape.free();
        player.checkMana(enemies);
        DrawHitValue.update();
        CameraManager.updateOffset(player.getPos(), player.getStats());
    }

    private void updateBullets(BoundingShape playerBoundingShape) {
        ListIterator<Bullet> bulletIterator = bullets.listIterator();
        ListIterator<Enemy> enemyIterator;
        while (bulletIterator.hasNext()) {
            Bullet b = bulletIterator.next();
            b.update(mapObstacles);
            BoundingShape bulletBoundingShape = b.getBoundingShape();
            if (b.isPlayer()) {
                enemyIterator = enemies.listIterator();
                while (enemyIterator.hasNext()) {
                    Enemy e = enemyIterator.next();
                    BoundingShape enemyBoundingShape = e.getBoundingShape();
                    if (BoundingShape.intersect(enemyBoundingShape, bulletBoundingShape)) {
                        e.getHit(b);
                        b.die();
                    }
                    enemyBoundingShape.free();
                }
            } else {
                if (BoundingShape.intersect(playerBoundingShape, bulletBoundingShape)) {
                    player.getHit(b);
                    b.die();
                }
            }
            bulletBoundingShape.free();
            if (!b.isAlive()) bulletIterator.remove();
        }
    }

    private void updateChests() {
        ListIterator<Chest> chestIterator = chests.listIterator();
        while (chestIterator.hasNext()) {
            Chest c = chestIterator.next();
            BoundingShape boundingShape = c.getBoundingShape();
            if (c.isEmpty()) {
                chestIterator.remove();
            }
            boundingShape.free();
        }
    }

    private void updateEnemies() {
        ListIterator<Enemy> enemyIterator = enemies.listIterator();
        while (enemyIterator.hasNext()) {
            Enemy e = enemyIterator.next();
            e.update(player, this);
            if (!e.isAlive()) {
                player.addExperience(e.getExperience());
                addChest(e.getDropItems(), e.getPos());
                enemyIterator.remove();
            }
        }
    }

    /**
     * Draws map layers before entities
     */
    public void drawBottom(SpriteBatch batch) {
        tiledMapRenderer.render(backgroundLayers);
        batch.begin();
        if (!Game.assetManager.update()) return;
        for (Enemy e : enemies) e.draw(batch);
        for (Bullet b : bullets) b.draw(batch);
        for (Chest c : chests) c.draw(batch);
        player.draw(batch);
        story.draw(batch);
        DrawHitValue.draw(batch);
        for (SavePillar savePillar : savePillars) {
            savePillar.draw(batch);
            if (BoundingShape.intersect(player.getBoundingShape(), savePillar.getBoundingShape()))
                ScreenWriter.write(batch, "Press " + Constants.INTERACTION_KEY + " to save...", 10, 30, Color.GREEN);
        }
    }

    public void cameraUpdate(SpriteBatch batch) {
        CameraManager.update(batch, tiledMapRenderer);
    }

    /**
     * Draws layers after entities
     */
    public void drawTop() {
        tiledMapRenderer.render(foregroundLayers);
    }

    /**
     * Adds a bullet
     * @param startPos Bullet starting position
     * @param targetPos Bullet target
     * @param es Stats of firing entity
     * @param ws Stats of firing weapon
     */
    public void fire(Vector2 startPos, Vector2 targetPos, EntityStats es, WeaponStats ws) {
        bullets.add(new Bullet(startPos, targetPos, es, ws));
    }

    /**
     * Draws shapes for GUI.
     */
    public void shapeDraw(ShapeRenderer shapeRenderer) {
        if (Debug.isTrue("SHOW_BOUNDING")) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            //Player
            shapeRenderer.setColor(1, 0, 1, 1);
            player.getBoundingShape().draw(shapeRenderer);
            //Enemies
            shapeRenderer.setColor(1, 0, 0, 1);
            for (Enemy e : enemies) e.getBoundingShape().draw(shapeRenderer);
            //Bullets
            shapeRenderer.setColor(0, 0, 1, 1);
            for (Bullet b : bullets) b.getBoundingShape().draw(shapeRenderer);
            //Chests
            shapeRenderer.setColor(0, 1, 1, 1);
            for (Chest c : chests) c.getBoundingShape().draw(shapeRenderer);
            //Obstacles
            shapeRenderer.setColor(1, 1, 0, 1);
            for (BoundingRect rect : mapObstacles) rect.drawOnScreen(shapeRenderer, CameraManager.getCameraOffset());
            //Exits
            shapeRenderer.setColor(0, 1, 0, 1);
            for (ExitBoundingRect rect : exits) rect.drawOnScreen(shapeRenderer, CameraManager.getCameraOffset());
            //SpawnPoints
            shapeRenderer.setColor(0.5f, 1, 0, 1);
            for (BoundingRect rect : spawnPoints) rect.drawOnScreen(shapeRenderer, CameraManager.getCameraOffset());
            //SavePillars
            shapeRenderer.setColor(1f, 0.5f, 0, 1);
            for (SavePillar savePillar : savePillars)
                savePillar.getBoundingShape().drawOnScreen(shapeRenderer, CameraManager.getCameraOffset());

        }

        if (Debug.isTrue("SHOW_OFFSET_TRIGGERS")) {
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.line(0, Constants.Y_LIMIT_MAX, Constants.gameWidth,Constants.Y_LIMIT_MAX);
            shapeRenderer.line(0, Constants.Y_LIMIT_MIN, Constants.gameWidth,Constants.Y_LIMIT_MIN);
            shapeRenderer.line(Constants.X_LIMIT_MAX, 0, Constants.X_LIMIT_MAX, Constants.gameHeight);
            shapeRenderer.line(Constants.X_LIMIT_MIN, 0, Constants.X_LIMIT_MIN, Constants.gameHeight);
        }

        shapeRenderer.end();
    }

    /**
     * Generate enemies at map loading.
     *
     * @param spawnArea   Spawn area
     * @param type        Type of enemy
     * @param enemyNumber Number of enemies
     */
    private void generateEnemies(Rectangle spawnArea, String type, int enemyNumber) {
        Random random = new Random();
        for (int i = 0; i < enemyNumber; i++) {
            int x = (int) (random.nextInt((int) (spawnArea.width)) + spawnArea.x);
            int y = (int) (random.nextInt((int) (spawnArea.height)) + spawnArea.y);
            addEnemy(type, x, y);
        }
    }

    @Override
    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        for (Enemy e : enemies) e.dispose();
        for (Bullet b : bullets) b.dispose();
        for (Chest c : chests) c.dispose();
        player.dispose();
    }

    public List<BoundingRect> getMapObstacles() {
        return mapObstacles;
    }

    public void interaction() {
        for (SavePillar savePillar : savePillars)
            if (BoundingShape.intersect(player.getBoundingShape(), savePillar.getBoundingShape()))
                SavePillar.save(player);
    }

    public void load() {
        String[] saves = Utils.readSave();
        if (saves == null) {
            Logger.log("World.load()", "Save file is null.");
            return;
        }
        if (saves[0] == null || saves[0].equals("")) {
            Logger.log("World.load()", "Save file corrupted! Map is not valid.");
            return;
        }
        loadNewMap(saves[0]);
        player.load();
    }

    public void manaTrigger() {
        player.manaTrigger();
    }

    public void playerStartFire(int targetX, int targetY) {
        player.getFireManager().setIsFiring(true);
        playerUpdateTarget(targetX, targetY);
    }

    public void playerStopFire() {
        player.getFireManager().setIsFiring(false);
    }

    public void playerUpdateTarget(int targetX, int targetY) {
        Vector2 offset = CameraManager.getCameraOffset();
        if (offset == null) return;
        player.getFireManager().setTarget(targetX + offset.x, targetY + offset.y);
    }
}