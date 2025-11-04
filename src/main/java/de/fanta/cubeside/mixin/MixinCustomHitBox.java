package de.fanta.cubeside.mixin;

import com.google.common.collect.ImmutableList;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.ColorUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public abstract class MixinCustomHitBox<T extends Entity> {

    @Shadow
    protected abstract void extractAdditionalHitboxes(T entity, ImmutableList.Builder<HitboxRenderState> builder, float tickProgress);

    /**
     * @author fantahund
     * @reason Make HitBoxes fancy
     */
    @Overwrite
    private HitboxesRenderState extractHitboxes(T entity, float tickProgress, boolean green) {
        ImmutableList.Builder<HitboxRenderState> builder = new ImmutableList.Builder<>();
        AABB box = entity.getBoundingBox();
        HitboxRenderState entityHitbox;
        Color color;
        if (Configs.HitBox.RainbowEntityHitBox.getBooleanValue()) {
            List<Color4f> color4fList = Configs.HitBox.RainbowEntityHitBoxColorList.getColors();
            if (color4fList.isEmpty()) {
                color4fList = Configs.HitBox.RainbowEntityHitBoxColorList.getDefaultColors();
            }
            color = ColorUtils.getColorGradient(CubesideClientFabric.getTime(), Configs.HitBox.RainbowEntityHitBoxSpeed.getDoubleValue(), color4fList);
        } else {
            Color4f color4f = Configs.HitBox.EntityHitBoxColor.getColor();
            color = new Color(color4f.r, color4f.g, color4f.b);
        }

        if (green && !Configs.HitBox.RainbowEntityHitBox.getBooleanValue()) {
            entityHitbox = new HitboxRenderState(box.minX - entity.getX(), box.minY - entity.getY(), box.minZ - entity.getZ(), box.maxX - entity.getX(), box.maxY - entity.getY(), box.maxZ - entity.getZ(), 0.0F, 1.0F, 0.0F);
        } else if (!green && !Configs.HitBox.RainbowEntityHitBox.getBooleanValue()) {
            entityHitbox = new HitboxRenderState(box.minX - entity.getX(), box.minY - entity.getY(), box.minZ - entity.getZ(), box.maxX - entity.getX(), box.maxY - entity.getY(), box.maxZ - entity.getZ(), 1.0F, 1.0F, 1.0F);
        } else {
            entityHitbox = new HitboxRenderState(box.minX - entity.getX(), box.minY - entity.getY(), box.minZ - entity.getZ(), box.maxX - entity.getX(), box.maxY - entity.getY(), box.maxZ - entity.getZ(), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        }

        builder.add(entityHitbox);
        Entity entity2 = entity.getVehicle();
        if (entity2 != null) {
            float f = Math.min(entity2.getBbWidth(), entity.getBbWidth()) / 2.0F;
            // float g = 0.0625F;
            Vec3 vec3d = entity2.getPassengerRidingPosition(entity).subtract(entity.position());
            HitboxRenderState entityHitbox2 = new HitboxRenderState(vec3d.x - f, vec3d.y, vec3d.z - f, vec3d.x + f, vec3d.y + 0.0625F, vec3d.z + f, 1.0F, 1.0F, 0.0F);
            builder.add(entityHitbox2);
        }

        this.extractAdditionalHitboxes(entity, builder, tickProgress);
        Vec3 vec3d2 = entity.getViewVector(tickProgress);
        return new HitboxesRenderState(vec3d2.x, vec3d2.y, vec3d2.z, builder.build());
    }
}
