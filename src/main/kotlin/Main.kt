import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = 4444, host = "localhost") {
        module()
    }.start(wait = true)
}

/*
fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module).start(wait = true)
}*/

fun Application.module() {
    configureRouting()
    loginModule()
}

fun Application.loginModule() {
    routing {
/*
        get("/login") {
            call.respond(HttpStatusCode.Accepted, "Hey")
        }
*/

        get {
            val jsonFile = File("src/main/resources/response.json")
            val jsonData = jsonFile.readText()
            call.respondText(jsonData, contentType = ContentType.Application.Json)
        }
        post("/api") {
            val customHeader = call.request.headers["X-API-Header"]
            if (customHeader == "ranjan") {
                val post = call.receive<String>()
                call.respondText("Data received: $post")
            } else if (customHeader != null) {
                call.respondText("X-API-Header not valid")
            } else call.respond(HttpStatusCode.Forbidden, "Api needs X-API-header")
        }
        post {
            val post = call.receive<String>()
            call.respondText("Data received: $post")
        }
        post("/upload") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = File(part.originalFileName.toString()).extension
                    val file = File("src/main/resources/uploads/${part.originalFileName}")
                    part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyTo(it) } }
                    call.respondText(
                        uploadJsonResponse(part.originalFileName.toString()),
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }
    }
}

fun uploadJsonResponse(name: String): String {
    return "{\n" +
            "  \"response\": \"File $name is uploaded to the server\"\n" +
            "}"
}

fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respondText("Hello World!")
        }
    }
}