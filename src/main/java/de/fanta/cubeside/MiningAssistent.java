package de.fanta.cubeside;

import com.mojang.blaze3d.vertex.PoseStack;
import de.fanta.cubeside.config.Configs;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

public class MiningAssistent {

    private static BlockPos startPos;

    public static int count = 0;

    public enum MiningDirection {
        NORTH(0, 1),
        EAST(1, 0),
        SOUTH(0, -1),
        WEST(-1, 0);

        public final int xDiff;
        public final int zDiff;

        MiningDirection(int xDiff, int zDiff) {
            this.xDiff = xDiff;
            this.zDiff = zDiff;
        }

        public MiningDirection next() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }

        public String getArrow() {
            return switch (this) {
                case NORTH -> "↑";
                case EAST -> "←";
                case SOUTH -> "↓";
                case WEST -> "→";
            };
        }

        public String getCornerArrow() {
            return switch (this) {
                case NORTH -> "←";
                case EAST -> "↓";
                case SOUTH -> "→";
                case WEST -> "↑";
            };
        }
    }

    public static void render(PoseStack stack) {
        if (stack == null) {
            return;
        }
        if (startPos == null) {
            startPos = new BlockPos(Configs.MiningAssistent.MiningAssistentStartX.getIntegerValue(), Configs.MiningAssistent.MiningAssistentStartY.getIntegerValue(), Configs.MiningAssistent.MiningAssistentStartZ.getIntegerValue());
        }

        if (Configs.MiningAssistent.MiningAssistentEnabled.getBooleanValue() && startPos != null) {
            spawnParticleSpiral(stack, Configs.MiningAssistent.MiningAssistentDistance.getIntegerValue(), Configs.MiningAssistent.MiningAssistentCircles.getIntegerValue());
        }
    }

    public static void spawnParticleSpiral(PoseStack stack, int distance, int circles) {
        Point current = new Point(startPos.getX(), startPos.getZ());
        double radius = 0;

        for (int i = 0; i < circles; i++) {
            MiningDirection dir = MiningDirection.NORTH;
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < radius; k++) {
                    current = current.move(dir);
                    BlockPos loc = getNextY(new BlockPos(current.x, startPos.getY(), current.z));
                    if (k == radius - 1) {
                        if (dir == MiningDirection.WEST) {
                            renderTextOnBlock(stack, loc, dir, String.valueOf(i + 2), 0xFFFF00C3);
                        } else {
                            renderTextOnBlock(stack, loc, dir, dir.getCornerArrow(), 0xFFFA5C5F);
                        }
                    } else {
                        renderTextOnBlock(stack, loc, dir, dir.getArrow(), 0xFF45FF73); // ↑
                    }
                }
                dir = dir.next();
                radius += 0.5 + (distance / 2.0);
            }
        }
    }

    private static BlockPos getNextY(BlockPos pos) {
        for (int y = pos.getY(); y <= pos.getY() + 5; y++) {
            BlockPos currentLocation = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = Minecraft.getInstance().level.getBlockState(currentLocation);
            if (state != null && state.isAir()) {
                return new BlockPos(pos.getX(), y, pos.getZ());
            }
        }
        return pos;
    }

    public record Point(int x, int z) {
        public Point move(MiningDirection dir) {
            return new Point(x() + dir.xDiff, z() + dir.zDiff);
        }
    }

    public static void setStartPos(BlockPos startPos) {
        MiningAssistent.startPos = startPos;
        Configs.MiningAssistent.MiningAssistentStartX.setIntegerValue(startPos.getX());
        Configs.MiningAssistent.MiningAssistentStartY.setIntegerValue(startPos.getY());
        Configs.MiningAssistent.MiningAssistentStartZ.setIntegerValue(startPos.getZ());
        Configs.saveToFile();
    }

    public static void renderTextOnBlock(PoseStack matrixStack, BlockPos pos, MiningDirection direction, String string, int color) {
        BlockPos down = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        Level world = Minecraft.getInstance().level;
        String text = String.valueOf(string);
        Font font = Minecraft.getInstance().font;
        double cameraX = camera.position().x;
        double cameraY = camera.position().y;
        VoxelShape upperOutlineShape = world.getBlockState(down).getShape(world, down, CollisionContext.of(Minecraft.getInstance().player));
        if (!upperOutlineShape.isEmpty()) {
            cameraY += 1 - upperOutlineShape.max(Direction.Axis.Y);
        }
        double cameraZ = camera.position().z;
        matrixStack.pushPose();
        matrixStack.translate(pos.getX() + 0.5 - cameraX, pos.getY() - cameraY + 0.005, pos.getZ() + 0.5 - cameraZ);
        matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(1, 0, 0, 90)); // 90
        float size = 0.07F;
        matrixStack.scale(-size, -size, size);
        float float_3 = (-font.width(text)) / 2.0F + 0.4f;
        font.drawInBatch(text, float_3, -3.5f, color, false, matrixStack.last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880);
        matrixStack.popPose();
    }
}
