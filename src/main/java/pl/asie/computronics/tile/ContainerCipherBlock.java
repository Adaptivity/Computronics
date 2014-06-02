package pl.asie.computronics.tile;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import pl.asie.lib.block.ContainerBase;
import pl.asie.lib.block.TileEntityInventory;

public class ContainerCipherBlock extends ContainerBase {

	public ContainerCipherBlock(TileEntityInventory entity,
			InventoryPlayer inventoryPlayer) {
		super(entity, inventoryPlayer);
		for(int i = 0; i < 6; i++) {
			this.addSlotToContainer(new Slot(entity, i, 35 + (i * 18), 34));
		}
		this.bindPlayerInventory(inventoryPlayer, 8, 84);
	}

}