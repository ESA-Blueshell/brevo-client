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
