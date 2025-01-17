package fr.maxlego08.zitemstacker;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.maxlego08.zitemstacker.save.Config;
import fr.maxlego08.zitemstacker.zcorea.utils.ZUtils;

public class ZItem extends ZUtils implements fr.maxlego08.zitemstacker.api.Item {

	private transient Item item;
	private final UUID uuid;
	private int amount;
	private final long createdAt;
	private final long expireAt;

	/**
	 * @param item
	 */
	public ZItem(Item item) {
		super();
		this.item = item;
		this.createdAt = System.currentTimeMillis();
		this.expireAt = System.currentTimeMillis() + (1000 * Config.expireItemSeconds);
		this.uuid = item.getUniqueId();
		this.amount = item.getItemStack().getAmount();
		this.item.getItemStack().setAmount(1);

		setItemName();
	}

	public boolean isValid() {
		return this.expireAt > System.currentTimeMillis();
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item == null ? item = create() : item;
	}

	private Item create() {
		for (World world : Bukkit.getWorlds()) {
			Entity entity = world.getEntities().stream().filter(e -> {
				return e.getType().equals(EntityType.DROPPED_ITEM) && e.getUniqueId().equals(uuid);
			}).findFirst().orElse(null);
			return (Item) entity;
		}
		return null;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
		setItemName();
	}

	public void add(int amount) {
		this.amount += amount;
		setItemName();
	}

	public void remove(int amount) {
		this.amount -= amount;
		setItemName();
	}

	public boolean isSimilar(ItemStack itemStack) {
		return itemStack != null && isValid() && this.getItem() != null && this.getItem().getItemStack() != null
				&& this.getItem().getItemStack().isSimilar(itemStack);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public boolean give(Inventory inventory) {

		if (this.getItem() == null || this.getItem().getItemStack() == null)
			return false;

		int inventorySize = inventory.getType().equals(InventoryType.HOPPER) ? 5 : 36;
		ItemStack itemStack = this.getItem().getItemStack();
		for (int a = 0; a != inventorySize; a++) {

			if (this.amount <= 0)
				return true;

			ItemStack currentItem = inventory.getItem(a);

			// Si l'item est null alors on peut ajouter 64
			if (currentItem == null) {

				int newAmount = Math.min(itemStack.getMaxStackSize(), this.amount);
				this.amount -= newAmount;

				ItemStack newItemStack = itemStack.clone();
				newItemStack.setAmount(newAmount);

				// inventory.setItem(a, newItemStack);
				inventory.addItem(newItemStack);

			}
			// Si l'item est le même
			else if (itemStack.isSimilar(currentItem) && currentItem.getAmount() < currentItem.getMaxStackSize()) {

				int freeAmount = currentItem.getMaxStackSize() - currentItem.getAmount();
				int newAmount = Math.min(freeAmount, this.amount);

				this.amount -= newAmount;

				currentItem.setAmount(currentItem.getAmount() + newAmount);

			}

			if (this.amount <= 0)
				return true;

		}

		setItemName();
		return true;
	}

	public void setItemName() {
		if (this.getItem() != null) {

			if (this.amount == 1 && Config.disableItemNameIfItsOne) {
				this.getItem().setCustomNameVisible(Config.customNameVisible);
			} else {
				String name = color(Config.itemName);
				name = name.replace("%amount%", String.valueOf(this.amount));
				name = name.replace("%item%", name(getItem().getItemStack()));
				this.getItem().setCustomNameVisible(Config.customNameVisible);
				this.getItem().setCustomName(color(name));
			}
		}
	}

	public void remove() {
		if (this.getItem() != null)
			this.getItem().remove();
	}

	@Override
	public Item toBukkitEntity() {
		return this.getItem();
	}

	@Override
	public long getCreatedAt() {
		return this.createdAt;
	}

	@Override
	public long getExpireAt() {
		return this.expireAt;
	}

}
