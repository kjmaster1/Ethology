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

    // Debounce State
    private EntityType<?> pendingScanType;
    private long lastSelectionTime;
    private static final long DEBOUNCE_DELAY_MS = 300;
    private boolean suppressSelectionEvent = false;

    public EthologyScreen() {
        super(Component.literal("Ethology"));
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        // 1. Calculate Dynamic Widths
        this.leftPanelWidth = Math.clamp((int)(this.width * 0.25), 130, 250);
        this.rightPanelWidth = Math.clamp((int)(this.width * 0.30), 170, 350);

        int contentTop = HEADER_HEIGHT + 24;
        int contentHeight = this.height - contentTop - 10;

        // 2. Search Box
        this.searchBox = new EditBox(this.font, this.leftPanelWidth, 20, Component.literal("Search..."));
        this.searchBox.setPosition(10, HEADER_HEIGHT);
        this.searchBox.setResponder(text -> {
            if (this.listWidget != null) this.listWidget.refreshList(text);
        });
        this.addRenderableWidget(this.searchBox);

        // 3. Left List (Mob List)
        this.listWidget = new MobListWidget(this.minecraft, this.leftPanelWidth, contentHeight, contentTop, 24, this);
        this.listWidget.setX(10);
        this.addRenderableWidget(this.listWidget);

        // 4. Right List (Info Panel)
        this.infoWidget = new MobInfoWidget(this.minecraft, this.rightPanelWidth, contentHeight, contentTop);
        this.infoWidget.setX(this.width - this.rightPanelWidth - 10);
        this.addRenderableWidget(this.infoWidget);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();

        // Check for crosshair target on open
        Entity crosshairTarget = this.minecraft.crosshairPickEntity;
        if (crosshairTarget instanceof LivingEntity living) {
            // Trigger State-Aware Scan
            EthologyScanner.scanTargetedEntity(living);

            // Set cached entity to the actual instance for "True" preview
            this.cachedEntity = living;

            // Update info immediately with local data
            this.infoWidget.updateInfo(EthologyDatabase.get(living.getType()));

            // Auto-select in list without triggering debounce scan
            this.suppressSelectionEvent = true;
            this.listWidget.setSelectedByType(living.getType());
            this.suppressSelectionEvent = false;
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (searchBox != null) searchBox.setX(10);
        if (listWidget != null) listWidget.setX(10);
        if (infoWidget != null) infoWidget.setX(this.width - this.rightPanelWidth - 10);
    }

    public void onMobSelected(EntityType<?> type) {
        if (suppressSelectionEvent) return;

        // Reset timer and update pending target
        this.pendingScanType = type;
        this.lastSelectionTime = System.currentTimeMillis();
    }

    private void executeScan(EntityType<?> type) {
        // Trigger Lazy Scan (Archetype)
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

        // Debounce Logic: Check if it's time to scan
        if (this.pendingScanType != null) {
            if (System.currentTimeMillis() - this.lastSelectionTime >= DEBOUNCE_DELAY_MS) {
                executeScan(this.pendingScanType);
                this.pendingScanType = null;
            }
        }

        // Draw Entity in the remaining center space
        drawCenterViewport(graphics, mouseX, mouseY);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    private void drawCenterViewport(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.cachedEntity == null) {
            int viewStartX = this.leftPanelWidth + 20;
            int viewEndX = this.width - this.rightPanelWidth - 20;
            int centerX = viewStartX + ((viewEndX - viewStartX) / 2);

            graphics.drawCenteredString(this.font, "Select a Mob", centerX, this.height / 2, 0xAAAAAA);
            return;
        }

        int viewStartX = this.leftPanelWidth + 20;
        int viewEndX = this.width - this.rightPanelWidth - 20;
        int viewTopY = HEADER_HEIGHT + 24;
        int viewBottomY = this.height - 10;
        int viewHeight = viewBottomY - viewTopY;
        int viewWidth = viewEndX - viewStartX;

        if (viewHeight <= 0 || viewWidth <= 0) return;

        int centerX = viewStartX + (viewWidth / 2);
        int centerY = viewTopY + (viewHeight / 2);

        float entityHeight = Math.max(this.cachedEntity.getBbHeight(), 0.1f);
        float entityWidth = Math.max(this.cachedEntity.getBbWidth(), 0.1f);

        float verticalScale = (viewHeight * 0.8f) / entityHeight;
        float horizontalScale = (viewWidth * 0.8f) / entityWidth;
        int scale = (int) Math.min(verticalScale, horizontalScale);
        scale = Math.clamp(scale, 10, 100);

        int renderY = centerY + (int) ((entityHeight * scale) / 2.0f);

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