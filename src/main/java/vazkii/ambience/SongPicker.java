package vazkii.ambience;

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	public static final Map<String, String> eventMap = new HashMap();
	public static final Map<BiomeGenBase, String> biomeMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String> primaryTagMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String> secondaryTagMap = new HashMap();
	
	public static void reset() {
		eventMap.clear();
		biomeMap.clear();
		primaryTagMap.clear();
		secondaryTagMap.clear();
	}

	public static String getSong() {
		List<String> applicableEvents = getApplicableEvents();
		String event = applicableEvents.get(0);

		if(event.startsWith("biome:"))
			return event.substring("biome:".length());

		if(event.startsWith("biomeType:"))
			return event.substring("biomeType:".length());

		return getSongForEvent(event);
	}

	public static List<String> getApplicableEvents() {
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

		String eventForBiome = getEventForBiome(world, x, y, z);
		if(eventForBiome != null) {
			resultEvents.add(eventForBiome);
		}

		resultEvents.add(EVENT_GENERIC);

		return resultEvents;
	}

	public static String getSongForEvent(String event) {
		if(eventMap.containsKey(event))
			return eventMap.get(event);

		return null;
	}

	public static String getEventForBiome(World world, int x, int y, int z) {
        if(world.blockExists(x, y, z)) {
            Chunk chunk = world.getChunkFromBlockCoords(x, z);
            BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager());
            if(biomeMap.containsKey(biome))
                return "biome:" + biomeMap.get(biome);

            BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
            for(BiomeDictionary.Type t : types)
                if(primaryTagMap.containsKey(t))
                    return "biomeType:" + primaryTagMap.get(t);
            for(BiomeDictionary.Type t : types)
                if(secondaryTagMap.containsKey(t))
                    return "biomeType:" + secondaryTagMap.get(t);
        }

		return null;
	}
	
	public static String getSongName(String song) {
		return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
	}
	
}
