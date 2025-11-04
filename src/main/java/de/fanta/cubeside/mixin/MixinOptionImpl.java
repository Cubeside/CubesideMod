package de.fanta.cubeside.mixin;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.caffeinemc.mods.sodium.client.gui.options.OptionFlag;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.binding.GenericBinding;
import net.caffeinemc.mods.sodium.client.gui.options.binding.OptionBinding;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(OptionImpl.class)
public class MixinOptionImpl<S, T> {

    @Mutable
    @Shadow
    @Final
    private OptionBinding<S, T> binding;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(OptionStorage<S> storage, Component name, Function<T, Component> tooltip, OptionBinding<S, T> binding, Function<OptionImpl<S, T>, Control<T>> control, EnumSet<OptionFlag> flags, OptionImpact impact, BooleanSupplier enabled, CallbackInfo ci) {
        if (name.getContents() instanceof TranslatableContents content && content.getKey().equals("options.gamma")) {
            this.binding = new GenericBinding<>((opt, val) -> Minecraft.getInstance().options.gamma().set((Integer) val * 0.01D), binding::getValue);
        }
    }
}
