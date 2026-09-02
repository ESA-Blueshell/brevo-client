# brevo-client

Generated Kotlin and TypeScript clients for the part of the
[Brevo API](https://developers.brevo.com/) that ESA-Blueshell actually calls.

Both artefacts are generated from a single OpenAPI surface, released together,
and share one version number that is derived from what changed upstream. The
only hand-written code is the authenticated client factory in each language and
the pipeline under `tools/`.

| | |
| --- | --- |
| Maven | `net.blueshell.clients:brevo-client` |
| npm | `@esa-blueshell/brevo-client` |
| Surface | tags `Transactional emails` + `Contacts` — 52 operations ([`specs/surface.json`](specs/surface.json)) |
| Upstream | `https://api.brevo.com/v3/swagger_definition_v3.yml` |
| Spec refreshed | nightly, 00:00 UTC |

## Installing

> [!IMPORTANT]
> Both packages live in **GitHub Packages**, which requires an access token to
> install *even though the packages are public*. This is a GitHub limitation,
> not a setting on these repositories — only its Container registry serves
> anonymous pulls. A token with `read:packages` is enough, and any GitHub
> account can create one.

### TypeScript

`.npmrc`, alongside your `package.json`:

```ini
@esa-blueshell:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=${GITHUB_TOKEN}
```

```bash
npm install @esa-blueshell/brevo-client
```

```ts
import { createBrevoClient, sendTransacEmail, getContacts } from '@esa-blueshell/brevo-client'

const client = createBrevoClient({ apiKey: process.env.BREVO_API_KEY! })

await sendTransacEmail({
  client,
  body: {
    sender: { email: 'noreply@blueshell.net', name: 'Blueshell' },
    to: [{ email: 'member@example.com' }],
    subject: 'Welcome',
    htmlContent: '<p>Hello</p>',
  },
})

const { data } = await getContacts({ client, query: { limit: 50 } })
```

`axios` is a peer dependency, so your application pins the version.

Operations return `{ data, error, status }` rather than throwing, so a missing
`try`/`catch` cannot swallow a failure.

### Kotlin

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/ESA-Blueshell/brevo-client")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.token").orNull
        }
    }
}

dependencies {
    implementation("net.blueshell.clients:brevo-client:<version>")
}
```

```kotlin
val brevo = BrevoClient.create(apiKey = System.getenv("BREVO_API_KEY"))

brevo.transactionalEmails.sendTransacEmail(
    SendTransacEmailRequest(
        sender = SendTransacEmailRequestSender(email = "noreply@blueshell.net", name = "Blueshell"),
        to = listOf(SendTransacEmailRequestMessageVersionsInnerToInner(email = "member@example.com")),
        subject = "Welcome",
        htmlContent = "<p>Hello</p>",
    ),
)

val contacts = brevo.contacts.getContacts(limit = 50)
```

Errors surface as `RestClientResponseException`, which carries the status code.

> [!TIP]
> Use `BrevoClient.create(...)`, not the generated `TransactionalEmailsApi`
> constructor directly. The generated class knows nothing about Brevo's
> `api-key` scheme, and its convenience constructor registers a JSON converter
> *behind* Spring's own — so it authenticates with nothing and sends an explicit
> `null` for every field you did not set. Brevo reads a null on several fields
> as a value rather than as absence. `BrevoClient` fixes both.
>
> `BrevoClient.using(restClient)` wraps a `RestClient` you have configured
> yourself, if your application routes all outbound calls through its own
> builder.

## How it stays current

A nightly workflow fetches the upstream spec, reduces it to the tags declared in
`specs/surface.json`, and classifies any change with `oasdiff`:

- breaking change → **major**
- addition → **minor**
- documentation only → **patch**
- consumed surface unchanged → nothing at all

It then regenerates both clients, runs their tests, and opens a single pull
request whose title is a conventional commit encoding the bump. Merging it lets
release-please cut the version and publish.

> [!NOTE]
> GitHub does not run `pull_request` workflows on a pull request opened by the
> default `GITHUB_TOKEN`, so the nightly and release pull requests show no
> checks out of the box. The sync job builds and tests both clients *before*
> opening the PR, so nothing ships unverified — the evidence is in the workflow
> run. To get checks on the PR itself, add an `AUTOMATION_TOKEN` secret (a PAT
> or GitHub App token with `contents` and `pull-requests` write); both
> workflows pick it up automatically and fall back to the default token.

See [docs/versioning.md](docs/versioning.md) for why the surface is filtered
before it is classified, and why the tag strings must be Brevo's raw tags.

## Adding a tag

Edit [`specs/surface.json`](specs/surface.json) — using Brevo's raw tag string,
spaces and casing included — then:

```bash
npm --prefix tools ci
npm --prefix tools run sync          # refetches, filters, classifies
npm --prefix typescript run generate # regenerates the TypeScript client
```

`BrevoApiSurfaceTest` pins the exact operation list and will fail until you
update it; that is deliberate, so a change in what gets published is always
visible in the diff. Or just open an issue with the
[tag request template](.github/ISSUE_TEMPLATE/surface-request.yml).

## Working on it

```bash
npm --prefix tools ci && npm --prefix tools test   # pipeline tests
(cd kotlin && ./gradlew build)                     # generate, compile, test
(cd typescript && npm ci && npm test)              # typecheck and test
node tools/src/cli.mjs check                       # the CI spec gate
```

The Kotlin build shells out to `node` for the generator-fixup step so both
languages apply an identical set of workarounds. No `npm install` is needed for
that path — it reads only JSON and imports nothing.

## Layout

```
specs/       surface.json (tags we expose) + the filtered spec + upstream lock
tools/       fetch, filter, classify, fixups — with tests
kotlin/      Gradle build; generated at compile time into build/
typescript/  hey-api client; generated tree is committed under src/generated
docs/        versioning.md
```

`typescript/src/generated` is committed and `kotlin`'s is not, on purpose: the
committed TypeScript tree makes the nightly pull request show the real client
diff a reviewer needs to judge a version bump, and CI proves it reproduces.
