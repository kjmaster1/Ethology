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
        return this.width - 24;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void updateInfo(MobScopedInfo info) {
        this.clearEntries();
        this.setScrollAmount(0);

        if (info == null) return;

        // 1. Stats Section
        this.addEntry(new HeaderEntry("Attributes"));
        this.addEntry(new StatEntry("Health", (int) info.getMaxHealth() + " HP"));
        this.addEntry(new StatEntry("Armor", (int) info.getArmor() + ""));
        this.addEntry(new StatEntry("Damage", (int) info.getAttackDamage() + ""));
        this.addEntry(new StatEntry("Speed", String.format("%.2f", info.getMovementSpeed())));
        this.addEntry(new SpacerEntry());

        // 2. Current Activity Section (The "Doing Now" list)
        // Only show if there are active states (usually only true for Instance Scans)
        if (!info.getCurrentStates().isEmpty()) {
            this.addEntry(new HeaderEntry("Current Activity"));
            this.addEntry(new SpacerEntry());

            for (MobTrait trait : info.getCurrentStates()) {
                // TRUE for isActive -> Renders green dot / distinct visual
                this.addEntry(new TraitHeaderEntry(trait, true));

                // Use the ".active" translation key
                Component desc = Component.translatable(trait.translationKey() + ".active");
                addDescriptionLines(desc);

                this.addEntry(new SpacerEntry());
            }
            this.addEntry(new SpacerEntry());
        }

        // 3. Capabilities Section (The "Can Do" list)
        // Only show if capabilities exist
        if (!info.getCapabilities().isEmpty()) {
            this.addEntry(new HeaderEntry("Potential Behaviors"));
            this.addEntry(new SpacerEntry());

            for (MobTrait trait : info.getCapabilities()) {
                // FALSE for isActive -> Standard render
                this.addEntry(new TraitHeaderEntry(trait, false));

                // Use the ".static" translation key
                Component desc = Component.translatable(trait.translationKey() + ".static");
                addDescriptionLines(desc);

                this.addEntry(new SpacerEntry());
            }
        }
    }

    private void addDescriptionLines(Component text) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(text, this.getRowWidth());
        for (FormattedCharSequence line : lines) {
            this.addEntry(new TextLineEntry(line, 0xAAAAAA));
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
            // Gold Header
            graphics.drawString(Minecraft.getInstance().font, Component.literal(text).withColor(0xFFAA00), left, top + 4, 0xFFFFFF, false);
            // Underline
            graphics.hLine(left, left + width, top + 14, 0xFFFFAA00);
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
        private final boolean isActive;

        public TraitHeaderEntry(MobTrait trait, boolean isActive) {
            this.trait = trait;
            this.isActive = isActive;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Icon
            graphics.renderItem(trait.icon(), left, top - 2);

            // Title Component
            Component title = Component.translatable(trait.translationKey() + ".title");

            int color = isActive ? 0x55FF55 : 0xFFFFFF; // Green if active, White if capability
            graphics.drawString(Minecraft.getInstance().font, title, left + 20, top + 4, color, false);

            // "Active" Indicator (Green pulsing dot)
            if (isActive) {
                long time = System.currentTimeMillis() / 500;
                int dotColor = (time % 2 == 0) ? 0x00FF00 : 0x00AA00;
                graphics.fill(left + width - 5, top + 6, left + width - 1, top + 10, 0xFF000000 | dotColor);
            }

            if (hovering && mouseX >= left && mouseX <= left + 16) {
                graphics.renderTooltip(Minecraft.getInstance().font, trait.icon(), mouseX, mouseY);
            }
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable(trait.translationKey() + ".title");
        }
    }

    public static class TextLineEntry extends InfoEntry {
        private final FormattedCharSequence text;
        private final int color;
        public TextLineEntry(FormattedCharSequence text, int color) { this.text = text; this.color = color; }
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, text, left, top + 2, color, false);
        }
        @Override
        public @NotNull Component getNarration() { return Component.empty(); }
    }

    public static class SpacerEntry extends InfoEntry {
        public SpacerEntry() {}
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Empty
        }
        @Override
        public @NotNull Component getNarration() { return Component.empty(); }

    }
}