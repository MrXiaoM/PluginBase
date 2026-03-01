package top.mrxiaom.pluginbase.utils;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据类名获取 Bukkit 的 Registry，用于 <code>Util.valueOr</code>
 * @see Util#valueOr(Class, String, Object)
 */
public class RegistryConverter {
    private static final Map<String, Registry<?>> registries = new HashMap<>();
    @SuppressWarnings({"UnstableApiUsage"})
    protected static void init() {
        try {
            add(Advancement.class, Registry.ADVANCEMENT);
        } catch (Throwable ignored) {}
        try {
            add(Art.class, Registry.ART);
        } catch (Throwable ignored) {}
        try {
            add(Attribute.class, Registry.ATTRIBUTE);
        } catch (Throwable ignored) {}
        try {
            add(PatternType.class, Registry.BANNER_PATTERN);
        } catch (Throwable ignored) {}
        try {
            add(Biome.class, Registry.BIOME);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.block.BlockType", "BLOCK");
        } catch (Throwable ignored) {}
        try {
            add(KeyedBossBar.class, Registry.BOSS_BARS);
        } catch (Throwable ignored) {}
        try {
            add(Cat.Type.class, Registry.CAT_VARIANT);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.entity.Chicken.Variant", "CHICKEN_VARIANT");
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.entity.Cow.Variant", "COW_VARIANT");
        } catch (Throwable ignored) {}
        try {
            add(DamageType.class, Registry.DAMAGE_TYPE);
        } catch (Throwable ignored) {}
        try {
            // Paper
            add("org.bukkit.potion.PotionEffectType", "POTION_EFFECT_TYPE");
        } catch (Throwable t) {
            try {
                add(PotionEffectType.class, Registry.EFFECT);
            } catch (Throwable ignored) {
            }
        }
        try {
            add(Enchantment.class, Registry.ENCHANTMENT);
        } catch (Throwable ignored) {}
        try {
            add(EntityType.class, Registry.ENTITY_TYPE);
        } catch (Throwable ignored) {}
        try {
            add(Fluid.class, Registry.FLUID);
        } catch (Throwable ignored) {}
        try {
            add(Frog.Variant.class, Registry.FROG_VARIANT);
        } catch (Throwable ignored) {}
        try {
            add(GameEvent.class, Registry.GAME_EVENT);
        } catch (Throwable ignored) {}
        try {
            add(MusicInstrument.class, Registry.INSTRUMENT);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.inventory.ItemType", "ITEM");
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.JukeboxSong", "JUKEBOX_SONG");
        } catch (Throwable ignored) {}
        try {
            add(LootTables.class, Registry.LOOT_TABLES);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.map.MapCursor.Type", "MAP_DECORATION_TYPE");
        } catch (Throwable ignored) {}
        try {
            add(Material.class, Registry.MATERIAL);
        } catch (Throwable ignored) {}
        try {
            add(MemoryKey.class, Registry.MEMORY_MODULE_TYPE);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.inventory.MenuType", "MENU");
        } catch (Throwable ignored) {}
        try {
            add(Particle.class, Registry.PARTICLE_TYPE);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.entity.Pig.Variant", "PIG_VARIANT");
        } catch (Throwable ignored) {}
        try {
            add(PotionType.class, Registry.POTION);
        } catch (Throwable ignored) {}
        try {
            add(Sound.class, Registry.SOUNDS);
        } catch (Throwable ignored) {}
        try {
            add(Statistic.class, Registry.STATISTIC);
        } catch (Throwable ignored) {}
        try {
            add(Structure.class, Registry.STRUCTURE);
        } catch (Throwable ignored) {}
        try {
            add(StructureType.class, Registry.STRUCTURE_TYPE);
        } catch (Throwable ignored) {}
        try {
            add(TrimMaterial.class, Registry.TRIM_MATERIAL);
        } catch (Throwable ignored) {}
        try {
            add(TrimPattern.class, Registry.TRIM_PATTERN);
        } catch (Throwable ignored) {}
        try {
            add(Villager.Profession.class, Registry.VILLAGER_PROFESSION);
        } catch (Throwable ignored) {}
        try {
            add(Villager.Type.class, Registry.VILLAGER_TYPE);
        } catch (Throwable ignored) {}
        try {
            add("org.bukkit.entity.Wolf.Variant", "WOLF_VARIANT");
        } catch (Throwable ignored) {}
    }

    private static void add(String classType, String registry) throws Throwable {
        Class<?> type = Class.forName(classType);
        Field field = Registry.class.getDeclaredField(registry);
        Registry<?> object = (Registry<?>) field.get(null);
        registries.put(type.getName(), object);
    }

    private static <T extends Keyed> void add(Class<T> type, Registry<T> registry) {
        registries.put(type.getName(), registry);
    }

    public static Registry<?> fromType(Class<?> type) {
        return registries.get(type.getName());
    }
}
