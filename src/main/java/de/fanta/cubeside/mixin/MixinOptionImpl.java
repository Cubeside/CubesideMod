package de.fanta.cubeside.mixin;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionBinding;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.client.config.AnonymousOptionBinding;
import net.caffeinemc.mods.sodium.client.config.structure.StatefulOption;
import net.caffeinemc.mods.sodium.client.config.value.DependentValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(StatefulOption.class)
public class MixinOptionImpl<V> {

    @Mutable
    @Shadow
    @Final
    private OptionBinding<V> binding;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(Identifier id, Collection<Identifier> dependencies, Component name, DependentValue<Boolean> enabled, StorageEventHandler storage, Function<V, Component> tooltipProvider, OptionImpact impact, EnumSet<OptionFlag> flags, DependentValue<V> defaultValue, OptionBinding<V> binding,
            CallbackInfo ci) {
        if (name.getContents() instanceof TranslatableContents content && content.getKey().equals("options.gamma")) {
            this.binding = (OptionBinding<V>) new AnonymousOptionBinding<>((val) -> Minecraft.getInstance().options.gamma().set(val * 0.01D), () -> (int) (Minecraft.getInstance().options.gamma().get() / 0.01D));
        }
    }
}
