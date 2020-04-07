package blogify.backend.events.models

import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.models.Mapped

import java.time.Instant

open class Event (
    val emitter: EventEmitter,
    val source: EventSource
) : Mapped() {

    val timestamp: Instant = Instant.now()

    suspend fun send(request: RequestContext) {
        source.targets.forEach { it.sendEvent(request.appContext, this) }
    }

}