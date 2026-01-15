package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Shadow
    PatchedDataComponentMap components;

    @Inject(method = "addToTooltip", at = @At(value = "RETURN"))
    private <T extends TooltipProvider> void appendComponentTooltip(DataComponentType<T> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type, CallbackInfo ci) {
        if (componentType == DataComponents.ENCHANTMENTS && Configs.Generic.ShowAdditionalRepairCosts.getBooleanValue() && displayComponent.shows(componentType)) {
            Integer repairCost = components.get(DataComponents.REPAIR_COST);
            if (repairCost != null && repairCost > 0) {
                textConsumer.accept(Component.translatable("cubeside.additional_repair_costs").withStyle(ChatFormatting.RED).append(": " + repairCost));
            }
        }
    }
}
