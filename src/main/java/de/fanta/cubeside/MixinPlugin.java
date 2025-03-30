package de.fanta.cubeside;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MixinPlugin implements IMixinConfigPlugin {

    private static final Supplier<Boolean> TRUE = () -> true;
    private static final Map<String, Supplier<Boolean>> CONDITIONS = new HashMap<>();

    @Override
    public void onLoad(String mixinPackage) {
        CONDITIONS.put("de.fanta.cubeside.mixin.DurabilityViewerMixin", () -> FabricLoader.getInstance().isModLoaded("durabilityviewer"));
        CONDITIONS.put("de.fanta.cubeside.mixin.MixinSliderControl", () -> FabricLoader.getInstance().isModLoaded("sodium"));
        CONDITIONS.put("de.fanta.cubeside.mixin.MixinOptionImpl", () -> FabricLoader.getInstance().isModLoaded("sodium"));
        CONDITIONS.put("de.fanta.cubeside.mixin.MixinChatLimit", () -> !containsFeather());
        CONDITIONS.put("de.fanta.cubeside.mixin.MixinKeyboard", () -> !FabricLoader.getInstance().isModLoaded("rebind_all_the_keys"));
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        System.out.println(mixinClassName);
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private boolean containsFeather() {
        boolean contains = false;
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            if (container.getMetadata().getId().contains("feather")) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
