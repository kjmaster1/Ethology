package com.kjmaster.ethology.client.gui;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EthologyDatabase;
import com.kjmaster.ethology.core.EthologyScanner;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EthologyScreen extends Screen {
    // Layout Constants
    private static final int HEADER_HEIGHT = 30;

    // Dynamic Layout Dimensions
    private int leftPanelWidth;
    private int rightPanelWidth;

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

        // 1. Calculate Dynamic Widths
        // Left Panel: ~25% of screen, Min 130px, Max 250px
        this.leftPanelWidth = Math.clamp((int)(this.width * 0.25), 130, 250);

        // Right Panel: ~30% of screen, Min 170px, Max 350px
        this.rightPanelWidth = Math.clamp((int)(this.width * 0.30), 170, 350);

        int contentTop = HEADER_HEIGHT + 24; // Space for header + search
        int contentHeight = this.height - contentTop - 10;

        // 2. Search Box (Top Left)
        // Matches the width of the left panel
        this.searchBox = new EditBox(this.font, this.leftPanelWidth, 20, Component.literal("Search..."));
        this.searchBox.setPosition(10, HEADER_HEIGHT); // 10px Margin
        this.searchBox.setResponder(text -> {
            if (this.listWidget != null) this.listWidget.refreshList(text);
        });
        this.addRenderableWidget(this.searchBox);

        // 3. Left List (Mob List)
        this.listWidget = new MobListWidget(this.minecraft, this.leftPanelWidth, contentHeight, contentTop, 24, this);
        this.listWidget.setX(10); // 10px Margin
        this.addRenderableWidget(this.listWidget);

        // 4. Right List (Info Panel)
        this.infoWidget = new MobInfoWidget(this.minecraft, this.rightPanelWidth, contentHeight, contentTop);
        this.infoWidget.setX(this.width - this.rightPanelWidth - 10); // 10px Margin from right
        this.addRenderableWidget(this.infoWidget);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();

        // Check for crosshair target on open
        Entity crosshairTarget = this.minecraft.crosshairPickEntity;
        if (crosshairTarget instanceof LivingEntity living) {
            // Trigger State-Aware Scan
            EthologyScanner.scanTargetedEntity(living);

            // Auto-select in list
            this.listWidget.setSelectedByType(living.getType());

            // Set cached entity to the actual instance for "True" preview
            // Note: renderEntityInInventory might need a copy if the world entity moves too much,
            // but for a paused screen or short viewing, using the instance directly is fine.
            this.cachedEntity = living;

            // Update info immediately with local data
            this.infoWidget.updateInfo(EthologyDatabase.get(living.getType()));
        }


    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        // Ensure widgets stay pinned to their margins on resize
        if (searchBox != null) searchBox.setX(10);
        if (listWidget != null) listWidget.setX(10);
        if (infoWidget != null) infoWidget.setX(this.width - this.rightPanelWidth - 10);
    }

    public void onMobSelected(EntityType<?> type) {
        // Trigger Lazy Scan
        EthologyScanner.scanEntity(type);

        // Fetch Result (Immediate local or waiting for packet)
        MobScopedInfo info = EthologyDatabase.get(type);

        // Update Cached Entity for rendering
        if (this.cachedEntity == null || this.cachedEntity.getType() != type) {
            if (this.minecraft.level != null) {
                try {
                    Entity entity = type.create(this.minecraft.level);
                    if (entity instanceof LivingEntity living) {
                        this.cachedEntity = living;
                    } else {
                        this.cachedEntity = null;
                    }
                } catch (Exception e) {
                    this.cachedEntity = null;
                }
            }
        }

        this.infoWidget.updateInfo(info);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw Entity in the remaining center space
        drawCenterViewport(graphics, mouseX, mouseY);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    private void drawCenterViewport(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.cachedEntity == null) {
            // Draw empty state hint centered in the available viewport
            int viewStartX = this.leftPanelWidth + 20; // 10px margin + 10px padding
            int viewEndX = this.width - this.rightPanelWidth - 20;
            int centerX = viewStartX + ((viewEndX - viewStartX) / 2);

            graphics.drawCenteredString(this.font, "Select a Mob", centerX, this.height / 2, 0xAAAAAA);
            return;
        }

        // 1. Define Viewport Bounds
        int viewStartX = this.leftPanelWidth + 20;
        int viewEndX = this.width - this.rightPanelWidth - 20;
        int viewTopY = HEADER_HEIGHT + 24;
        int viewBottomY = this.height - 10;

        // 2. Calculate Available Space
        int viewHeight = viewBottomY - viewTopY;
        int viewWidth = viewEndX - viewStartX;

        // Sanity check to prevent crashes on very small screens
        if (viewHeight <= 0 || viewWidth <= 0) return;

        int centerX = viewStartX + (viewWidth / 2);
        int centerY = viewTopY + (viewHeight / 2);

        // 3. Get Entity Dimensions
        float entityHeight = Math.max(this.cachedEntity.getBbHeight(), 0.1f);
        float entityWidth = Math.max(this.cachedEntity.getBbWidth(), 0.1f);

        // 4. Calculate Dynamic Scale (80% of viewport)
        float verticalScale = (viewHeight * 0.8f) / entityHeight;
        float horizontalScale = (viewWidth * 0.8f) / entityWidth;
        int scale = (int) Math.min(verticalScale, horizontalScale);
        scale = Math.clamp(scale, 10, 100); // Prevent it from disappearing or becoming pixelatedly massive

        // 5. Calculate Render Position (The Feet)
        int renderY = centerY + (int) ((entityHeight * scale) / 2.0f);

        // 6. Scissor to prevent overflow
        graphics.enableScissor(viewStartX, viewTopY, viewEndX, viewBottomY);

        float eyeOffset = this.cachedEntity.getEyeHeight() * scale;
        float lookX = centerX - this.xMouse;
        float lookY = (renderY - eyeOffset) - this.yMouse;

        renderEntityInInventory(
                graphics,
                centerX,
                renderY,
                scale,
                lookX, lookY,
                this.cachedEntity
        );

        graphics.disableScissor();
    }

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