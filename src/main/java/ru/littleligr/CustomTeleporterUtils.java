package ru.littleligr;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

public abstract class CustomTeleporterUtils {

    private static boolean canHoldPortalUpdated(World world, BlockPos pos, Block frameBlock, int y) {
        BlockState state = world.getBlockState(pos.withY(y));

        return state.getBlock() instanceof CustomPortalBlock;
    }

    public static boolean searchDestinationPortal(ServerWorld world,
                                                  Entity entity,
                                                  BlockPos enteredPortalPos,
                                                  Block frameBlock,
                                                  BlockLocating.Rectangle fromPortalRectangle) {
        Direction.Axis axis = CustomPortalHelper.getAxisFrom(entity.getEntityWorld().getBlockState(enteredPortalPos));
        WorldBorder worldBorder = world.getWorldBorder();
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        PortalFrameTester portalFrameTester = link.getFrameTester().createInstanceOfPortalFrameTester();

        int topY = Math.min(world.getTopY(), world.getBottomY() + world.getLogicalHeight()) - 5;
        int bottomY = world.getBottomY() + 5;

        if (world.getRegistryKey().getValue().equals(link.dimID)) {
            if (link.portalSearchYTop != null)
                topY = link.portalSearchYTop;
            if (link.portalSearchYBottom != null)
                bottomY = link.portalSearchYBottom;
        } else {
            if (link.returnPortalSearchYTop != null)
                topY = link.returnPortalSearchYTop;
            if (link.returnPortalSearchYBottom != null)
                bottomY = link.returnPortalSearchYBottom;
        }

        for (BlockPos.Mutable mutable : BlockPos.iterateInSquare(entity.getBlockPos(), 32, Direction.WEST, Direction.SOUTH)) {
            BlockPos testingPos = mutable.toImmutable();
            if (!worldBorder.contains(testingPos)) continue;

            for (int y = topY; y >= bottomY; y--) {
                if (canHoldPortalUpdated(world, testingPos, frameBlock, y)) {
                    portalFrameTester.init(world, testingPos.withY(y), axis, frameBlock);
                    if (portalFrameTester.isValidFrame()) {
                        CustomPortalsMod.portalLinkingStorage.createLink(
                                fromPortalRectangle.lowerLeft,
                                entity.getWorld().getRegistryKey(),
                                portalFrameTester.getRectangle().lowerLeft,
                                world.getRegistryKey()
                        );
                        return true;
                    }

                }
            }
        }
        return false;
    }
}
