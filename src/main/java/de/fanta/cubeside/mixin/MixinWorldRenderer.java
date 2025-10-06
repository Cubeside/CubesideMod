package de.fanta.cubeside.mixin;

import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.ColorUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @ModifyConstant(method = "renderTargetBlockOutline(Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/util/math/MatrixStack;ZLnet/minecraft/client/render/state/WorldRenderState;)V", constant = @Constant(intValue = -16777216), expect = 1)
    private int replaceColor(int original) {
        Color color;
        if (Configs.HitBox.RainbowBlockHitBox.getBooleanValue()) {
            List<Color4f> color4fList = Configs.HitBox.RainbowBlockHitBoxColorList.getColors();
            if (color4fList.isEmpty()) {
                color4fList = Configs.HitBox.RainbowBlockHitBoxColorList.getDefaultColors();
            }
            color = ColorUtils.getColorGradient(CubesideClientFabric.getTime(), Configs.HitBox.RainbowBlockHitBoxSpeed.getDoubleValue(), color4fList);
        } else {
            Color4f color4f = Configs.HitBox.BlockHitBoxColor.getColor();
            color = new Color(color4f.r, color4f.g, color4f.b);
        }
        return color.getRGB();
    }
}
