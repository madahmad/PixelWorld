package com.mygdx.pixelworld.data.draw;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.pixelworld.data.assets.AssetType;

public class StaticDrawData extends DrawData {

    public StaticDrawData() {
        //EmptyItem
        setScaleFactor(new Vector2(1, 1));
        setRotationAngle(0);
        texture = new TextureRegion(new Texture("core/assets/placeholder.png"));
    }

    public StaticDrawData(String name, float bulletDirection) {
        //Bullet
        setScaleFactor(new Vector2(1, 1));
        setRotationAngle(bulletDirection);
        texture = new TextureRegion(new Texture("core/assets/Bullets/" + name + ".png"));
    }

    public StaticDrawData(AssetType assetType, String name) {
        //Weapon // Armor //Sigil
        setScaleFactor(new Vector2(1, 1));
        setRotationAngle(0);
        texture = new TextureRegion(new Texture("core/assets/" + assetType.toString().toLowerCase() + "/" + name + ".png"));

    }

    public StaticDrawData(AssetType assetType) {
        //Chest
        setScaleFactor(new Vector2(1, 1));
        setRotationAngle(0);
        texture = new TextureRegion(new Texture("core/assets/" + assetType.toString().toLowerCase() + ".png"));

    }

    @Override
    public void draw(SpriteBatch batch, Vector2 absolutePosition) {
        if (!isVisible(absolutePosition)) return;
        batch.draw(texture, getOriginalPosition(absolutePosition).x, getOriginalPosition(absolutePosition).y, getOriginCenter().x,
                getOriginCenter().y, getOriginalWidth(), getOriginalHeight(), scaleFactor.x, scaleFactor.y, rotationAngle);
    }

    @Override
    public void update() {
    }
}
