package fr.rader.oldworldmenu.mixin;

import fr.rader.oldworldmenu.MoreWorldOptionsComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen extends Screen {

    @Shadow
    abstract void openPackScreen(DataConfiguration dataConfiguration);
    @Shadow
    protected abstract void createLevel();

    @Shadow @Final
    WorldCreator worldCreator;

    private static final Text NAME_LABEL = Text.translatable("selectWorld.enterName");
    private static final Text OUTPUT_DIR_INFO_LABEL = Text.translatable("selectWorld.resultFolder");
    private static final Text GAME_MODE_LABEL = Text.translatable("selectWorld.gameMode");
    private static final Text COMMANDS_INFO_LABEL = Text.translatable("selectWorld.allowCommands.info");
    private static final Text SEED_LABEL = Text.translatable("selectWorld.enterSeed");
    private static final Text SEED_INFO_LABEL = Text.translatable("selectWorld.seedInfo");

    private static final Text DIFFICULTY_TEXT = Text.translatable("options.difficulty");
    private static final Text ALLOW_CHEATS_TEXT = Text.translatable("selectWorld.allowCommands");
    private static final Text DATA_PACKS_TEXT = Text.translatable("selectWorld.dataPacks");
    private static final Text GAME_RULES_TEXT = Text.translatable("selectWorld.gameRules");
    private static final Text MORE_WORLD_OPTIONS_TEXT = Text.translatable("selectWorld.moreWorldOptions");
    private static final Text DONE_TEXT = Text.translatable("gui.done");
    private static final Text CREATE_NEW_WORLD_TEXT = Text.translatable("selectWorld.create");
    private static final Text CANCEL_TEXT = Text.translatable("gui.cancel");

    private static final int GRAY_COLOR = ColorHelper.Argb.getArgb(0xFF, 0xA0, 0xA0, 0xA0);

    private MoreWorldOptionsComponent moreWorldOptionsComponent;
    private boolean isWorldOptionsToggled;

    private TextFieldWidget worldName;

    private CyclingButtonWidget<WorldCreator.Mode> gameModeButton;
    private WorldCreator.Mode nonDebugGameMode;
    private Text gameModeHelp1;
    private Text gameModeHelp2;

    private CyclingButtonWidget<Difficulty> difficultyButton;
    private CyclingButtonWidget<Boolean> allowCheatsButton;

    private ButtonWidget dataPacksButton;
    private ButtonWidget gameRulesButton;
    private ButtonWidget moreWorldOptionsButton;
    private ButtonWidget createNewWorldButton;
    private ButtonWidget cancelButton;

    protected MixinCreateWorldScreen(Text title) {
        super(title);

        this.isWorldOptionsToggled = false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, -1);

        if (this.isWorldOptionsToggled) {
            drawTextWithShadow(matrices, this.textRenderer, SEED_LABEL, this.width / 2 - 100, 47, GRAY_COLOR);
            drawTextWithShadow(matrices, this.textRenderer, SEED_INFO_LABEL, this.width / 2 - 100, 85, GRAY_COLOR);

            this.moreWorldOptionsComponent.render(matrices, mouseX, mouseY, delta);
        } else {
            drawTextWithShadow(matrices, this.textRenderer, NAME_LABEL, this.width / 2 - 100, 47, GRAY_COLOR);
            drawTextWithShadow(matrices, this.textRenderer, Text.empty().append(OUTPUT_DIR_INFO_LABEL).append(" " + this.worldCreator.getWorldDirectoryName()), this.width / 2 - 100, 85, GRAY_COLOR);

            drawTextWithShadow(matrices, this.textRenderer, this.gameModeHelp1, this.width / 2 - 150, 122, GRAY_COLOR);
            drawTextWithShadow(matrices, this.textRenderer, this.gameModeHelp2, this.width / 2 - 150, 134, GRAY_COLOR);

            if (!this.worldCreator.isDebug()) {
                drawTextWithShadow(matrices, this.textRenderer, COMMANDS_INFO_LABEL, this.width / 2 - 150, 172, GRAY_COLOR);
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void init() {
        this.moreWorldOptionsComponent = new MoreWorldOptionsComponent();

        this.worldName = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, NAME_LABEL);
        this.worldName.setText(this.worldCreator.getWorldName());
        this.worldName.setChangedListener(text -> {
            this.worldCreator.setWorldName(text);
        });

        int i = this.width / 2 - 155;
        int j = this.width / 2 + 5;
        this.gameModeButton = CyclingButtonWidget.<WorldCreator.Mode>builder(value -> value.name)
                .values(List.of(
                        WorldCreator.Mode.SURVIVAL,
                        WorldCreator.Mode.HARDCORE,
                        WorldCreator.Mode.CREATIVE
                ))
                .initially(this.worldCreator.getGameMode())
                .build(i, 100, 150, 20, GAME_MODE_LABEL, (button, gameMode) -> {
                    this.worldCreator.setGameMode(gameMode);
                    updateGameModeHelp(gameMode);
                });
        this.worldCreator.addListener(creator -> {
            this.gameModeButton.setValue(this.worldCreator.getGameMode());
            this.gameModeButton.active = !this.worldCreator.isDebug();
        });

        this.difficultyButton = CyclingButtonWidget.builder(Difficulty::getTranslatableName)
                .values(Difficulty.values())
                .initially(this.worldCreator.getDifficulty())
                .build(j, 100, 150, 20, DIFFICULTY_TEXT, (button, difficulty) -> {
                    this.worldCreator.setDifficulty(difficulty);
                });
        this.worldCreator.addListener(creator -> {
            this.difficultyButton.setValue(this.worldCreator.getDifficulty());
            this.difficultyButton.active = !this.worldCreator.isHardcore();
        });

        this.allowCheatsButton = CyclingButtonWidget.onOffBuilder(this.worldCreator.areCheatsEnabled())
                .build(i, 151, 150, 20, ALLOW_CHEATS_TEXT, (button, allowCheats) -> {
                    this.worldCreator.setCheatsEnabled(allowCheats);
                });
        this.worldCreator.addListener(creator -> {
            this.allowCheatsButton.setValue(this.worldCreator.areCheatsEnabled());
            this.allowCheatsButton.active = !this.worldCreator.isDebug() && !this.worldCreator.isHardcore();
        });

        this.dataPacksButton = ButtonWidget.builder(DATA_PACKS_TEXT, button -> {
                    openPackScreen(this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
                })
                .dimensions(j, 151, 150, 20)
                .build();

        this.gameRulesButton = ButtonWidget.builder(GAME_RULES_TEXT, button -> {
                    this.client.setScreen(new EditGameRulesScreen(this.worldCreator.getGameRules().copy(), optional -> {
                        this.client.setScreen(this);
                        optional.ifPresent(this.worldCreator::setGameRules);
                    }));
                })
                .dimensions(i, 185, 150, 20)
                .build();

        this.moreWorldOptionsButton = ButtonWidget.builder(MORE_WORLD_OPTIONS_TEXT, button -> {
                    toggleWorldOptionsVisibility();
                })
                .dimensions(j, 185, 150, 20)
                .build();

        this.createNewWorldButton = ButtonWidget.builder(CREATE_NEW_WORLD_TEXT, button -> createLevel())
                .dimensions(i, this.height - 28, 150, 20)
                .build();

        this.cancelButton = ButtonWidget.builder(CANCEL_TEXT, button -> close())
                .dimensions(j, this.height - 28, 150, 20)
                .build();

        List<ClickableWidget> elements = this.moreWorldOptionsComponent.init((CreateWorldScreen) (Object) this, this.client, this.textRenderer);

        addDrawableChild(this.worldName);
        addDrawableChild(this.gameModeButton);
        addDrawableChild(this.difficultyButton);
        addDrawableChild(this.allowCheatsButton);
        addDrawableChild(this.dataPacksButton);
        addDrawableChild(this.gameRulesButton);
        addDrawableChild(this.moreWorldOptionsButton);
        addDrawableChild(this.createNewWorldButton);
        addDrawableChild(this.cancelButton);

        elements.forEach(this::addDrawableChild);

        refreshWorldOptionsVisibility();
        setInitialFocus(this.worldName);

        this.worldCreator.update();
        updateGameModeHelp(this.worldCreator.getGameMode());
    }

    @Override
    public void tick() {
        this.worldName.tick();
        this.moreWorldOptionsComponent.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void initTabNavigation() {
        super.initTabNavigation();
    }

    private void setGameMode(WorldCreator.Mode gameMode) {
        this.worldCreator.setGameMode(gameMode);

        updateGameModeHelp(gameMode);
    }

    private void updateGameModeHelp(WorldCreator.Mode gameMode) {
        String gameModeName = gameMode.name().toLowerCase();
        if (gameModeName.equals("debug")) {
            gameModeName = "spectator";
        }

        this.gameModeHelp1 = Text.translatable("selectWorld.gameMode." + gameModeName + ".line1");
        this.gameModeHelp2 = Text.translatable("selectWorld.gameMode." + gameModeName + ".line2");
    }

    private void toggleWorldOptionsVisibility() {
        setWorldOptionsVisible(!this.isWorldOptionsToggled);
    }

    private void refreshWorldOptionsVisibility() {
        setWorldOptionsVisible(this.isWorldOptionsToggled);
    }

    private void setWorldOptionsVisible(boolean visible) {
        this.isWorldOptionsToggled = visible;
        this.gameModeButton.visible = !visible;
        this.difficultyButton.visible = !visible;

        if (this.moreWorldOptionsComponent.isDebug()) {
            this.dataPacksButton.visible = false;
            this.gameModeButton.active = false;

            if (this.nonDebugGameMode == null) {
                this.nonDebugGameMode = this.gameModeButton.getValue();
            }

            this.allowCheatsButton.visible = false;
            setGameMode(WorldCreator.Mode.DEBUG);
        } else {
            this.gameModeButton.active = true;
            if (this.nonDebugGameMode != null) {
                setGameMode(this.nonDebugGameMode);
            }

            this.allowCheatsButton.visible = !visible;
            this.dataPacksButton.visible = !visible;
        }

        this.moreWorldOptionsComponent.setVisibility(visible);
        this.worldName.setVisible(!visible);

        if (visible) {
            this.moreWorldOptionsButton.setMessage(DONE_TEXT);
        } else {
            this.moreWorldOptionsButton.setMessage(MORE_WORLD_OPTIONS_TEXT);
        }

        this.gameRulesButton.visible = !visible;
    }
}
