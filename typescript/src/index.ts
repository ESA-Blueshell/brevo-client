/**
 * Public surface of the Brevo client.
 *
 * Everything under `./generated` is produced by `@hey-api/openapi-ts` from
 * `specs/brevo.json` and is overwritten on every spec sync — never edit it.
 * This module is the only hand-written file: it re-exports the generated
 * operations and adds the wiring every consumer would otherwise repeat.
 */

import { createClient, createConfig, type Client } from './generated/client'
import type { ClientOptions } from './generated/types.gen'

export * from './generated'
export type { Client } from './generated/client'

/** Brevo's documented API base URL. */
export const BREVO_API_BASE_URL = 'https://api.brevo.com/v3'

export interface BrevoClientOptions {
  /** Account API key, sent as the `api-key` header. */
  apiKey: string
  /**
   * Optional `partner-key`, required only for partner accounts. Omitted
   * entirely when absent — an empty header is not the same as no header, and
   * Brevo rejects the former on non-partner accounts.
   */
  partnerKey?: string
  /** Override the base URL; useful only for tests and proxies. */
  baseURL?: string
  /** Request timeout in milliseconds. */
  timeoutMs?: number
}

/**
 * Builds a client authenticated with a Brevo API key.
 *
 * ```ts
 * const client = createBrevoClient({ apiKey: process.env.BREVO_API_KEY! })
 * const { data } = await getContacts({ client, query: { limit: 10 } })
 * ```
 */
export function createBrevoClient(options: BrevoClientOptions): Client {
  const { apiKey, partnerKey, baseURL = BREVO_API_BASE_URL, timeoutMs } = options

  if (apiKey.trim() === '') {
    // Brevo answers a missing key with a 401 whose message does not mention
    // the header, so failing here is considerably easier to debug.
    throw new Error('createBrevoClient requires a non-empty apiKey.')
  }

  return createClient(
    createConfig<ClientOptions>({
      baseURL,
      timeout: timeoutMs,
      headers: {
        'api-key': apiKey,
        ...(partnerKey === undefined ? {} : { 'partner-key': partnerKey }),
      },
    }),
  )
}
