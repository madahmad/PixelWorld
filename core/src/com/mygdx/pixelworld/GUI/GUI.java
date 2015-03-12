package com.mygdx.pixelworld.GUI;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.pixelworld.data.assets.AssetType;
import com.mygdx.pixelworld.data.assets.Assets;
import com.mygdx.pixelworld.data.classes.Player;
import com.mygdx.pixelworld.data.draw.BoundingCircle;
import com.mygdx.pixelworld.data.items.Chest;
import com.mygdx.pixelworld.data.items.Item;
import com.mygdx.pixelworld.data.utilities.Constants;

public class GUI {

    private static final Vector2[] itemPositions = new Vector2[20];
    private final static int LEFT_BORDER = 20;
    private final static int ITEMS_SIZE = 20;
    private final static int SLOT_SIZE = 30;
    private static final Vector2 mousePosition = new Vector2(0, 0);
    private static SpriteBatch batch;
    private static Player player;
    private static boolean[] isSelected = new boolean[20];
    private static Chest currentChest = null;
    private static Vector2 mouseCatchOffset = new Vector2(0, 0);
    private static int selectedPreviousPosition;


    public static void init(SpriteBatch batch, Player player) {
        GUI.batch = batch;
        GUI.player = player;
        for (int i = 0; i < 20; i++) {
            if (i < 4)//Equipped
                itemPositions[i] = new Vector2(Constants.gameWidth + LEFT_BORDER + SLOT_SIZE * i, 220);
            else if (i < 12) //Inventory
                itemPositions[i] = new Vector2(Constants.gameWidth + LEFT_BORDER + SLOT_SIZE * (i % 4), 170 - SLOT_SIZE * ((i - 4) / 4));
            else //eventual chest
                itemPositions[i] = new Vector2(Constants.gameWidth + LEFT_BORDER + SLOT_SIZE * (i % 4), 120 - SLOT_SIZE * ((i - 4) / 4));
        }
        clearSelected();
    }

    public static void draw() {
        batch.draw(Assets.getTexture(AssetType.PANEL, GUI.class), Constants.gameWidth, 0);
        drawEquipped();
        drawInventory();
        if (currentChest != null) drawChest();
    }


    public static void updateChest(Chest c) {
        currentChest = c;
    }

    private static void drawEquipped() {
        if (!isSelected[0]) player.getWeapon().getImg().drawEffective(batch, itemPositions[0]);
        else player.getWeapon().getImg().drawEffective(batch, new Vector2(mousePosition).add(mouseCatchOffset));

        if (!isSelected[1]) player.getManaSigil().getImg().drawEffective(batch, itemPositions[1]);
        else player.getManaSigil().getImg().drawEffective(batch, new Vector2(mousePosition).add(mouseCatchOffset));

        if (!isSelected[2]) player.getArmor().getImg().drawEffective(batch, itemPositions[2]);
        else player.getArmor().getImg().drawEffective(batch, new Vector2(mousePosition).add(mouseCatchOffset));
    }

    private static void drawInventory() {
        Item[] inv = player.getInventory();
        for (int i = 0; i < inv.length; i++) {
            if (!isSelected[i + 4]) inv[i].getImg().drawEffective(batch, itemPositions[i + 4]);
            else inv[i].getImg().drawEffective(batch, new Vector2(mousePosition).add(mouseCatchOffset));
        }
    }

    private static void drawChest() {
        Item[] inv = currentChest.getInventory();
        for (int i = 0; i < inv.length; i++) {
            if (!isSelected[i + 12]) inv[i].getImg().drawEffective(batch, itemPositions[i + 12]);
            else inv[i].getImg().drawEffective(batch, new Vector2(mousePosition).add(mouseCatchOffset));
        }
    }

    public static void clickDown(int screenX, int screenY) {
        clearSelected();
        updateMousePosition(screenX, screenY);
        for (int i = 0; i < 20; i++) {
            Vector2 center = new Vector2(itemPositions[i].x + ITEMS_SIZE / 2, itemPositions[i].y + ITEMS_SIZE / 2);
            if (new BoundingCircle(center, ITEMS_SIZE / 2).intersect(new BoundingCircle(new Vector2(screenX, screenY), 1))) {
                isSelected[i] = true;
                selectedPreviousPosition = i;
                mouseCatchOffset = new Vector2(itemPositions[i].x - screenX, itemPositions[i].y - screenY);
                System.out.println("Selected slot " + i);
                return;
            }
        }
    }

    public static void updateMousePosition(int screenX, int screenY) {
        mousePosition.x = screenX;
        mousePosition.y = screenY;
    }

    public static void clickUp(int screenX, int screenY) {
        for (int i = 0; i < 20; i++) {
            Vector2 center = new Vector2(itemPositions[i].x + ITEMS_SIZE / 2, itemPositions[i].y + ITEMS_SIZE / 2);
            if (!new BoundingCircle(center, ITEMS_SIZE / 2).intersect(new BoundingCircle(new Vector2(screenX, screenY), 1)))
                continue;
            System.out.println("Click released at slot " + i + ", previous slot was " + selectedPreviousPosition);
            if (i < 4) {//Equipped
                if (selectedPreviousPosition < 4) continue;
                else if (selectedPreviousPosition < 12) player.tryToEquip(selectedPreviousPosition - 4);
                else player.tryToEquip(currentChest, selectedPreviousPosition, i);
            } else if (i < 12) //Inventory
            {
                if (selectedPreviousPosition < 4) player.placeInInventory(selectedPreviousPosition, i - 4);
                else if (selectedPreviousPosition < 12) player.swapInventory(selectedPreviousPosition - 4, i - 4);
                else player.placeInInventory(currentChest, selectedPreviousPosition - 12, i - 4);
            } else //eventual chest
            {
                if (selectedPreviousPosition < 4) player.placeInChest(currentChest, selectedPreviousPosition, i - 12);
                else if (selectedPreviousPosition < 12)
                    player.placeInvInChest(currentChest, selectedPreviousPosition - 4, i - 12);
                else currentChest.swapInventory(selectedPreviousPosition - 12, i - 12);
            }

            clearSelected();
        }
        clearSelected();
    }

    private static void clearSelected() {
        for (int i = 0; i < 20; i++) {
            isSelected[i] = false;
        }
    }


}
