package de.fanta.cubeside.mixin;

import com.google.common.collect.ImmutableList;
import de.fanta.cubeside.config.Configs;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity> {
    @Inject(method = "extractAdditionalHitboxes(Lnet/minecraft/world/entity/LivingEntity;Lcom/google/common/collect/ImmutableList$Builder;F)V", at = @At("HEAD"), cancellable = true)
    private void appendHitboxes(T livingEntity, ImmutableList.Builder<HitboxRenderState> builder, float f, CallbackInfo ci) {
        if (!Configs.HitBox.EntityHitBoxDirection.getBooleanValue()) {
            ci.cancel();
        }
    }
}
