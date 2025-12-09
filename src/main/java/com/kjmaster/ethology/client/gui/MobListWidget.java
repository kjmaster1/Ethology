package com.kjmaster.ethology.client.gui;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class MobListWidget extends ObjectSelectionList<MobListWidget.MobEntry> {
    private final EthologyScreen parent;

    public MobListWidget(Minecraft minecraft, int width, int height, int top, int itemHeight, EthologyScreen parent) {
        super(minecraft, width, height, top, itemHeight);
        this.parent = parent;
        this.refreshList("");
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void refreshList(String filter) {
        this.clearEntries();
        String lowerFilter = filter.toLowerCase();

        // Iterate Registry instead of Database
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(this::isLikelyMob) // Lightweight filter
                .sorted(Comparator.comparing(e -> e.getDescription().getString()))
                .forEach(type -> {
                    if (type.getDescription().getString().toLowerCase().contains(lowerFilter)) {
                        this.addEntry(new MobEntry(type));
                    }
                });
    }

    /**
     * Filters out technical entities, projectiles, and non-living objects
     * to ensure the list only contains analyzable Mobs.
     */
    private boolean isLikelyMob(EntityType<?> type) {
        // 1. Must be Summonable (filters out technical entities like Lightning, Fishing Bobbers, etc.)
        if (!type.canSummon()) return false;

        // 2. Category Filter (Filters out Items, Projectiles, etc.)
        if (type.getCategory() == MobCategory.MISC) return false;

        // 3. Must have Default Attributes (Health, etc.)
        // This acts as a robust check for "Is this a Living Entity?".
        // It filters out Boats, Minecarts, and Primed TNT which are not MISC but have no biology.
        if (!DefaultAttributes.hasSupplier(type)) return false;

        // 4. Specific Exclusions
        // Armor Stands are technically LivingEntities with attributes, but we don't study their behavior.
        if (type == EntityType.ARMOR_STAND) return false;
        if (type == EntityType.GIANT) return false; // Vanilla Giants are non-functional and often have no AI.

        return true;
    }

    public void setSelectedByType(EntityType<?> type) {
        for (MobEntry entry : this.children()) {
            if (entry.type == type) {
                this.setSelected(entry);
                this.ensureVisible(entry);
                break;
            }
        }
    }

    public class MobEntry extends ObjectSelectionList.Entry<MobEntry> {
        private final EntityType<?> type;
        private final ItemStack icon;

        public MobEntry(EntityType<?> type) {
            this.type = type;
            SpawnEggItem egg = SpawnEggItem.byId(type);
            this.icon = (egg != null) ? new ItemStack(egg) : new ItemStack(Items.GHAST_TEAR);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.renderItem(this.icon, left + 2, top + 4);

            Component name = type.getDescription();
            int maxTextWidth = width - 25;
            FormattedCharSequence truncatedName = Minecraft.getInstance().font.split(name, maxTextWidth).getFirst();

            graphics.drawString(Minecraft.getInstance().font, truncatedName, left + 24, top + 8, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MobListWidget.this.setSelected(this);
            // Notify parent to trigger scan
            parent.onMobSelected(this.type);
            return true;
        }

        @Override
        public @NotNull Component getNarration() {
            return type.getDescription();
        }
    }
}