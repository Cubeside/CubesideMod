package de.fanta.cubeside.mixin;

import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.ColorUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class MixinCustomHitBox {
    @ModifyConstant(method = "showHitboxes", constant = @Constant(intValue = -1))
    private int hitboxColor(int old) {
        if (!Configs.HitBox.RainbowEntityHitBox.getBooleanValue()) {
            return old;
        }
        List<Color4f> color4fList = Configs.HitBox.RainbowEntityHitBoxColorList.getColors();
        if (color4fList.isEmpty()) {
            color4fList = Configs.HitBox.RainbowEntityHitBoxColorList.getDefaultColors();
        }
        Color color = ColorUtils.getColorGradient(CubesideClientFabric.getTime(), Configs.HitBox.RainbowEntityHitBoxSpeed.getDoubleValue(), color4fList);
        return color.getRGB();
    }

    @ModifyConstant(method = "showHitboxes", constant = @Constant(intValue = -16776961))
    private int arrowColor(int old) {
        return Configs.HitBox.EntityHitBoxDirection.getBooleanValue() ? old : 0;
    }
}
