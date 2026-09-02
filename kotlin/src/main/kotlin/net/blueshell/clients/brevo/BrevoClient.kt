package net.blueshell.clients.brevo

import net.blueshell.clients.brevo.api.ContactsApi
import net.blueshell.clients.brevo.api.TransactionalEmailsApi
import net.blueshell.clients.brevo.infrastructure.Serializer
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient

/**
 * Authenticated entry point for the Brevo API.
 *
 * The generated API classes take a bare [RestClient] and know nothing about
 * authentication — openapi-generator does not wire `apiKey` security schemes
 * into the Kotlin `jvm-spring-restclient` library. Without this, every consumer
 * rebuilds the same `RestClient` with the same header, and forgetting it yields
 * a 401 whose message does not mention the missing header.
 *
 * This is the only hand-written Kotlin in the repository; everything under
 * `net.blueshell.clients.brevo.api` and `.model` is generated.
 */
class BrevoClient private constructor(restClient: RestClient) {

    /** Operations Brevo tags `Transactional emails`. */
    val transactionalEmails: TransactionalEmailsApi = TransactionalEmailsApi(restClient)

    /** Operations Brevo tags `Contacts`. */
    val contacts: ContactsApi = ContactsApi(restClient)

    companion object {
        /** Brevo's documented API base URL. */
        const val DEFAULT_BASE_URL: String = "https://api.brevo.com/v3"

        private const val API_KEY_HEADER = "api-key"
        private const val PARTNER_KEY_HEADER = "partner-key"

        /**
         * Builds a client authenticated with an account API key.
         *
         * @param apiKey the value Brevo sends as the `api-key` header.
         * @param partnerKey optional `partner-key`, required only for partner
         *   accounts and omitted entirely when null.
         * @param baseUrl override; useful only for tests and proxies.
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            apiKey: String,
            partnerKey: String? = null,
            baseUrl: String = DEFAULT_BASE_URL,
        ): BrevoClient {
            require(apiKey.isNotBlank()) { "BrevoClient.create requires a non-blank apiKey." }

            val builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(API_KEY_HEADER, apiKey)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                // The generated Serializer's mapper, and `withJsonConverter`
                // so it replaces the default JSON converter rather than sitting
                // behind it. That mapper sets NON_ABSENT inclusion, so an unset
                // field is omitted rather than sent as an explicit null —
                // behavioural for Brevo, which reads a null on several fields as
                // a value rather than as absence.
                .configureMessageConverters {
                    it.registerDefaults().withJsonConverter(JacksonJsonHttpMessageConverter(Serializer.jacksonObjectMapper))
                }

            // Set only when present. An empty `partner-key` is not the same as
            // no `partner-key`: Brevo rejects the former on non-partner accounts.
            if (partnerKey != null) {
                builder.defaultHeader(PARTNER_KEY_HEADER, partnerKey)
            }

            return BrevoClient(builder.build())
        }

        /**
         * Wraps a [RestClient] the caller has already configured.
         *
         * For applications that route every outbound call through their own
         * builder — timeouts, retries, metrics, tracing. Authentication is then
         * the caller's responsibility.
         */
        @JvmStatic
        fun using(restClient: RestClient): BrevoClient = BrevoClient(restClient)
    }
}
