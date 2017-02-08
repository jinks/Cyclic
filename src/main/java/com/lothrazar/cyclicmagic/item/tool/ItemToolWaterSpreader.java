package com.lothrazar.cyclicmagic.item.tool;
import com.lothrazar.cyclicmagic.IHasRecipe;
import com.lothrazar.cyclicmagic.item.BaseTool;
import com.lothrazar.cyclicmagic.util.UtilParticle;
import com.lothrazar.cyclicmagic.util.UtilSound;
import com.lothrazar.cyclicmagic.util.UtilWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemToolWaterSpreader extends BaseTool implements IHasRecipe {
  private static final int DURABILITY = 256;
  private static final int COOLDOWN = 10;
  private static final int RADIUS = 1;
  public ItemToolWaterSpreader() {
    super(DURABILITY);
  }
  @Override
  public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (pos == null) { return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ); }
    if (side != null) {
      pos = pos.offset(side);
    }
    if (spreadWaterFromCenter(world, player, pos))
      super.onUse(stack, player, world, hand);
    return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
  }
  @Override
  public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
    if (spreadWaterFromCenter(world, player, player.getPosition()))
      super.onUse(stack, player, world, hand);
    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
  }
  private boolean spreadWaterFromCenter(World world, EntityPlayer player, BlockPos posCenter) {
    int count = 0;
    for (BlockPos pos : UtilWorld.findBlocks(world, posCenter, Blocks.WATER, RADIUS)) {
      world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 3);
      UtilParticle.spawnParticle(world, EnumParticleTypes.WATER_SPLASH, pos);
      UtilParticle.spawnParticle(world, EnumParticleTypes.WATER_SPLASH, pos.up());
      count++;
    }
    boolean success = count > 0;
    if (success) {//particles are on each location, sound is just once
      player.getCooldownTracker().setCooldown(this, COOLDOWN);
      UtilSound.playSound(player, SoundEvents.ENTITY_PLAYER_SPLASH);
    }
    return success;
  }
  @Override
  public void addRecipe() {
    GameRegistry.addShapedRecipe(new ItemStack(this),
        "wdw",
        "iwi",
        " o ",
        'w', new ItemStack(Items.WATER_BUCKET),
        'd', new ItemStack(Items.DIAMOND),
        'o', new ItemStack(Blocks.OBSIDIAN),
        'i', new ItemStack(Blocks.ICE));
  }
}
