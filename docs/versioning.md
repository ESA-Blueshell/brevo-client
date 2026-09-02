# How versions are decided

The version number on both published artefacts describes **one thing**: the
consumed surface of the upstream Brevo API, as declared in
[`specs/surface.json`](../specs/surface.json). It is derived mechanically, not
chosen by a human.

## The rule

| What changed on the consumed surface | Bump | Conventional commit |
| --- | --- | --- |
| An operation, parameter or field was removed or changed incompatibly | **major** | `feat(spec)!:` + `BREAKING CHANGE:` footer |
| An operation or optional field was added | **minor** | `feat(spec):` |
| Only descriptions, examples or key ordering moved | **patch** | `fix(spec):` |
| Nothing this client exposes changed | none | no commit, no release |

The classification comes from [oasdiff](https://github.com/oasdiff/oasdiff),
run against the previous and new surface. `oasdiff breaking` is consulted first
because `oasdiff changelog` is a superset of it — checking the changelog first
would report a removed operation as a minor.

## Why the surface, not the upstream document

Brevo publishes roughly 380 operations across 36 tags in a 1.15 MB document.
This client binds two tags — `Transactional emails` and `Contacts` — which is
52 operations and 320 KB.

If the whole document decided the version, every unrelated change Brevo made to
Email Campaigns, Deals, Conversations or WhatsApp would move this client's
version number. It would describe Brevo's release cadence rather than this
client's compatibility, which is the opposite of what a version is for.

## Surfaces are declared by tag here

Unlike the Discord client, which allow-lists individual paths, Brevo tags its
operations and this surface names tags. Adding a tag pulls in **every**
operation Brevo files under it.

The tag strings are Brevo's raw tags, spaces and casing included:

```json
"tags": ["Transactional emails", "Contacts"]
```

`TransactionalEmails` is openapi-generator's *normalised class name*, not a tag,
and matches nothing. This is not hypothetical — it is the first thing that went
wrong when this repository was set up, and the pipeline now fails loudly rather
than generating an empty client:

```
Error: Upstream serves no operations under tag(s) this client declares:
TransactionalEmails.
```

The same guard fires if Brevo ever renames or retires a tag. Silently shipping a
client with no methods would be a breaking change wearing a patch's clothing.

## Nothing waits for a human

The chain runs unattended, so the published clients stay in step with the
upstream spec rather than with whoever remembers to press merge:

```
upstream spec moves
  -> spec-sync filters, classifies, regenerates, builds, tests
  -> opens a pull request titled with the conventional commit
  -> merges it
  -> release-please opens a release pull request
  -> merges that
  -> tags, and publishes both artefacts
```

Merging the spec-sync pull request without review is safe for two specific
reasons rather than as a general policy. The job has already regenerated both
clients, compiled them and run their tests before it opens the pull request, so
a spec change that breaks generation never reaches the merge step. And the
surface guards fail the run outright if upstream stops serving a declared
operation or tag, so the failure that actually matters — silently publishing a
smaller client — cannot get there either.

**Majors merge too.** Consumers pin versions, so publishing a major breaks
nobody who has not chosen to upgrade, and semver is precisely how the break is
communicated. Holding a breaking spec change back would leave the client
describing an API that no longer exists, which is the worse failure. To review
majors by hand instead, set the repository variable:

```
HOLD_MAJOR_RELEASES=true
```

The release job is skipped on the run that follows a release, which is what
stops the two workflows from cycling.

### How the release pull request is verified

GitHub does not execute workflow runs for a pull request opened with the
default `GITHUB_TOKEN`. It creates the run, allocates no job, and concludes it
`failure` — measured here as `jobs=0` and a red mark on all four workflows of
every release pull request. Those marks say nothing about the change, and
merging past them is indistinguishable from merging past a real failure.

So the pull-request-triggered workflows skip the two bot branches entirely, and
`release.yml` verifies the release pull request itself: it checks out the pull
request's head, runs the spec gate, the pipeline tests, both client builds and
their tests, and confirms the version in `kotlin/gradle.properties` and
`typescript/package.json` matches what release-please is about to publish. The
merge job depends on that verification, so a release cannot merge on a check
that never ran.

### The one gap

`main` is protected by the `Main` ruleset — pull request required, squash only,
linear history, no force pushes or deletion. It deliberately does **not**
require a status check, because GitHub does not run workflows on pull requests
opened with the default `GITHUB_TOKEN`: a required check would never report on
an automated pull request and the chain above would stall forever.

To require the check as well, add an `AUTOMATION_TOKEN` secret (a PAT or GitHub
App token with `contents` and `pull-requests` write). Both workflows already
prefer it over the default token, so the automated pull requests would then run
CI like any other, `Verify spec, Kotlin and TypeScript` can be added to the
ruleset, and the `branches-ignore` exclusions plus the `verify-release-pr` job
can go away. Until then the verification lives in the workflows that have a
working token context rather than on the pull request.

## Who owns the number

Nothing writes a version directly. The nightly sync writes a **conventional
commit**, and [release-please](https://github.com/googleapis/release-please)
turns accumulated commits into a version and a tag. One mechanism, so there is
nothing for two systems to disagree about.

A major bump emits both the `!` marker and a `BREAKING CHANGE:` footer:
release-please reads the footer rather than the `!` when deciding to bump the
major on a `1.x` line, so emitting only one of the two can silently release a
breaking change as a minor.

## Changing the surface deliberately

- **Adding** a tag: add it to `specs/surface.json`, run
  `npm --prefix tools run sync`, regenerate, and update the operation list in
  `BrevoApiSurfaceTest`. Commit as `feat(spec): expose the Email Campaigns tag`.
  Minor release.
- **Removing** a tag: delete the entry and commit as `feat(spec)!: ...` with a
  `BREAKING CHANGE:` footer. Major release.

Because a tag is a moving target, `BrevoApiSurfaceTest` pins the exact operation
list. When a spec sync makes it fail, that is the test working: check the
classification, then update the list in the same commit.

## What is not covered

The spec is not the only thing that can break a consumer. A generator upgrade
can rename a model or change a method signature without the spec moving at all.
Those changes arrive through ordinary Dependabot pull requests and need a human
to write the conventional commit — the automation has no view into them.
