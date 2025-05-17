/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.dev

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.commands.SkytilsCommands
import net.minecraft.block.BlockDynamicLiquid
import net.minecraft.block.BlockStaticLiquid
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.Flag
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
* The goal of this class is to generate an image that stitches together the textures of the highest blocks in the dungeon
*/
@Commands
object TopographyGenerator {
    init {
        SkytilsCommands.annotationParser.parse(this)
    }

    @Command("skytils|st dungeonmap stitch <x1> <y1> <z1> <x2> <y2> <z2>")
    fun stitch(
        @Argument("x1") x1: Int,
        @Argument("y1") y1: Int,
        @Argument("z1") z1: Int,
        @Argument("x2") x2: Int,
        @Argument("y2") y2: Int,
        @Argument("z2") z2: Int,
        @Flag("fit") fit: Boolean = false
    ) {
        val region = AxisAlignedBB(x1.toDouble(), y1.toDouble(), z1.toDouble(), x2.toDouble(), y2.toDouble(), z2.toDouble())
        val width = (region.maxX - region.minX + 1).toInt()
        val height = (region.maxZ - region.minZ + 1).toInt()

        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Invalid region dimensions: $width x $height")
        }

        val texturePx = 16

        val outputImage = BufferedImage(
            width * texturePx,
            height * texturePx,
            BufferedImage.TYPE_INT_ARGB
        )

        val graphics = outputImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) // Use nearest neighbor for blocky look
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        var minXDraw = Int.MAX_VALUE
        var maxXDraw = Int.MIN_VALUE
        var minYDraw = Int.MAX_VALUE
        var maxYDraw = Int.MIN_VALUE
        for (imgXIndex in 0 until width) {
            for (imgZIndex in 0 until height) {

                val currentWorldX = (region.minX + imgXIndex).toInt()
                val currentWorldZ = (region.minZ + imgZIndex).toInt()

                var highestBlock: IBlockState? = null

                for (currentWorldY in region.maxY.toInt() downTo region.minY.toInt()) {
                    val currentBlockPos = BlockPos(currentWorldX, currentWorldY, currentWorldZ)

                    val blockState = mc.theWorld!!.getBlockState(currentBlockPos)
                    val block = blockState.block

                    if (block != Blocks.air && hasRoomForPlayer(mc.theWorld!!, currentBlockPos.up())) {
                        highestBlock = blockState
                        break
                    }
                }

                if (highestBlock != null) {
                    val imagePixelX = imgXIndex * texturePx
                    val imagePixelY = imgZIndex * texturePx
                    drawBlockTexture(graphics, highestBlock, imagePixelX, imagePixelY, texturePx)

                    if (fit) {
                        minXDraw = min(minXDraw, imagePixelX)
                        maxXDraw = max(maxXDraw, imagePixelX)
                        minYDraw = min(minYDraw, imagePixelY)
                        maxYDraw = max(maxYDraw, imagePixelY)
                    }
                }
            }
        }
        graphics.dispose()

        try {
            val outputFile = File("dungeon_topography_map.png")
            if (!fit) {
                ImageIO.write(outputImage, "PNG", outputFile)
            } else {
                val fittedWorldMinX = region.minX.toInt() + (minXDraw / texturePx)
                val fittedWorldMinZ = region.minZ.toInt() + (minYDraw / texturePx)

                val imgXIndexOfMax = (maxXDraw + 1 - texturePx) / texturePx
                val fittedWorldMaxX = region.minX.toInt() + imgXIndexOfMax

                val imgZIndexOfMax = (maxYDraw + 1 - texturePx) / texturePx
                val fittedWorldMaxZ = region.minZ.toInt() + imgZIndexOfMax

                UChat.chat("§bFitted image corresponds to world area:")
                UChat.chat("§b  X: [$fittedWorldMinX, $fittedWorldMaxX]")
                UChat.chat("§b  Z: [$fittedWorldMinZ, $fittedWorldMaxZ]")
                ImageIO.write(outputImage.getSubimage(minXDraw, minYDraw, maxXDraw - minXDraw + 1, maxYDraw - minYDraw + 1), "PNG", outputFile)
            }
            UChat.chat(
                UTextComponent("§aTopography map successfully generated and saved to: ${outputFile.absolutePath}\n")
                    .setClick(MCClickEventAction.OPEN_FILE, outputFile.absolutePath)
            )
        } catch (e: IOException) {
            throw e
        }
    }

    private fun hasRoomForPlayer(worldIn: World, pos: BlockPos): Boolean {
        return /*World.doesBlockHaveSolidTopSurface(
            worldIn,
            pos.down()
        ) && */!worldIn.getBlockState(pos).block.material
            .isSolid && !worldIn.getBlockState(pos.up()).block.material.isSolid
    }

    private val textureCache = mutableMapOf<IBlockState, BufferedImage>()

    /**
     * Draws either the actual texture of the block or a representative color
     */
    private fun drawBlockTexture(graphics: Graphics2D, blockState: IBlockState, x: Int, y: Int, size: Int) {
        val blockTexture = getBlockTexture(blockState)

        if (blockTexture != null) {
            graphics.drawImage(blockTexture, x, y, size, size, null)
        } else {
            graphics.color = Color(blockState.block.getMapColor(blockState).colorValue)
            graphics.fillRect(x, y, size, size)
        }
    }

    /**
     * Gets the texture for a block, either from cache or by rendering it
     */
    private fun getBlockTexture(blockState: IBlockState): BufferedImage? {
        if (textureCache.containsKey(blockState)) {
            return textureCache[blockState]
        }

        try {
            val textureMap = mc.textureMapBlocks

            val sprite = getTextureSprite(blockState, textureMap)

            if (sprite != null) {
                val image = BufferedImage(sprite.iconWidth, sprite.iconHeight, BufferedImage.TYPE_INT_ARGB)
                val frameData = sprite.getFrameTextureData(0)[0]
                for (pixelY in 0 until sprite.iconHeight) {
                    for (pixelX in 0 until sprite.iconWidth) {
                        val index = sprite.iconWidth * pixelY + pixelX
                        val color = frameData[index]
                        image.setRGB(pixelX, pixelY, color)
                    }
                }

                textureCache[blockState] = image
                return image
            }
        } catch (e: Exception) {
            println("Error getting texture for ${blockState.block.registryName}: ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    /**
     * Tries to get a representative TextureAtlasSprite for a given block state.
     */
    private fun getTextureSprite(blockState: IBlockState, textureMap: TextureMap): TextureAtlasSprite? {
        // Attempt 1: Use BlockModelShapes to get the texture
        try {
            val bakedModel = mc.blockRendererDispatcher.blockModelShapes.getModelForState(blockState)
            // particleTexture can be the "missingno" sprite.
            val particleSprite = bakedModel.particleTexture
            if (particleSprite != null && particleSprite.iconName != "missingno" && particleSprite.iconName != null) {
                if (particleSprite.iconWidth > 0 && particleSprite.iconHeight > 0 && particleSprite.frameCount > 0) {
                    return particleSprite
                }
            }
        } catch (e: Exception) {
            println("Error getting texture for ${blockState.block.registryName} via BakedModel: ${e.message}")
            e.printStackTrace()
        }

        // Attempt 2: Fallback using the block's registry name to construct a plausible texture path
        val registryName = blockState.block.registryName
        if (registryName != null) {
            // Construct texture name like "minecraft:blocks/stone"
            val textureName = "${registryName.split(":")[0]}:blocks/${registryName.split(":")[1]}${if (blockState.block is BlockStaticLiquid) "_still" else if (blockState.block is BlockDynamicLiquid) "flow" else ""}"
            try {
                val sprite = textureMap.getAtlasSprite(textureName)
                if (sprite != null && sprite.iconName != "missingno" && sprite.iconName != null) {
                    if (sprite.iconWidth > 0 && sprite.iconHeight > 0 && sprite.frameCount > 0) {
                        return sprite
                    }
                }
            } catch (e: Exception) {
                println("Error getting texture via registry name for ${blockState.block.registryName} (tried $textureName): ${e.message}")
                e.printStackTrace()
            }
        }

        return null
    }
}