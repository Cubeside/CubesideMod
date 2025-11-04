package de.fanta.cubeside.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import de.fanta.cubeside.CubesideClientFabric;
import java.util.Optional;
import net.minecraft.client.OptionInstance.SliderableValueSet;

public enum BoostedSliderCallbacks implements SliderableValueSet<Double> {
    INSTANCE;

    @Override
    public Optional<Double> validateValue(Double double_) {
        return double_ >= CubesideClientFabric.minGamma && double_ <= CubesideClientFabric.maxGamma ? Optional.of(double_) : Optional.empty();
    }

    @Override
    public double toSliderValue(Double double_) {
        double range = CubesideClientFabric.maxGamma - CubesideClientFabric.minGamma;
        double offset = CubesideClientFabric.minGamma;
        return (double_ - offset) / range;
    }

    @Override
    public Double fromSliderValue(double d) {
        double range = CubesideClientFabric.maxGamma - CubesideClientFabric.minGamma;
        double offset = CubesideClientFabric.minGamma;
        return d * range + offset;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(CubesideClientFabric.minGamma, CubesideClientFabric.maxGamma), Codec.BOOL).xmap(either -> either.map(value -> value, value -> value ? 1.0 : 0.0), Either::left);
    }
}