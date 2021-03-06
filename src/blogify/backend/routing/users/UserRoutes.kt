package blogify.backend.routing.users

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routing.pipelines.fetchResource
import blogify.backend.routing.pipelines.pipeline
import blogify.backend.routing.handling.deleteResource
import blogify.backend.routing.handling.deleteUpload
import blogify.backend.routing.handling.fetchAllResources
import blogify.backend.routing.handling.fetchResource
import blogify.backend.routing.handling.respondExceptionMessage
import blogify.backend.routing.handling.updateResource
import blogify.backend.routing.handling.uploadToResource
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.services.UserService
import blogify.backend.services.models.Service
import blogify.backend.util.toUUID

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        get("/") {
            fetchAllResources<User>()
        }

        get("/{uuid}") {
            fetchResource<User>()
        }

        delete("/{uuid}") {
            deleteResource<User> (
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        patch("/{uuid}") {
            updateResource<User> (
                authPredicate = { user, replaced -> user eqr replaced }
            )
        }

        get("/byUsername/{username}") {
            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            UserService.getMatching { Users.username eq username }.fold(
                success = {
                    val user = it.single()
                    try {
                        selectedPropertyNames?.let { props ->

                            call.respond(user.slice(props))

                        } ?: call.respond(user.sanitize())
                    } catch (bruhMoment: Service.Exception) {
                        call.respondExceptionMessage(bruhMoment)
                    }
                },
                failure = { call.respondExceptionMessage(it) }
            )

        }

        post("/upload/{uuid}") {
            uploadToResource<User> (
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        delete("/upload/{uuid}") {
            deleteUpload<User>(authPredicate = { user, manipulated -> user eqr manipulated })
        }

        get("/search") {
            pipeline("q") { (query) ->
                call.respond(Typesense.search<User>(query).asSearchView())
            }
        }

        post("{uuid}/follow") {
            val follows = Users.Follows

            pipeline("uuid") { (uuid) ->

                val following = fetchResource(UserService::get, uuid.toUUID())

                runAuthenticated {

                    val hasAlreadyFollowed = query {
                        follows.select {
                            (follows.follower eq subject.uuid) and (follows.following eq following.uuid)
                        }.count()
                    }.get() == 1

                    if (!hasAlreadyFollowed) {
                        query {
                            follows.insert {
                                it[Users.Follows.follower] = subject.uuid
                                it[Users.Follows.following] = following.uuid
                            }
                        }
                    } else {
                        query {
                            follows.deleteWhere {
                                (follows.follower eq subject.uuid) and (follows.following eq following.uuid)
                            }
                        }
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }

        }

    }

}
