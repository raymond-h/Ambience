package vazkii.ambience;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import org.apache.logging.log4j.Level;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;

public final class SongLoader {

	public static File mainDir;
	public static boolean enabled = false;
	
	public static void loadFrom(File f) {
		File config = new File(f, "ambience.properties");
		if(!config.exists())
			initConfig(config); 
		
		Properties props = new Properties();
		try {
			props.load(new FileReader(config));
			enabled = props.getProperty("enabled").equals("true");
			
			if(enabled) {
				SongPicker.reset();
				Set<Object> keys = props.keySet();
				for(Object obj : keys) {
					String s = (String) obj;
					
					String[] tokens = s.split("\\.");
					if(tokens.length < 2)
						continue;

					String keyType = tokens[0];
					if(keyType.equals("event")) {
						String event = tokens[1];
						
						SongPicker.putEvent(event, props.getProperty(s));
					} else if(keyType.equals("biome")) {
						String biomeName = joinTokensExceptFirst(tokens).replaceAll("\\+", " ");
						BiomeGenBase biome = BiomeMapper.getBiome(biomeName);
						
						if(biome != null) {
							SongPicker.putBiome(biome, props.getProperty(s));
						}
					} else if(keyType.matches("primarytag|secondarytag")) {
						boolean primary = keyType.equals("primarytag");
						String tagName = tokens[1].toUpperCase();
						BiomeDictionary.Type type = BiomeMapper.getBiomeType(tagName);
						
						if(type != null) {
							SongPicker.putBiomeType(type, primary, props.getProperty(s));
						}
					} else if(keyType.equals("multi")) {
						int priority = Integer.parseInt(tokens[1], 10);
						List<String> events = ImmutableList.copyOf(tokens[2].split("\\|"));
						SongPicker.addMultiEventEntry(events, props.getProperty(s), priority);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		FMLLog.log(Level.INFO, "%s", SongPicker.getMultiEventJson());
		File musicDir = new File(f, "music");
		if(!musicDir.exists())
			musicDir.mkdir();
			
		mainDir = musicDir;
	}
	
	public static void initConfig(File f) {
		try {
			f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("# Ambience Config\n");
			writer.write("enabled=false");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static InputStream getStream() {
		if(PlayerThread.currentSong == null || PlayerThread.currentSong.equals("null"))
			return null;
		
		File f = new File(mainDir, PlayerThread.currentSong + ".mp3");
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			FMLLog.log(Level.ERROR, "File " + f + " not found. Fix your Ambience config!");
			e.printStackTrace();
			return null;
		}
	}
	
	private static String joinTokensExceptFirst(String[] tokens) {
		String s = "";
		int i = 0;
		for(String token : tokens) {
			i++;
			if(i == 1)
				continue;
			s += token;
		}
		return s;
	}
}
