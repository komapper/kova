package example

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testJsonValidation() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/json") {
            contentType(ContentType.Application.Json)
            setBody(Customer(-1, "Jet", "Brains"))
        }
        assertEquals("A customer ID should be greater than 0", response.bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}