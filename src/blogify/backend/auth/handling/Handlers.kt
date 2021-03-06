package blogify.backend.auth.handling

import blogify.backend.auth.jwt.validateJwt

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.util.pipeline.pipelineExecutorFor

import blogify.backend.resources.User
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.resources.models.eqr
import blogify.backend.routing.pipelines.CallPipeline
import blogify.backend.util.reason

/**
 * Represents a predicate applied on a [user][User].
 */
typealias UserAuthPredicate = suspend (user: User) -> Boolean

/**
 * Validates a token by making sure it authenticates a certain [User].
 *
 * @param mustBe the mustBe to whom the token must belong to
 */
fun isUser(mustBe: User): UserAuthPredicate = { user ->
        mustBe eqr user
}

/**
 * Allows to wrap a pipeline into a block that takes care of authentication using a given [predicate][UserAuthPredicate].
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 */
@BlogifyDsl
suspend fun CallPipeline.runAuthenticated (
    predicate: UserAuthPredicate = { true },
    block:     PipelineInterceptor<User, ApplicationCall>
) {
    val header = call.request.header(HttpHeaders.Authorization) ?: run {
        call.respond(HttpStatusCode.Unauthorized) // Header is missing
        return
    }

    val token = header // Header validity procedure
        .substringAfter("Bearer ", "none")

    if (token == "none") {
        call.respond(HttpStatusCode.BadRequest, reason("malforrmed token"))
        return
    }

    validateJwt(call, token).fold (
        success = { user ->
            if (predicate.invoke(user)) { // Check token against predicate
                pipelineExecutorFor(call, listOf(block), user).execute(user)
            } else call.respond(HttpStatusCode.Forbidden)
        }, failure = { ex ->
            call.respond(HttpStatusCode.Forbidden, reason("invalid token - ${ex.javaClass.simpleName}"))
        }
    )

}
