package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin.aw.LivingEntityAccessor;
import io.github.kurrycat.mpkmod.util.*;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.property.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.ServerListEntry;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.WorldGeneratorType;
import net.minecraft.client.render.vertex.Tesselator;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCompatibility implements FunctionHolder,
        SoundManager.Interface,
        WorldInteraction.Interface,
        Renderer3D.Interface,
        Renderer2D.Interface,
        FontRenderer.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface,
        io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard.Interface,
        Profiler.Interface {
    private static final Stack<ScissorBox> scissorStack = new Stack<>();

    /**
     * Is called in {@link SoundManager.Interface}
     */
    public void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.of(new Identifier("gui.button.press"), 1.0F));
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec) {
        BlockPos blockPos = new BlockPos(blockPosVec.getX(), blockPosVec.getY(), blockPosVec.getZ());
        World world = Minecraft.getInstance().world;
        BlockState blockState = world.getBlockState(blockPos);
        Box mask = new Box(blockPosVec.getX() - 1, blockPosVec.getY() - 1, blockPosVec.getZ() - 1, blockPosVec.getX() + 1, blockPosVec.getY() + 1, blockPosVec.getZ() + 1);
        ArrayList<Box> result = new ArrayList<>();
        blockState.getBlock().addCollisions(world, blockPos, blockState, mask, result, null);

        return result.stream().map((aabb) -> new BoundingBox3D(new Vector3D(aabb.minX, aabb.minY, aabb.minZ), new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ))).collect(Collectors.toList());
    }

    /**
     * Is called in {@link WorldInteraction.Interface}
     */
    public Vector3D getLookingAt() {
        BlockPos blockPos = Minecraft.getInstance().player.rayTrace(20, 0).getPos();
        if (blockPos == null) return null;
        return new Vector3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * Is called in {@link WorldInteraction.Interface WorldInteraction.Interface}
     */
    public String getBlockName(Vector3D blockPos) {
        String blockName = "";
        //if (Minecraft.getMinecraft().objectMouseOver != null && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Minecraft.getMinecraft().objectMouseOver.getBlockPos() != null && !(Minecraft.getMinecraft().thePlayer.hasReducedDebug() || Minecraft.getMinecraft().gameSettings.reducedDebugInfo)) {
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockState iblockstate = Minecraft.getInstance().world.getBlockState(blockpos);
        if (Minecraft.getInstance().world.getGeneratorType() != WorldGeneratorType.DEBUG_ALL_BLOCK_STATES) {
            iblockstate = iblockstate.getBlock().resolveVirtualProperties(iblockstate, Minecraft.getInstance().world, blockpos);
        }
        blockName = String.valueOf(Block.REGISTRY.getId(iblockstate.getBlock()));
        //}
        return blockName;
    }

    public HashMap<String, String> getBlockProperties(Vector3D blockPos) {
        HashMap<String, String> properties = new HashMap<>();
        BlockPos blockpos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockState iblockstate = Minecraft.getInstance().world.getBlockState(blockpos);
        if (Minecraft.getInstance().world.getGeneratorType() != WorldGeneratorType.DEBUG_ALL_BLOCK_STATES) {
            iblockstate = iblockstate.getBlock().resolveVirtualProperties(iblockstate, Minecraft.getInstance().world, blockpos);
        }
        //noinspection rawtypes
        for (Map.Entry<Property, Comparable> e : iblockstate.values().entrySet()) {
            properties.put(e.getKey().getName(), e.getValue().toString());
        }
        return properties;
    }

    /**
     * Is called in {@link Renderer3D.Interface}
     */
    public void drawBox(BoundingBox3D bb, Color color, float partialTicks) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder wr = tesselator.getBuffer();

        Entity entity = Minecraft.getInstance().getCamera();

        double entityX = entity.lastX + (entity.x - entity.lastX) * (double) partialTicks;
        double entityY = entity.lastY + (entity.y - entity.lastY) * (double) partialTicks;
        double entityZ = entity.lastZ + (entity.z - entity.lastZ) * (double) partialTicks;

        wr.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
        wr.offset(-entityX, -entityY, -entityZ);

        wr.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();

        wr.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();

        wr.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();

        wr.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();

        wr.vertex(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();

        wr.vertex(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).nextVertex();
        wr.vertex(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).nextVertex();

        wr.offset(0, 0, 0);

        tesselator.end();

        GlStateManager.enableTexture();
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public void drawRect(Vector2D pos, Vector2D size, Color color) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        double x = pos.getX(), y = pos.getY(), w = size.getX(), h = size.getY();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder wr = tesselator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        //GlStateManager.shadeModel(GL11.GL_SMOOTH); // - for gradients
        wr.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
        wr.vertex(x, y + h, 0.0).color(r, g, b, a).nextVertex();
        wr.vertex(x + w, y + h, 0.0).color(r, g, b, a).nextVertex();
        wr.vertex(x + w, y, 0.0).color(r, g, b, a).nextVertex();
        wr.vertex(x, y, 0.0).color(r, g, b, a).nextVertex();
        tesselator.end();
        //GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
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

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (Vector2D p : points) {
            wr.vertex(p.getX(), p.getY(), 0).color(r, g, b, a).nextVertex();
        }

        wr.offset(0, 0, 0);

        tessellator.end();

        GlStateManager.enableTexture();
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    /**
     * Is called in {@link Renderer2D.Interface}
     */
    public Vector2D getScaledSize() {
        Window r = new Window(Minecraft.getInstance());
        return new Vector2D(
                r.getScaledWidth(),
                r.getScaledHeight()
        );
    }

    public Vector2D getScreenSize() {
        return new Vector2D(Minecraft.getInstance().width, Minecraft.getInstance().height);
    }

    public void enableScissor(double x, double y, double w, double h) {
        ScissorBox box;
        if (scissorStack.isEmpty()) box = new ScissorBox(x, y, w, h);
        else {
            ScissorBox prev = scissorStack.peek();
            double bx = Math.max(prev.x, x), by = Math.max(prev.y, y);
            box = new ScissorBox(bx, by,
                    Math.min(x + w, prev.x + prev.w) - bx,
                    Math.min(y + h, prev.y + prev.h) - by
            );
        }
        scissorStack.push(box);
        setScissor(box);
    }

    public void disableScissor() {
        if (!scissorStack.isEmpty()) scissorStack.pop();
        if (scissorStack.isEmpty()) setScissor(null);
        else setScissor(scissorStack.peek());
    }

    public void clearScissors() {
        scissorStack.clear();
        setScissor(null);
    }

    private void setScissor(ScissorBox box) {
        if (box == null) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            Window r = new Window(Minecraft.getInstance());

            double scaleFactor = r.getScale();
            double posX = box.x * scaleFactor;
            double posY = Minecraft.getInstance().height - (box.y + box.h) * scaleFactor;
            double width = box.w * scaleFactor;
            double height = box.h * scaleFactor;
            GL11.glScissor((int) posX, (int) posY, Math.max(0, (int) width), Math.max(0, (int) height));
        }
    }

    public boolean scissorContains(Vector2D point) {
        return scissorStack.isEmpty() || scissorStack.peek().contains(point);
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public void drawString(String text, double x, double y, Color color, double fontSize, boolean shadow) {
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 0);
        double scale = fontSize / (Minecraft.getInstance().textRenderer.fontHeight * 1F);
        GlStateManager.scaled(scale, scale, 1);
        Minecraft.getInstance().textRenderer.draw(text, 0, 0, color.getRGB(), shadow);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    /**
     * Is called in {@link FontRenderer.Interface}
     */
    public Vector2D getStringSize(String text, double fontSize) {
        double scale = fontSize / (Minecraft.getInstance().textRenderer.fontHeight * 1F);
        return new Vector2D(
                Minecraft.getInstance().textRenderer.getWidth(text) * scale,
                fontSize
        );
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getIP() {
        ServerListEntry d = Minecraft.getInstance().getCurrentServerEntry();
        if (d == null) return "Multiplayer";
        else return d.ip;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getFPS() {
        return String.valueOf(Minecraft.getCurrentFps());
    }

    public int getPing() {
        return Minecraft.getInstance().getNetworkHandler().getOnlinePlayer(Minecraft.getInstance().player.getUuid()).getPing();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public void displayGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        Minecraft.getInstance().executeTask(() ->
                Minecraft.getInstance().openScreen(
                        screen == null ? null : new MPKGuiScreen(screen)));
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getCurrentGuiScreen() {
        Screen curr = Minecraft.getInstance().screen;
        if (curr == null) return null;
        else if (curr instanceof MPKGuiScreen) {
            String id = ((MPKGuiScreen) curr).eventReceiver.getID();
            if (id == null) id = "unknown";
            return id;
        }
        return curr.getClass().getSimpleName();
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.Interface Minecraft.Interface}
     */
    public String getUserName() {
        if (Minecraft.getInstance().player == null) return null;
        return Minecraft.getInstance().player.getName();
    }

    public void copyToClipboard(String content) {
        StringSelection selection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public boolean isF3Enabled() {
        return Minecraft.getInstance().options.debugEnabled;
    }

    public void sendPacket(MPKPacket packet) {
        ClientPlayNetworking.send(MPKMod.MPK_ID, packet.getData());
    }

    public boolean setInputs(Float yaw, boolean relYaw, Float pitch, boolean relPitch, int pressedInputs, int releasedInputs, int L, int R) {
        if (!io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.isSingleplayer()) return false;
        LocalClientPlayerEntity player = Minecraft.getInstance().player;
        GameOptions gs = Minecraft.getInstance().options;

        float prevPitch = player.pitch;
        float prevYaw = player.yaw;

        if (yaw != null) {
            player.yaw = relYaw ? (float) ((double) player.yaw + (double) yaw) : yaw;
            player.lastYaw += player.yaw - prevYaw;
        }
        if (pitch != null) {
            player.pitch = relPitch ? (float) ((double) player.pitch - (double) pitch) : pitch;
            player.pitch = MathHelper.clamp(player.pitch, -90.0F, 90.0F);
            player.lastPitch += player.pitch - prevPitch;
        }

        int[] keys = new int[]{
                gs.forwardKey.getKeyCode(),
                gs.leftKey.getKeyCode(),
                gs.backKey.getKeyCode(),
                gs.rightKey.getKeyCode(),
                gs.sprintKey.getKeyCode(),
                gs.sneakKey.getKeyCode(),
                gs.jumpKey.getKeyCode()
        };

        for (int i = 0; i < keys.length; i++) {
            if ((releasedInputs & 1 << i) != 0) {
                net.minecraft.client.options.KeyBinding.set(keys[i], false);
            }
            if ((pressedInputs & 1 << i) != 0) {
                net.minecraft.client.options.KeyBinding.set(keys[i], true);
                net.minecraft.client.options.KeyBinding.click(keys[i]);
            }
        }

        net.minecraft.client.options.KeyBinding.set(gs.attackKey.getKeyCode(), L > 0);
        for (int i = 0; i < L; i++)
            net.minecraft.client.options.KeyBinding.click(gs.attackKey.getKeyCode());

        net.minecraft.client.options.KeyBinding.set(gs.useKey.getKeyCode(), R > 0);
        for (int i = 0; i < R; i++)
            net.minecraft.client.options.KeyBinding.click(gs.useKey.getKeyCode());

        return true;
    }

    /**
     * Is called in {@link io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard.Interface Keyboard.Interface}
     */
    public List<Integer> getPressedButtons() {
        List<Integer> keysDown = new ArrayList<>();
        for (int i = 0; i < Keyboard.getKeyCount(); i++)
            if (Keyboard.isKeyDown(i))
                keysDown.add(InputConstants.convert(i));
        return keysDown;
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void startSection(String name) {
        Minecraft.getInstance().profiler.push(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endStartSection(String name) {
        Minecraft.getInstance().profiler.getResults(name);
    }

    /**
     * Is called in {@link Profiler.Interface}
     */
    public void endSection() {
        Minecraft.getInstance().profiler.pop();
    }

    @Override
    public Player.KeyInput getKeyInput() {
        Entity ce = Minecraft.getInstance().getCamera();
        if (!(ce instanceof LivingEntity)) return new Player.KeyInput();

        boolean w, a, s, d, sprint, sneak, jump;
        if (ce instanceof LocalClientPlayerEntity) {
            GameOptions gs = Minecraft.getInstance().options;

            w = gs.forwardKey.isPressed();
            a = gs.leftKey.isPressed();
            s = gs.backKey.isPressed();
            d = gs.rightKey.isPressed();

            sprint = gs.sprintKey.isPressed();
            sneak = gs.sneakKey.isPressed();
            jump = gs.jumpKey.isPressed();
        } else {
            LivingEntity elb = (LivingEntity) ce;

            w = elb.forwardSpeed > 0;
            a = elb.sidewaysSpeed > 0;
            s = elb.forwardSpeed < 0;
            d = elb.sidewaysSpeed < 0;

            sprint = elb.isSprinting();
            sneak = elb.isSneaking();

            jump = ((LivingEntityAccessor) elb).isJumping();
        }

        return new Player.KeyInput(w, a, s, d, sprint, sneak, jump);
    }
}
