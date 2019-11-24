package blogify.backend.resources

import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.annotations.noslice
import blogify.backend.annotations.type

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import java.util.*

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class User (
             val username:       String,
    @noslice val password:       String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !
             val name:           String,
             val email:          String,
             val profilePicture: @type("image/png") StaticResourceHandle,
             val isAdmin: Boolean = true,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)
