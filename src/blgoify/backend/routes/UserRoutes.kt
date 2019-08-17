package blgoify.backend.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import blgoify.backend.resources.User
import blgoify.backend.services.UserService

fun Route.users() {

    route("/users") {

        // Get all users

        get("/") {
            call.respond(UserService.getAll())
        }

        get("/user/{uuid}") {
            call.parameters["uid"]?.let {
                UserService.getUserByUid(it)?.let { user ->
                    call.respond(user)
                } ?: call.respond(HttpStatusCode.NotFound)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/user") {
            val user = call.receive<User>()
            UserService.add(user)
            println(user)
            call.respond(HttpStatusCode.Created)
        }
    }
}