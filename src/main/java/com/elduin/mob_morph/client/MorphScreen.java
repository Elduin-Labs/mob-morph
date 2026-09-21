package com.elduin.mob_morph.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

/** The menu you get with B: every mob you have killed, and a way back to yourself. */
public class MorphScreen extends Screen {

	private static final int COLUMNS = 3;
	private static final int ROWS = 5;
	private static final int PER_PAGE = COLUMNS * ROWS;
	private static final int BUTTON_W = 120;
	private static final int BUTTON_H = 20;
	private static final int GAP = 4;

	private List<EntityType<?>> unlocked = List.of();
	private int page;

	public MorphScreen() {
		super(Component.translatable("screen.mob_morph.title"));
	}

	@Override
	protected void init() {
		unlocked = ClientMorphs.unlockedTypes();

		int pages = Math.max(1, (unlocked.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.min(page, pages - 1);

		int gridW = COLUMNS * BUTTON_W + (COLUMNS - 1) * GAP;
		int left = (this.width - gridW) / 2;
		int top = 44;

		int first = page * PER_PAGE;
		int last = Math.min(first + PER_PAGE, unlocked.size());

		for (int i = first; i < last; i++) {
			EntityType<?> type = unlocked.get(i);
			int slot = i - first;
			int x = left + (slot % COLUMNS) * (BUTTON_W + GAP);
			int y = top + (slot / COLUMNS) * (BUTTON_H + GAP);

			addRenderableWidget(Button.builder(type.getDescription(), button -> {
				MobMorphClient.requestMorph(type);
				onClose();
			}).bounds(x, y, BUTTON_W, BUTTON_H).build());
		}

		int bottom = top + ROWS * (BUTTON_H + GAP) + 8;

		if (pages > 1) {
			addRenderableWidget(Button.builder(Component.literal("<"), button -> {
				page = Math.max(0, page - 1);
				rebuild();
			}).bounds(left, bottom, 40, BUTTON_H).build());

			addRenderableWidget(Button.builder(Component.literal(">"), button -> {
				page = Math.min(pages - 1, page + 1);
				rebuild();
			}).bounds(left + gridW - 40, bottom, 40, BUTTON_H).build());
		}

		addRenderableWidget(Button.builder(
				Component.translatable("screen.mob_morph.be_yourself"), button -> {
					MobMorphClient.requestMorph(null);
					onClose();
				}).bounds(this.width / 2 - 80, bottom + BUTTON_H + GAP, 160, BUTTON_H).build());
	}

	private void rebuild() {
		clearWidgets();
		init();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);

		if (unlocked.isEmpty()) {
			graphics.drawCenteredString(this.font,
					Component.translatable("screen.mob_morph.nothing_yet"),
					this.width / 2, this.height / 2 - 4, 0xA0A0A0);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
