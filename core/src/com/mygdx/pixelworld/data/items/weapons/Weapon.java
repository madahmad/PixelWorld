package com.mygdx.pixelworld.data.items.weapons;

import com.mygdx.pixelworld.data.assets.AssetType;
import com.mygdx.pixelworld.data.draw.StaticDrawData;
import com.mygdx.pixelworld.data.entities.characters.GameClasses;
import com.mygdx.pixelworld.data.items.EquipItem;
import com.mygdx.pixelworld.data.items.Item;
import com.mygdx.pixelworld.data.utilities.XMLLoader;

public class Weapon extends Item implements EquipItem {
    private final PlayerWeaponStats weaponStats;
    private final boolean isEmpty;

    public Weapon(GameClasses playerClass, int rank) {
        weaponStats = XMLLoader.retrieveWeapon(playerClass, rank);
        img = new StaticDrawData(AssetType.WEAPON, weaponStats != null ? weaponStats.getName() : null);
        isEmpty = false;
    }

    public Weapon() {
        img = new StaticDrawData();
        weaponStats = null;
        isEmpty = true;
    }

    public Weapon(GameClasses playerClass) {
        this(playerClass, 1);
    }

    public WeaponStats getStats() {
        return weaponStats;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public GameClasses getGameClass() {
        return weaponStats.getType();
    }
}
