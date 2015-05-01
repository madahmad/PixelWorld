package com.mygdx.pixelworld.data.utilities;


import com.mygdx.pixelworld.data.draw.DrawHitValue;
import com.mygdx.pixelworld.data.entities.Entity;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stats for every entity.
 */
public class EntityStats {

    private final Map<StatType, Float> stats = new EnumMap<>(StatType.class);
    private Class type;
    private boolean alive = true;
    private Map<StatType, Float> maxStats;
    private boolean visible = true;

    /**
     * @param health    Health
     * @param mana      Mana
     * @param speed     Movement speed
     * @param dexterity Fire rate
     * @param wisdom    Mana regen rate
     * @param vitality  Health regen rate
     * @param attack    Attack
     * @param defense   Defense
     */
    public EntityStats(float health, float mana, int speed, int dexterity, int wisdom, int vitality, int attack, int defense) {
        stats.put(StatType.HEALTH, health);
        stats.put(StatType.MANA, mana);
        stats.put(StatType.SPD, (float) speed);
        stats.put(StatType.DEX, (float) dexterity);
        stats.put(StatType.WIS, (float) wisdom);
        stats.put(StatType.VIT, (float) vitality);
        stats.put(StatType.ATK, (float) attack);
        stats.put(StatType.DEF, (float) defense);
        maxStats = new EnumMap<>(stats);
        stats.get(StatType.HEALTH);
        maxStats.get(StatType.HEALTH);
        type = null;
    }

    private EntityStats(EntityStats ps) {
        for (StatType st : StatType.values()) stats.put(st, ps.get(st));
        type = null;
    }

    public EntityStats(Class<? extends Entity> entity) {
        this(Config.getStats(entity.getSimpleName()));
        maxStats = new EnumMap<>(stats);
        this.type = entity;
    }

    private void setStat(StatType statType, float value) {
        stats.put(statType, value);
    }
    public void addStat(StatType statType, float value) {
        setStat(statType, value + get(statType));
    }
    public float get(StatType statType) {
        return stats.get(statType);
    }
    public float getInit(StatType statType) {
        return Config.getStats(type.getSimpleName()).get(statType);
    }
    public void setAsInit(StatType statType) {
        setStat(statType, getInit(statType));
    }
    public void getHit(Entity e, int damage) {
        if (damage > get(StatType.DEF)) addStat(StatType.HEALTH, get(StatType.DEF) - damage);
        if (get(StatType.HEALTH) <= 0) alive = false;
        DrawHitValue.add(e, damage);
    }
    public boolean isAlive() {
        return alive;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setIsVisible(boolean isVisible) {
        this.visible = isVisible;
    }
}
