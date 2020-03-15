package blogify.backend.push

import blogify.backend.annotations.Invisible
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.sanitize
import blogify.backend.notifications.models.Notification as ActualNotification

import io.ktor.http.cio.websocket.Frame

/**
 * Represents a message sent from a connected client or to that client
 *
 * @author Benjozork
 */
sealed class Message : Mapped() {

    /**
     * Represents a message that is going out to the client
     *
     * @property frame the content of the frame to be sent for that message
     */
    abstract class Outgoing(message: String) : Message() {

        @Invisible val frame = Frame.Text(message)

        class Notification(notification: ActualNotification<*, *, *>) : Outgoing("notif ${notification.sanitize()}")

    }

    /**
     * Represents a message that is being sent by the client
     *
     * @property connection the [PushServer.Connection] from which the message originated
     */
    abstract class Incoming (
        @Invisible open val connection: PushServer.Connection
    ) : Message() {

        abstract suspend fun onArrival()

    }

}
