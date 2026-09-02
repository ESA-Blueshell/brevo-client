import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'

import * as brevo from '../src/index.js'
import {
  BREVO_API_BASE_URL,
  createBrevoClient,
  getContacts,
  sendTransacEmail,
} from '../src/index.js'
import { StubServer } from './stub-server.js'

const server = new StubServer()

beforeAll(() => server.start())
afterAll(() => server.stop())

function client(partnerKey?: string) {
  return createBrevoClient({ apiKey: 'test-api-key', partnerKey, baseURL: server.baseURL })
}

describe('createBrevoClient', () => {
  beforeEach(() => server.reply(() => ({ json: { contacts: [], count: 0 } })))

  it('sends the api-key header', async () => {
    // Brevo answers a missing key with a 401 that does not mention the header.
    await getContacts({ client: client() })
    expect(server.lastRequest.headers['api-key']).toBe('test-api-key')
  })

  it('omits partner-key entirely when none is configured', async () => {
    // An empty partner-key is not the same as none: Brevo rejects the former
    // on non-partner accounts.
    await getContacts({ client: client() })
    expect(server.lastRequest.headers['partner-key']).toBeUndefined()
  })

  it('sends partner-key when one is configured', async () => {
    await getContacts({ client: client('test-partner-key') })
    expect(server.lastRequest.headers['partner-key']).toBe('test-partner-key')
  })

  it('rejects an empty api key rather than sending an unauthenticated request', () => {
    expect(() => createBrevoClient({ apiKey: '   ' })).toThrow(/non-empty apiKey/)
  })

  it('defaults to the documented Brevo base URL', () => {
    expect(BREVO_API_BASE_URL).toBe('https://api.brevo.com/v3')
  })
})

describe('operation surface', () => {
  it('exposes every operation under the declared tags', () => {
    // 52 operations across `Transactional emails` and `Contacts`, matching the
    // Kotlin client exactly — both are generated from one spec surface and
    // share a version number, so a drift between them would make it a lie.
    const operations = Object.entries(brevo).filter(
      ([name, value]) => typeof value === 'function' && name !== 'createBrevoClient',
    )
    expect(operations).toHaveLength(52)
  })

  it('exports the operations the website actually calls', () => {
    for (const name of ['sendTransacEmail', 'getContacts', 'createContact', 'updateContact', 'getContactInfo']) {
      expect(typeof (brevo as Record<string, unknown>)[name], `${name} should be exported`).toBe('function')
    }
  })
})

describe('request wiring', () => {
  it('posts a transactional email to the smtp endpoint', async () => {
    server.reply(() => ({ status: 201, json: { messageId: '<202609021200.1@smtp-relay.mailin.fr>' } }))

    const { data } = await sendTransacEmail({
      client: client(),
      body: {
        sender: { email: 'noreply@blueshell.net', name: 'Blueshell' },
        to: [{ email: 'member@example.com' }],
        subject: 'Welcome',
        htmlContent: '<p>Hello</p>',
      },
    })

    expect(server.lastRequest.method).toBe('POST')
    expect(server.lastRequest.path).toBe('/smtp/email')
    expect(data?.messageId).toBe('<202609021200.1@smtp-relay.mailin.fr>')
  })

  it('sends only the fields that were set', async () => {
    // Brevo reads an explicit null on several fields as a value rather than as
    // absence, so a client that helpfully fills in nulls changes behaviour.
    server.reply(() => ({ status: 201, json: { messageId: 'x' } }))

    await sendTransacEmail({ client: client(), body: { subject: 'Only a subject' } })

    expect(JSON.parse(server.lastRequest.body)).toEqual({ subject: 'Only a subject' })
  })

  it('sends pagination as query parameters', async () => {
    server.reply(() => ({ json: { contacts: [], count: 0 } }))

    await getContacts({ client: client(), query: { limit: 10, offset: 20 } })

    expect(server.lastRequest.query.get('limit')).toBe('10')
    expect(server.lastRequest.query.get('offset')).toBe('20')
  })
})

describe('responses', () => {
  it('deserialises the contact list and count', async () => {
    server.reply(() => ({
      json: { contacts: [{ email: 'member@example.com', id: 42, listIds: [3] }], count: 1 },
    }))

    const { data } = await getContacts({ client: client() })

    expect(data?.count).toBe(1)
    expect(data?.contacts?.[0]?.email).toBe('member@example.com')
  })

  it('reports an upstream error instead of throwing by default', async () => {
    server.reply(() => ({ status: 401, json: { code: 'unauthorized', message: 'Key not found' } }))

    const result = await getContacts({ client: client() })

    expect(result.error).toBeDefined()
    expect(result.status).toBe(401)
    expect(result.data).toBeUndefined()
  })
})
