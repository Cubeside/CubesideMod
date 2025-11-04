package de.fanta.cubeside.mixin;

import de.fanta.cubeside.LogicalZoom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class LogicalZoomMixin {
    // Code from LogicalGeekBoy -> https://github.com/LogicalGeekBoy/logical_zoom
    @Inject(method = "getFov(Lnet/minecraft/client/Camera;FZ)F", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Float> callbackInfo) {
        if (LogicalZoom.isZooming()) {
            float fov = callbackInfo.getReturnValue();
            callbackInfo.setReturnValue(fov * LogicalZoom.zoomLevel);
        }

        LogicalZoom.manageSmoothCamera();
    }
}
