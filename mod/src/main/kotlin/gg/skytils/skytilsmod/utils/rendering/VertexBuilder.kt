/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.utils.rendering

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.renderer.GLAllocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.awt.Color
import java.io.Closeable
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.nio.FloatBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

fun <S : VertexScope> mesh(
    drawMode: DrawMode,
    vertexFormat: VertexFormats<S>,
    block: S.() -> Unit
): Mesh<S> = MeshImpl(drawMode, vertexFormat).build(block)

interface Mesh<S : VertexScope> {
    val drawMode: DrawMode
    val format: VertexFormats<S>

    fun render(matrixStack: UMatrixStack)
}

internal class MeshImpl<S : VertexScope>(
    override val drawMode: DrawMode,
    override val format: VertexFormats<S>
) : Mesh<S> {
    lateinit var vao: VertexArray

    override fun render(matrixStack: UMatrixStack) {
        if (!::vao.isInitialized) return
        matrixStack.applyToGlobalState()
        vao.render(drawMode.glID)
    }

    fun build(block: S.() -> Unit) = apply { vao = format.scopeInstance().apply(block).upload(this) }
}

enum class DrawMode(internal val glID: Int) {
    QUADS(GL11.GL_QUADS)
}

/**
 * Represents a [Vertex Array Object](https://www.khronos.org/opengl/wiki/Vertex_Specification#Vertex_Array_Object)
 */
class VertexArray internal constructor(val id: Int, val vertexCount: Int, val scope: VertexScope, mesh: Mesh<*>) :
    PhantomReference<Mesh<*>>(mesh, referenceQueue), Closeable {
    val vbos = HashSet<VertexBuffer>()

    init {
        cleanupQueue.add(this)

        drainCleanupQueue()
    }

    override fun close() {
        Utils.checkThreadAndQueue {
            cleanupQueue.remove(this)

            GL30.glDeleteVertexArrays(id)
        }
    }

    fun render(drawMode: Int) {
        whileBound {
            scope.attributes.forEach(GL20::glEnableVertexAttribArray) // enable attributes
            GL11.glDrawArrays(drawMode, 0, vertexCount)
            scope.attributes.forEach(GL20::glDisableVertexAttribArray) // cleanup
        }
    }

    /**
     * Executes a block of code with the vertex array bound
     */
    fun <T> whileBound(block: VertexArray.() -> T): T {
        GL30.glBindVertexArray(id)
        val res = with(this, block)
        GL30.glBindVertexArray(0)
        return res
    }

    companion object {
        val referenceQueue: ReferenceQueue<Mesh<*>> = ReferenceQueue()
        val cleanupQueue: MutableSet<VertexArray> = Collections.newSetFromMap(ConcurrentHashMap())

        fun drainCleanupQueue() {
            while (true) {
                ((referenceQueue.poll() ?: break) as VertexArray).close()
            }
        }
    }
}

fun Collection<Float>.toBuffer(): FloatBuffer = GLAllocation.createDirectFloatBuffer(this.size).put(this.toFloatArray())
fun <T> MutableCollection<T>.addAll(vararg elements: T) = addAll(elements.toList())

interface VertexScope {
    val attributes: MutableList<Int>

    fun <S : VertexScope> upload(mesh: Mesh<S>): VertexArray
}

interface PositionScope : VertexScope {
    val positions: MutableList<Float>
    val pos: FloatBuffer
        get() = positions.toBuffer()
    val posId: Int

    /**
     * Uploads the position buffer to the currently bound VAO at position 0
     */
    fun VertexArray.uploadPos(): VertexBuffer {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posId) // bind vbo
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos, GL15.GL_STATIC_DRAW) // bind buffer
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0) // upload to vao ad index 0
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0) // unbind vbo

        // also gotta add our attribute to the attributes list
        attributes.add(0)

        return VertexBuffer(posId, this)
    }
}

fun <S : PositionScope> S.pos(x: Float, y: Float, z: Float) =
    apply {
        positions.addAll(x, y, z)
    }

fun <S : PositionScope> S.pos(x: Number, y: Number, z: Number) =
    pos(x.toFloat(), y.toFloat(), z.toFloat())

interface ColorScope : VertexScope {
    val colors: MutableList<Float>
    val color: FloatBuffer
        get() = colors.toBuffer()
    val colorId: Int

    fun VertexArray.uploadColor(): VertexBuffer {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorId)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color, GL15.GL_STATIC_DRAW)
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        attributes.add(2)

        return VertexBuffer(colorId, this)
    }
}

fun <S : ColorScope> S.color(r: Float, g: Float, b: Float, a: Float) =
    apply {
        colors.addAll(r, g, b, a)
    }

fun <S : ColorScope> S.color(r: Number, g: Number, b: Number, a: Number = 255f) =
    color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())

fun <S : ColorScope> S.color(color: Color) =
    color(color.red, color.green, color.blue, color.alpha)

fun <S : ColorScope> S.color(rgba: Int) =
    color(rgba shr 16 and 0xFF, rgba shr 8 and 0xFF, rgba shr 0 and 0xFF, rgba shr 24 and 0xFF)

interface TextureScope : VertexScope {
    val texCoords: MutableList<Float>
    val tex: FloatBuffer
        get() = texCoords.toBuffer()
    val texId: Int

    fun VertexArray.uploadTextures(): VertexBuffer {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texId)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex, GL15.GL_STATIC_DRAW)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        attributes.add(1)

        return VertexBuffer(texId, this)
    }
}

fun <S : TextureScope> S.texture(x: Float, y: Float) =
    apply {
        texCoords.addAll(x, y)
    }

fun <S : TextureScope> S.texture(x: Number, y: Number) = texture(x.toFloat(), y.toFloat())

interface PositionColorScope : PositionScope, ColorScope {
    override fun <S : VertexScope> upload(mesh: Mesh<S>) =
        VertexArray(GL30.glGenVertexArrays(), positions.size / 3, this, mesh)
            .whileBound {
                vbos.addAll(
                    uploadPos(),
                    uploadColor()
                )
                this
            }
}

interface PositionTextureScope : PositionScope, TextureScope {
    override fun <S : VertexScope> upload(mesh: Mesh<S>) =
        VertexArray(GL30.glGenVertexArrays(), positions.size / 3, this, mesh)
            .whileBound {
                vbos.addAll(
                    uploadPos(),
                    uploadTextures()
                )
                this
            }
}

interface PositionTextureColorScope : PositionScope, TextureScope, ColorScope {
    override fun <S : VertexScope> upload(mesh: Mesh<S>) =
        VertexArray(GL30.glGenVertexArrays(), positions.size / 3, this, mesh)
            .whileBound {
                vbos.addAll(
                    uploadPos(),
                    uploadTextures(),
                    uploadColor()
                )
                this
            }
}

class MasterScope : PositionColorScope, PositionTextureScope, PositionTextureColorScope {
    override val positions: MutableList<Float> = ArrayList()
    override val posId: Int by lazy { GL15.glGenBuffers() }
    override val colors: MutableList<Float> = ArrayList()
    override val colorId: Int by lazy { GL15.glGenBuffers() }
    override val texCoords: MutableList<Float> = ArrayList()
    override val texId: Int by lazy { GL15.glGenBuffers() }
    override val attributes: MutableList<Int> = ArrayList()

    override fun <S : VertexScope> upload(mesh: Mesh<S>): VertexArray {
        throw IllegalStateException("You should never be here!")
    }
}

class VertexBuffer(val id: Int, vao: VertexArray) : PhantomReference<VertexArray>(vao, referenceQueue), Closeable {

    init {
        cleanupQueue.add(this)

        drainCleanupQueue()
    }

    override fun close() {
        Utils.checkThreadAndQueue {
            cleanupQueue.remove(this)
            GL15.glDeleteBuffers(id)
        }
    }

    companion object {
        val referenceQueue: ReferenceQueue<VertexArray> = ReferenceQueue()
        val cleanupQueue: MutableSet<VertexBuffer> = Collections.newSetFromMap(ConcurrentHashMap())

        fun drainCleanupQueue() {
            while (true) {
                ((referenceQueue.poll() ?: break) as VertexBuffer).close()
            }
        }
    }
}

sealed interface VertexFormats<S : VertexScope> {
    data object Position : VertexFormats<PositionScope>
    data object PositionColor : VertexFormats<PositionColorScope>
    data object PositionTexture : VertexFormats<PositionTextureScope>
    data object PositionTextureColor : VertexFormats<PositionTextureColorScope>

    @Suppress("UNCHECKED_CAST")
    fun scopeInstance() = MasterScope() as S
}