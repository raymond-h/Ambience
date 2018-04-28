package vazkii.ambience;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SongPicker {

	public static final String EVENT_MAIN_MENU = "mainMenu";
	public static final String EVENT_BOSS = "boss";
	public static final String EVENT_IN_NETHER = "nether";
	public static final String EVENT_IN_END = "end";
	public static final String EVENT_HORDE = "horde";
	public static final String EVENT_NIGHT = "night";
	public static final String EVENT_RAIN = "rain";
	public static final String EVENT_UNDERWATER = "underwater";
	public static final String EVENT_UNDERGROUND = "underground";
	public static final String EVENT_DEEP_UNDEGROUND = "deepUnderground";
	public static final String EVENT_HIGH_UP = "highUp";
	public static final String EVENT_VILLAGE = "village";
	public static final String EVENT_MINECART = "minecart";
	public static final String EVENT_BOAT = "boat";
	public static final String EVENT_HORSE = "horse";
	public static final String EVENT_PIG = "pig";
	public static final String EVENT_FISHING = "fishing";
	public static final String EVENT_DYING = "dying";
	public static final String EVENT_PUMPKIN_HEAD = "pumpkinHead";
	public static final String EVENT_GENERIC = "generic";

	private static final Map<String, Integer> eventPriorityMap;
	static {
		eventPriorityMap = Maps.newHashMap();
		eventPriorityMap.put(EVENT_MAIN_MENU, 500);
		eventPriorityMap.put(EVENT_BOSS, 501);
		eventPriorityMap.put(EVENT_DYING, 502);
		eventPriorityMap.put(EVENT_HORDE, 503);
		eventPriorityMap.put(EVENT_FISHING, 504);
		eventPriorityMap.put(EVENT_PUMPKIN_HEAD, 505);
		eventPriorityMap.put(EVENT_IN_NETHER, 506);
		eventPriorityMap.put(EVENT_IN_END, 507);
		eventPriorityMap.put(EVENT_MINECART, 508);
		eventPriorityMap.put(EVENT_BOAT, 509);
		eventPriorityMap.put(EVENT_HORSE, 510);
		eventPriorityMap.put(EVENT_PIG, 511);
		eventPriorityMap.put(EVENT_UNDERWATER, 512);
		eventPriorityMap.put(EVENT_DEEP_UNDEGROUND, 513);
		eventPriorityMap.put(EVENT_UNDERGROUND, 514);
		eventPriorityMap.put(EVENT_RAIN, 515);
		eventPriorityMap.put(EVENT_HIGH_UP, 516);
		eventPriorityMap.put(EVENT_NIGHT, 517);
		eventPriorityMap.put(EVENT_VILLAGE, 518);
		eventPriorityMap.put(EVENT_GENERIC, 10000);
	}

	public static void putEvent(@Nonnull String event, @Nonnull String song) {
		addMultiEventEntry(Lists.newArrayList(event), song, eventPriorityMap.containsKey(event) ? eventPriorityMap.get(event) : 1000);
	}

	public static void putBiome(@Nonnull BiomeGenBase biome, @Nonnull String song) {
		SongPicker.addMultiEventEntry(Lists.newArrayList("biome+" + biome.biomeName.replaceAll(" ", "+")), song, 2000);
	}

	public static void putBiomeType(@Nonnull BiomeDictionary.Type type, boolean primary, @Nonnull String song) {
		SongPicker.addMultiEventEntry(Lists.newArrayList("biomeType+" + type.name().toLowerCase()), song, primary ? 3000 : 4000);
	}

	public static class MultiEventEntry implements Comparable<MultiEventEntry> {
		private List<String> eventMatcher;
		private String song;
		private int priority;

		private MultiEventEntry(@Nonnull List<String> eventMatcher, @Nonnull String song, int priority) {
			this.eventMatcher = eventMatcher;
			this.song = song;
			this.priority = priority;
		}

		@Nonnull
		public List<String> getEventMatcher() {
			return eventMatcher;
		}

		@Nonnull
		public String getSong() {
			return song;
		}

		public int getPriority() {
			return priority;
		}

		@Override
		public int compareTo(@Nonnull MultiEventEntry o) {
			return priority - o.priority;
		}
	}

	private static final List<MultiEventEntry> multiEventList = new LinkedList<MultiEventEntry>();

	public static void addMultiEventEntry(
		@Nonnull
			List<String> eventMatcher,
		@Nonnull
			String song, int priority) {

		multiEventList.add(new MultiEventEntry(eventMatcher, song, priority));
		Collections.sort(multiEventList);
	}

	@Nonnull
	public static String getMultiEventJson() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(multiEventList);
	}

	public static boolean matches(List<String> eventList, List<String> againstList) {
		for(String event : eventList) {
			if(!againstList.contains(event)) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	public static String getSongMatching(@Nonnull List<String> eventList) {
		for(MultiEventEntry entry : multiEventList) {
			if(matches(entry.eventMatcher, eventList)) {
				return entry.song;
			}
		}

		return null;
	}
	
	public static void reset() {
		multiEventList.clear();
	}

	public static String getSong() {
		List<String> applicableEvents = getApplicableEvents();
		return getSongMatching(applicableEvents);
	}

	public static List<String> getApplicableEvents() {
		assert matches(Lists.newArrayList("highUp", "village"), Lists.newArrayList("biome+Plains", "biomeType+plains", "highUp", "village", "night"));
		assert !matches(Lists.newArrayList("highUp", "village"), Lists.newArrayList("biome+Plains", "biomeType+plains", "highUp", "night"));

		List<String> resultEvents = new LinkedList<String>();

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		World world = mc.theWorld;

		if(player == null || world == null) {
			resultEvents.add(EVENT_MAIN_MENU);
			return resultEvents;
		}

		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);

		AmbienceEventEvent event = new AmbienceEventEvent.Pre(world, x, y, z);
		MinecraftForge.EVENT_BUS.post(event);
		String eventr = event.event;
		if(eventr != null && !eventr.equals(""))
			resultEvents.add(eventr);

		if(BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
			resultEvents.add(EVENT_BOSS);
		}

		float hp = player.getHealth();
		if(hp < 7) {
			resultEvents.add(EVENT_DYING);
		}

		int monsterCount = world.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16)).size();
		if(monsterCount > 5) {
			resultEvents.add(EVENT_HORDE);
		}

		if(player.fishEntity != null) {
			resultEvents.add(EVENT_FISHING);
		}

		ItemStack headItem = player.getEquipmentInSlot(4);
		if(headItem != null && headItem.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
			resultEvents.add(EVENT_PUMPKIN_HEAD);
		}
		int indimension = world.provider.dimensionId;

		if(indimension == -1) {
			resultEvents.add(EVENT_IN_NETHER);
		} else if(indimension == 1) {
			resultEvents.add(EVENT_IN_END);
		}

		Entity riding = player.ridingEntity;
		if(riding != null) {
			if(riding instanceof EntityMinecart) {
				resultEvents.add(EVENT_MINECART);
			}
			if(riding instanceof EntityBoat) {
				resultEvents.add(EVENT_BOAT);
			}
			if(riding instanceof EntityHorse) {
				resultEvents.add(EVENT_HORSE);
			}
			if(riding instanceof EntityPig) {
				resultEvents.add(EVENT_PIG);
			}
		}

		if(player.isInsideOfMaterial(Material.water)) {
			resultEvents.add(EVENT_UNDERWATER);
		}

		boolean underground = !world.canBlockSeeTheSky(x, y, z);
		if(underground) {
			if(y < 20) {
				resultEvents.add(EVENT_DEEP_UNDEGROUND);
			}
			if(y < 55) {
				resultEvents.add(EVENT_UNDERGROUND);
			}
		}

		if(world.isRaining()) {
			resultEvents.add(EVENT_RAIN);
		}

		if(y > 128) {
			resultEvents.add(EVENT_HIGH_UP);
		}

		long time = world.getWorldTime() % 24000;
		if(time > 13300 && time < 23200) {
			resultEvents.add(EVENT_NIGHT);
		}

		int villagerCount = world.getEntitiesWithinAABB(EntityVillager.class, AxisAlignedBB.getBoundingBox(player.posX - 30, player.posY - 8, player.posZ - 30, player.posX + 30, player.posY + 8, player.posZ + 30)).size();
		if(villagerCount > 3) {
			resultEvents.add(EVENT_VILLAGE);
		}

		event = new AmbienceEventEvent.Post(world, x, y, z);
		MinecraftForge.EVENT_BUS.post(event);
		eventr = event.event;
		if(eventr != null && !eventr.equals(""))
			resultEvents.add(eventr);

		List<String> eventsForBiome = getEventsForBiome(world, x, y, z);
		resultEvents.addAll(eventsForBiome);

		resultEvents.add(EVENT_GENERIC);

		return resultEvents;
	}

	public static List<String> getEventsForBiome(World world, int x, int y, int z) {
		List<String> resultEvents = new LinkedList<String>();

        if(world.blockExists(x, y, z)) {
            Chunk chunk = world.getChunkFromBlockCoords(x, z);
            BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager());
            resultEvents.add("biome+" + biome.biomeName.replaceAll(" ", "+"));

            BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
            for(BiomeDictionary.Type t : types) {
	            resultEvents.add("biomeType+" + t.name().toLowerCase());
            }
        }

		return resultEvents;
	}
	
	public static String getSongName(String song) {
		return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
	}
	
}
