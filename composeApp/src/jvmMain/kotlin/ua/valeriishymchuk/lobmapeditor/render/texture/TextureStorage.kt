package ua.valeriishymchuk.lobmapeditor.render.texture

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Vector2i
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.render.RGBAImage
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.collections.set

class TextureStorage {

    companion object {
        const val TERRAIN_PREPEND = "tilesets/terrain"
    }
    val textures: MutableMap<String, Int> = ConcurrentHashMap()

    fun getTerrain(path: String): Int {
        return textures["$TERRAIN_PREPEND/$path"]!!
    }

    fun getTerrainTile(terrain: TerrainType): Int {
        return textures["tilesets/${terrain.textureLocation}"]
            ?: throw IllegalStateException("Can't find tile texture for $terrain")
    }

    fun getTerrainOverlay(terrain: TerrainType): Int {
        val overlay = terrain.overlay ?: throw IllegalArgumentException("$terrain doesn't have an overlay.")
        return textures["tilesets/${overlay.textureLocation}"]
            ?: throw IllegalStateException("Can't find overlay texture for $terrain")
    }



    var backgroundImage: Int = -1
        private set
    var terrainMaskTexture: Int = -1
        private set
    var farmOverlayTexture: Int = -1
        private set
    var heightBlobTexture: Int = -1
        private set
    var objectiveMaskTexture: Int = -1
        private set
    var objectiveOverlayTexture: Int = -1
        private set
    var selectionTexture: Int = -1
        private set

    var arrowBody: Int = -1
    private set
    var arrowHead: Int = -1

    fun loadTextures(ctx: GL3) {
        loadTexture(ctx, "wood")
        TerrainType.MAIN_TERRAIN.forEach { terrain ->
            loadTexture(ctx, "tilesets/${terrain.textureLocation}", false)
        }
        TerrainType.BLOB_TERRAIN.forEach { terrain ->
            loadAtlas(
                ctx, "tilesets/${terrain.textureLocation}", Vector2i(16), Vector2i(8, 6), ImageFilter(
                    useClamp = true,
                    useLinear = false

                )
            )
        }

        GameUnitType.entries.forEach { unitType ->
            loadTexture(ctx, unitType.maskTexture)
            unitType.overlayTexture?.let { loadTexture(ctx, it) }
        }

        loadTexture(ctx, "objectives/default", useNearest = false, useClamp = true)
        loadTexture(ctx, "objectives/default1", useNearest = false, useClamp = true)

        objectiveMaskTexture = textures["objectives/default"]!!
        objectiveOverlayTexture = textures["objectives/default1"]!!

        loadTexture(ctx, "other/indicators",
            offset = Vector2i(65, 0),
            imageSize = Vector2i(63, 63),
            useClamp = true,
            useNearest = false,
            mapKey = "other/selection"
        )

        selectionTexture = textures["other/selection"]!!


        TerrainType.entries.mapNotNull { it.overlay }.distinct().forEach {
            println("Loading overlay: $it")
            loadAtlas(
                ctx, "tilesets/${it.textureLocation}", it.elementSize, Vector2i(4), ImageFilter(
                    useClamp = true,
                    useLinear = false
                )
            )
        }
        backgroundImage = textures["wood"]!!

        loadAtlas(
            ctx, "tilesets/borderblending/mask", Vector2i(32, 32), Vector2i(16, 1), ImageFilter(
                useClamp = true,
                useLinear = false

            )
        )

        terrainMaskTexture = textures["tilesets/borderblending/mask"]!!


        loadAtlas(
            ctx,
            TerrainType.FARM_BORDERS_LOCATION,
            Vector2i(32, 32),
            Vector2i(16, 1),
            ImageFilter(
                useClamp = true,
                useLinear = false

            )
        )
        farmOverlayTexture = textures[TerrainType.FARM_BORDERS_LOCATION]!!

        loadAtlas(
            ctx, "tilesets/blending/height",
            Vector2i(16),
            Vector2i(8, 6),
            ImageFilter(
                useClamp = true,
                useLinear = false
            )
        )
        heightBlobTexture = textures["tilesets/blending/height"]!!

        loadTexture(ctx, "other/arrow-body")
        arrowBody = textures["other/arrow-body"]!!

        loadTexture(ctx, "other/arrow-head")
        arrowHead = textures["other/arrow-head"]!!
    }

    data class ImageFilter(
        val useLinear: Boolean = true,
        val useClamp: Boolean = false,
        val useMipmaps: Boolean = true,
    )

    private fun loadTexture(
        ctx: GL3,
        key: String,
        useNearest: Boolean = true,
        useClamp: Boolean = false,
        offset: Vector2i = Vector2i(),
        imageSize: Vector2i? = null,
        mapKey: String = key
    ) {
        val image = loadTextureData(key, offset, imageSize)
        val textureNameArray: IntArray = IntArray(1)
        ctx.glGenTextures(1, textureNameArray, 0)
        val texture: Int = textureNameArray[0]

        ctx.glBindTexture(GL.GL_TEXTURE_2D, texture)


        val format = GL.GL_RGBA
        ctx.glTexImage2D(
            GL.GL_TEXTURE_2D,
            0,
            format,
            image.width,
            image.height,
            0,
            format,
            GL.GL_UNSIGNED_BYTE,
            image.image
        )
//        ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAX_LEVEL, 4);
        ctx.glGenerateMipmap(GL.GL_TEXTURE_2D)

        if (useClamp) {
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
        } else {
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT)
        }


        if (useNearest)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST_MIPMAP_NEAREST)
        else ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR)
        if (useNearest)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
        else ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)

        ctx.glBindTexture(GL.GL_TEXTURE_2D, 0)

        textures[mapKey] = texture
    }


    private fun loadTextureData(
        path: String,
        offset: Vector2i = Vector2i(),
        imageSize: Vector2i? = null
    ): RGBAImage {
        val imageWebp = ImageIO.read(ResourceLoader.loadResource("drawable/images/${path}.webp").inputStream())
        val imageRgba = BufferedImage(
            imageWebp.width,
            imageWebp.height,
            BufferedImage.TYPE_4BYTE_ABGR
        )

        val g: Graphics2D = imageRgba.createGraphics()
        g.drawImage(imageWebp, 0, 0, null)
        g.dispose()
        val rawPixelData = (imageRgba.raster.dataBuffer as DataBufferByte).data
        val newDimensions: Vector2i = imageSize ?: Vector2i(imageRgba.width, imageRgba.height).sub(offset)
        val newSize = newDimensions.x * newDimensions.y
        val rgbaData = ByteArray(newSize * 4)

        var newImageIndex = 0

        for (i in 0..<rawPixelData.size step 4) {
            val currentPixelIndex = i / 4
            val newImagePixelIndex = newImageIndex / 4
            val pixelY = currentPixelIndex / imageRgba.width
            val pixelX = currentPixelIndex % imageRgba.width
            if (pixelX < offset.x) continue
            if (pixelY < offset.y) continue
            if (newImagePixelIndex >= newSize) continue
            rgbaData[newImageIndex] = rawPixelData[i + 3]  // R (was last byte)
            rgbaData[newImageIndex + 1] = rawPixelData[i + 2]  // G
            rgbaData[newImageIndex + 2] = rawPixelData[i + 1]  // B
            rgbaData[newImageIndex + 3] = rawPixelData[i]      // A (was first byte)
            newImageIndex += 4
        }

        val nioBuffer = ByteBuffer.allocateDirect(rgbaData.size)
        nioBuffer.put(rgbaData)
        nioBuffer.flip()
        return RGBAImage(nioBuffer, newDimensions.x, newDimensions.y)
    }

    fun loadAtlas(
        ctx: GL3,
        key: String,
        tileSize: Vector2i, // size of a tile
        tileDimensions: Vector2i, // amount of tiles
        filter: ImageFilter = ImageFilter(),
    ) {
        val image = loadTextureData(key)


        // Calculate number of tiles
        val tiles = tileDimensions.x * tileDimensions.y

        // Create texture array
        val texturePointer = IntPointer()
        ctx.glGenTextures(1, texturePointer.array, 0)
        val textureId = texturePointer.value

        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, textureId)

        // Allocate storage for the texture array
        ctx.glTexStorage3D(
            GL3.GL_TEXTURE_2D_ARRAY,
            1, // Mipmap levels
            GL3.GL_RGBA8,
            tileSize.x,
            tileSize.y,
            tiles
        )

        val alphas: MutableList<Int> = mutableListOf()
        // Extract each tile and upload to the texture array
        for (y in 0 until tileDimensions.y) {
            for (x in 0 until tileDimensions.x) {
                val bufferImage = BufferedImage(tileSize.x, tileSize.y, BufferedImage.TYPE_INT_ARGB)

                val layer = y * tileDimensions.x + x

                // Allocate buffer for tile data
                val channel = 4 // Assuming RGBA
                val buffer = ByteBuffer.allocateDirect(tileSize.x * tileSize.y * channel)
                buffer.order(ByteOrder.nativeOrder())

                // Extract tile data from atlas
                for (row in 0 until tileSize.y) {
                    for (col in 0 until tileSize.x) {
                        // Calculate position in atlas
                        val atlasX = x * tileSize.x + col
                        val atlasY = y * tileSize.y + row

                        // Calculate index in atlas data
                        val atlasIndex = (atlasY * image.width + atlasX) * channel

                        // Copy pixel data to buffer
                        if (atlasIndex + 3 < image.image.capacity()) {
                            buffer.put(image.image.get(atlasIndex))     // R
                            buffer.put(image.image.get(atlasIndex + 1)) // G
                            buffer.put(image.image.get(atlasIndex + 2)) // B
                            buffer.put(image.image.get(atlasIndex + 3)) // A
                            alphas.add(image.image.get(atlasIndex + 3).toInt())
//                            val argb = ((image.image.getInt(atlasIndex)) shl 8) or image.image.get(atlasIndex + 3).toInt()

                            val r = image.image.get(atlasIndex).toInt() and 0xFF
                            val g = image.image.get(atlasIndex + 1).toInt() and 0xFF
                            val b = image.image.get(atlasIndex + 2).toInt() and 0xFF
                            val a = image.image.get(atlasIndex + 3).toInt() and 0xFF
                            val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
                            bufferImage.setRGB(row, col, argb)
                        } else {
                            // Handle edge cases by adding transparent pixels
                            buffer.put(0) // R
                            buffer.put(0) // G
                            buffer.put(0) // B
                            buffer.put(0) // A
                            bufferImage.setRGB(row, col, 0)
                        }
                    }
                }
//                val location = "${key}/${layer}.png"
//                val file = File(location)
//                file.parentFile.mkdirs()
//                ImageIO.write(bufferImage, "PNG", file)
//                println("Storing image at ${file.absolutePath} ${bufferImage.width}x${bufferImage.height}")
                // Flip buffer for OpenGL
                buffer.flip()

                // Upload tile to texture array layer
                ctx.glTexSubImage3D(
                    GL3.GL_TEXTURE_2D_ARRAY,
                    0, // Mipmap level
                    0, 0, layer, // x, y, z offsets
                    tileSize.x, tileSize.y, 1, // width, height, depth
                    GL3.GL_RGBA,
                    GL3.GL_UNSIGNED_BYTE,
                    buffer
                )
            }
        }


        if (key.contains("road"))
            println("Average alpha for $key:${alphas.average()}")


        // Set texture parameters based on filter settings
        val minFilter = if (filter.useMipmaps) {
            if (filter.useLinear) GL3.GL_LINEAR_MIPMAP_LINEAR else GL3.GL_NEAREST_MIPMAP_NEAREST
        } else {
            if (filter.useLinear) GL3.GL_LINEAR else GL3.GL_NEAREST
        }

        ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MIN_FILTER, minFilter)
        ctx.glTexParameteri(
            GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MAG_FILTER,
            if (filter.useLinear) GL3.GL_LINEAR else GL3.GL_NEAREST
        )

        if (filter.useClamp) {
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE)
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE)
        } else {
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT)
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT)
        }

        // Generate mipmaps if requested
        if (filter.useMipmaps) {
            ctx.glGenerateMipmap(GL3.GL_TEXTURE_2D_ARRAY)
        }

        // Unbind texture
        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, 0)

        textures[key] = texturePointer.value


    }

}