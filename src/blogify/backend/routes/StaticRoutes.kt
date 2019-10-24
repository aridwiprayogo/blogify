package blogify.backend.routes

import blogify.backend.resources.User
import blogify.backend.resources.static.fs.StaticFileHandler
import blogify.backend.routes.handling.pipeline
import blogify.backend.routes.handling.uploadToResource
import blogify.backend.services.UserService

import io.ktor.application.call
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

fun Route.static() {

    post("/testupload/{uuid}") {
        uploadToResource<User> (
            fetch  = UserService::get,
            modify = { r, h -> r.copy(profilePicture = h) },
            update = UserService::update
        )
    }

    get("/get/{uploadableId}") {

        pipeline("uploadableId") { (uploadableId) ->
            uploadableId!!
            val data = StaticFileHandler.readStaticResource(uploadableId.toLong())

            call.respondBytes(data.bytes, data.contentType)
        }

    }

}