package net.p3pp3rf1y.sophisticatedstorage.upgrades.compression;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompressionUpgradeConfig {
	private static final String DECOMPRESSIBLE_MATCHER = "([a-z0-9_.-]+:[a-z0-9_/.-]+)=(\\d+)x([a-z0-9_.-]+:[a-z0-9_/.-]+)";
	public final ForgeConfigSpec.IntValue maxNumberOfSlots;
	public final ForgeConfigSpec.ConfigValue<List<String>> additionalDecompressibleItems;

	@Nullable
	private Map<Item, RecipeHelper.UncompactingResult> additionalDecompressibleItemsMap = null;

	public CompressionUpgradeConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Compression Upgrade Settings").push("compressionUpgrade");
		maxNumberOfSlots = builder.comment("Defines how many slots at a maximum compression upgrade is able to use").defineInRange("maxNumberOfSlots", 5, 3, 9);
		additionalDecompressibleItems = builder.comment("List of items that can be decompressed by compression upgrade and their results. "
				+ "Item registry names are expected here in format of \"mod:itemBeingDecompressed=Nxmod:itemDecompressResult").define("additionalDecompressibleItems", getDecompressibleItemsDefault(), entries -> {
			//noinspection unchecked
			List<String> decompressibleItems = (List<String>) entries;
			return decompressibleItems != null && decompressibleItems.stream().allMatch(itemName -> itemName.matches(DECOMPRESSIBLE_MATCHER));
		});
		builder.pop();
	}

	@Nonnull
	private static List<String> getDecompressibleItemsDefault() {
		return List.of(
				getDecompressibleEntry(Items.GLOWSTONE, 4, Items.GLOWSTONE_DUST),
				getDecompressibleEntry(Items.QUARTZ_BLOCK, 4, Items.QUARTZ)
		);
	}

	private static String getDecompressibleEntry(Item fromItem, int count, Item toItem) {
		return Registry.ITEM.getKey(fromItem) + "=" + count + "x" + Registry.ITEM.getKey(toItem);
	}

	public Optional<RecipeHelper.UncompactingResult> getDecompressionResult(Item item) {
		if (additionalDecompressibleItemsMap == null) {
			additionalDecompressibleItemsMap = new HashMap<>();
			Pattern pattern = Pattern.compile(DECOMPRESSIBLE_MATCHER);
			additionalDecompressibleItems.get().forEach(decompressibleItem -> {
				Matcher matcher = pattern.matcher(decompressibleItem);
				if (matcher.find()) {
					Item fromItem = Registry.ITEM.get(new ResourceLocation(matcher.group(1)));
					int count = Integer.parseInt(matcher.group(2));
					Item toItem = Registry.ITEM.get(new ResourceLocation(matcher.group(3)));
					if (fromItem != null && toItem != null && (count == 4 || count == 9)) {
						additionalDecompressibleItemsMap.put(fromItem, new RecipeHelper.UncompactingResult(toItem, count == 4 ? RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE : RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE));
					}
				}
			});
		}
		return Optional.ofNullable(additionalDecompressibleItemsMap.get(item));
	}
}
