package fr.rader.oldworldmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class MoreWorldOptionsComponent {

    private static final Text SEED_TEXT = Text.translatable("selectWorld.enterSeed");
    private static final Text GENERATE_STRUCTURES_TEXT = Text.translatable("selectWorld.mapFeatures");
    private static final Text GENERATE_STRUCTURES_INFO_TEXT = Text.translatable("selectWorld.mapFeatures.info");
    private static final Text WORLD_TYPE_TEXT = Text.translatable("selectWorld.mapType");
    private static final Text BONUS_CHEST_TEXT = Text.translatable("selectWorld.bonusItems");
    private static final Text CUSTOMIZE_TEXT = Text.translatable("selectWorld.customizeType");
    private static final Text IMPORT_SETTINGS_TEXT = Text.translatable("selectWorld.import_worldgen_settings");
    private static final Text AMPLIFIED_HELP_TEXT = Text.translatable("generator.minecraft.amplified.info");

    private static final int GRAY_COLOR = ColorHelper.Argb.getArgb(0xFF, 0xA0, 0xA0, 0xA0);

    private MultilineText amplifiedWorldInfo;

    private TextFieldWidget seedField;

    private CyclingButtonWidget<Boolean> generateStructuresButton;

    private CyclingButtonWidget<WorldCreator.WorldType> worldTypeButton;
    private ButtonWidget customizeWorldButton;

    private CyclingButtonWidget<Boolean> bonusChestButton;

    private ButtonWidget importSettingsButton;

    private WorldCreator worldCreator;
    private TextRenderer textRenderer;
    private int width;

    public MoreWorldOptionsComponent() {
        this.amplifiedWorldInfo = MultilineText.EMPTY;
    }

    public List<ClickableWidget> init(CreateWorldScreen createWorldScreen, MinecraftClient client, TextRenderer textRenderer) {
        this.worldCreator = createWorldScreen.getWorldCreator();
        this.textRenderer = textRenderer;
        this.width = createWorldScreen.width;

        List<ClickableWidget> elements = new ArrayList<>();

        this.seedField = new TextFieldWidget(textRenderer, this.width / 2 - 100, 60, 200, 20, SEED_TEXT);
        this.seedField.setText(this.worldCreator.getSeed());

        int i = this.width / 2 - 155;
        int j = this.width / 2 + 5;

        this.generateStructuresButton = CyclingButtonWidget.onOffBuilder(this.worldCreator.shouldGenerateStructures())
                .build(i, 100, 150, 20, GENERATE_STRUCTURES_TEXT, (button, shouldGenerateStructures) -> {
                    this.worldCreator.setGenerateStructures(shouldGenerateStructures);
                });

        this.worldTypeButton = CyclingButtonWidget.builder(WorldCreator.WorldType::getName)
                .values(getWorldTypes())
                .initially(this.worldCreator.getWorldType())
                .build(j, 100, 150, 20, WORLD_TYPE_TEXT, (button, worldType) -> {
                    this.worldCreator.setWorldType(worldType);
                });

        this.amplifiedWorldInfo = MultilineText.create(textRenderer, AMPLIFIED_HELP_TEXT, this.worldTypeButton.getWidth());

        this.customizeWorldButton = ButtonWidget.builder(CUSTOMIZE_TEXT, (button) -> {
                    LevelScreenProvider levelScreenProvider = this.worldCreator.getLevelScreenProvider();
                    if (levelScreenProvider != null) {
                        MinecraftClient.getInstance().setScreen(levelScreenProvider.createEditScreen(createWorldScreen, this.worldCreator.getGeneratorOptionsHolder()));
                    }
                })
                .dimensions(j, 120, 150, 20)
                .build();

        this.bonusChestButton = CyclingButtonWidget.onOffBuilder(this.worldCreator.isBonusChestEnabled())
                .build(i, 151, 150, 20, BONUS_CHEST_TEXT, (button, bonusChestEnabled) -> {
                    this.worldCreator.setBonusChestEnabled(bonusChestEnabled);
                });

        this.importSettingsButton = ButtonWidget.builder(IMPORT_SETTINGS_TEXT, (button) -> {
                    LevelScreenProvider levelScreenProvider = this.worldCreator.getLevelScreenProvider();
                    if (levelScreenProvider != null) {
                        MinecraftClient.getInstance().setScreen(levelScreenProvider.createEditScreen(createWorldScreen, this.worldCreator.getGeneratorOptionsHolder()));
                    }
                })
                .dimensions(i, 185, 150, 20)
                .build();

        elements.add(this.seedField);
        elements.add(this.generateStructuresButton);
        elements.add(this.worldTypeButton);
        elements.add(this.customizeWorldButton);
        elements.add(this.bonusChestButton);
        elements.add(this.importSettingsButton);

        return elements;
    }

    public void tick() {
        this.seedField.tick();
    }

    public boolean isDebug() {
        return this.worldCreator.isDebug();
    }

    public void setVisibility(boolean visible) {
        if (isDebug()) {
            this.generateStructuresButton.visible = false;
            this.bonusChestButton.visible = false;
            this.customizeWorldButton.visible = false;
            this.importSettingsButton.visible = false;
        } else {
            this.generateStructuresButton.visible = visible;
            this.bonusChestButton.visible = visible;
            this.customizeWorldButton.visible = visible;
            this.importSettingsButton.visible = visible;
        }

        this.worldTypeButton.visible = visible;
        this.seedField.setVisible(visible);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean isDebug = isDebug();

        if (!isDebug) {
            this.textRenderer.drawWithShadow(matrices, GENERATE_STRUCTURES_INFO_TEXT, width / 2 - 150, 122, GRAY_COLOR);
        }

        if (this.worldCreator.getWorldType().isAmplified()) {
            this.amplifiedWorldInfo.drawWithShadow(matrices, this.worldTypeButton.getX() + 2, this.worldTypeButton.getY() + 22, 9, GRAY_COLOR);
        }

        this.generateStructuresButton.visible = !isDebug;
        this.bonusChestButton.visible = !isDebug;
        this.importSettingsButton.visible = !isDebug;
        this.customizeWorldButton.visible = !isDebug && this.worldCreator.getLevelScreenProvider() != null;
    }

    private CyclingButtonWidget.Values<WorldCreator.WorldType> getWorldTypes() {
        return new CyclingButtonWidget.Values<>() {

            @Override
            public List<WorldCreator.WorldType> getCurrent() {
                return CyclingButtonWidget.HAS_ALT_DOWN.getAsBoolean() ? worldCreator.getExtendedWorldTypes() : worldCreator.getNormalWorldTypes();
            }

            @Override
            public List<WorldCreator.WorldType> getDefaults() {
                return worldCreator.getNormalWorldTypes();
            }
        };
    }
}
