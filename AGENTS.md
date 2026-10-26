# Working conventions

- Every change is tracked in Jira. Put the issue key on the branch name, every commit message and the PR title (e.g. `LAB-123`).
- One PR per module (repository). Don't bundle changes to several services into one PR.
- CI must be green before a PR is ready for review.
- Upgrades and refactors keep behaviour identical: existing tests must keep passing unchanged, and add characterization tests first for anything risky.
- Plan large changes from the codebase itself and split them into stories that can be worked in parallel, one per module.

## Build

- JDK and Maven versions come from the parent POM and the Maven wrapper (`./mvnw`).
- Services build against `treasury-platform-parent` at the ref in `.platform-ref`; install the parent locally (`./mvnw -B -ntp install -DskipTests` in that repo) before building a service.
- `./mvnw -B -ntp verify` runs the full test suite, same as CI.
