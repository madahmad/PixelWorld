package com.mygdx.pixelworld.data.enemies;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.pixelworld.Game;

import java.util.Random;

public class Blocker extends Enemy {

    private float ATK_DISTANCE = 200.0f;
    private float SPEED = 80f;

    public Blocker(float x, float y) {
        super(x, y);
    }

    @Override
    protected void AI(Vector2 playerPos) {
        if (playerPos.dst(pos) < ATK_DISTANCE) {
            Vector2 pos2 = new Vector2(playerPos.x - pos.x, playerPos.y - pos.y);
            pos2.nor();
            pos.add(pos2.x * Game.deltaTime * SPEED, pos2.y * Game.deltaTime * SPEED);
        } else {
            Random rand = new Random();
            float x = rand.nextFloat();
            float y = rand.nextFloat();

            if (rand.nextInt(10) >= 5) x = -x;
            if (rand.nextInt(10) >= 5) y = -y;
            pos.add(x * 5, y * 5);
        }
    }
}