package ru.littleligr.mixin;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portal.linking.DimensionalBlockPos;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.littleligr.CustomTeleporterUtils;

@Mixin(CustomTeleporter.class)
public class CustomTeleporterMixin {

     @Shadow
    public static TeleportTarget createDestinationPortal(ServerWorld destination, Entity entity, Direction.Axis axis, BlockLocating.Rectangle portalFramePos, BlockState frameBlock) {return null;}



    @Inject(method = "customTPTarget", at = @At("HEAD"), cancellable = true)
    private static void customTPTarget(
            ServerWorld destinationWorld, Entity entity, BlockPos enteredPortalPos, Block frameBlock, PortalFrameTester.PortalFrameTesterFactory portalFrameTesterFactory, CallbackInfoReturnable<TeleportTarget> cir) {

        Direction.Axis portalAxis = CustomPortalHelper.getAxisFrom(entity.getEntityWorld().getBlockState(enteredPortalPos));
        BlockLocating.Rectangle fromPortalRectangle = portalFrameTesterFactory.createInstanceOfPortalFrameTester().init(entity.getEntityWorld(), enteredPortalPos, portalAxis, frameBlock).getRectangle();
        DimensionalBlockPos destinationPos = CustomPortalsMod.portalLinkingStorage.getDestination(fromPortalRectangle.lowerLeft, entity.getEntityWorld().getRegistryKey());

        if (destinationPos != null && destinationPos.dimensionType.equals(destinationWorld.getRegistryKey().getValue())) {
            PortalFrameTester portalFrameTester = portalFrameTesterFactory.createInstanceOfPortalFrameTester().init(destinationWorld, destinationPos.pos, portalAxis, frameBlock);
            if (portalFrameTester.isValidFrame()) {
                if (!portalFrameTester.isAlreadyLitPortalFrame()) {
                    portalFrameTester.lightPortal(frameBlock);
                }
                cir.setReturnValue(portalFrameTester.getTPTargetInPortal(
                        portalFrameTester.getRectangle(),
                        portalAxis,
                        portalFrameTester.getEntityOffsetInPortal(fromPortalRectangle, entity, portalAxis),
                        entity
                ));
            }
        }

        boolean trySearch = CustomTeleporterUtils.searchDestinationPortal(destinationWorld, entity, enteredPortalPos, frameBlock, fromPortalRectangle);
        if (!trySearch)
            cir.setReturnValue(
                createDestinationPortal(destinationWorld, entity, portalAxis, fromPortalRectangle, frameBlock.getDefaultState())
            );
    }
}
