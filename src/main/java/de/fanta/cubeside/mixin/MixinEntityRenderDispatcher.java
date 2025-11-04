package de.fanta.cubeside.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    // FIXME 1.21.9
    // @Inject(at = @At("RETURN"), method = "setRenderHitboxes")
    // public void setRenderHitboxes(boolean renderHitboxes, CallbackInfo ci) {
    // Configs.HitBox.ShowHitBox.setBooleanValue(renderHitboxes);
    // Configs.saveToFile();
    // }
    //
    // @Inject(method = "renderHitboxes(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityHitboxAndView;Lnet/minecraft/client/render/VertexConsumer;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;<init>(DDD)V", ordinal = 0), cancellable = true)
    // private static void conditionalVec3dCancel(MatrixStack matrices, EntityHitboxAndView hitbox, VertexConsumer vertexConsumer, float standingEyeHeight, CallbackInfo ci) {
    // if (!Configs.HitBox.EntityHitBoxDirection.getBooleanValue()) {
    // ci.cancel();
    // }
    // }
}
