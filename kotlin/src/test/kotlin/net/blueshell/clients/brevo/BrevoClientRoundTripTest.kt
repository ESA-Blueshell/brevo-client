package net.blueshell.clients.brevo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.absent
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import net.blueshell.clients.brevo.model.SendTransacEmailRequest
import net.blueshell.clients.brevo.model.SendTransacEmailRequestMessageVersionsInnerToInner
import net.blueshell.clients.brevo.model.SendTransacEmailRequestSender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClientResponseException

/**
 * Exercises the generated client against a stubbed Brevo.
 *
 * Split the same way as the Discord client's tests: request wiring is asserted
 * with `server.verify` so it needs no response fixture and does not rot when
 * upstream adds a field, and deserialisation is proven on the lightweight
 * models. The authentication header gets particular attention because
 * openapi-generator does not wire Brevo's `apiKey` scheme into the client at
 * all — [BrevoClient] is the only thing that puts it on the wire.
 */
class BrevoClientRoundTripTest {

    companion object {
        private lateinit var server: WireMockServer

        @BeforeAll
        @JvmStatic
        fun start() {
            server = WireMockServer(options().dynamicPort())
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() = server.stop()
    }

    private fun client(partnerKey: String? = null) =
        BrevoClient.create(apiKey = "test-api-key", partnerKey = partnerKey, baseUrl = server.baseUrl())

    @BeforeEach
    fun reset() = server.resetAll()

    private fun stub(method: String, path: String, body: String, status: Int = 200) {
        val response = aResponse().withStatus(status)
            .withHeader("Content-Type", "application/json").withBody(body)
        when (method) {
            "GET" -> server.stubFor(get(urlPathEqualTo(path)).willReturn(response))
            "POST" -> server.stubFor(post(urlPathEqualTo(path)).willReturn(response))
            else -> error("Unsupported stub method $method")
        }
    }

    // ── Authentication ──────────────────────────────────────────────────────

    @Test
    fun `sends the api-key header on every request`() {
        // Brevo answers a missing api-key with a 401 whose message does not
        // mention the header, so this is worth pinning rather than assuming.
        stub("GET", "/contacts", """{"contacts":[],"count":0}""")

        client().contacts.getContacts()

        server.verify(getRequestedFor(urlPathEqualTo("/contacts")).withHeader("api-key", equalTo("test-api-key")))
    }

    @Test
    fun `omits the partner-key header entirely when none is configured`() {
        // Sending an empty partner-key is not the same as sending none; Brevo
        // rejects the former on non-partner accounts.
        stub("GET", "/contacts", """{"contacts":[],"count":0}""")

        client().contacts.getContacts()

        server.verify(getRequestedFor(urlPathEqualTo("/contacts")).withHeader("partner-key", absent()))
    }

    @Test
    fun `sends the partner-key header when one is configured`() {
        stub("GET", "/contacts", """{"contacts":[],"count":0}""")

        client(partnerKey = "test-partner-key").contacts.getContacts()

        server.verify(getRequestedFor(urlPathEqualTo("/contacts")).withHeader("partner-key", equalTo("test-partner-key")))
    }

    @Test
    fun `rejects a blank api key rather than sending an unauthenticated request`() {
        assertThatThrownBy { BrevoClient.create(apiKey = "  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("non-blank apiKey")
    }

    // ── Request wiring ──────────────────────────────────────────────────────

    @Test
    fun `sendTransacEmail posts to the smtp email endpoint`() {
        stub("POST", "/smtp/email", """{"messageId":"<202609021200.1@smtp-relay.mailin.fr>"}""", status = 201)

        val response = client().transactionalEmails.sendTransacEmail(
            SendTransacEmailRequest(
                sender = SendTransacEmailRequestSender(email = "noreply@blueshell.net", name = "Blueshell"),
                to = listOf(SendTransacEmailRequestMessageVersionsInnerToInner(email = "member@example.com")),
                subject = "Welcome",
                htmlContent = "<p>Hello</p>",
            ),
        )

        assertThat(response.messageId).isEqualTo("<202609021200.1@smtp-relay.mailin.fr>")
        server.verify(
            postRequestedFor(urlPathEqualTo("/smtp/email"))
                .withHeader("api-key", equalTo("test-api-key"))
                .withRequestBody(
                    equalToJson(
                        """
                        {
                          "sender": {"email":"noreply@blueshell.net","name":"Blueshell"},
                          "to": [{"email":"member@example.com"}],
                          "subject": "Welcome",
                          "htmlContent": "<p>Hello</p>"
                        }
                        """.trimIndent(),
                        true,
                        true,
                    ),
                ),
        )
    }

    @Test
    fun `omits unset optional fields instead of sending nulls`() {
        // NON_ABSENT inclusion. Brevo treats an explicit null on some fields as
        // a value rather than as absence, so this is behavioural, not cosmetic.
        stub("POST", "/smtp/email", """{"messageId":"x"}""", status = 201)

        client().transactionalEmails.sendTransacEmail(SendTransacEmailRequest(subject = "Only a subject"))

        val body = server.allServeEvents.single().request.bodyAsString
        assertThat(body).contains("subject")
        assertThat(body).doesNotContain("templateId")
        assertThat(body).doesNotContain("null")
        // Exactly one field, so nothing rode along unasked.
        assertThat(body).isEqualTo("""{"subject":"Only a subject"}""")
    }

    @Test
    fun `getContacts sends pagination as query parameters`() {
        stub("GET", "/contacts", """{"contacts":[],"count":0}""")

        client().contacts.getContacts(limit = 10, offset = 20)

        server.verify(
            getRequestedFor(urlPathEqualTo("/contacts"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("offset", equalTo("20")),
        )
    }

    // ── Response deserialisation ────────────────────────────────────────────

    @Test
    fun `getContacts deserialises the contact list and count`() {
        stub(
            "GET",
            "/contacts",
            """{"contacts":[{"email":"member@example.com","id":42,"emailBlacklisted":false,"smsBlacklisted":false,"whatsappBlacklisted":false,"createdAt":"2026-01-01T00:00:00.000Z","modifiedAt":"2026-01-01T00:00:00.000Z","listIds":[3],"attributes":{"FIRSTNAME":"Nelly"}}],"count":1}""",
        )

        val contacts = client().contacts.getContacts()

        assertThat(contacts.count).isEqualTo(1)
        assertThat(contacts.contacts).singleElement().satisfies({
            assertThat(it.email).isEqualTo("member@example.com")
        })
    }

    @Test
    fun `surfaces a Brevo error as a RestClientResponseException carrying the status`() {
        stub("GET", "/contacts", """{"code":"unauthorized","message":"Key not found"}""", status = 401)

        assertThatThrownBy { client().contacts.getContacts() }
            .isInstanceOf(RestClientResponseException::class.java)
            .satisfies({ assertThat((it as RestClientResponseException).statusCode.value()).isEqualTo(401) })
    }
}
