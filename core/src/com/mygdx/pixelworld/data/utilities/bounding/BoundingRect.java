package com.mygdx.pixelworld.data.utilities.bounding;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.pixelworld.data.World;

public class BoundingRect extends BoundingShape {
    private Rectangle rectangle;

    public BoundingRect(Vector2 pos, Vector2 dimensions) {
        rectangle = new Rectangle((int) pos.x, (int) pos.y, (int) dimensions.x, (int) dimensions.y);
    }

    public BoundingRect(Rectangle rectangle) {
        this.rectangle = new Rectangle(rectangle);
    }

    public Rectangle get() {
        return rectangle;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(rectangle.x - World.getCameraOffset().x, rectangle.y - World.getCameraOffset().y, rectangle.width, rectangle.height);
    }

    public void drawOnScreen(ShapeRenderer shapeRenderer, Vector2 offset) {
        shapeRenderer.rect(rectangle.x - offset.x, rectangle.y - offset.y, rectangle.width, rectangle.height);
    }

    @Override
    public void update(Vector2 pos) {
        rectangle.x = pos.x;
        rectangle.y = pos.y;
    }

    @Override
    public Vector2 getPos() {
        return new Vector2(rectangle.x, rectangle.y);
    }

    @Override
    public boolean isValid() {
        return rectangle.width > 0 && rectangle.height > 0;
    }

    @Override
    public String toString() {
        return String.format("X = %.2f, Y = %.2f, W = %.2f, H = %.2f", rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}