package com.volmit.fulcrum.adapter;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.volmit.fulcrum.bukkit.BlockType;
import com.volmit.fulcrum.custom.ContentRegistry;
import com.volmit.fulcrum.resourcepack.ResourcePack;
import com.volmit.volume.lang.collections.GList;

public interface IAdapter extends Listener
{
	public int cacheBlockData(ContentRegistry r);

	public void fleeFrom(Creature e, Location l, int ticks);

	public void lungeTo(Creature e, Location l, int vticks);

	public void causeNoise(Location l, double loudness);

	public void pathfindToLocation(Creature e, Location l, boolean sprint, double speed);

	public String getEffectiveTool(Block b);

	public String getEffectiveTool(Material b);

	public boolean shouldDigFaster(Block b, String tool);

	public int getMinimumLevel(Block b);

	public double getHardness(Material t);

	public double getHardness(Block t);

	public void forceSwing(Player p, Player ob);

	public void stopDigging(Block block, Player p);

	public boolean isMetal(Material type);

	public boolean isCloth(Material type);

	public boolean isStone(Material type);

	public boolean isGlass(Material type);

	public void queueUpdate(Block b);

	public void pushPhysics();

	public boolean isPushingPhysics();

	public void popPhysics();

	public ItemStack getSkull(String uri) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException;

	public void makeSectionDirty(Location l);

	public void applyPhysics(Block b);

	public void sendReload(Chunk c);

	public void sendReload(Chunk c, Player p);

	public void notifyEntity(Entity e);

	public void notifyEntity(Entity e, Player p);

	public void sendChunkSection(Chunk c, int bitmask, Player p);

	public void sendUnload(Chunk c);

	public void sendUnload(Chunk c, Player p);

	public int getBiomeId(Biome biome);

	public Biome getBiome(int id);

	public void setBiome(World world, int x, int z, Biome b);

	public BlockType getBlock(Location location);

	public void setBlock(Location l, BlockType m);

	public void makeDirty(Chunk c, int section);

	public void makeDirty(Chunk c);

	public void makeFullyDirty(Chunk c);

	public int getBitmask(Chunk c);

	public boolean[] getValidSections(Chunk c);

	public void makeDirty(Location l);

	public void sendBlockChange(Location l, BlockType t, Player player);

	public void sendMultiBlockChange(Chunk c, GList<Location> points) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException;

	public void sendChunkSection(Chunk c, int section);

	public BlockType getBlockAsync(Location location);

	public int getGhostSize();

	public void sendResourcePackPacket(Player p, String url);

	public void sendResourcePack(Player p, String url);

	public void sendResourcePack(Player p, ResourcePack pack);

	public void sendResourcePackWeb(Player p, String pack);

	public void sendResourcePackPrepare(Player p, Runnable r);

	public void updateBlockData(Location block, String mojangson);

	public void updateBlockData(Location block, String mojangson, boolean notify);

	public void sendActionBar(String s, Player p);

	public void sendTitle(String title, String subtitle, int i, int s, int o, Player p);

	public void sendTabHeaderFooter(String header, String footer, Player p);

	public void sendMessage(String s, Player p);

	public void sendSystemMessage(String s, Player p);

	public void sendPacket(Player p, Object packet);

	public void sendPacket(Location l, Object packet);

	public void sendPacket(Chunk c, Object packet);

	public void sendPacket(World world, Object packet);

	public void sendPacket(Object packet);

	public String getServerPublicAddress();

	public void setSpawnerType(Location block, int superid);

	public int getSpawnerType(Location block);

	public void setSpawnerType(Location block, String mat, short dmg, boolean enchanted);

	public void sendResourcePackPacket(Player p, String url, byte[] hash);

	public void pickup(Entity who, Entity item);

	public void damageBlock(Block b, double percent);

	public void brokedBlock(Block b);

	public boolean shouldBeBroken(Block b);

	public boolean isBeingBroken(Block b);

	public double getBreakProgress(Block b);

	public void sendCrack(Block b, Entity i, double progress);

	public void sendCrack(Block b, int eid, double progress);

	public boolean canPlace(Player player, Block target);

	public void setBlockNoPacket(Location l, BlockType m);

	public void resetSpawnerRotation(Location block);

	public void sendAdvancementSubtle(Player p, ItemStack is, String text);

	public void hideSpawner(Location block);

	public GList<Location> getSpawners(Chunk c);

	public void showSpawner(Location block);

	public void sendAdvancementIntense(Player p, ItemStack is, String text);

	public boolean isTileEntity(Block b);

	public PotionEffect getGlowEffect(DyeColor color);
}
