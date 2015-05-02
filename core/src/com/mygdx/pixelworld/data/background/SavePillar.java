package com.mygdx.pixelworld.data.background;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.pixelworld.GUI.Logger;
import com.mygdx.pixelworld.data.World;
import com.mygdx.pixelworld.data.draw.AnimationDrawData;
import com.mygdx.pixelworld.data.entities.characters.Player;
import com.mygdx.pixelworld.data.items.Item;
import com.mygdx.pixelworld.data.utilities.bounding.BoundingCircle;
import com.mygdx.pixelworld.data.utilities.bounding.BoundingRect;
import com.mygdx.pixelworld.data.utilities.bounding.BoundingShape;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SavePillar extends Item {

    private final Vector2 pos;
    private final AnimationDrawData aura;
    private Vector2 auraPos;
    private BoundingCircle interactionBounding;

    public SavePillar(Rectangle position) {
        pos = new Vector2(position.x, position.y);
        img = new AnimationDrawData("core/assets/background/savePillar/", new String[]{"pillar"}, 8, 8, BoundingRect.class);
        aura = new AnimationDrawData("core/assets/background/savePillar/", new String[]{"aura"}, 8, 8, BoundingCircle.class);
        interactionBounding = new BoundingCircle(new Vector2(pos).add(20, 0), 80);
    }

    public static void save(Player player) {
        try (PrintWriter writer = new PrintWriter("core/assets/saves/save.save", "UTF-8")) {
            writer.println(World.getCurrentMap());
            writer.print(player.getSaveParameters());
        } catch (FileNotFoundException e) {
            Logger.log("SavePillar.save()", "Saves file not found!");
            return;
        } catch (UnsupportedEncodingException e) {
            Logger.log("SavePillar.save()", "Encoding not supported!");
            return;
        }
        System.out.println("Saved!");
    }

    public BoundingShape getBoundingShape() {
        return interactionBounding;
    }

    public void update() {
        if (auraPos == null) init();
        img.update();
        aura.update();
    }

    private void init() {
        auraPos = new Vector2(pos.x - aura.getWidth() / 2 + img.getWidth() / 2 - 5, pos.y - aura.getHeight() / 2 + 6);
    }

    public void draw(SpriteBatch batch) {
        aura.draw(batch, auraPos);
        img.draw(batch, pos);
    }

    @Override
    public String toString() {
        return "";
    }
}
