package fr.rader.oldworldmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

import static fr.rader.oldworldmenu.Constants.*;

public class MoreWorldOptionsComponent {

    private MultilineText amplifiedWorldInfo;

    private TextFieldWidget seedField;

    private CyclingButtonWidget<Boolean> generateStructuresButton;

    private CyclingButtonWidget<WorldCreator.WorldType> worldTypeButton;
    private ButtonWidget customizeWorldButton;

    private CyclingButtonWidget<Boolean> bonusChestButton;

    private WorldCreator worldCreator;
    private TextRenderer textRenderer;
    private int halfWidth;

    public MoreWorldOptionsComponent() {
        this.amplifiedWorldInfo = MultilineText.EMPTY;
    }

    public List<ClickableWidget> init(CreateWorldScreen createWorldScreen, TextRenderer textRenderer) {
        this.worldCreator = createWorldScreen.getWorldCreator();
        this.textRenderer = textRenderer;
        this.halfWidth = createWorldScreen.width / 2;

        List<ClickableWidget> elements = new ArrayList<>();

        this.seedField = new TextFieldWidget(textRenderer, this.halfWidth - 100, 60, 200, 20, SEED_LABEL);
        this.seedField.setText(this.worldCreator.getSeed());

        int leftColumnX = this.halfWidth - 155;
        int rightColumnX = this.halfWidth + 5;

        this.generateStructuresButton = CyclingButtonWidget.onOffBuilder(this.worldCreator.shouldGenerateStructures())
                .build(leftColumnX, 100, BUTTON_WIDTH, BUTTON_HEIGHT, GENERATE_STRUCTURES_TEXT, (button, shouldGenerateStructures) -> {
                    this.worldCreator.setGenerateStructures(shouldGenerateStructures);
                });

        this.worldTypeButton = CyclingButtonWidget.builder(WorldCreator.WorldType::getName)
                .values(getWorldTypes())
                .initially(this.worldCreator.getWorldType())
                .build(rightColumnX, 100, BUTTON_WIDTH, BUTTON_HEIGHT, WORLD_TYPE_TEXT, (button, worldType) -> {
                    this.worldCreator.setWorldType(worldType);
                });

        this.amplifiedWorldInfo = MultilineText.create(textRenderer, AMPLIFIED_INFO_TEXT, this.worldTypeButton.getWidth());

        this.customizeWorldButton = ButtonWidget.builder(CUSTOMIZE_TEXT, (button) -> {
                    LevelScreenProvider levelScreenProvider = this.worldCreator.getLevelScreenProvider();
                    if (levelScreenProvider != null) {
                        MinecraftClient.getInstance().setScreen(
                                levelScreenProvider.createEditScreen(
                                        createWorldScreen,
                                        this.worldCreator.getGeneratorOptionsHolder()
                                )
                        );
                    }
                })
                .dimensions(rightColumnX, 120, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        this.bonusChestButton = CyclingButtonWidget.onOffBuilder(this.worldCreator.isBonusChestEnabled())
                .build(leftColumnX, 151, BUTTON_WIDTH, BUTTON_HEIGHT, BONUS_CHEST_TEXT, (button, bonusChestEnabled) -> {
                    this.worldCreator.setBonusChestEnabled(bonusChestEnabled);
                });

        elements.add(this.seedField);
        elements.add(this.generateStructuresButton);
        elements.add(this.worldTypeButton);
        elements.add(this.customizeWorldButton);
        elements.add(this.bonusChestButton);

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
        } else {
            this.generateStructuresButton.visible = visible;
            this.bonusChestButton.visible = visible;
            this.customizeWorldButton.visible = visible;
        }

        this.worldTypeButton.visible = visible;
        this.seedField.setVisible(visible);
    }

    public void render(MatrixStack matrices) {
        boolean isDebug = isDebug();

        if (!isDebug) {
            this.textRenderer.drawWithShadow(matrices, GENERATE_STRUCTURES_INFO_TEXT, this.halfWidth - 150, 122, GRAY_COLOR);
        }

        if (this.worldCreator.getWorldType().isAmplified()) {
            this.amplifiedWorldInfo.drawWithShadow(matrices, this.worldTypeButton.getX() + 2, this.worldTypeButton.getY() + 22, 9, GRAY_COLOR);
        }

        this.generateStructuresButton.visible = !isDebug;
        this.bonusChestButton.visible = !isDebug;
        this.customizeWorldButton.visible = !isDebug && this.worldCreator.getLevelScreenProvider() != null;
    }

    private CyclingButtonWidget.Values<WorldCreator.WorldType> getWorldTypes() {
        return new CyclingButtonWidget.Values<>() {

            @Override
            public List<WorldCreator.WorldType> getCurrent() {
                if (CyclingButtonWidget.HAS_ALT_DOWN.getAsBoolean()) {
                    return worldCreator.getExtendedWorldTypes();
                }

                return getDefaults();
            }

            @Override
            public List<WorldCreator.WorldType> getDefaults() {
                return worldCreator.getNormalWorldTypes();
            }
        };
    }
}
