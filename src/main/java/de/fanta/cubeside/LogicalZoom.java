package de.fanta.cubeside;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class LogicalZoom {
    // Code from LogicalGeekBoy -> https://github.com/LogicalGeekBoy/logical_zoom
    private static boolean currentlyZoomed;
    private static KeyMapping keyBinding;
    private static boolean originalSmoothCameraEnabled;
    private static final Minecraft mc = Minecraft.getInstance();

    public static final float zoomLevel = 0.23F;

    public void initLogicalZoom() {
        keyBinding = new KeyMapping("key.logical_zoom.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KeyBinds.CATEGORY_CUBESIDE);

        currentlyZoomed = false;
        originalSmoothCameraEnabled = false;

        KeyMappingHelper.registerKeyMapping(keyBinding);
    }

    public static boolean isZooming() {
        return keyBinding.isDown();
    }

    public static void manageSmoothCamera() {
        if (zoomStarting()) {
            zoomStarted();
            enableSmoothCamera();
        }

        if (zoomStopping()) {
            zoomStopped();
            resetSmoothCamera();
        }
    }

    private static boolean isSmoothCamera() {
        return mc.options.smoothCamera;
    }

    private static void enableSmoothCamera() {
        mc.options.smoothCamera = true;
    }

    private static void disableSmoothCamera() {
        mc.options.smoothCamera = false;
    }

    private static boolean zoomStarting() {
        return isZooming() && !currentlyZoomed;
    }

    private static boolean zoomStopping() {
        return !isZooming() && currentlyZoomed;
    }

    private static void zoomStarted() {
        originalSmoothCameraEnabled = isSmoothCamera();
        currentlyZoomed = true;
    }

    private static void zoomStopped() {
        currentlyZoomed = false;
    }

    private static void resetSmoothCamera() {
        if (originalSmoothCameraEnabled) {
            enableSmoothCamera();
        } else {
            disableSmoothCamera();
        }
    }
}
