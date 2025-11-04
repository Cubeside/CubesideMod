package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetHandler {

    @Inject(method = "handleForgetLevelChunk", at = { @At("HEAD") }, cancellable = true)
    private void onUnload(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        if (!Configs.ChunkLoading.UnloadChunks.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Redirect(method = "handleSetChunkCacheRadius", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetChunkCacheRadiusPacket;getRadius()I"))
    private int onViewDistChange(ClientboundSetChunkCacheRadiusPacket instance) {
        if (Configs.ChunkLoading.FullVerticalView.getBooleanValue()) {
            return Configs.ChunkLoading.FakeViewDistance.getIntegerValue();
        }
        return instance.getRadius();
    }

    @Redirect(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;chunkRadius()I"))
    private int onJoinGame(ClientboundLoginPacket instance) {
        if (Configs.ChunkLoading.FullVerticalView.getBooleanValue()) {
            return Configs.ChunkLoading.FakeViewDistance.getIntegerValue();
        }
        return instance.chunkRadius();
    }
}
