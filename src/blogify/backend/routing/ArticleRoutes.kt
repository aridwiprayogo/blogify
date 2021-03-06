@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Articles
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routing.pipelines.optionalParam
import blogify.backend.routing.pipelines.pipeline
import blogify.backend.routing.handling.createResource
import blogify.backend.routing.handling.deleteResource
import blogify.backend.routing.handling.fetchAllResources
import blogify.backend.routing.handling.fetchResource
import blogify.backend.routing.handling.getValidations
import blogify.backend.routing.handling.respondExceptionMessage
import blogify.backend.routing.handling.updateResource
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.services.UserService
import blogify.backend.services.ArticleService
import blogify.backend.services.models.Service
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAllResources<Article>()
        }

        get("/{uuid}") {
            fetchResource<Article>()
        }

        val likes = Articles.Likes

        get("/{uuid}/like") {
            pipeline("uuid") { (id) ->
                runAuthenticated {
                    val article = ArticleService.get(call, id.toUUID())
                        .getOrPipelineError(HttpStatusCode.NotFound, "couldn't fetch article")

                    val liked = query {
                        likes.select {
                            (likes.article eq article.uuid) and (likes.user eq subject.uuid) }.count()
                    }.getOrPipelineError() == 1;

                    call.respond(liked)
                }
            }
        }

        post("/{uuid}/like") {
            pipeline("uuid") { (id) ->
                runAuthenticated {
                    val articleToLike = ArticleService.get(call, id.toUUID())
                        .getOrPipelineError(HttpStatusCode.NotFound, "couldn't fetch article")

                    // Figure whether the article was already liked by the user
                    val alreadyLiked = query {
                        likes.select {
                            (likes.article eq articleToLike.uuid) and (likes.user eq subject.uuid) }.count()
                    }.getOrPipelineError() == 1

                    if (!alreadyLiked) { // Add a like if none were present
                        query {
                            likes.insert {
                                it[article] = articleToLike.uuid
                                it[user]    = subject.uuid
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't like article")

                        call.respond(HttpStatusCode.OK, reason("article liked"))
                    } else { // Remove an existing like if there was one
                        query {
                            likes.deleteWhere {
                                (likes.article eq articleToLike.uuid) and (likes.user eq subject.uuid)
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't unlike article")

                        call.respond(HttpStatusCode.OK, reason("article unliked"))
                    }
                }
            }
        }

        get("/forUser/{username}") {

            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            UserService.getMatching { Users.username eq username }.fold (
                success = {
                    ArticleService.getMatching { Articles.createdBy eq it.single().uuid }.fold (
                        success = { articles ->
                            try {
                                selectedPropertyNames?.let { props ->

                                    call.respond(articles.map { it.slice(props) })

                                } ?: call.respond(articles.map { it.sanitize() })
                            } catch (bruhMoment: Service.Exception) {
                                call.respondExceptionMessage(bruhMoment)
                            }
                        },
                        failure = { call.respondExceptionMessage(it) }
                    )
                },
                failure = { call.respondExceptionMessage(it) }
            )
        }

        delete("/{uuid}") {
            deleteResource<Article> (
                authPredicate = { user, article -> article.createdBy == user }
            )
        }

        patch("/{uuid}") {
            updateResource<Article> (
                authPredicate = { user, article -> article.createdBy eqr user }
            )
        }

        post("/") {
            createResource<Article> (
                authPredicate = { user, article -> article.createdBy eqr user }
            )
        }

        get("/search") {
            pipeline("q") { (query) ->
                val user = optionalParam("byUser")?.toUUID()
                if (user != null) {
                    val userHandle = Article::class.cachedPropMap().ok()["createdBy"] ?: error("a")
                    call.respond(Typesense.search<Article>(query, mapOf(userHandle to user)).asSearchView())
                } else {
                    call.respond(Typesense.search<Article>(query).asSearchView())
                }
            }
        }

        get("_validations") {
            getValidations<Article>()
        }

        articleComments()

    }

}
