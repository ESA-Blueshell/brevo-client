# Changelog

## [1.0.1](https://github.com/ESA-Blueshell/brevo-client/compare/v1.0.0...v1.0.1) (2026-09-02)


### Bug Fixes

* **ci:** repair CodeQL and read the version from a ref that may predate the task ([#29](https://github.com/ESA-Blueshell/brevo-client/issues/29)) ([5bbbdf4](https://github.com/ESA-Blueshell/brevo-client/commit/5bbbdf498ae2bf2da9c51f9e460792778f6da11d))
* stop publishing artefacts named "0.2.x # x-release-please-version" ([#27](https://github.com/ESA-Blueshell/brevo-client/issues/27)) ([b01cad2](https://github.com/ESA-Blueshell/brevo-client/commit/b01cad2b8ed06d7bba995853b42c46114e1c14a4))

## [1.0.0](https://github.com/ESA-Blueshell/brevo-client/compare/v0.2.4...v1.0.0) (2026-09-02)


### ⚠ BREAKING CHANGES

* **kotlin:** generate against Jackson 3 and Spring Boot 4 ([#25](https://github.com/ESA-Blueshell/brevo-client/issues/25))

### Features

* generated Brevo clients with spec-derived versioning ([fb81701](https://github.com/ESA-Blueshell/brevo-client/commit/fb81701b92e0b8980aead556c4500987bd5812f5))
* **kotlin:** generate against Jackson 3 and Spring Boot 4 ([#25](https://github.com/ESA-Blueshell/brevo-client/issues/25)) ([db41203](https://github.com/ESA-Blueshell/brevo-client/commit/db41203ea4d1417ff249bd85c0668c057cb97b25))


### Bug Fixes

* **ci:** pin release-please-action to v4, releases are broken on v5 ([#20](https://github.com/ESA-Blueshell/brevo-client/issues/20)) ([5495f38](https://github.com/ESA-Blueshell/brevo-client/commit/5495f38723fe216dd6f0c2ad4135f09bd832b1d9))
* **ci:** stop creating workflow runs that cannot execute, and verify releases properly ([#18](https://github.com/ESA-Blueshell/brevo-client/issues/18)) ([f28d49d](https://github.com/ESA-Blueshell/brevo-client/commit/f28d49db793a0feee376cee4f829c3160a31fd25))
* **ci:** take the release pull request number from release-please ([#14](https://github.com/ESA-Blueshell/brevo-client/issues/14)) ([5902f9f](https://github.com/ESA-Blueshell/brevo-client/commit/5902f9ff1d00cdc9d9a64178ab76b7821164990e))
* **deps:** raise axios, yaml and js-yaml above known advisories ([fc2537b](https://github.com/ESA-Blueshell/brevo-client/commit/fc2537b53e9b2a9cd9568816b104523de724a444))


### Documentation

* describe the unattended release chain and its one gap ([#12](https://github.com/ESA-Blueshell/brevo-client/issues/12)) ([fdf4e58](https://github.com/ESA-Blueshell/brevo-client/commit/fdf4e5831996d8367d3e3035afd27ebdd097e844))


### Build and Dependencies

* **deps-dev:** bump @types/node from 24.9.2 to 26.3.0 in /typescript ([#6](https://github.com/ESA-Blueshell/brevo-client/issues/6)) ([6e0c7d8](https://github.com/ESA-Blueshell/brevo-client/commit/6e0c7d827d4561659d289fb53dd5e49c9c8a9c39))
* **deps-dev:** bump typescript from 5.9.3 to 6.0.3 in /typescript ([#19](https://github.com/ESA-Blueshell/brevo-client/issues/19)) ([e62663f](https://github.com/ESA-Blueshell/brevo-client/commit/e62663f8135cffafdb4ecc8553d4da9a93e73b13))
* **deps:** bump actions/checkout from 5.0.0 to 7.0.1 ([#9](https://github.com/ESA-Blueshell/brevo-client/issues/9)) ([fc41142](https://github.com/ESA-Blueshell/brevo-client/commit/fc411422159a2a8ee473ab0a3e8d2e5ae7fb6cdc))
* **deps:** bump actions/dependency-review-action from 4.7.2 to 5.0.0 ([#11](https://github.com/ESA-Blueshell/brevo-client/issues/11)) ([ee9474a](https://github.com/ESA-Blueshell/brevo-client/commit/ee9474a8cfa90a51a39131ddd0422a577aba0861))
* **deps:** bump actions/setup-node from 6.0.0 to 7.0.0 ([#10](https://github.com/ESA-Blueshell/brevo-client/issues/10)) ([7a06984](https://github.com/ESA-Blueshell/brevo-client/commit/7a06984596ca5c496f0bb38b01e39b158fbbb1c6))
* **deps:** bump actions/upload-artifact from 4.6.2 to 7.0.1 ([#21](https://github.com/ESA-Blueshell/brevo-client/issues/21)) ([859cd79](https://github.com/ESA-Blueshell/brevo-client/commit/859cd798e12e29ddcf020bc3e52c418ad9286685))
* **deps:** bump googleapis/release-please-action from 4.2.0 to 5.0.0 ([#8](https://github.com/ESA-Blueshell/brevo-client/issues/8)) ([ea8555b](https://github.com/ESA-Blueshell/brevo-client/commit/ea8555b8d258e3453e5157139775112610de5095))
* **deps:** bump gradle/actions/setup-gradle from 4.4.1 to 6.3.0 ([#22](https://github.com/ESA-Blueshell/brevo-client/issues/22)) ([376e7d5](https://github.com/ESA-Blueshell/brevo-client/commit/376e7d5aafb8eec8c5871de77e80ab1e0645a07b))
* **deps:** bump the actions group with 4 updates ([#7](https://github.com/ESA-Blueshell/brevo-client/issues/7)) ([3d6fefc](https://github.com/ESA-Blueshell/brevo-client/commit/3d6fefc59cc062fd3ece6a59a9187283cd308fb1))
* **deps:** Bump the gradle group in /kotlin with 5 updates ([#2](https://github.com/ESA-Blueshell/brevo-client/issues/2)) ([3a408dc](https://github.com/ESA-Blueshell/brevo-client/commit/3a408dc2b795bf785af9edb5f2b3c6d82e2841a4))

## [0.2.4](https://github.com/ESA-Blueshell/brevo-client/compare/v0.2.3...v0.2.4) (2026-09-02)


### Build and Dependencies

* **deps:** bump the actions group with 4 updates ([#7](https://github.com/ESA-Blueshell/brevo-client/issues/7)) ([3d6fefc](https://github.com/ESA-Blueshell/brevo-client/commit/3d6fefc59cc062fd3ece6a59a9187283cd308fb1))

## [0.2.3](https://github.com/ESA-Blueshell/brevo-client/compare/v0.2.2...v0.2.3) (2026-09-02)


### Build and Dependencies

* **deps-dev:** bump @types/node from 24.9.2 to 26.3.0 in /typescript ([#6](https://github.com/ESA-Blueshell/brevo-client/issues/6)) ([6e0c7d8](https://github.com/ESA-Blueshell/brevo-client/commit/6e0c7d827d4561659d289fb53dd5e49c9c8a9c39))

## [0.2.2](https://github.com/ESA-Blueshell/brevo-client/compare/v0.2.1...v0.2.2) (2026-09-02)


### Build and Dependencies

* **deps:** Bump the gradle group in /kotlin with 5 updates ([#2](https://github.com/ESA-Blueshell/brevo-client/issues/2)) ([3a408dc](https://github.com/ESA-Blueshell/brevo-client/commit/3a408dc2b795bf785af9edb5f2b3c6d82e2841a4))

## [0.2.1](https://github.com/ESA-Blueshell/brevo-client/compare/v0.2.0...v0.2.1) (2026-09-02)


### Bug Fixes

* **ci:** take the release pull request number from release-please ([#14](https://github.com/ESA-Blueshell/brevo-client/issues/14)) ([5902f9f](https://github.com/ESA-Blueshell/brevo-client/commit/5902f9ff1d00cdc9d9a64178ab76b7821164990e))


### Documentation

* describe the unattended release chain and its one gap ([#12](https://github.com/ESA-Blueshell/brevo-client/issues/12)) ([fdf4e58](https://github.com/ESA-Blueshell/brevo-client/commit/fdf4e5831996d8367d3e3035afd27ebdd097e844))

## [0.2.0](https://github.com/ESA-Blueshell/brevo-client/compare/v0.1.0...v0.2.0) (2026-09-02)


### Features

* generated Brevo clients with spec-derived versioning ([fb81701](https://github.com/ESA-Blueshell/brevo-client/commit/fb81701b92e0b8980aead556c4500987bd5812f5))


### Bug Fixes

* **deps:** raise axios, yaml and js-yaml above known advisories ([fc2537b](https://github.com/ESA-Blueshell/brevo-client/commit/fc2537b53e9b2a9cd9568816b104523de724a444))

## Changelog

Maintained by [release-please](https://github.com/googleapis/release-please) from
conventional commits on `main`. Do not edit by hand.

Version numbers describe the consumed Brevo API surface — see
[docs/versioning.md](docs/versioning.md).
