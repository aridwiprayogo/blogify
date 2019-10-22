package blogify.backend.resources.static.fs

import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticResource
import blogify.backend.resources.static.models.StaticResourceHandle
import io.ktor.http.ContentType

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

import java.io.File

object StaticFileHandler {

    private const val BASE_STATIC_FILE_PATH = "/var/static"
    private const val STATIC_FILE_EXTENSION = "blogifystatic"
    private val STATIC_FILE_SIGNATURE = byteArrayOf(0x6c, 0x75, 0x63, 0x79)

    /**
     * Reads a file from filesystem and returns its [StaticResource]
     *
     * @param handle [StaticResourceHandle] for the file to read.
     *
     * @return [StaticResource] of the requested file
     *
     * @author hamza1311
     */
    suspend fun readStaticResource(handle: StaticResourceHandle.Ok): StaticResource = withContext(IO) {

        val file        = File("$BASE_STATIC_FILE_PATH/${handle.fileId}")
        val rawBytes    = file.readBytes().drop(STATIC_FILE_SIGNATURE.size)
        val contentType = String(rawBytes.takeWhile { it != 0x00.toByte() }.toByteArray())
        val content     = rawBytes.dropWhile { it != 0x00.toByte() }.drop(1).toByteArray()

        return@withContext StaticResource(ContentType.parse(contentType), content)
    }

    /**
     * Writes a file containing an uploaded [StaticResource] onto the filesystem
     *
     * @param slotCode       the code of the slot, of the format `<resource class name>_<slot property name>`
     * @param resource       the [Resource] instance on which the slot is present
     * @param staticResource the actual byte data of the uploaded static resource
     *
     * @return a [StaticResourceHandle.Ok] describing the stored [static resource][StaticResource]
     *
     * @author Benjozork
     */
    suspend fun writeStaticResource (
        slotCode:       String,
        resource:       Resource,
        staticResource: StaticResource
    ): StaticResourceHandle.Ok = withContext(IO) {

        val fileId = "${resource.uuid}_$slotCode"

        // Create file from base name, fileId and extension
        val targetFile = File("$BASE_STATIC_FILE_PATH/$fileId.$STATIC_FILE_EXTENSION")

        // Write contents
        targetFile.appendBytes (
             STATIC_FILE_SIGNATURE
                + staticResource.contentType.toString().toByteArray()
                + 0x00
                + staticResource.data
        )

        // Return created handle
        return@withContext StaticResourceHandle.Ok(slotCode, fileId)
    }

}