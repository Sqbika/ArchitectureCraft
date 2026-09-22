// ------------------------------------------------------
//
// ArchitectureCraft - Utilities
//
// ------------------------------------------------------

package gcewing.architecture.util;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.round;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import gcewing.architecture.common.item.ArchitectureItemBlock;
import gcewing.architecture.common.item.ItemCladding;
import gcewing.architecture.common.shape.Shape;
import gcewing.architecture.compat.BlockPos;
import gcewing.architecture.compat.IBlockState;
import gcewing.architecture.compat.MetaBlockState;
import gcewing.architecture.compat.Vector3;

public class Utils {

    public static final EnumFacing[] facings = EnumFacing.values();
    public static final EnumFacing[] horizontalFacings = { EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH,
            EnumFacing.EAST };

    public static int playerTurn(EntityLivingBase player) {
        return MathHelper.floor_double((player.rotationYaw * 4.0 / 360.0) + 0.5) & 3;
    }

    public static int lookTurn(Vector3 look) {
        double a = atan2(look.x, look.z);
        return (int) round(a * 2 / PI) & 3;
    }

    public static boolean playerIsInCreativeMode(EntityPlayer player) {
        return (player instanceof EntityPlayerMP) && ((EntityPlayerMP) player).theItemInWorldManager.isCreative();
    }

    public static String displayNameOfBlock(Block block, int meta) {
        String name = null;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            name = stack.getDisplayName();
        }
        if (name == null) name = block.getLocalizedName();
        return "Cut from " + name;
    }

    public static AxisAlignedBB unionOfBoxes(List<AxisAlignedBB> list) {
        AxisAlignedBB box = list.get(0);
        int n = list.size();
        for (int i = 1; i < n; i++) box = boxUnion(box, list.get(i));
        return box;
    }

    public static int ifloor(double x) {
        return (int) Math.floor(x);
    }

    public static int iround(double x) {
        return (int) round(x);
    }

    public static Object[] arrayOf(Collection<?> c) {
        int n = c.size();
        Object[] result = new Object[n];
        int i = 0;
        for (Object item : c) result[i++] = item;
        return result;
    }

    public static int packedColor(double red, double green, double blue) {
        return ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
    }

    public static int turnToFace(EnumFacing local, EnumFacing global) {
        return (turnToFaceEast(local) - turnToFaceEast(global)) & 3;
    }

    public static int turnToFaceEast(EnumFacing f) {
        return switch (f) {
            case SOUTH -> 1;
            case WEST -> 2;
            case NORTH -> 3;
            default -> 0;
        };
    }

    public static EnumFacing oppositeFacing(EnumFacing dir) {
        return facings[dir.ordinal() ^ 1];
    }

    public static boolean facingAxesEqual(EnumFacing facing1, EnumFacing facing2) {
        return (facing1.ordinal() & 6) == (facing2.ordinal() & 6);
    }

    public static int getStackMetadata(ItemStack stack) {
        return stack.getItem().getMetadata(stack.getItemDamage());
    }

    public static MovingObjectPosition newMovingObjectPosition(Vec3 hitVec, int sideHit, BlockPos pos) {
        return new MovingObjectPosition(pos.x, pos.y, pos.z, sideHit, hitVec, true);
    }

    public static AxisAlignedBB boxUnion(AxisAlignedBB box1, AxisAlignedBB box2) {
        return box1.func_111270_a(box2);
    }

    /**
     * Extracts the Shape data of an Architecture Block
     *
     * @param stack ItemStack of any kind
     * @return The Shape used for the ItemStack, if the item is an ArchitectureBlock, otherwise null
     */
    @Nullable
    public static Shape extractShapeFromItemStack(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }

        Item stackItem = stack.getItem();
        if (stackItem instanceof ItemCladding) {
            return Shape.CladdingSheet;
        }

        if (!(stackItem instanceof ArchitectureItemBlock)) {
            return null;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return null;
        }

        if (!tag.hasKey("Shape")) {
            return null;
        }

        int shapeId = tag.getInteger("Shape");
        if (shapeId < 0) {
            return null;
        }

        return Shape.forId(shapeId);
    }

    /**
     * Extracts the used Block and metadata data of an Architecture Block
     *
     * @param stack ItemStack of any kind
     * @return The specific Block and meta the block crafted from, if the item is an ArchitectureBlock, otherwise null
     */
    @Nullable
    public static IBlockState extractBlockStateFromItemStack(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }

        Item stackItem = stack.getItem();
        if (stackItem instanceof ItemCladding itemCladding) {
            return itemCladding.blockStateFromStack(stack);
        }

        if (!(stackItem instanceof ArchitectureItemBlock)) {
            return null;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return null;
        }

        Block baseBlock = Block.getBlockFromName(tag.getString("BaseName"));
        if (baseBlock == null) {
            return null;
        }

        int baseMetadata = tag.getInteger("BaseData");
        Item item = Item.getItemFromBlock(baseBlock);
        if (item == null) {
            return null;
        }

        return new MetaBlockState(baseBlock, baseMetadata);
    }
}
