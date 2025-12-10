package de.fanta.cubeside.mixin;

import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SliderControl.class)
public class MixinSliderControl {
    // FIXME 1.21.11
    // @Mutable
    // @Shadow
    // @Final
    // private int min;
    //
    // @Mutable
    // @Shadow
    // @Final
    // private int max;
    //
    // @Mutable
    // @Shadow
    // @Final
    // private int interval;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(IntegerOption option, int min, int max, int interval, CallbackInfo ci) {
        System.out.println("SliderControl" + option.getName().getString());
        if (option.getName().getContents() instanceof TranslatableContents content && content.getKey().equals("options.gamma")) {
            // FIXME 1.21.11
            // this.min = (int) (CubesideClientFabric.minGamma * 100);
            // this.max = (int) (CubesideClientFabric.maxGamma * 100);
            // this.interval = (int) (CubesideClientFabric.brightnessSliderInterval * 100);
        }
    }
}
