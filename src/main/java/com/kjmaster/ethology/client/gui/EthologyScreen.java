package com.kjmaster.ethology.client.gui;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EthologyScreen extends Screen {
    // Layout Constants
    private static final int LEFT_PANEL_WIDTH = 130;
    private static final int RIGHT_PANEL_WIDTH = 170;
    private static final int HEADER_HEIGHT = 30;

    // Widgets
    private MobListWidget listWidget;
    private MobInfoWidget infoWidget;
    private EditBox searchBox;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    // State
    @Nullable
    private LivingEntity cachedEntity;
    private float xMouse;
    private float yMouse;

    public EthologyScreen() {
        super(Component.literal("Ethology"));
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        int contentTop = HEADER_HEIGHT + 24; // Space for header + search
        int contentHeight = this.height - contentTop - 10;

        // 1. Search Box (Top Left)
        this.searchBox = new EditBox(this.font, LEFT_PANEL_WIDTH, 20, Component.literal("Search..."));
        this.searchBox.setPosition(0, HEADER_HEIGHT);
        this.searchBox.setResponder(text -> this.listWidget.refreshList(text));
        this.addRenderableWidget(this.searchBox);

        // 2. Left List (Mob List)
        this.listWidget = new MobListWidget(this.minecraft, LEFT_PANEL_WIDTH, contentHeight, contentTop, 24, this);
        this.listWidget.setX(0);
        this.addRenderableWidget(this.listWidget);

        // 3. Right List (Info Panel)
        this.infoWidget = new MobInfoWidget(this.minecraft, RIGHT_PANEL_WIDTH, contentHeight, contentTop);
        this.infoWidget.setX(this.width - RIGHT_PANEL_WIDTH);
        this.addRenderableWidget(this.infoWidget);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        // Manually adjust search box to align with list
        if (searchBox != null) searchBox.setX(10);
        if (listWidget != null) listWidget.setX(10);
        if (infoWidget != null) infoWidget.setX(this.width - RIGHT_PANEL_WIDTH - 10);
    }

    public void onMobSelected(EntityType<?> type, MobScopedInfo info) {
        if (this.minecraft.level != null) {
            try {
                this.cachedEntity = (LivingEntity) type.create(this.minecraft.level);
            } catch (Exception e) {
                this.cachedEntity = null;
            }
        }
        this.infoWidget.updateInfo(info);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Draw Background & Widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // 2. Draw Entity
        drawCenterViewport(graphics, mouseX, mouseY);

        // 3. Capture mouse for next frame rotation
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    private void drawCenterViewport(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.cachedEntity == null) {
            // Draw empty state hint
            int centerX = (LEFT_PANEL_WIDTH + (this.width - RIGHT_PANEL_WIDTH)) / 2;
            graphics.drawCenteredString(this.font, "Select a Mob", centerX, this.height / 2, 0xAAAAAA);
            return;
        }

        // 1. Define Viewport Bounds
        int viewStartX = LEFT_PANEL_WIDTH + 20;
        int viewEndX = this.width - RIGHT_PANEL_WIDTH - 20;
        int viewTopY = HEADER_HEIGHT + 24;
        int viewBottomY = this.height - 10;

        // 2. Calculate Available Space
        int viewHeight = viewBottomY - viewTopY;
        int viewWidth = viewEndX - viewStartX;
        int centerX = viewStartX + (viewWidth / 2);
        int centerY = viewTopY + (viewHeight / 2);

        // 3. Get Entity Dimensions
        // Use Math.max to prevent divide by zero on buggy entities
        float entityHeight = Math.max(this.cachedEntity.getBbHeight(), 0.1f);
        float entityWidth = Math.max(this.cachedEntity.getBbWidth(), 0.1f);

        // 4. Calculate Dynamic Scale
        // We want the mob to take up roughly 80% of the viewport to allow padding.
        // We calculate the ratio for both height and width and pick the smaller one
        // to ensure wide mobs (Ravagers) and tall mobs (Endermen) both fit.
        float verticalScale = (viewHeight * 0.8f) / entityHeight;
        float horizontalScale = (viewWidth * 0.8f) / entityWidth;

        // Use the constraining dimension
        int scale = (int) Math.min(verticalScale, horizontalScale);

        // Cap the scale for tiny mobs (Silverfish/Bees) so they don't look huge and pixelated
        scale = Math.min(scale, 100);

        // 5. Calculate Render Position (The Feet)
        // renderEntityInInventory draws from the feet up.
        // To center the mob vertically: FeetY = CenterY + (Half of Rendered Height)
        int renderY = centerY + (int) ((entityHeight * scale) / 2.0f);

        // 6. Scissor to prevent overflow
        graphics.enableScissor(viewStartX, viewTopY, viewEndX, viewBottomY);

        // 7. Calculate Look Vector
        // We want the mob to look at the mouse relative to its eye height, not its feet
        float eyeOffset = this.cachedEntity.getEyeHeight() * scale;
        float lookX = centerX - this.xMouse;
        float lookY = (renderY - eyeOffset) - this.yMouse;

        renderEntityInInventory(
                graphics,
                centerX,
                renderY, // Pass the calculated feet position
                scale,
                lookX, lookY,
                this.cachedEntity
        );

        graphics.disableScissor();
    }

    /**
     * Standard implementation of entity rendering in GUI with Mouse Following
     */
    public static void renderEntityInInventory(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float f1 = (float) Math.atan(mouseY / 40.0F);

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(f1 * 20.0F * ((float) Math.PI / 180.0F));
        pose.mul(camera);

        float yBodyRotO = entity.yBodyRot;
        float yRotO = entity.getYRot();
        float xRotO = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;

        // Force rotation to face forward (180 deg offset) + mouse tracking
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        Vector3f translation = new Vector3f(0, 0, 0);
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, scale, translation, pose, null, entity);

        entity.yBodyRot = yBodyRotO;
        entity.setYRot(yRotO);
        entity.setXRot(xRotO);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
    }
}