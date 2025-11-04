package de.fanta.cubeside.mixin;

import com.mojang.serialization.Codec;
import de.fanta.cubeside.util.BoostedSliderCallbacks;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.ValueSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionInstance.class)
public class MixinGammaSimpleOption {
    @Shadow
    @Final
    Component caption;

    @Shadow
    @Final
    @Mutable
    Function<Double, Component> toString;

    @Shadow
    @Final
    @Mutable
    ValueSet<Double> values;

    @Shadow
    @Final
    @Mutable
    Codec<Double> codec;

    @Shadow
    @Final
    @Mutable
    Consumer<Double> onValueUpdate;

    @Inject(at = @At("RETURN"), method = "<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;Lnet/minecraft/client/OptionInstance$ValueSet;Lcom/mojang/serialization/Codec;Ljava/lang/Object;Ljava/util/function/Consumer;)V")
    private void init(CallbackInfo info) {
        ComponentContents content = this.caption.getContents();
        if (!(content instanceof TranslatableContents translatableTextContent)) {
            return;
        }

        String key = translatableTextContent.getKey();
        if (!key.equals("options.gamma")) {
            return;
        }

        this.toString = this::textGetter;
        this.values = BoostedSliderCallbacks.INSTANCE;
        this.codec = this.values.codec();
        this.onValueUpdate = this::changeCallback;
    }

    @Unique
    private Component textGetter(Double gamma) {
        long brightness = Math.round(gamma * 100);
        return Component.translatable("options.gamma").append(": ").append(
                brightness == 0 ? Component.translatable("options.gamma.min") : brightness == 100 ? Component.translatable("options.gamma.max") : Component.literal(String.valueOf(brightness)));
    }

    @Unique
    private void changeCallback(Double gamma) {
        Minecraft.getInstance().options.gamma().set(gamma);
    }
}
