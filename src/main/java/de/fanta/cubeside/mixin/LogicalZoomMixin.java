package de.fanta.cubeside.mixin;

import de.fanta.cubeside.LogicalZoom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public class LogicalZoomMixin {
    // Code from LogicalGeekBoy -> https://github.com/LogicalGeekBoy/logical_zoom
    @Inject(method = "calculateFov(F)F", at = @At("RETURN"), cancellable = true)
    public void calculateFov(CallbackInfoReturnable<Float> callbackInfo) {
        if (LogicalZoom.isZooming()) {
            float fov = callbackInfo.getReturnValue();
            callbackInfo.setReturnValue(fov * LogicalZoom.zoomLevel);
        }

        LogicalZoom.manageSmoothCamera();
    }
}
