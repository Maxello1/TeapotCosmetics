package net.sidetopia.cosmetics.jeweling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.sidetopia.cosmetics.TeapotCosmetics;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JewelingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "teapot_jeweling.json";
    private static volatile JewelingConfig instance = new JewelingConfig();

    public boolean jewelingEnabled = true;
    public String gemTableBlockId = "teapot_cosmetics:gem_table";
    public String jewelTag = "teapot_cosmetics:jewels";
    public String jewelableTag = "teapot_cosmetics:jewelable";

    public boolean requireKnowledgeBound = false;
    public boolean useKnowledgeBoundProgression = true;
    public boolean grantKnowledgeBoundXp = true;
    public boolean bypassKnowledgeBoundForOperators = false;

    public int fallbackMaxSockets = 3;
    public boolean checkEnchantmentCompatibility = true;
    public boolean closeOpenTablesOnReload = true;

    public String successSound = "minecraft:block.enchantment_table.use";
    public float successSoundVolume = 1.0F;
    public float successSoundPitch = 1.0F;
    public Messages messages = new Messages();

    private transient Identifier parsedGemTableBlockId = Identifier.of(
            "teapot_cosmetics",
            "gem_table"
    );
    private transient Identifier parsedJewelTag = Identifier.of(
            "teapot_cosmetics",
            "jewels"
    );
    private transient Identifier parsedJewelableTag = Identifier.of(
            "teapot_cosmetics",
            "jewelable"
    );
    private transient Identifier parsedSuccessSound = Identifier.of(
            "minecraft",
            "block.enchantment_table.use"
    );

    public static JewelingConfig get() {
        return instance;
    }

    public static synchronized void load() {
        Path path = configPath();
        JewelingConfig loaded = new JewelingConfig();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JewelingConfig decoded = GSON.fromJson(reader, JewelingConfig.class);
                if (decoded != null) loaded = decoded;
            } catch (Exception exception) {
                TeapotCosmetics.LOGGER.error(
                        "Could not read {}; using safe defaults",
                        path,
                        exception
                );
            }
        }
        loaded.normalize();
        instance = loaded;
        save(path, loaded);
    }

    public Identifier gemTableBlockId() {
        return parsedGemTableBlockId;
    }

    public Identifier jewelTagId() {
        return parsedJewelTag;
    }

    public Identifier jewelableTagId() {
        return parsedJewelableTag;
    }

    public Identifier successSoundId() {
        return parsedSuccessSound;
    }

    void normalize() {
        gemTableBlockId = validIdentifier(
                gemTableBlockId,
                "teapot_cosmetics:gem_table"
        );
        jewelTag = validIdentifier(jewelTag, "teapot_cosmetics:jewels");
        jewelableTag = validIdentifier(jewelableTag, "teapot_cosmetics:jewelable");
        successSound = validIdentifier(
                successSound,
                "minecraft:block.enchantment_table.use"
        );

        parsedGemTableBlockId = Identifier.of(gemTableBlockId);
        parsedJewelTag = Identifier.of(jewelTag);
        parsedJewelableTag = Identifier.of(jewelableTag);
        parsedSuccessSound = Identifier.of(successSound);

        fallbackMaxSockets = Math.clamp(fallbackMaxSockets, 0, 64);
        if (!Float.isFinite(successSoundVolume) || successSoundVolume < 0.0F) {
            successSoundVolume = 1.0F;
        } else {
            successSoundVolume = Math.min(successSoundVolume, 4.0F);
        }
        if (!Float.isFinite(successSoundPitch) || successSoundPitch <= 0.0F) {
            successSoundPitch = 1.0F;
        } else {
            successSoundPitch = Math.min(successSoundPitch, 2.0F);
        }

        if (messages == null) messages = new Messages();
        messages.normalize();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static String validIdentifier(String value, String fallback) {
        if (value == null || Identifier.tryParse(value) == null) return fallback;
        return value;
    }

    private static void save(Path path, JewelingConfig config) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            TeapotCosmetics.LOGGER.error("Could not write {}", path, exception);
        }
    }

    public static final class Messages {
        public String featureDisabled = "Jeweling is currently disabled.";
        public String invalidJewel = "That item is not a valid jewel.";
        public String noRecipe = "This jewel has no configured effect.";
        public String noRecipesLoaded =
                "No Jeweling recipes are loaded. Ask an administrator to check the datapack.";
        public String invalidEquipment = "This item cannot be jeweled.";
        public String unsupportedEquipment = "This jewel cannot be applied to that item.";
        public String ambiguousRecipe = "This jewel has conflicting recipes. Ask an administrator.";
        public String missingEnchantment = "The enchantment for this jewel is missing.";
        public String invalidLevel = "This jewel recipe has an invalid enchantment level.";
        public String duplicateEnchantment = "This item already contains that jewel effect.";
        public String incompatibleEnchantment = "This jewel conflicts with an existing enchantment.";
        public String tierTooLow = "You need Jeweller Tier {minTier}.";
        public String socketLimit = "Your Jeweller tier allows only {maxSockets} jewel sockets.";
        public String knowledgeBoundRequired = "KnowledgeBound is required to use this table.";
        public String systemDisabled = "KnowledgeBound Jeweller progression is disabled.";
        public String success = "You successfully set the jewel into the item.";
        public String dataReloaded = "Jeweling data was reloaded. The table was closed.";
        public String inventoryFull = "Your inventory is full.";

        private void normalize() {
            Messages defaults = new Messages();
            featureDisabled = value(featureDisabled, defaults.featureDisabled);
            invalidJewel = value(invalidJewel, defaults.invalidJewel);
            noRecipe = value(noRecipe, defaults.noRecipe);
            noRecipesLoaded = value(noRecipesLoaded, defaults.noRecipesLoaded);
            invalidEquipment = value(invalidEquipment, defaults.invalidEquipment);
            unsupportedEquipment = value(unsupportedEquipment, defaults.unsupportedEquipment);
            ambiguousRecipe = value(ambiguousRecipe, defaults.ambiguousRecipe);
            missingEnchantment = value(missingEnchantment, defaults.missingEnchantment);
            invalidLevel = value(invalidLevel, defaults.invalidLevel);
            duplicateEnchantment = value(duplicateEnchantment, defaults.duplicateEnchantment);
            incompatibleEnchantment = value(incompatibleEnchantment, defaults.incompatibleEnchantment);
            tierTooLow = value(tierTooLow, defaults.tierTooLow);
            socketLimit = value(socketLimit, defaults.socketLimit);
            knowledgeBoundRequired = value(knowledgeBoundRequired, defaults.knowledgeBoundRequired);
            systemDisabled = value(systemDisabled, defaults.systemDisabled);
            success = value(success, defaults.success);
            dataReloaded = value(dataReloaded, defaults.dataReloaded);
            inventoryFull = value(inventoryFull, defaults.inventoryFull);
        }

        private static String value(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }
    }
}
