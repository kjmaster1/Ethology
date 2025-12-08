package com.kjmaster.ethology.client.gui;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
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

        EthologyDatabase.getAll().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getDescription().getString()))
                .forEach(entry -> {
                    if (entry.getKey().getDescription().getString().toLowerCase().contains(lowerFilter)) {
                        this.addEntry(new MobEntry(entry.getKey(), entry.getValue()));
                    }
                });
    }

    public class MobEntry extends ObjectSelectionList.Entry<MobEntry> {
        private final EntityType<?> type;
        private final MobScopedInfo info;
        private final ItemStack icon;

        public MobEntry(EntityType<?> type, MobScopedInfo info) {
            this.type = type;
            this.info = info;
            SpawnEggItem egg = SpawnEggItem.byId(type);
            this.icon = (egg != null) ? new ItemStack(egg) : new ItemStack(Items.GHAST_TEAR);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.renderItem(this.icon, left + 2, top + 4);

            Component name = type.getDescription();
            // Truncate text to fit
            int maxTextWidth = width - 25;
            FormattedCharSequence truncatedName = Minecraft.getInstance().font.split(name, maxTextWidth).getFirst();

            graphics.drawString(Minecraft.getInstance().font, truncatedName, left + 24, top + 8, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MobListWidget.this.setSelected(this);
            parent.onMobSelected(this.type, this.info);
            return true;
        }

        @Override
        public @NotNull Component getNarration() {
            return type.getDescription();
        }
    }
}