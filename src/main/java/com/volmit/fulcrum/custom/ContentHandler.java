package com.volmit.fulcrum.custom;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.volmit.fulcrum.Fulcrum;
import com.volmit.fulcrum.bukkit.BlockType;
import com.volmit.fulcrum.bukkit.P;
import com.volmit.fulcrum.bukkit.TICK;
import com.volmit.fulcrum.bukkit.Task;
import com.volmit.fulcrum.bukkit.TaskLater;
import com.volmit.fulcrum.bukkit.W;
import com.volmit.fulcrum.event.CustomBlockPlaceEvent;
import com.volmit.fulcrum.event.PlayerCancelledDiggingEvent;
import com.volmit.fulcrum.event.PlayerFinishedDiggingEvent;
import com.volmit.fulcrum.event.PlayerStartDiggingEvent;
import com.volmit.fulcrum.sfx.Audible;
import com.volmit.fulcrum.sfx.Audio;
import com.volmit.volume.lang.collections.GList;
import com.volmit.volume.lang.collections.GMap;
import com.volmit.volume.math.M;

public class ContentHandler implements Listener
{
	public GMap<Block, Double> digging;
	public GMap<Block, Player> lastDug;
	public GMap<Player, Integer> vdel;
	private GMap<Player, Boolean> ground;
	private GMap<Player, Double> dist;
	private GMap<Player, Integer> steps;
	public GList<Block> stopped;
	public GMap<Player, Double> view;
	public GMap<Player, GList<Location>> controlled;
	public GMap<Player, GList<Location>> hidden;
	private GList<Block> breaks;
	private GList<Block> update;
	public static double max = 80.0;
	public static double min = 12.0;

	public ContentHandler()
	{
		update = new GList<Block>();
		steps = new GMap<Player, Integer>();
		dist = new GMap<Player, Double>();
		ground = new GMap<Player, Boolean>();
		digging = new GMap<Block, Double>();
		lastDug = new GMap<Block, Player>();
		stopped = new GList<Block>();
		vdel = new GMap<Player, Integer>();
		view = new GMap<Player, Double>();
		breaks = new GList<Block>();
		controlled = new GMap<Player, GList<Location>>();
		hidden = new GMap<Player, GList<Location>>();
		Fulcrum.register(this);

		new Task(0)
		{
			@Override
			public void run()
			{
				tick();
			}
		};
	}

	public void tick()
	{
		if(!Fulcrum.registered)
		{
			return;
		}

		for(Player i : P.onlinePlayers())
		{
			if(!ground.containsKey(i))
			{
				ground.put(i, i.isOnGround());
			}

			if(ground.get(i) != i.isOnGround() && i.isOnGround())
			{
				Block b = i.getLocation().clone().add(0, -0.5, 0).getBlock();
				CustomBlock cb = ContentManager.getBlock(b);

				if(cb != null && cb.getStepSound() != null)
				{
					cb.getStepSound().play(i.getLocation());
				}

				else if(!ContentManager.isOverrided(b))
				{
					if(ContentManager.a().isCloth(b.getType()))
					{
						ContentManager.getClothFallSound().play(b.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isMetal(b.getType()))
					{
						ContentManager.getMetalFallSound().play(b.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isStone(b.getType()))
					{
						ContentManager.getStoneFallSound().play(b.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isGlass(b.getType()))
					{
						ContentManager.getGlassFallSound().play(b.getLocation().clone().add(0.5, 0.5, 0.5));
					}
				}

				else
				{
					ContentManager.getOverrided(b).getStepSound().play(b.getLocation().clone().add(0.5, 0.5, 0.5));
				}
			}

			ground.put(i, i.isOnGround());
		}

		for(Player i : vdel.k())
		{
			vdel.put(i, vdel.get(i) - 1);

			if(vdel.get(i) <= 0)
			{
				vdel.remove(i);
			}
		}

		for(Block i : digging.k())
		{
			if(vdel.containsKey(lastDug.get(i)))
			{
				continue;
			}

			ContentManager.a().damageBlock(i, digging.get(i));
			double progress = ContentManager.a().getBreakProgress(i);
			double inc = digging.get(i);

			if(progress > 1 + (inc * 20))
			{
				ContentManager.a().brokedBlock(i);
				stopped.add(i);
				continue;
			}

			if(((int) (progress * 9.0)) != ((int) ((progress - inc) * 9.0)))
			{
				ContentManager.a().sendCrack(i, getBlockPos(i), M.clip(progress, 0D, 1D));
			}

			if(ContentManager.isOverrided(i))
			{
				ContentManager.a().stopDigging(i, lastDug.get(i));

				if(TICK.tick % 5 == 0)
				{
					ContentManager.a().sendCrack(i, getBlockPos(i), M.clip(progress, 0D, 1D));
				}
			}

			if(progress > 1)
			{
				if(ContentManager.isOverrided(i))
				{
					breaks.add(i);
				}

				Fulcrum.callEvent(new BlockBreakEvent(i, lastDug.get(i)));
				stopped.add(i);
			}

			CustomBlock cb = ContentManager.getBlock(i);

			if(cb == null && ContentManager.isOverrided(i))
			{
				cb = ContentManager.getOverrided(i);
			}

			if(cb != null && TICK.tick % 5 == 0 && cb.getDigSound() != null)
			{
				if(ContentManager.isOverrided(i))
				{
					ContentManager.a().forceSwing(lastDug.get(i), lastDug.get(i));
				}

				cb.getDigSound().osc(0.35).play(i.getLocation().clone().add(0.5, 0.5, 0.5));
			}
		}

		for(Block i : stopped)
		{
			ContentManager.a().brokedBlock(i);
			ContentManager.a().sendCrack(i, getBlockPos(i), 100);
			digging.remove(i);
		}

		for(Block i : update.copy())
		{
			CustomBlock cb = ContentManager.getAny(i);

			if(cb != null)
			{
				cb.onUpdate(i);

				if(cb.hasFlag(BlockFlag.GRAVITY) && cb.getBlockRegistryType().equals(BlockRegistryType.BUILDING_BLOCK))
				{
					processGravity(cb, i);
				}
			}

			update.remove(i);
		}

		stopped.clear();
	}

	private void processGravity(CustomBlock cb, Block b)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		Block below = b.getRelative(BlockFace.DOWN);

		if(below.getType().equals(Material.AIR))
		{
			doFall(cb, b);
			notify(b);
		}
	}

	public void notify(Block b)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		for(Block i : W.blockFaces(b))
		{
			update.add(i);
		}
	}

	public void doFall(CustomBlock cb, Block b)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		b.setType(Material.AIR);
		@SuppressWarnings("deprecation")
		FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation().clone().add(0.5, 0, 0.5), cb.getType(), cb.getData());
		fb.setDropItem(true);
	}

	private int getBlockPos(Block b)
	{
		return b.getX() + b.getY() + b.getZ();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(CraftItemEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		ItemStack is = e.getCurrentItem().clone();

		if(e.getClick().equals(ClickType.SHIFT_LEFT))
		{
			if(ContentManager.isCustom(is))
			{
				new TaskLater(0)
				{
					@Override
					public void run()
					{
						ContentManager.stack(is, e.getWhoClicked().getInventory(), e.getWhoClicked(), 0);
					}
				};
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(InventoryDragEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		ItemStack is = e.getOldCursor();

		if(is != null && ContentManager.isCustom(is))
		{
			int count = ContentManager.getBlock(is) != null ? ContentManager.getBlock(is).getStackSize() : ContentManager.getItem(is).getStackSize();
			int left = e.getOldCursor().getAmount();
			int div = e.getRawSlots().size();
			int f = left / div;

			if(f <= 1)
			{
				return;
			}

			e.setCancelled(true);
			for(int i : e.getRawSlots())
			{
				int place = Math.min(f, count);
				ItemStack ix = e.getView().getItem(i);

				if(ix == null || ix.getType().equals(Material.AIR))
				{
					ItemStack iv = is.clone();
					iv.setAmount(place);
					e.getView().setItem(i, iv);

					left -= place;
				}
			}

			int ll = left;

			new TaskLater(0)
			{
				@Override
				public void run()
				{
					if(ll == 0)
					{
						e.getWhoClicked().setItemOnCursor(null);
					}

					else
					{
						ItemStack ss = e.getWhoClicked().getItemOnCursor().clone();
						ss.setAmount(ll);
						e.getWhoClicked().setItemOnCursor(ss);
					}
				}
			};
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(InventoryClickEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.getCurrentItem() == null)
		{
			return;
		}

		ItemStack is = e.getCurrentItem().clone();
		ItemStack cursor = e.getCursor();
		Inventory top = e.getView().getTopInventory();
		Inventory bottom = e.getView().getBottomInventory();
		Inventory clickedInventory = e.getClickedInventory();
		int clickedSlot = e.getSlot();

		if(e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
		{
			if(bottom != null && top != null)
			{
				Inventory other = bottom.equals(clickedInventory) ? top : bottom;

				if(is != null && ContentManager.isCustom(is))
				{
					new TaskLater(0)
					{
						@Override
						public void run()
						{
							ContentManager.stack(is, other, e.getWhoClicked(), 0);
						}
					};
				}
			}
		}

		if(e.getAction().equals(InventoryAction.COLLECT_TO_CURSOR))
		{
			if(cursor != null && ContentManager.isCustom(cursor))
			{
				int stack = ContentManager.getBlock(cursor) != null ? ContentManager.getBlock(cursor).getStackSize() : ContentManager.getItem(cursor).getStackSize();

				ItemStack[] isx = e.getClickedInventory().getContents();

				for(int i = 0; i < isx.length; i++)
				{
					ItemStack isv = isx[i];

					if(isv != null && isv.getType().equals(cursor.getType()) && isv.getDurability() == cursor.getDurability() && cursor.getItemMeta().isUnbreakable() == true && cursor.getItemMeta().isUnbreakable() == isv.getItemMeta().isUnbreakable())
					{
						if(cursor.getAmount() < stack)
						{
							if(cursor.getAmount() + isv.getAmount() <= stack)
							{
								cursor.setAmount(cursor.getAmount() + isv.getAmount());
								e.getClickedInventory().setItem(i, new ItemStack(Material.AIR));
								e.setCursor(cursor.clone());
							}
						}

						else
						{
							break;
						}
					}
				}

				if(cursor.getAmount() < stack && bottom != null && top != null)
				{
					Inventory other = bottom.equals(clickedInventory) ? top : bottom;

					isx = other.getContents();

					for(int i = 0; i < isx.length; i++)
					{
						ItemStack isv = isx[i];

						if(isv != null && isv.getType().equals(cursor.getType()) && isv.getDurability() == cursor.getDurability() && cursor.getItemMeta().isUnbreakable() == true && cursor.getItemMeta().isUnbreakable() == isv.getItemMeta().isUnbreakable())
						{
							if(cursor.getAmount() < stack)
							{
								if(cursor.getAmount() + isv.getAmount() <= stack)
								{
									cursor.setAmount(cursor.getAmount() + isv.getAmount());
									other.setItem(i, new ItemStack(Material.AIR));
									e.setCursor(cursor.clone());
								}
							}

							else
							{
								break;
							}
						}
					}
				}
			}
		}

		if(e.getAction().equals(InventoryAction.NOTHING) || e.getAction().equals(InventoryAction.PICKUP_SOME) || e.getAction().equals(InventoryAction.PICKUP_ONE))
		{
			if(e.getClick().equals(ClickType.RIGHT))
			{
				if(cursor != null && ContentManager.isCustom(cursor))
				{
					if(is != null && cursor.getType().equals(is.getType()) && cursor.getDurability() == is.getDurability() && cursor.getItemMeta().isUnbreakable() && is.getItemMeta().isUnbreakable())
					{
						e.setCancelled(true);

						if(cursor != null && ContentManager.isCustom(cursor))
						{
							if(is != null && cursor.getType().equals(is.getType()) && cursor.getDurability() == is.getDurability() && cursor.getItemMeta().isUnbreakable() && is.getItemMeta().isUnbreakable())
							{
								int count = ContentManager.getBlock(cursor) != null ? ContentManager.getBlock(cursor).getStackSize() : ContentManager.getItem(cursor).getStackSize();
								int maxPull = count - is.getAmount();

								if(cursor.getAmount() == 1 && is.getAmount() < count)
								{
									is.setAmount(is.getAmount() + 1);
									clickedInventory.setItem(clickedSlot, is.clone());
									e.setCursor(new ItemStack(Material.AIR));
								}

								else if(maxPull > 0 && cursor.getAmount() > 1)
								{
									cursor.setAmount(cursor.getAmount() - 1);
									e.setCursor(cursor.clone());
									is.setAmount(is.getAmount() + 1);
									clickedInventory.setItem(clickedSlot, is.clone());
								}
							}
						}

					}
				}
			}

			if(e.getClick().equals(ClickType.LEFT))
			{
				if(cursor != null && ContentManager.isCustom(cursor))
				{
					if(is != null && cursor.getType().equals(is.getType()) && cursor.getDurability() == is.getDurability() && cursor.getItemMeta().isUnbreakable() && is.getItemMeta().isUnbreakable())
					{
						e.setCancelled(true);

						if(cursor != null && ContentManager.isCustom(cursor))
						{
							if(is != null && cursor.getType().equals(is.getType()) && cursor.getDurability() == is.getDurability() && cursor.getItemMeta().isUnbreakable() && is.getItemMeta().isUnbreakable())
							{
								int count = ContentManager.getBlock(cursor) != null ? ContentManager.getBlock(cursor).getStackSize() : ContentManager.getItem(cursor).getStackSize();
								int maxPull = count - is.getAmount();

								while(maxPull > 0 && cursor.getAmount() > 1)
								{
									cursor.setAmount(cursor.getAmount() - 1);
									e.setCursor(cursor.clone());
									is.setAmount(is.getAmount() + 1);
									clickedInventory.setItem(clickedSlot, is.clone());
								}

								if(cursor.getAmount() == 1 && is.getAmount() < count)
								{
									is.setAmount(is.getAmount() + 1);
									clickedInventory.setItem(clickedSlot, is.clone());
									e.setCursor(new ItemStack(Material.AIR));
								}
							}
						}

					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PrepareItemCraftEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(ContentManager.r().getOass().getImpossibleRecipes().contains(e.getRecipe()))
		{
			e.getInventory().setResult(new ItemStack(Material.AIR));
			return;
		}

		if(e.isRepair())
		{
			ItemStack is = e.getRecipe().getResult();

			if(!is.getItemMeta().isUnbreakable() && ContentManager.isUsed(is.getType()))
			{
				e.getInventory().setResult(new ItemStack(Material.AIR));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerMoveEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.getPlayer().isOnGround() && (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()))
		{
			int factor = e.getPlayer().isSneaking() ? 20 : e.getPlayer().isSprinting() ? 5 : 7;
			double dd = Math.abs(e.getFrom().getX() - e.getTo().getX()) + Math.abs(e.getFrom().getZ() - e.getTo().getZ());
			dist.put(e.getPlayer(), dd + (dist.containsKey(e.getPlayer()) ? dist.get(e.getPlayer()) : 0.0));
			steps.put(e.getPlayer(), 1 + (steps.containsKey(e.getPlayer()) ? steps.get(e.getPlayer()) : 0));

			if(steps.get(e.getPlayer()) % factor == 0)
			{
				Block bb = e.getPlayer().getLocation().getBlock().getLocation().clone().add(0, -0.5, 0).getBlock();
				CustomBlock cb = ContentManager.getBlock(bb);

				if(cb != null && cb.getStepSound() != null)
				{
					cb.getStepSound().play(e.getPlayer().getLocation());
				}

				else if(!ContentManager.isOverrided(bb))
				{
					if(ContentManager.a().isCloth(bb.getType()))
					{
						ContentManager.getClothStepSound().play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isStone(bb.getType()))
					{
						ContentManager.getStoneStepSound().play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isGlass(bb.getType()))
					{
						ContentManager.getGlassStepSound().play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
					}

					if(ContentManager.a().isMetal(bb.getType()))
					{
						ContentManager.getMetalStepSound().play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
					}
				}

				else
				{
					ContentManager.getOverrided(bb).getStepSound().play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerStartDiggingEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		CustomBlock cb = ContentManager.getBlock(e.getBlock());

		if(cb != null)
		{
			boolean cancel = false;
			cb.onStartDig(e.getPlayer(), e.getBlock(), cancel);

			if(cancel)
			{
				return;
			}

			Audible aa = cb.getDigSound();

			if(aa != null)
			{
				aa.osc(0.35).play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				return;
			}

			ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
			String type = ToolType.getType(is);
			double speed = ToolLevel.getMiningSpeed(cb, is);

			if(!type.equals(cb.getToolType()))
			{
				speed = ToolLevel.getMiningSpeed(cb, null);
			}

			digging.put(e.getBlock(), speed);
			lastDug.put(e.getBlock(), e.getPlayer());
		}

		else if(!ContentManager.isOverrided(e.getBlock()))
		{
			if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				return;
			}

			if(ContentManager.a().isCloth(e.getBlock().getType()))
			{
				ContentManager.getClothHitSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isStone(e.getBlock().getType()))
			{
				ContentManager.getStoneHitSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isGlass(e.getBlock().getType()))
			{
				ContentManager.getGlassHitSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isMetal(e.getBlock().getType()))
			{
				ContentManager.getMetalHitSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			ItemStack is = e.getPlayer().getInventory().getItemInMainHand();

			if(ContentManager.isTool(is))
			{
				String type = ToolType.getType(is);

				if(Fulcrum.adapter.shouldDigFaster(e.getBlock(), type))
				{
					double speed = ToolLevel.getMiningSpeed(e.getBlock(), is);
					digging.put(e.getBlock(), speed);
					lastDug.put(e.getBlock(), e.getPlayer());
				}
			}
		}

		else
		{
			ContentManager.getOverrided(e.getBlock()).getDigSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			cb = ContentManager.getOverrided(e.getBlock());
			boolean cancel = false;
			cb.onStartDig(e.getPlayer(), e.getBlock(), cancel);

			if(cancel)
			{
				return;
			}

			Audible aa = cb.getDigSound();

			if(aa != null)
			{
				aa.osc(0.35).play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				return;
			}

			ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
			String type = ToolType.getType(is);
			double speed = ToolLevel.getMiningSpeed(cb, is);

			if(!type.equals(cb.getToolType()))
			{
				speed = ToolLevel.getMiningSpeed(cb, null);
			}

			digging.put(e.getBlock(), speed);
			lastDug.put(e.getBlock(), e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerFinishedDiggingEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		stopped.add(e.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerCancelledDiggingEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		stopped.add(e.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockBurnEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockExplodeEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		for(Block i : new GList<Block>(e.blockList()))
		{
			if((ContentManager.isOverrided(i) && ContentManager.getOverrided(i).hasFlag(BlockFlag.BLAST_RESISTANT)) || ContentManager.isCustom(i) && ContentManager.getBlock(i).hasFlag(BlockFlag.BLAST_RESISTANT))
			{
				e.blockList().remove(i);
			}

			notify(i);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityExplodeEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		for(Block i : new GList<Block>(e.blockList()))
		{
			if((ContentManager.isOverrided(i) && ContentManager.getOverrided(i).hasFlag(BlockFlag.BLAST_RESISTANT)) || ContentManager.isCustom(i) && ContentManager.getBlock(i).hasFlag(BlockFlag.BLAST_RESISTANT))
			{
				e.blockList().remove(i);
			}

			notify(i);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityPickupItemEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.getEntity() instanceof Player)
		{
			Player p = (Player) e.getEntity();

			if(ContentManager.isCustom(e.getItem()))
			{
				boolean cancel = false;
				ItemStack is = e.getItem().getItemStack().clone();
				e.setCancelled(true);
				Audible picksound = ContentManager.getPickupSound();
				CustomItem ci = ContentManager.getItem(is);
				CustomBlock cb = ContentManager.getBlock(is);

				if(ci != null)
				{
					if(ci.onPickedUp(p, e.getItem()))
					{
						e.setCancelled(true);
						return;
					}

					picksound = ci.getPickupSound();
				}

				if(cb != null)
				{
					cb.onPickedUp(p, e.getItem(), cancel);

					if(cancel)
					{
						return;
					}

					picksound = cb.getPickupSound();
				}

				float osc = (float) (0.15 * Math.sin(Math.random()));
				Audio a = new Audio(picksound);
				a.p(a.getPitch() + osc);
				a.play(e.getItem().getLocation());
				ContentManager.addToInventory(p.getInventory(), is);
				ContentManager.a().pickup(p, e.getItem());
				e.getItem().remove();
			}

			else if(ContentManager.isOverrided(e.getItem().getItemStack()))
			{
				boolean cancel = false;
				ItemStack is = e.getItem().getItemStack().clone();
				Audible picksound = ContentManager.getPickupSound();
				CustomBlock cb = ContentManager.getOverrided(is);

				if(cb != null)
				{
					cb.onPickedUp(p, e.getItem(), cancel);

					if(cancel)
					{
						e.setCancelled(true);
						return;
					}

					picksound = cb.getPickupSound();
				}

				float osc = (float) (0.15 * Math.sin(Math.random()));
				Audio a = new Audio(picksound);
				a.p(a.getPitch() + osc);
				a.play(e.getItem().getLocation());
			}

			else
			{
				ContentManager.getPickupSound().play(e.getItem().getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockPlaceEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.isCancelled())
		{
			return;
		}

		if(ContentManager.a().isMetal(e.getBlock().getType()))
		{
			if(ContentManager.isCustom(e.getBlock()))
			{
				return;
			}

			ContentManager.getMetalBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
		}

		if(!ContentManager.isOverrided(e.getBlock()))
		{
			if(ContentManager.a().isCloth(e.getBlock().getType()))
			{
				ContentManager.getClothBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isStone(e.getBlock().getType()))
			{
				ContentManager.getStoneBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isGlass(e.getBlock().getType()))
			{
				ContentManager.getGlassPlaceSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}
		}

		else
		{
			CustomBlock cb = ContentManager.getOverrided(e.getBlock());
			boolean cancel = false;
			cb.onPlaced(e.getPlayer(), e.getBlock(), e.getBlockAgainst(), e.getBlockAgainst().getFace(e.getBlock()), cancel);

			if(cancel)
			{
				e.setCancelled(true);
				return;
			}

			ContentManager.getOverrided(e.getBlock()).getPlaceSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
		}

		if(!e.isCancelled())
		{
			notify(e.getBlock());
			update.add(e.getBlock());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityChangeBlockEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockFromToEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockFormEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.getBlock().getType().equals(Material.CONCRETE_POWDER))
		{
			e.setCancelled(true);
			return;
		}

		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}

		if(ContentManager.isOverrided(new BlockType(e.getNewState().getType(), e.getNewState().getData().getData())))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockGrowEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}

		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockPhysicsEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockSpreadEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}

		if(ContentManager.isOverrided(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(FurnaceSmeltEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}

		for(ICustomRecipe i : Fulcrum.contentRegistry.getRecipes())
		{
			if(i instanceof CustomSmeltingRecipe)
			{
				CustomSmeltingRecipe c = (CustomSmeltingRecipe) i;

				if(c.getIngredient().getType().equals(e.getSource().getType()))
				{
					if(c.getIngredient().getData().getData() == e.getSource().getData().getData())
					{
						if(c.getIngredient().getDurability() == e.getSource().getDurability())
						{
							e.setCancelled(true);
							ItemStack iv = c.getResult().clone();
							iv.setAmount(e.getSource().getAmount());
							Furnace f = (Furnace) e.getBlock().getState();
							f.getInventory().setResult(iv);
							f.getInventory().setSmelting(null);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockBreakEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.isCancelled())
		{
			return;
		}

		CustomBlock cb = ContentManager.getBlock(e.getBlock());

		if(cb != null)
		{
			stopped.add(e.getBlock());
			ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
			double hardness = cb.getHardness();
			double speed = ToolLevel.getMiningSpeed(cb, is);
			String toolType = ToolType.getType(is);
			int level = ToolLevel.getToolLevel(is);
			boolean shouldDrop = !e.getPlayer().getGameMode().equals(GameMode.CREATIVE);
			boolean instantBreak = false;
			boolean toolsMatch = toolType.equals(cb.getToolType());

			boolean cancel = false;
			cb.onBroke(e.getPlayer(), e.getBlock(), cancel);

			if(cancel)
			{
				e.setCancelled(true);
				return;
			}

			if(level < cb.getMinimumToolLevel())
			{
				shouldDrop = false;
			}

			if(cb.getMinimumToolLevel() > ToolLevel.HAND && !toolsMatch)
			{
				shouldDrop = false;
			}

			if((speed / 20D) * 30D > hardness && toolsMatch)
			{
				instantBreak = true;
			}

			if(shouldDrop && !e.isDropItems())
			{
				shouldDrop = false;
			}

			ContentManager.breakBlock(e.getBlock(), shouldDrop);
			e.setDropItems(false);
			e.setExpToDrop(0);

			if(!instantBreak)
			{
				vdel.put(e.getPlayer(), 5);
			}

			else
			{
				vdel.put(e.getPlayer(), 1);
			}
		}

		else if(ContentManager.a().isMetal(e.getBlock().getType()))
		{
			ContentManager.getMetalBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
		}

		if(!ContentManager.isOverrided(e.getBlock()))
		{
			if(ContentManager.a().isCloth(e.getBlock().getType()))
			{
				ContentManager.getClothBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isStone(e.getBlock().getType()))
			{
				ContentManager.getStoneBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.a().isGlass(e.getBlock().getType()))
			{
				ContentManager.getGlassBreakSound().play(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
			}

			if(ContentManager.isTool(e.getPlayer().getInventory().getItemInMainHand()))
			{
				ItemStack iv = e.getPlayer().getInventory().getItemInMainHand();
				int level = ContentManager.getTool(e.getPlayer().getInventory().getItemInMainHand()).getToolLevel();

				if(level >= ContentManager.a().getMinimumLevel(e.getBlock()) && e.isDropItems())
				{
					e.getBlock().breakNaturally();
				}

				else
				{
					e.getBlock().breakNaturally(new ItemStack(Material.AIR));
				}

				double hardness = Fulcrum.adapter.getHardness(e.getBlock());
				double speed = ToolLevel.getMiningSpeed(e.getBlock(), iv);
				boolean instantBreak = false;
				boolean toolsMatch = ToolType.getType(iv).equals(Fulcrum.adapter.getEffectiveTool(e.getBlock()));

				if((speed / 20D) * 30D > hardness && toolsMatch)
				{
					instantBreak = true;
				}

				if(!instantBreak)
				{
					vdel.put(e.getPlayer(), 5);
				}

				else
				{
					vdel.put(e.getPlayer(), 1);
				}
			}
		}

		else
		{
			if(!breaks.contains(e.getBlock()) && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				e.setCancelled(true);
				return;
			}

			cb = ContentManager.getOverrided(e.getBlock());
			breaks.remove(e.getBlock());
			stopped.add(e.getBlock());
			ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
			double hardness = cb.getHardness();
			String toolType = ToolType.getType(is);
			int level = ToolLevel.getToolLevel(is);
			double speed = ToolLevel.getMiningSpeed(cb, is);
			boolean shouldDrop = !e.getPlayer().getGameMode().equals(GameMode.CREATIVE);
			boolean instantBreak = false;
			boolean toolsMatch = toolType.equals(cb.getToolType());
			boolean cancel = false;
			cb.onBroke(e.getPlayer(), e.getBlock(), cancel);

			if(cancel)
			{
				e.setCancelled(true);
				return;
			}

			if(level < cb.getMinimumToolLevel())
			{
				shouldDrop = false;
			}

			if(cb.getMinimumToolLevel() > ToolLevel.HAND && !toolsMatch)
			{
				shouldDrop = false;
			}

			if((speed / 20D) * 30D > hardness && toolsMatch)
			{
				instantBreak = true;
			}

			if(shouldDrop && !e.isDropItems())
			{
				shouldDrop = false;
			}

			ContentManager.breakBlock(e.getBlock(), shouldDrop);
			e.setDropItems(false);
			e.setExpToDrop(0);

			if(!instantBreak)
			{
				vdel.put(e.getPlayer(), 5);
			}

			else
			{
				vdel.put(e.getPlayer(), 1);
			}
		}

		if(!e.isCancelled())
		{
			notify(e.getBlock());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(BlockDamageEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}

		if(ContentManager.isOverrided(e.getBlock()))
		{
			lastDug.put(e.getBlock(), e.getPlayer());

			if(e.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
			{
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerInteractEvent e)
	{
		if(!Fulcrum.registered)
		{
			return;
		}
		if(e.isCancelled())
		{
			return;
		}

		if(e.getItem() == null)
		{
			return;
		}

		ItemStack is = e.getItem();
		EquipmentSlot hand = is.equals(e.getPlayer().getInventory().getItemInMainHand()) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
		CustomItem ci = ContentManager.getItem(is);

		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if(!e.getPlayer().isSneaking() && Fulcrum.adapter.isTileEntity(e.getClickedBlock()))
			{
				return;
			}

			CustomBlock cb = ContentManager.getBlock(is);

			if(ci != null)
			{
				boolean cancel = false;

				ci.onUsed(e.getPlayer(), hand, e.getAction(), e.getClickedBlock(), e.getBlockFace(), cancel);

				if(cancel)
				{
					e.setCancelled(true);
					return;
				}

				if(e.getClickedBlock().getType().equals(Material.GRASS) || e.getClickedBlock().getType().equals(Material.DIRT) || e.getClickedBlock().getType().equals(Material.GRASS_PATH))
				{
					e.setCancelled(true);
				}

				return;
			}

			if(cb != null)
			{
				Block target = null;
				Block clicked = e.getClickedBlock();

				if(clicked.getType().isSolid())
				{
					target = clicked.getRelative(e.getBlockFace());
				}

				else
				{
					target = clicked;
				}

				if(!ContentManager.a().canPlace(e.getPlayer(), target))
				{
					e.setCancelled(true);
					return;
				}

				BlockState state = target.getState();
				ContentManager.setBlock(target, cb);
				BlockPlaceEvent bp = new BlockPlaceEvent(target, state, clicked, is, e.getPlayer(), true, hand);
				Fulcrum.callEvent(bp);
				CustomBlockPlaceEvent ee = new CustomBlockPlaceEvent(target, cb, e.getPlayer());
				Fulcrum.callEvent(ee);
				boolean cancel = false;
				cb.onPlaced(e.getPlayer(), target, target.getRelative(e.getBlockFace().getOppositeFace()), e.getBlockFace(), cancel);

				if(ee.isCancelled() || bp.isCancelled() || cancel)
				{
					target.setType(state.getType(), false);
					target.getState().setData(state.getData());
					return;
				}

				e.setCancelled(true);
				ContentManager.placeBlock(target, cb);

				if(!target.equals(clicked))
				{
					Block bb = target.getRelative(e.getBlockFace().getOppositeFace());

					if(ContentManager.isCustom(bb))
					{
						CustomBlock cc = ContentManager.getBlock(bb);

						if(cc != null)
						{
							if(cc.getPlaceSound() != null)
							{
								new Audio(cc.getPlaceSound()).v(0.9f).play(bb.getLocation().clone().add(0.5, 0.5, 0.5));
							}
						}
					}
				}

				if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
				{
					ItemStack result = is.clone();

					if(is.getAmount() <= 1)
					{
						result = new ItemStack(Material.AIR);
					}

					else
					{
						result.setAmount(is.getAmount() - 1);
					}

					if(hand.equals(EquipmentSlot.HAND))
					{
						e.getPlayer().getInventory().setItemInMainHand(result);
					}

					else
					{
						e.getPlayer().getInventory().setItemInOffHand(result);
					}
				}
			}
		}

		else if(e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			lastDug.put(e.getClickedBlock(), e.getPlayer());
		}

		else
		{
			boolean cancel = false;

			if(ci == null)
			{
				return;
			}

			ci.onUsed(e.getPlayer(), hand, e.getAction(), e.getClickedBlock(), e.getBlockFace(), cancel);

			if(cancel)
			{
				e.setCancelled(true);
				return;
			}
		}
	}
}
