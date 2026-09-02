package net.blueshell.clients.brevo

import net.blueshell.clients.brevo.api.ContactsApi
import net.blueshell.clients.brevo.api.TransactionalEmailsApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredFunctions

/**
 * Pins the generated surface.
 *
 * The client is generated, so nothing here tests hand-written logic. What it
 * does test is that the spec pipeline still produces the client the version
 * number claims. Brevo's surface is declared by tag, and a tag pulls in
 * everything upstream files under it — so an upstream operation appearing or
 * disappearing shows up here as a failing assertion with a name, rather than as
 * a silent change in what the published artefact contains.
 *
 * When this fails after a spec sync, that is the test doing its job: check the
 * classification, then update the list in the same commit.
 */
class BrevoApiSurfaceTest {

    private fun operationsOf(type: kotlin.reflect.KClass<*>): Set<String> =
        type.declaredFunctions
            .map { it.name }
            .filterNot { it.endsWith("WithHttpInfo") }
            .filterNot { it.endsWith("RequestConfig") }
            .toSet()

    @Test
    fun `exposes exactly the transactional email operations upstream tags`() {
        assertThat(operationsOf(TransactionalEmailsApi::class)).containsExactlyInAnyOrder(
            "blockNewDomain",
            "createSmtpTemplate",
            "deleteBlockedDomain",
            "deleteHardbounces",
            "deleteScheduledEmailById",
            "deleteSmtpTemplate",
            "getAggregatedSmtpReport",
            "getBlockedDomains",
            "getEmailEventReport",
            "getScheduledEmailById",
            "getSmtpReport",
            "getSmtpTemplate",
            "getSmtpTemplates",
            "getTransacBlockedContacts",
            "getTransacEmailContent",
            "getTransacEmailsList",
            "postPreviewSmtpEmailTemplates",
            "sendTestTemplate",
            "sendTransacEmail",
            "smtpBlockedContactsEmailDelete",
            "smtpLogIdentifierDelete",
            "updateSmtpTemplate",
        )
    }

    @Test
    fun `exposes exactly the contact operations upstream tags`() {
        assertThat(operationsOf(ContactsApi::class)).containsExactlyInAnyOrder(
            "addContactToList",
            "createAttribute",
            "createContact",
            "createDoiContact",
            "createFolder",
            "createList",
            "deleteAttribute",
            "deleteContact",
            "deleteFolder",
            "deleteList",
            "deleteMultiAttributeOptions",
            "getAttributes",
            "getContactInfo",
            "getContactStats",
            "getContacts",
            "getContactsFromList",
            "getFolder",
            "getFolderLists",
            "getFolders",
            "getList",
            "getLists",
            "getSegments",
            "importContacts",
            "removeContactFromList",
            "requestContactExport",
            "updateAttribute",
            "updateBatchContacts",
            "updateContact",
            "updateFolder",
            "updateList",
        )
    }

    @Test
    fun `exposes both tagged API groups and nothing else`() {
        val client = BrevoClient.create(apiKey = "test-key")
        assertThat(client.transactionalEmails).isInstanceOf(TransactionalEmailsApi::class.java)
        assertThat(client.contacts).isInstanceOf(ContactsApi::class.java)
    }

    @Test
    fun `covers the whole consumed surface`() {
        // 52 operations across the two tags declared in specs/surface.json.
        val total = operationsOf(TransactionalEmailsApi::class).size +
            operationsOf(ContactsApi::class).size
        assertThat(total).isEqualTo(52)
    }
}
