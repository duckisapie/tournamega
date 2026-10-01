package com.tournament.managers;

import com.tournament.TournamentPlugin;
import com.tournament.model.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitManager {
	private TournamentPlugin plugin;
	private Map<String, Kit> kits;

	public KitManager(TournamentPlugin plugin) {
		this.plugin = plugin;
		this.kits = new HashMap<>();
		loadKitsFromConfig();
	}

	private void loadKitsFromConfig() {
		ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
		if (kitsSection == null) {
			plugin.getLogger().warning("No kits configured in config.yml");
			return;
		}

		for (String kitKey : kitsSection.getKeys(false)) {
			ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
			String kitName = kitSection.getString("name", kitKey);
			double rarity = kitSection.getDouble("rarity", 0.5);

			Kit kit = new Kit(kitName, rarity);

			ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
			if (itemsSection != null) {
				for (String itemKey : itemsSection.getKeys(false)) {
					String materialStr = itemsSection.getString(itemKey + ".material", "STONE").toUpperCase();
					int amount = itemsSection.getInt(itemKey + ".amount", 1);

					try {
						Material material = Material.valueOf(materialStr);
						kit.addItem(new ItemStack(material, Math.max(1, amount)));
					} catch (IllegalArgumentException e) {
						plugin.getLogger().warning("Invalid material in kit " + kitKey + ": " + materialStr);
					}
				}
			}

			kits.put(kitKey, kit);
		}

		plugin.getLogger().info("Loaded " + kits.size() + " kits from config");
	}

	public Kit getKit(String kitId) {
		return kits.get(kitId);
	}

	public Map<String, Kit> getAllKits() {
		return new HashMap<>(kits);
	}

	public Kit getRandomKit() {
		if (kits.isEmpty()) {
			return null;
		}

		List<Kit> kitList = new ArrayList<>(kits.values());
		double totalWeight = 0;
		for (Kit kit : kitList) {
			totalWeight += kit.getRarity();
		}

		double random = Math.random() * totalWeight;
		double current = 0;
		for (Kit kit : kitList) {
			current += kit.getRarity();
			if (random <= current) {
				return kit;
			}
		}

		return kitList.get(0);
	}

	public List<ItemStack> getKitItems(String kitId) {
		Kit kit = kits.get(kitId);
		if (kit == null) {
			return new ArrayList<>();
		}

		List<ItemStack> items = new ArrayList<>();
		for (ItemStack item : kit.getItems()) {
			if (item != null) {
				items.add(item.clone());
			}
		}
		return items;
	}
}
