# Requirements Workflow

## Sources of truth

| Artifact | Role |
|---|---|
| Jira project **TP** ([board](https://mpburton.atlassian.net/jira/software/projects/TP/boards/40)) | Planning, status, acceptance criteria |
| `.requirements` | Product brief + append-only delivery log of implemented requirements |
| `docs/ARCHITECTURE.md` | Technical design decisions |

## Commit / PR convention

1. Create or identify a Jira ticket (`TP-xxx`) before implementation.
2. Work on `feature/TP-xxx-short-slug`.
3. Every commit message references `TP-xxx`.
4. Append a dated delivery line to `.requirements` for each discrete requirement shipped in that commit.
5. Promote `feature → dev` via PR after local `./gradlew test` (and `dependencyCheckAnalyze` before merge to `dev`).

## Status rules

- Set ticket **In Progress** when work starts.
- Set **In Review** when the PR to `dev` opens.
- Set **Done** only after merge to `dev`.
