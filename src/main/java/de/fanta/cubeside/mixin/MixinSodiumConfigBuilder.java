package de.fanta.cubeside.mixin;

import de.fanta.cubeside.CubesideClientFabric;
import net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(SodiumConfigBuilder.class)
public class MixinSodiumConfigBuilder {
    /*
     * Sets the max gamma value
     */
    @ModifyConstant(method = "buildGeneralPage", constant = @Constant(intValue = 100), require = 1, allow = 1)
    private int max(int old) {
        return (int) (CubesideClientFabric.maxGamma * 100);
    }
}
