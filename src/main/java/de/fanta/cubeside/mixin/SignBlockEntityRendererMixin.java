package de.fanta.cubeside.mixin;

import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSignRenderer.class)
class SignBlockEntityRendererMixin {
    // FIXME 1.21.9
    // @Shadow
    // @Final
    // private TextRenderer textRenderer;
    //
    // @Redirect(method = "renderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    // private void renderOldGlowText(TextRenderer textRenderer, OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light) {
    // if (Configs.Fixes.SimpleSignGlow.getBooleanValue()) {
    // textRenderer.draw(text, x, y, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    // } else {
    // this.textRenderer.drawWithOutline(text, x, y, color, outlineColor, matrix, vertexConsumers, light);
    // }
    // }

}
