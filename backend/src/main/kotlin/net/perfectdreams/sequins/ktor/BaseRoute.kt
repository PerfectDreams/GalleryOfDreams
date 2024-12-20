package net.perfectdreams.sequins.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * A [BaseRoute] is used to register Ktor routes on the [routing] section, allowing you to split up routes in different classes.
 *
 * One of the advantages of using [BaseRoute] is that all routes are in different files, keeping the code base tidy and nice.
 *
 * Another advantage is that the HTTP Method is inferred from the class name, so, if the class is named `PostUserDataRoute`, the route will be
 * registered as a POST instead of an GET.
 *
 * All HTTP Methods, except GET, needs to be explictly set in the class name.
 *
 * @param path the path's route
 */
abstract class BaseRoute(val path: String) {
    abstract suspend fun onRequest(call: ApplicationCall)

    fun register(routing: Routing) = registerWithPath(routing, path) { onRequest(call) }
    fun registerWithPath(routing: Routing, path: String) = registerWithPath(routing, path) { onRequest(call) }

    fun registerWithPath(routing: Routing, path: String, callback: RoutingHandler) {
        val method = getMethod()
        when (method) {
            HttpMethod.Get -> routing.get(path, callback)
            HttpMethod.Post -> routing.post(path, callback)
            HttpMethod.Patch -> routing.patch(path, callback)
            HttpMethod.Put -> routing.put(path, callback)
            HttpMethod.Delete -> routing.delete(path, callback)
            else -> routing.get(path, callback)
        }
    }

    open fun getMethod(): HttpMethod {
        val className = this::class.simpleName?.lowercase() ?: "Unknown"
        return when {
            className.startsWith("get") -> HttpMethod.Get
            className.startsWith("post") -> HttpMethod.Post
            className.startsWith("patch") -> HttpMethod.Patch
            className.startsWith("put") -> HttpMethod.Put
            className.startsWith("delete") -> HttpMethod.Delete
            else -> HttpMethod.Get
        }
    }
}