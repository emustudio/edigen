# emuStudio Repo Routing

## Current Repository
- `edigen` owns the `.eds` language, parser, and generation of Java instruction decoders and disassemblers.

## Sibling Repositories
- [emuLib](https://github.com/emustudio/emuLib): shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- [edigen](https://github.com/emustudio/edigen): decoder/disassembler generator from `.eds` specifications.
- [emuStudio](https://github.com/emustudio/emuStudio): desktop application, CLI launcher, bundled plugins, bundled virtual computers, configs, and packaging.
- [emustudio.github.io](https://github.com/emustudio/emustudio.github.io): website, user documentation, developer documentation, and release-facing pages.
- [edigen-gradle-plugin](https://github.com/emustudio/edigen-gradle-plugin): Gradle task and DSL integration for Edigen source generation.
- [cpu-testsuite](https://github.com/emustudio/cpu-testsuite): shared CPU instruction test framework and reusable verification helpers.

Before checking or updating a sibling repository, verify that it is cloned locally. If it is not available locally, report this to the user instead of assuming its contents.

## When To Update Which Repository
- `.eds` syntax, parser behavior, decoder generation, disassembler generation, or generated code shape: update `edigen`.
- Gradle integration for generated sources: update `edigen-gradle-plugin`; check `edigen` if generator inputs or outputs change.
- Shared plugin API or runtime contract needed by generated code: update `emuLib`; then check `emuStudio` and `cpu-testsuite` consumers.
- Bundled CPU plugins or bundled computers affected by generator changes: update `emuStudio`.
- User or developer documentation for generator usage: update `emustudio.github.io`.

## Tickets And Commits
- Every change must have an existing GitHub ticket.
- Every commit subject must start with the ticket prefix: `[#123] Short summary`.
- If one task touches multiple emuStudio repositories, use the same ticket prefix in each related commit.
