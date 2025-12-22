package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.mixin.KeyMappingAccessor;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.network.DataCustomPayload;
import io.github.kurrycat.mpkmod.gui.MPKGuiScreen;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Debug;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        Minecraft.Interface,
        Keyboard.Interface,
        Profiler.Interface {
    public static final Set<Integer> pressedButtons = new HashSet<>();
    public GuiGraphics drawContext = null;

    public void playButtonSound() {
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public ArrayList<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVector) {
        final Vector3D blockPosVec = blockPosVector.copy();
        BlockPos blockPos = new BlockPos(blockPosVec.getXI(), blockPosVec.getYI(), blockPosVec.getZI());
        if (net.minecraft.client.Minecraft.getInstance().level == null) return null;
        ArrayList<BoundingBox3D> boundingBoxes = new ArrayList<>();
        BlockState blockState = net.minecraft.client.Minecraft.getInstance().level.getBlockState(blockPos);

        blockState.getCollisionShape(net.minecraft.client.Minecraft.getInstance().level, blockPos).optimize().forAllBoxes(
                ((minX, minY, minZ, maxX, maxY, maxZ) -> boundingBoxes.add(
                        new BoundingBox3D(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ)).move(blockPosVec)
                ))
        );

        return boundingBoxes;
    }

    public Vector3D getLookingAt() {
        if (net.minecraft.client.Minecraft.getInstance().getCameraEntity() == null)
            return null;

        HitResult hitResult = net.minecraft.client.Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult instanceof BlockHitResult) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return new Vector3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        return null;
    }

    @Override
    public String getBlockName(Vector3D blockPos) {
        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        if (net.minecraft.client.Minecraft.getInstance().level == null)
            return null;

        return BuiltInRegistries.BLOCK.getResourceKey(
                net.minecraft.client.Minecraft.getInstance().level.getBlockState(blockpos).getBlock()
        ).get().identifier().toString();
    }

    @Override
    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        if (net.minecraft.client.Minecraft.getInstance().level == null)
            return properties;

        BlockPos blockpos = new BlockPos(blockPos.getXI(), blockPos.getYI(), blockPos.getZI());
        BlockState blockState = net.minecraft.client.Minecraft.getInstance().level.getBlockState(blockpos);
        blockState.getValues().forEach((key, value) ->
                properties.put(key.getName(), Util.getPropertyName(key, value))
        );
        return null;
    }

    /**
     * Is called in {@link WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getLookingAtBlock() {
        if (net.minecraft.client.Minecraft.getInstance().getCameraEntity() == null)
            return null;

        HitResult hitResult = net.minecraft.client.Minecraft.getInstance().getCameraEntity().pick(20, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && net.minecraft.client.Minecraft.getInstance().level != null) {
            return BuiltInRegistries.BLOCK.getResourceKey(
                    net.minecraft.client.Minecraft.getInstance().level.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock()
            ).get().identifier().toLanguageKey();
        }
        return null;
    }

    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        var ms = MPKMod.INSTANCE.matrixStack;
        ms.pushPose();
        ms.translate((float) bb.minX(), (float) bb.minY(), (float) bb.minZ());

        VoxelShape shape = Shapes.box(
                0.0, 0.0, 0.0,
                bb.maxX() - bb.minX(),
                bb.maxY() - bb.minY(),
                bb.maxZ() - bb.minZ()
        );

        VertexConsumer buf = net.minecraft.client.Minecraft.getInstance()
                .renderBuffers().bufferSource().getBuffer(RenderTypes.debugFilledBox());

        renderFilled(ms, buf, shape, 0, 0, 0, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        ms.popPose();
    }

    public static void renderFilled(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double x, double y, double z, float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        voxelShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float x1 = (float)(minX + x);
            float y1 = (float)(minY + y);
            float z1 = (float)(minZ + z);
            float x2 = (float)(maxX + x);
            float y2 = (float)(maxY + y);
            float z2 = (float)(maxZ + z);

            // Draw all 6 faces of the box
            // Down
            vertexConsumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
            vertexConsumer.addVertex(pose, x2, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
            vertexConsumer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
            vertexConsumer.addVertex(pose, x1, y1, z2).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);

            // Up
            vertexConsumer.addVertex(pose, x1, y2, z2).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
            vertexConsumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
            vertexConsumer.addVertex(pose, x2, y2, z1).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
            vertexConsumer.addVertex(pose, x1, y2, z1).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

            // North
            vertexConsumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
            vertexConsumer.addVertex(pose, x1, y2, z1).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
            vertexConsumer.addVertex(pose, x2, y2, z1).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
            vertexConsumer.addVertex(pose, x2, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);

            // South
            vertexConsumer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
            vertexConsumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
            vertexConsumer.addVertex(pose, x1, y2, z2).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
            vertexConsumer.addVertex(pose, x1, y1, z2).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);

            // West
            vertexConsumer.addVertex(pose, x1, y1, z2).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
            vertexConsumer.addVertex(pose, x1, y2, z2).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
            vertexConsumer.addVertex(pose, x1, y2, z1).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
            vertexConsumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);

            // East
            vertexConsumer.addVertex(pose, x2, y1, z1).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
            vertexConsumer.addVertex(pose, x2, y2, z1).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
            vertexConsumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
            vertexConsumer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        });
    }


    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        if (drawContext == null) return;
        drawContext.fill(
                (int) pos.getX(), (int) pos.getY(),
                (int) (pos.getX() + size.getX()), (int) (pos.getY() + size.getY()),
                color.getRGB()
        );
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawLines(Collection<Vector2D> points, Color color) {
        if (points.size() < 2) {
            Debug.stacktrace("At least two points expected, got: " + points.size());
            return;
        }
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        var window = net.minecraft.client.Minecraft.getInstance().getWindow();
        var bounds = new ScreenRectangle(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight());

        drawContext.guiRenderState.submitGuiElement(new PointsRenderState(
                points,
                r, g, b, a,
                PictureInPictureRenderState.getBounds(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), drawContext.scissorStack.peek()),
                drawContext.scissorStack.peek()
        ));
    }

    public Vector2D getScaledSize() {
        return new Vector2D(
                net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight()
        );
    }

    public Vector2D getScreenSize() {
        return new Vector2D(net.minecraft.client.Minecraft.getInstance().getWindow().getScreenWidth(), net.minecraft.client.Minecraft.getInstance().getWindow().getScreenHeight());
    }

    public void enableScissor(double x, double y, double w, double h) {
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + w);
        int y2 = (int) (y + h);
        drawContext.enableScissor(x1, y1, x2, y2);
    }

    public void disableScissor() {
        try {
            drawContext.disableScissor();
        } catch (IllegalStateException ignored) {}
    }

    public void clearScissors() {
        boolean clearedAll = false;
        while (!clearedAll) {
            try {
                drawContext.disableScissor();
            } catch (IllegalStateException e) {
                clearedAll = true;
            }
        }
    }

    public boolean scissorContains(Vector2D point) {
        return drawContext.containsPointInScissor(point.getXI(), point.getYI());
    }

    public void drawString(String text, double x, double y, Color color, double fontSize, boolean shadow) {
        if (drawContext == null) return;
        var matrixStack = drawContext.pose();
        matrixStack.pushMatrix();
        matrixStack.translate((float) x, (float) y);
        double scale = fontSize / net.minecraft.client.Minecraft.getInstance().font.lineHeight;
        matrixStack.scale((float) scale, (float) scale);
        drawContext.drawString(
                net.minecraft.client.Minecraft.getInstance().font, text,
                0, 0, color.getRGB(), shadow
        );
        matrixStack.popMatrix();
    }

    public Vector2D getStringSize(String text, double fontSize) {
        return new Vector2D(
                net.minecraft.client.Minecraft.getInstance().font.width(text) *
                        (float) (fontSize / net.minecraft.client.Minecraft.getInstance().font.lineHeight),
                (float) fontSize
        );
    }

    public String getIP() {
        ServerData d = net.minecraft.client.Minecraft.getInstance().getCurrentServer();

        if (d == null)
            return "Multiplayer";
        else
            return d.ip;
    }

    public String getFPS() {
        return String.valueOf(net.minecraft.client.Minecraft.getInstance().getFps());
    }

    public long getPing() {
        return net.minecraft.client.Minecraft.getInstance().getConnection().getServerData().ping;
    }

    public void displayGuiScreen(MPKGuiScreen screen) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                screen == null
                        ? null
                        : new io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.MPKGuiScreen(screen));
    }

    public String getCurrentGuiScreen() {
        Screen curr = net.minecraft.client.Minecraft.getInstance().screen;

        if (curr == null)
            return null;
        else if (curr instanceof io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.MPKGuiScreen) {
            String id = ((io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null)
                id = "unknown";

            return id;
        }

        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if (net.minecraft.client.Minecraft.getInstance().player == null) return null;
        return net.minecraft.client.Minecraft.getInstance().player.getName().getString();
    }

    public void copyToClipboard(String content) {
        net.minecraft.client.Minecraft.getInstance().keyboardHandler.setClipboard(content);
    }

    public boolean setInputs(Float yaw, boolean relYaw, Float pitch, boolean relPitch, int pressedInputs, int releasedInputs, int L, int R) {
        if (!Minecraft.isSingleplayer()) return false;
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return false;
        Options op = net.minecraft.client.Minecraft.getInstance().options;

        float prevYaw = player.getYRot();
        float prevPitch = player.getXRot();

        if (yaw != null) {
            player.setYRot(relYaw ? (player.getYRot() + yaw) : yaw);
            player.yRotO += player.getYRot() - prevYaw;
        }
        if (pitch != null) {
            player.setXRot(relPitch ? (player.getXRot() + pitch) : pitch);
            player.setXRot(Mth.clamp(player.getXRot(), -90.0F, 90.0F));

            player.xRotO += player.getXRot() - prevPitch;
            player.xRotO = Mth.clamp(player.xRotO, -90.0F, 90.0F);
        }

        if (player.getVehicle() != null) {
            player.getVehicle().onPassengerTurned(player);
        }

        KeyMapping[] keys = new KeyMapping[]{
                op.keyUp,
                op.keyLeft,
                op.keyDown,
                op.keyRight,
                op.keySprint,
                op.keyShift,
                op.keyJump
        };

        for (int i = 0; i < keys.length; i++) {
            if ((releasedInputs & 1 << i) != 0) {
                KeyMapping.set(((KeyMappingAccessor) keys[i]).getKey(), false);
            }
            if ((pressedInputs & 1 << i) != 0) {
                KeyMapping.set(((KeyMappingAccessor) keys[i]).getKey(), true);
                KeyMapping.click(((KeyMappingAccessor) keys[i]).getKey());
            }
        }

        KeyMapping.set(((KeyMappingAccessor) op.keyAttack).getKey(), L > 0);
        for (int i = 0; i < L; i++)
            KeyMapping.click(((KeyMappingAccessor) op.keyAttack).getKey());

        KeyMapping.set(((KeyMappingAccessor) op.keyUse).getKey(), R > 0);
        for (int i = 0; i < R; i++)
            KeyMapping.click(((KeyMappingAccessor) op.keyUse).getKey());

        return true;
    }

    public boolean isF3Enabled() {
        return net.minecraft.client.Minecraft.getInstance().debugEntries.isOverlayVisible();
    }

    public void sendPacket(MPKPacket packet) {
        ClientPlayNetworking.send(new DataCustomPayload(packet.getData()));
    }

    public ArrayList<Integer> getPressedButtons() {
        return new ArrayList<>(pressedButtons);
    }

    public void startSection(String name) {
        net.minecraft.util.profiling.Profiler.get().push(name);
    }

    public void endStartSection(String name) {
        net.minecraft.util.profiling.Profiler.get().popPush(name);
    }

    public void endSection() {
        net.minecraft.util.profiling.Profiler.get().pop();
    }

    private record PointsRenderState(
            Collection<Vector2D> points,
            int r, int g, int b, int a,
            ScreenRectangle bounds,
            ScreenRectangle scissor
    ) implements GuiElementRenderState {

        @Override
        public @Nullable ScreenRectangle bounds() {
            return this.bounds;
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (Vector2D p : this.points) {
                consumer.addVertex((float) p.getX(), (float) p.getY(), 0).setColor(r, g, b, a);
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return this.scissor;
        }
    }
}
