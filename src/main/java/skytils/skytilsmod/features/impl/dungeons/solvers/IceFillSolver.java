package skytils.skytilsmod.features.impl.dungeons.solvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;

public class IceFillSolver {


    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int ticks = 0;

    public static JsonObject iceFillData = new JsonObject();

    private static BlockPos chestPos;
    private static EnumFacing roomFacing;

    private static final ArrayList<Vec3> variant3 = new ArrayList<>();
    private static final ArrayList<Vec3> variant5 = new ArrayList<>();
    private static final ArrayList<Vec3> variant7 = new ArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return;

        World world = mc.theWorld;

        if (ticks % 20 == 0) {
            new Thread(() -> {
                if (chestPos == null || roomFacing == null) {
                    findChest:
                    for (BlockPos pos : Utils.getBlocksWithinRangeAtSameY(mc.thePlayer.getPosition(), 25, 75)) {
                        IBlockState block = world.getBlockState(pos);
                        if (block.getBlock() == Blocks.chest && world.getBlockState(pos.down()).getBlock() == Blocks.stone) {
                            for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                                if (world.getBlockState(pos.offset(direction)).getBlock() == Blocks.cobblestone && world.getBlockState(pos.offset(direction.getOpposite(), 2)).getBlock() == Blocks.iron_bars && world.getBlockState(pos.offset(direction.rotateY(), 2)).getBlock() == Blocks.torch && world.getBlockState(pos.offset(direction.rotateYCCW(), 2)).getBlock() == Blocks.torch && world.getBlockState(pos.offset(direction.getOpposite()).down(2)).getBlock() == Blocks.stone_brick_stairs) {
                                    chestPos = pos;
                                    roomFacing = direction;
                                    System.out.println(String.format("Ice fill chest is at %s and is facing %s", chestPos, roomFacing));
                                    if (iceFillData.size() == 0) {
                                        mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7cSkytils failed to load solutions for Ice Fill."));
                                        DataFetcher.reloadData();
                                    }
                                    break findChest;
                                }
                            }
                        }
                    }
                } else {
                    if (variant3.size() == 0) {
                        if (iceFillData.has("3x3")) {
                            JsonArray all = iceFillData.get("3x3").getAsJsonArray();
                            determineVariant:
                            for (JsonElement el : all) {
                                JsonArray blocks = el.getAsJsonObject().get("blocks").getAsJsonArray();
                                for (int i = 0; i < blocks.size(); i++) {
                                    JsonArray point = blocks.get(i).getAsJsonArray();
                                    if (world.getBlockState(new BlockPos(getVec3RelativeToGrid3(point.get(0).getAsInt(), point.get(1).getAsInt()))).getBlock() == Blocks.air) break;
                                    if (i == blocks.size() - 1) {
                                        JsonArray steps = el.getAsJsonObject().get("steps").getAsJsonArray();
                                        for (JsonElement e : steps) {
                                            JsonArray step = e.getAsJsonArray();
                                            variant3.add(getVec3RelativeToGrid3(step.get(0).getAsInt(), step.get(1).getAsInt()));
                                        }
                                        break determineVariant;
                                    }
                                }
                            }
                        }
                    }
                    if (variant5.size() == 0) {
                        if (iceFillData.has("5x5")) {
                            JsonArray all = iceFillData.get("5x5").getAsJsonArray();
                            determineVariant:
                            for (JsonElement el : all) {
                                JsonArray blocks = el.getAsJsonObject().get("blocks").getAsJsonArray();
                                for (int i = 0; i < blocks.size(); i++) {
                                    JsonArray point = blocks.get(i).getAsJsonArray();
                                    if (world.getBlockState(new BlockPos(getVec3RelativeToGrid5(point.get(0).getAsInt(), point.get(1).getAsInt()))).getBlock() == Blocks.air) break;
                                    if (i == blocks.size() - 1) {
                                        JsonArray steps = el.getAsJsonObject().get("steps").getAsJsonArray();
                                        for (JsonElement e : steps) {
                                            JsonArray step = e.getAsJsonArray();
                                            variant5.add(getVec3RelativeToGrid5(step.get(0).getAsInt(), step.get(1).getAsInt()));
                                        }
                                        break determineVariant;
                                    }
                                }
                            }
                        }
                    }
                    if (variant7.size() == 0) {
                        if (iceFillData.has("7x7")) {
                            JsonArray all = iceFillData.get("7x7").getAsJsonArray();
                            determineVariant:
                            for (JsonElement el : all) {
                                JsonArray blocks = el.getAsJsonObject().get("blocks").getAsJsonArray();
                                for (int i = 0; i < blocks.size(); i++) {
                                    JsonArray point = blocks.get(i).getAsJsonArray();
                                    if (world.getBlockState(new BlockPos(getVec3RelativeToGrid7(point.get(0).getAsInt(), point.get(1).getAsInt()))).getBlock() == Blocks.air) break;
                                    if (i == blocks.size() - 1) {
                                        JsonArray steps = el.getAsJsonObject().get("steps").getAsJsonArray();
                                        for (JsonElement e : steps) {
                                            JsonArray step = e.getAsJsonArray();
                                            variant7.add(getVec3RelativeToGrid7(step.get(0).getAsInt(), step.get(1).getAsInt()));
                                        }
                                        break determineVariant;
                                    }
                                }
                            }
                        }
                    }
                }
            }).start();
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (chestPos != null && roomFacing != null) {
            if (iceFillData.size() == 0) return;
            if (variant3.size() > 0) {
                for (int i = 0; i < variant3.size() - 1; i++) {
                    Vec3 pos = variant3.get(i);
                    Vec3 pos2 = variant3.get(i + 1);
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, -0.5, 0.5), pos2.addVector(0.5, -0.5, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
            if (variant5.size() > 0) {
                for (int i = 0; i < variant5.size() - 1; i++) {
                    Vec3 pos = variant5.get(i);
                    Vec3 pos2 = variant5.get(i + 1);
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, -0.5, 0.5), pos2.addVector(0.5, -0.5, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
            if (variant7.size() > 0) {
                for (int i = 0; i < variant7.size() - 1; i++) {
                    Vec3 pos = variant7.get(i);
                    Vec3 pos2 = variant7.get(i + 1);
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, -0.5, 0.5), pos2.addVector(0.5, -0.5, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        chestPos = null;
        roomFacing = null;
        variant3.clear();
        variant5.clear();
        variant7.clear();
    }


    private Vec3 getVec3RelativeToGrid7(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(chestPos
                .offset(roomFacing.getOpposite(), 4)
                .down(3)
                .offset(roomFacing.rotateYCCW(), 3)
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

    private Vec3 getVec3RelativeToGrid5(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(new BlockPos(getVec3RelativeToGrid7(1,6))
                .offset(roomFacing.getOpposite(), 3)
                .down()
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

    private Vec3 getVec3RelativeToGrid3(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(new BlockPos(getVec3RelativeToGrid5(1,4))
                .offset(roomFacing.getOpposite(), 3)
                .down()
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

}
