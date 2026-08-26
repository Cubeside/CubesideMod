package de.fanta.cubeside.mixin;

import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.config.option.ArgbColor;
import de.fanta.cubeside.util.ColorUtils;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public abstract class MixinWorldRenderer {

    private static final String SUBMIT_BLOCK_OUTLINE = "submitBlockOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V";
    private static final String SUBMIT_HIT_OUTLINE = "Lnet/minecraft/client/renderer/LevelRenderer;submitHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IFZ)V";

    private int replaceColor(int original) {
        if (!Configs.HitBox.ModifiedBlockHitBox.getBooleanValue()) {
            return original;
        }
        Color color;
        if (Configs.HitBox.RainbowBlockHitBox.getBooleanValue()) {
            List<ArgbColor> color4fList = Configs.HitBox.RainbowBlockHitBoxColorList.getColors();
            if (color4fList.isEmpty()) {
                color4fList = Configs.HitBox.RainbowBlockHitBoxColorList.getDefaultColors();
            }
            color = ColorUtils.getColorGradient(CubesideClientFabric.getTime(), Configs.HitBox.RainbowBlockHitBoxSpeed.getDoubleValue(), color4fList);
        } else {
            ArgbColor color4f = Configs.HitBox.BlockHitBoxColor.getColor();
            color = new Color(color4f.red(), color4f.green(), color4f.blue());
        }
        return color.getRGB();
    }

    @ModifyArg(method = SUBMIT_BLOCK_OUTLINE, at = @At(value = "INVOKE", target = SUBMIT_HIT_OUTLINE, ordinal = 0), index = 4)
    private int blockHitBoxHighContrastColor(int original) {
        return replaceColor(original);
    }

    @ModifyArg(method = SUBMIT_BLOCK_OUTLINE, at = @At(value = "INVOKE", target = SUBMIT_HIT_OUTLINE, ordinal = 1), index = 4)
    private int blockHitBoxColor(int original) {
        return replaceColor(original);
    }
}
