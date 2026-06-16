package io.github.xfacthd.foup.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.client.util.GhostVertexConsumer;
import io.github.xfacthd.foup.client.util.SingleBlockFakeLevel;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.block.AbstractOverheadRailBlock;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class OverheadRailInfoRenderer {
    private static final Direction[] HORIZONTAL_DIRECTIONS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
    private static final int GHOST_OPACITY = 170;
    private static final Function<@Nullable StationType, @Nullable Component> STATION_FORMATTER = type ->
            type != null && type != StationType.UNKNOWN ? type.getTranslation() : null;
    private static final ContextKey<RailInfoRenderState> DATA_KEY = new ContextKey<>(Utils.rl("rail_info_renderer"));

    public static void onExtractRenderState(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (Objects.requireNonNull(mc.player).isSpectator()) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        RailInfoRenderState renderState = null;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.is(FoupContent.ITEM_RAIL_INSPECTOR)) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = Objects.requireNonNull(mc.level).getBlockState(pos);
            if (state.getBlock() instanceof AbstractOverheadRailBlock) {
                renderState = extractRailInfoAround(mc.level, pos, state, false);
            }
        } else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractOverheadRailBlock block) {
            BlockPlaceContext context = new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, stack, hitResult);
            BlockState state = block.getStateForPlacement(context, true);
            BlockPos pos = context.getClickedPos();
            if (canPlaceAt(Objects.requireNonNull(mc.level), pos, state, context, mc.player)) {
                renderState = extractRailInfoAround(mc.level, pos, state, true);
            }
        }
        if (renderState != null) {
            event.getRenderState().setRenderData(DATA_KEY, renderState);
        }
    }

    private static boolean canPlaceAt(Level level, BlockPos pos, @Nullable BlockState state, BlockPlaceContext ctx, Player player) {
        if (level.getBlockState(pos).canBeReplaced(ctx)) {
            return state == null || level.isUnobstructed(state, pos, CollisionContext.of(player));
        }
        return false;
    }

    private static RailInfoRenderState extractRailInfoAround(ClientLevel level, BlockPos pos, @Nullable BlockState state, boolean renderGhost) {
        List<RailNodeInfoRenderState> nodes = new ArrayList<>(9);
        GhostBlockRenderState ghost = null;
        if (state != null) {
            if (renderGhost) {
                BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
                ghost = new GhostBlockRenderState(state, pos, model);
            }
            nodes.add(extractRailInfo(level, pos, state, !renderGhost));
        }

        for (BlockPos adjPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            if (adjPos.equals(pos)) {
                continue;
            }

            BlockState adjState = level.getBlockState(adjPos);
            if (adjState.getBlock() instanceof AbstractOverheadRailBlock) {
                nodes.add(extractRailInfo(level, adjPos, adjState, true));
            }
        }

        return new RailInfoRenderState(ghost, nodes);
    }

    private static RailNodeInfoRenderState extractRailInfo(ClientLevel level, BlockPos pos, BlockState state, boolean stationInfo) {
        StationInfoRenderState stationData = null;
        if (stationInfo && state.is(FoupContent.BLOCK_RAIL_STATION) && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station) {
            stationData = new StationInfoRenderState(station.getName(), station.getLinkedType());
        }
        return new RailNodeInfoRenderState(state, pos.immutable(), stationData);
    }

    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        RailInfoRenderState renderState = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (renderState == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        SubmitNodeCollector submitNodeCollector = event.getSubmitNodeCollector();
        Font font = minecraft.font;

        GhostBlockRenderState ghost = renderState.ghost;
        if (ghost != null) {
            Vec3 offset = Vec3.atLowerCornerOf(ghost.pos).subtract(camera.pos);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);

            boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
            ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
            // FIXME: switch to ghost render lib once it's extracted from FramedBlocks
            RenderType bufferType = Sheets.translucentBlockItemSheet();//NeoForgeRenderTypes.TRANSLUCENT_ON_PARTICLES_TARGET.get();
            submitNodeCollector.submitSpecial(RenderPhaseKeys.AFTER_TERRAIN, new CustomFeatureRenderer.Submit(poseStack.last().copy(), bufferType, (pose, buffer) -> {
                VertexConsumer builder = new GhostVertexConsumer(buffer, GHOST_OPACITY);
                BlockQuadOutput output = (_, _, _, quad, instance) -> builder.putBakedQuad(pose, quad, instance);

                ClientLevel realLevel = Objects.requireNonNull(minecraft.level);
                BlockAndTintGetter level = new SingleBlockFakeLevel(realLevel, ghost.pos, ghost.state);
                blockRenderer.tesselateBlock(output, 0, 0, 0, level, ghost.pos, ghost.state, ghost.model, 0);
            }));

            poseStack.popPose();
        }

        for (RailNodeInfoRenderState node : renderState.nodes) {
            renderRailInfo(poseStack, camera, submitNodeCollector, font, node);
        }
    }

    private static void renderRailInfo(PoseStack poseStack, CameraRenderState camera, SubmitNodeCollector submitNodeCollector, Font font, RailNodeInfoRenderState node) {
        poseStack.pushPose();

        Vec3 offset = Vec3.atCenterOf(node.pos).add(0, .25, 0).subtract(camera.pos);
        poseStack.translate(offset.x, offset.y, offset.z);

        BlockState state = node.state;
        for (Direction dir : HORIZONTAL_DIRECTIONS) {
            AbstractOverheadRailBlock block = (AbstractOverheadRailBlock) state.getBlock();
            if (block.isEntrySide(state, dir)) {
                renderArrow(poseStack, submitNodeCollector, dir, -.05F, false);
            } else if (block.isExitSide(state, dir)) {
                renderArrow(poseStack, submitNodeCollector, dir, -.05F, true);
            }
        }

        StationInfoRenderState station = node.station;
        if (station != null) {
            renderStationInfo(poseStack, camera, submitNodeCollector, font, station.name, station.linkedType, STATION_FORMATTER);
        }

        poseStack.popPose();
    }

    public static void renderArrow(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction dir, float start, boolean arrow) {
        Quaternionf yRot = Axis.YN.rotationDegrees(dir.toYRot());
        renderArrow(poseStack, submitNodeCollector, yRot, start, arrow, 0xFF0000FF);
    }

    public static void renderArrow(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Quaternionfc yRot, float start, boolean arrow, int color) {
        poseStack.pushPose();
        poseStack.mulPose(yRot);

        float end = arrow ? .4F : .5F;
        submitCustom(submitNodeCollector, poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            buffer.addVertex(pose, -.05F, 0, start).setColor(color);
            buffer.addVertex(pose, -.05F, 0, end).setColor(color);
            buffer.addVertex(pose, .05F, 0, end).setColor(color);
            buffer.addVertex(pose, .05F, 0, start).setColor(color);

            if (arrow) {
                buffer.addVertex(pose, -.15F, 0, .35F).setColor(color);
                buffer.addVertex(pose, 0F, 0, .5F).setColor(color);
                buffer.addVertex(pose, 0F, 0, .5F).setColor(color);
                buffer.addVertex(pose, .15F, 0, .35F).setColor(color);
            }
        });

        poseStack.popPose();
    }

    public static void renderStationInfo(
            PoseStack poseStack,
            CameraRenderState camera,
            SubmitNodeCollector submitNodeCollector,
            Font font,
            String name,
            @Nullable StationType type,
            Function<@Nullable StationType, @Nullable Component> formatter
    ) {
        poseStack.pushPose();

        poseStack.translate(0, .5, 0);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.scale(1F / 40F, 1F / 40F, 1);

        Component typeName = formatter.apply(type);

        submitText(submitNodeCollector, poseStack, -(font.width(name) / 2F), -9, name, 0xFFBB00FF);
        if (typeName != null) {
            submitText(submitNodeCollector, poseStack, -(font.width(typeName) / 2F), 1, typeName, 0xFFBB00FF);
        }

        poseStack.popPose();
    }

    public static void submitText(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, float x, float y, String text, int color) {
        submitText(submitNodeCollector, poseStack, x, y, FormattedCharSequence.forward(text, Style.EMPTY), color);
    }

    public static void submitText(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, float x, float y, Component text, int color) {
        submitText(submitNodeCollector, poseStack, x, y, text.getVisualOrderText(), color);
    }

    public static void submitText(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, float x, float y, FormattedCharSequence text, int color) {
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        submit(submitNodeCollector, new TextFeatureRenderer.Submit(pose, x, y, text, false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, color, 0, 0));
    }

    public static void submitCustom(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        submit(submitNodeCollector, new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType, renderer));
    }

    public static void submit(SubmitNodeCollector submitNodeCollector, SubmitNode submitNode) {
        submitNodeCollector.submitSpecial(RenderPhaseKeys.ALWAYS_ON_TOP, submitNode);
    }

    private record RailInfoRenderState(@Nullable GhostBlockRenderState ghost, List<RailNodeInfoRenderState> nodes) { }

    private record GhostBlockRenderState(BlockState state, BlockPos pos, BlockStateModel model) { }

    private record RailNodeInfoRenderState(BlockState state, BlockPos pos, @Nullable StationInfoRenderState station) { }

    private record StationInfoRenderState(String name, @Nullable StationType linkedType) { }

    private OverheadRailInfoRenderer() { }
}
