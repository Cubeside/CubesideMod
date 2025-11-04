package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.Entity.class)
public abstract class MixinRenderEntity {

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract boolean isInvisible();

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void showInvisibleArmorstands(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
        if (Configs.Generic.ShowInvisibleArmorstands.getBooleanValue()) {
            Item iteminmainhand = player.getMainHandItem().getItem();
            if (iteminmainhand == Items.ARMOR_STAND && getType().equals(EntityType.ARMOR_STAND) && isInvisible()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z", shift = At.Shift.AFTER), cancellable = true)
    private void hideInvisibleEntities(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!Configs.Generic.ShowInvisibleEntitiesInSpectator.getBooleanValue()) {
            if (isInvisible() && player.isSpectator()) {
                cir.setReturnValue(true);
            }
        }
    }

}
