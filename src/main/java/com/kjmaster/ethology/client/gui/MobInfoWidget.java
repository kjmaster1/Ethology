package com.kjmaster.ethology.client.gui;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MobInfoWidget extends ObjectSelectionList<MobInfoWidget.InfoEntry> {

    public MobInfoWidget(Minecraft minecraft, int width, int height, int top) {
        super(minecraft, width, height, top, 16); // 16px base slot height
        this.setRenderHeader(false, 0);
    }

    @Override
    public int getRowWidth() {
        return this.width - 24; // Ensure plenty of padding from scrollbar
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void updateInfo(MobScopedInfo info) {
        this.clearEntries();
        this.setScrollAmount(0); // Reset scroll on new mob
        if (info == null) return;

        // 1. Stats
        this.addEntry(new HeaderEntry("Attributes"));
        this.addEntry(new StatEntry("Health", (int) info.getMaxHealth() + " HP"));
        this.addEntry(new StatEntry("Armor", (int) info.getArmor() + ""));
        this.addEntry(new StatEntry("Damage", (int) info.getAttackDamage() + ""));
        this.addEntry(new StatEntry("Speed", String.format("%.2f", info.getMovementSpeed())));
        this.addEntry(new SpacerEntry(10));

        // 2. Behaviors
        if (!info.getTraits().isEmpty()) {
            this.addEntry(new HeaderEntry("Behaviors"));
            this.addEntry(new SpacerEntry(5));

            for (MobTrait trait : info.getTraits()) {
                this.addEntry(new TraitHeaderEntry(trait));

                // Wrap text based on widget width
                List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(trait.description(), this.getRowWidth());
                for (FormattedCharSequence line : lines) {
                    this.addEntry(new TextLineEntry(line, 0xAAAAAA));
                }

                this.addEntry(new SpacerEntry(8));
            }
        }
    }

    // --- Entry Classes ---

    public abstract static class InfoEntry extends ObjectSelectionList.Entry<InfoEntry> {
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    }

    public static class HeaderEntry extends InfoEntry {
        private final String text;
        public HeaderEntry(String text) { this.text = text; }
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, Component.literal(text).withColor(0xFFAA00), left, top + 4, 0xFFFFFF, false);
        }
        @Override
        public @NotNull Component getNarration() { return Component.literal(text); }
    }

    public static class StatEntry extends InfoEntry {
        private final String label;
        private final String value;
        public StatEntry(String label, String value) { this.label = label; this.value = value; }
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, label + ":", left, top + 2, 0xCCCCCC, false);
            graphics.drawString(Minecraft.getInstance().font, value, left + width - Minecraft.getInstance().font.width(value), top + 2, 0xFFFFFF, false);
        }
        @Override
        public @NotNull Component getNarration() { return Component.literal(label + " " + value); }
    }

    public static class TraitHeaderEntry extends InfoEntry {
        private final MobTrait trait;
        public TraitHeaderEntry(MobTrait trait) { this.trait = trait; }
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.renderItem(trait.icon(), left, top - 2);
            graphics.drawString(Minecraft.getInstance().font, trait.title(), left + 20, top + 4, 0x55FF55, false);

            // Standard tooltip handling via Screen is tricky with clipped lists,
            // but this render immediately approach works for simple cases.
            if (hovering && mouseX >= left && mouseX <= left + 16) {
                graphics.renderTooltip(Minecraft.getInstance().font, trait.icon(), mouseX, mouseY);
            }
        }
        @Override
        public @NotNull Component getNarration() { return trait.title(); }
    }

    public static class TextLineEntry extends InfoEntry {
        private final FormattedCharSequence text;
        private final int color;
        public TextLineEntry(FormattedCharSequence text, int color) { this.text = text; this.color = color; }
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Render slightly smaller or normal? Normal for readability.
            graphics.drawString(Minecraft.getInstance().font, text, left, top + 2, color, false);
        }
        @Override
        public @NotNull Component getNarration() { return Component.empty(); }
    }

    public static class SpacerEntry extends InfoEntry {
        public SpacerEntry(int ignored) {}
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {}
        @Override
        public @NotNull Component getNarration() { return Component.empty(); }
    }
}