# emuStudio Repo Routing

## Current Repository
- `edigen` owns the `.eds` language, parser, and generation of Java instruction decoders and disassemblers.

## Sibling Repositories
- `/home/vbmacher/projects/emustudio/emuLib`: shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- `/home/vbmacher/projects/emustudio/edigen`: decoder/disassembler generator from `.eds` specifications.
- `/home/vbmacher/projects/emustudio/emuStudio`: desktop application, CLI launcher, bundled plugins, bundled virtual computers, configs, and packaging.
- `/home/vbmacher/projects/emustudio/emustudio.github.io`: website, user documentation, developer documentation, and release-facing pages.
- `/home/vbmacher/projects/emustudio/edigen-gradle-plugin`: Gradle task and DSL integration for Edigen source generation.
- `/home/vbmacher/projects/emustudio/cpu-testsuite`: shared CPU instruction test framework and reusable verification helpers.

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
