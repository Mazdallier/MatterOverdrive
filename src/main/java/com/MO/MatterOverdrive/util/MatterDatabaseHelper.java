package com.MO.MatterOverdrive.util;

import com.MO.MatterOverdrive.api.matter.IMatterDatabase;
import com.MO.MatterOverdrive.api.matter.IMatterPatternStorage;

import cofh.lib.util.helpers.MathHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class MatterDatabaseHelper 
{
	public static final int MAX_ITEM_PROGRESS = 100;
	public static final String PROGRESS_TAG_NAME = "scan_progress";
	public static final String ITEMS_TAG_NAME = "items";
	public static final String CAPACITY_TAG_NAME = "Capacity";

    public static void InitTagCompound(ItemStack scanner)
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        scanner.setTagCompound(tagCompound);
        InitItemListTagCompound(scanner);

    }
	
	public static void InitItemListTagCompound(ItemStack scanner)
	{
		NBTTagList items = new NBTTagList();
		scanner.setTagInfo(ITEMS_TAG_NAME, items);
	}
	
	public static Item GetItem(World world,int x,int y,int z)
	{
		return world.getBlock(x, y, z).getItem(world, x, y, z);
	}
	
	private static NBTTagCompound newNBTTagCompoundDatabase()
	{
		NBTTagCompound comp = new NBTTagCompound();
		return comp;
	}

	public static int GetPatternCapacity(ItemStack storage)
	{
		if(storage != null) {
			return GetPatternCapacity(storage.getTagCompound());
		}
		return 0;
	}
	public static int GetPatternCapacity(NBTTagCompound storageCompund)
	{
		if (storageCompund != null)
		{
			return storageCompund.getShort(MatterDatabaseHelper.CAPACITY_TAG_NAME);
		}
		return 0;
	}

	public static boolean HasFreeSpace(ItemStack storage)
	{
		if (storage != null)
		{
			if(MatterHelper.isMatterPatternStorage(storage))
			{
				//chek to see if the storage has initialized it's NBT
				if (storage.hasTagCompound())
				{
					return HasFreeSpace(storage.getTagCompound());
				}
				else
				{
					//the pattern storage NBT wasn't created so that means it has free space
					return true;
				}
			}

		}
		return false;
	}
	public static boolean HasFreeSpace(NBTTagCompound storageCompund)
	{
		if(storageCompund != null)
		{
			NBTTagList itemList = storageCompund.getTagList(ITEMS_TAG_NAME, 10);
			if (itemList.tagCount() < MatterDatabaseHelper.GetPatternCapacity(storageCompund))
			{
				return true;
			}
		}
		return false;
	}

	public static ItemStack getFirstFreePatternStorage(IMatterDatabase database)
	{
		ItemStack[] patternStorages = database.getPatternStorageList();

		for (int i = 0;i < patternStorages.length;i++)
		{
			if (patternStorages[i] != null)
			{
				if (HasFreeSpace(patternStorages[i]))
				{
					return patternStorages[i];
				}
			}
		}
		return null;
	}
	
	public static boolean HasItem(ItemStack scanner,ItemStack item)
	{
		return HasItem(scanner.getTagCompound(), item);
	}
	public static boolean HasItem(NBTTagCompound tagcompound, ItemStack item)
	{
			if(tagcompound != null)
			{
				NBTTagList itemList = tagcompound.getTagList(ITEMS_TAG_NAME, 10);
				
				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(areEqual(GetItemStackFromNBT(itemList.getCompoundTagAt(i)), item))
					{
						return true;
					}
				}
			}
		
		return false;
	}
	public static boolean HasItem(ItemStack scanner,String id)
	{
		return HasItem(scanner.getTagCompound(), id);
	}
	public static boolean HasItem(NBTTagCompound tagcompound, String id)
	{
		if(tagcompound != null)
		{
			NBTTagCompound items = tagcompound;

			if(items != null)
			{
				NBTTagList itemList = items.getTagList(ITEMS_TAG_NAME, 10);

				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(GetItemStackFromNBT(itemList.getCompoundTagAt(i)).getUnlocalizedName() == id)
					{
						return true;
					}
				}
			}
		}

		return false;
	}
	public static boolean HasItem(ItemStack database, int id) 
	{
		return HasItem(database.getTagCompound(),id);
	}
	public static boolean HasItem(NBTTagCompound tagcompound, int id)
	{
		if(tagcompound != null)
		{
			NBTTagCompound items = tagcompound;
			
			if(items != null)
			{
				NBTTagList itemList = items.getTagList(ITEMS_TAG_NAME, 10);
				
				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(itemList.getCompoundTagAt(i).getShort("id") == id)
						return true;
				}
			}
		}
		
		return false;
	}
	
	public static int GetProgressFromNBT(NBTTagCompound itemNBT)
	{
		if(itemNBT != null)
			return itemNBT.getByte(PROGRESS_TAG_NAME);
		return 0;
	}

	public static void SetProgressToNBT(NBTTagCompound itemNBT,byte amount)
	{
		if(itemNBT != null)
			 itemNBT.setByte(PROGRESS_TAG_NAME, amount);
	}
	
	public static int GetItemProgress(ItemStack storage, ItemStack item)
	{
		NBTTagCompound tag = GetItemAsNBT(storage,item);
		
		if(tag != null && tag.hasKey(PROGRESS_TAG_NAME))
		{
			return tag.getByte(PROGRESS_TAG_NAME);
		}
		
		return -1;
	}
	
	public static void writeToNBT(ItemStack patternStorage,ItemStack item,int progress)
	{
        if (!patternStorage.hasTagCompound()) {
            InitItemListTagCompound(patternStorage);
        }

        NBTTagCompound itemNBT = GetItemAsNBT(patternStorage, item);

        if (itemNBT != null)
		{
            int lastProgress = itemNBT.getByte(PROGRESS_TAG_NAME);
            int newProgress = MathHelper.clampI(lastProgress + progress, 0, MAX_ITEM_PROGRESS);
            itemNBT.setByte(PROGRESS_TAG_NAME, (byte) newProgress);
            System.out.println("Item in list");
        }
		else
		{
            NBTTagList itemList = GetItemsTagList(patternStorage);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte(PROGRESS_TAG_NAME, (byte) progress);
            writeStackToNBT(item, nbttagcompound1);
            itemList.appendTag(nbttagcompound1);
        }
    }

    public static void writeStackToNBT(ItemStack itemStack,NBTTagCompound tagCompound)
    {
        if(itemStack != null && tagCompound != null)
        {
            itemStack.writeToNBT(tagCompound);
			tagCompound.setByte("Count", (byte) 1);
		}
    }
	
	public static ItemStack GetItemWithInfo(ItemStack scanner, int id)
	{
		NBTTagCompound itemCompound = GetItemAsNBT(scanner,id);
		ItemStack item = GetItem(scanner,id);
		if(item != null && itemCompound != null)
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte(PROGRESS_TAG_NAME, itemCompound.getByte(PROGRESS_TAG_NAME));
			item.setTagCompound(tag);
			return item;
		}
		return null;
	}
	public static ItemStack GetItem(ItemStack scanner, ItemStack item)
	{
		return GetItemStackFromNBT(GetItemAsNBT(scanner,item));
	}
	public static ItemStack GetItem(ItemStack scanner, int id)
	{
		return GetItemStackFromNBT(GetItemAsNBT(scanner,id));
	}
	public static ItemStack GetItem(ItemStack scanner, String id)
	{
		return GetItemStackFromNBT(GetItemAsNBT(scanner,id));
	}
	public static ItemStack GetItemStackFromNBT(NBTTagCompound comp)
	{
		if(comp != null)
			return ItemStack.loadItemStackFromNBT(comp);
		else 
			return null;
	}
	public static ItemStack GetItemAt(ItemStack scanner, int index) 
	{
		return GetItemStackFromNBT(GetItemAsNBTAt(scanner,index));
	}
	
	public static NBTTagCompound GetItemAsNBT(ItemStack patternStorage, ItemStack item)
	{
		if(patternStorage.hasTagCompound())
		{
				NBTTagList itemList = GetItemsTagList(patternStorage);
				
				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(areEqual(item, GetItemStackFromNBT(itemList.getCompoundTagAt(i))))
						return GetItemAsNBTAt(patternStorage,i);
				}
		}
		
		return null;
	}
	public static NBTTagCompound GetItemAsNBT(ItemStack scanner, int id)
	{
		if(scanner.hasTagCompound())
		{
				NBTTagList itemList = GetItemsTagList(scanner);
				
				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(itemList.getCompoundTagAt(i).getShort("id") == id)
						return GetItemAsNBTAt(scanner,i);
				}
		}
		
		return null;
	}
	public static NBTTagCompound GetItemAsNBT(ItemStack scanner, String id)
	{
		if(scanner.hasTagCompound())
		{
			NBTTagList itemList = GetItemsTagList(scanner);

			for(int i = 0;i < itemList.tagCount();i++)
			{
				if(GetItemStackFromNBT(itemList.getCompoundTagAt(i)).getUnlocalizedName().equalsIgnoreCase(id))
					return GetItemAsNBTAt(scanner,i);
			}
		}

		return null;
	}

	private static NBTTagCompound GetItemAsNBTAt(ItemStack scanner, int index)
	{
		if(scanner.hasTagCompound())
		{
			NBTTagList itemList = GetItemsTagList(scanner);
			if(itemList.tagCount() > index)
				return itemList.getCompoundTagAt(index);
		}
		return null;
	}
	
	public static int GetIndexOfItem(ItemStack scanner, Block block)
	{
		ItemStack item = new ItemStack(block);
		return GetIndexOfItem(scanner,item);
	}
	
	public static int GetIndexOfItem(ItemStack scanner, World world,int x,int y,int z)
	{
		ItemStack item = GetItemStackFromWorld(world,x,y,z);
		return GetIndexOfItem(scanner,item);
	}
	
	public static int GetIndexOfItem(ItemStack scanner, ItemStack item)
	{
		if(scanner.hasTagCompound())
		{
				NBTTagList itemList = GetItemsTagList(scanner);
				System.out.println(itemList.tagCount());
				
				for(int i = 0;i < itemList.tagCount();i++)
				{
					if(areEqual(item, GetItemStackFromNBT(itemList.getCompoundTagAt(i))))
						return i;
				}
		}
		
		return -1;
	}

	public static boolean areEqual(ItemStack one,ItemStack two)
	{
        if(one != null && two != null)
        {
            if(one.getItem() == two.getItem())
            {
                if(one.getHasSubtypes() && two.getHasSubtypes())
                {
                    if(one.getItemDamage() == two.getItemDamage())
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
		return false;
	}

	public static boolean areEqual(NBTTagCompound one,NBTTagCompound two)
	{
		if(one == null || two == null)
			return false;

		return areEqual(ItemStack.loadItemStackFromNBT(one),ItemStack.loadItemStackFromNBT(two));
	}
	
	public static NBTTagList GetItemsTagList(ItemStack patternStorage)
	{
		if(patternStorage.hasTagCompound())
			return patternStorage.getTagCompound().getTagList(ITEMS_TAG_NAME, 10);
		return null;
	}
	
	public static ItemStack GetItemStackFromWorld(World world,int x, int y,int z)
	{
		Block b = world.getBlock(x, y, z);
		
		if(b != null && Item.getItemFromBlock(b) != null)
		{
			//Item bi = Item.getItemFromBlock(b);
			//List subBlocks = new ArrayList();
			//b.getSubBlocks(bi, null, subBlocks);
			int meta = world.getBlockMetadata(x, y, z);
			return new ItemStack(b,1,b.damageDropped(meta));
			
		}
		
		return new ItemStack(b);
	}

	public static EnumChatFormatting getPatternInfoColor(int progress)
	{
		EnumChatFormatting color = EnumChatFormatting.GRAY;

		if (progress > 0 && progress <= 20)
			color = EnumChatFormatting.RED;
		else if (progress > 20 && progress <= 40)
			color = EnumChatFormatting.GOLD;
		else if (progress > 40 && progress <= 60)
			color = EnumChatFormatting.YELLOW;
		else if (progress > 40 && progress <= 80)
			color = EnumChatFormatting.AQUA;
		else if (progress > 80 && progress <= 100)
			color = EnumChatFormatting.GREEN;
		else
			color = EnumChatFormatting.GREEN;

		return color;
	}
}