package com.rickweek.ambitiousbees.client;

import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.item.BeeBeeGunItem;
import com.rickweek.ambitiousbees.menu.GunMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Hexagonal loading screen of the Bee-Bee Gun: six cells for queens around a "chamber" cell that shows the
 * selected one (right click a bee to select it), the feed cell on the left and the honey gauge on the right.
 */
public class GunScreen extends AbstractContainerScreen<GunMenu> {
	private static final ResourceLocation TEXTURE =
			ResourceLocation.fromNamespaceAndPath(AmbitiousBees.MOD_ID, "textures/gui/bee_bee_gun.png");
	/** Centers of the six bee cells, in the same order as the menu slots. */
	private static final int[][] CELLS = {{88, 19}, {109, 31}, {109, 55}, {88, 67}, {67, 55}, {67, 31}};
	private static final int GAUGE_X = 138;
	private static final int GAUGE_Y = 14;
	private static final int GAUGE_W = 12;
	private static final int GAUGE_H = 60;

	public GunScreen(GunMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int fill = Math.min(GAUGE_H, menu.getHoney() * GAUGE_H / BeeBeeGunItem.CAPACITY);
		if (fill > 0) {
			int x = leftPos + GAUGE_X;
			int bottom = topPos + GAUGE_Y + GAUGE_H;
			graphics.fill(x, bottom - fill, x + GAUGE_W, bottom, 0xFFE8A317);
			graphics.fill(x, bottom - fill, x + GAUGE_W, bottom - fill + 1, 0xFFFFD866);
			graphics.fill(x + GAUGE_W - 3, bottom - fill + 1, x + GAUGE_W, bottom, 0xFFC98410);
		}

		ItemStack chamber = menu.getSelectedBee();
		if (!chamber.isEmpty()) {
			graphics.renderItem(chamber, leftPos + 80, topPos + 35);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
		if (mouseX >= leftPos + GAUGE_X - 2 && mouseX < leftPos + GAUGE_X + GAUGE_W + 2
				&& mouseY >= topPos + GAUGE_Y - 2 && mouseY < topPos + GAUGE_Y + GAUGE_H + 2) {
			graphics.renderTooltip(font, Component.translatable("tooltip.ambitious_bees.gun_honey",
					menu.getHoney(), BeeBeeGunItem.CAPACITY), mouseX, mouseY);
		}
	}

	/** Light labels: the panel is dark. */
	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, 0xE8E8E8, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xA8A8B0, false);
		// The selection ring is drawn here, after the slots, so it frames the bee instead of being covered by it.
		int[] cell = CELLS[menu.getSelected()];
		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, 0.0F, 300.0F);
		graphics.blit(TEXTURE, cell[0] - 14, cell[1] - 12, 0, 170, 28, 24);
		graphics.pose().popPose();
	}

	/** Right click on a bee (with an empty cursor) selects it instead of picking it up. */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 1 && menu.getCarried().isEmpty() && minecraft != null && minecraft.gameMode != null) {
			Slot slot = hoveredSlot;
			if (slot != null && menu.beeIndex(slot) >= 0) {
				minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.beeIndex(slot));
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
