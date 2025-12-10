package de.fanta.cubeside.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity> {
    // FIXME 1.21.11
    // @Inject(method = "extractAdditionalHitboxes(Lnet/minecraft/world/entity/LivingEntity;Lcom/google/common/collect/ImmutableList$Builder;F)V", at = @At("HEAD"), cancellable = true)
    // private void appendHitboxes(T livingEntity, ImmutableList.Builder<HitboxRenderState> builder, float f, CallbackInfo ci) {
    // if (!Configs.HitBox.EntityHitBoxDirection.getBooleanValue()) {
    // ci.cancel();
    // }
    // }
}
