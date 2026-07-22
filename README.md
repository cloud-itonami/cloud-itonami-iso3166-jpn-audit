# cloud-itonami-iso3166-jpn-audit

Open ISO 3166 Agency Blueprint for **JPN-AUDIT**: Board of Audit
(会計検査院, Board of Audit) — a Japan-agency-level LEAF under
the `cloud-itonami-iso3166-jpn` country-level coordinator.

This repository designs a forkable OSS business for an independent
compliance consultant: an already-incorporated operator (typically one
already using `cloud-itonami-iso3166-jpn` for general Japan market entry)
gets a Compliance Advisor + independent **Audit-Readiness Compliance Governor** to
navigate financial record-keeping and documentation standards a public-sector contractor should maintain in anticipation of a Board of Audit (会計検査院) inspection under the Board of Audit Act (会計検査院法).

## No robotics premise — digital/data service exemption

Agency-specific compliance navigation is a pure data/software service with
no physical-domain work — the same exemption class as `cloud-itonami-6310`
and `cloud-itonami-gtin-*`. `blueprint.edn` sets
`:itonami.blueprint/robotics false` and `:required-technologies` lists only
real capabilities (`:identity`, `:forms`, `:dmn`, `:bpmn`, `:audit-ledger`),
no `:robotics`.

## Core Contract

```text
operator intake + entity's own funding/ownership/contract facts
        |
        v
Compliance Advisor -> Audit-Readiness Compliance Governor -> compliance draft, or human sign-off
        |
        v
gated audit-readiness compliance-package draft/finalization + audit ledger
```

No automated proposal can finalize an audit-readiness compliance
package the governor refuses, suppress a compliance record, or claim a
regulatory conclusion the governor has not cleared. `:filing/submit` is
never in any phase's `:auto` set — it always requires human sign-off
(mirrors `cloud-itonami-M6910`'s `filing-submit-never-auto-at-any-phase`
invariant).

**Important framing**: coming under Board of Audit jurisdiction is
**not** something an entity applies for or registers into — it attaches
automatically by statute (Board of Audit Act Articles 22/23) based on
an entity's own funding/ownership/contract facts. `:filing/draft` and
`:filing/submit` here name the **operator's own act** of drafting and
finalizing its **own audit-readiness compliance package** (a
self-attestation record of jurisdiction classification, cooperation
readiness, and — where applicable — subsidy record-retention
compliance) for its own records, kept ready to present when the Board
of Audit exercises its Article 24/25/26 document-request or field-audit
powers. It is never a submission the Board of Audit receives or
approves.

## Implementation

`src/auditreadiness/` — a langgraph-clj StateGraph actor, same
containment shape as `cloud-itonami-iso3166-ago`'s `marketentry.*` /
`cloud-itonami-iso3166-jpn-mof`'s `mofcompliance.*` /
`cloud-itonami-iso3166-jpn-mod`'s `defensecompliance.*` /
`cloud-itonami-iso3166-jpn-moe`'s `greenprocurement.*` (advisor sealed
to proposals-only, independent governor, append-only ledger, `Store`
protocol swap, phase gate):

- `facts.cljc` — the Board of Audit Act Article 22 (mandatory audit
  scope) + Article 23 (discretionary audit scope) + Articles 24-27
  (cooperation duties) + Act No. 179/1955 (subsidy record-retention)
  catalog, the ONLY source of regulatory-requirement facts the actor
  may cite. Five entries: `:mandatory-audit-scope`,
  `:discretionary-audit-scope`, `:audit-cooperation-duties`,
  `:subsidy-record-retention`, and a NEGATIVE/boundary entry `:no-
  separate-boa-vendor-qualification` (`:filing-track? false`) that
  exists only so the governor has a citable spec-basis for REJECTING
  any proposal that invents a Board-of-Audit-specific vendor
  qualification distinct from 全省庁統一資格 — no such qualification
  exists (see "Fabrication traps addressed" below).
- `registry.cljc` — `classify-audit-scope`, the FLAGSHIP classification/
  routing function: given an entity's own ground-truth funding/
  ownership facts, determines whether it falls under Article 22
  mandatory scope, Article 23 discretionary scope, or neither. A
  genuine multi-branch classification, not a yes/no gate — and
  deliberately NOT modeled as a registration/application op (see Core
  Contract above). Plus pure-function filing-draft/filing-submit record
  construction for the single actionable filing track.
- `governor.cljc` — the Audit-Readiness Compliance Governor: a
  spec-basis/no-fabrication HARD check, an **audit-scope classification**
  HARD check (`:filing/submit`, unconditional — verifies the engagement
  IS classified AND that the claimed classification matches an
  INDEPENDENT recompute of `registry/classify-audit-scope`), a
  **cooperation-readiness missing** HARD check (`:filing/submit`,
  CONDITIONAL on the engagement's own classification not being `:none`),
  a **subsidy-retention noncompliant** HARD check (`:filing/submit`,
  CONDITIONAL on the engagement's own `:subsidy-recipient?` — never a
  hardcoded universal retention-years number), a **fabricated
  Board-of-Audit vendor qualification** HARD check (negative/boundary
  defense), a confidence-floor/actuation gate, and double-draft/
  double-submit guards.
- `store.cljc` — `MemStore`/`DatomicStore` (via
  `kotoba-lang/langchain-store`'s entity field-spec `map->tx`/
  `pull->map`/`pull-pattern` AND its identity-schema/event-log
  helpers, not a hand-rolled `enc`/`dec*` or hand-rolled tx/pull pair)
  for the `engagement` entity, which tracks the engagement-level
  ground-truth classification facts, the cooperation-readiness flag,
  the conditional subsidy-retention fields, and the single actionable
  filing track's actuation state.
- `auditreadinessllm.cljc` — the Compliance Advisor (mock LLM,
  proposals only).
- `operation.cljc` — the StateGraph: intake → advise → govern → decide
  → [request-approval →] commit/hold, `interrupt-before` on human
  approval.
- `phase.cljc` — phase 0→3 rollout; `:filing/draft`/`:filing/submit`
  are permanently absent from every phase's `:auto` set.

Ops: `:engagement/intake`, `:compliance/assess` (per underlying
regulatory-catalog-track evidence checklist — `:mandatory-audit-scope`/
`:discretionary-audit-scope`/`:audit-cooperation-duties`/`:subsidy-
record-retention`/`:no-separate-boa-vendor-qualification`),
`:filing/draft`, `:filing/submit` (the latter two always target the
single `:audit-readiness-package` track).

## Fabrication traps addressed

This research pass explicitly verified and defended against:

- No invented universal retention period (e.g. "always 5 years for
  everyone") mandated directly by the Board of Audit Act — it doesn't
  exist. Record retention for subsidy recipients derives from a
  SEPARATE act (Act No. 179 of 1955, 補助金等に係る予算の執行の適正化に関する法律)
  plus program-specific 交付要綱, and varies by program. This actor's
  `subsidy-retention` governor check always compares against the
  engagement's OWN `:program-retention-years` field, never a hardcoded
  number, and is a no-op for any engagement that is not a subsidy
  recipient.
- No invented Board-of-Audit-specific vendor/bidder qualification
  distinct from 全省庁統一資格 (Unified Qualification for All Ministries/
  Agencies) — actively researched, found no evidence it exists. The
  `facts.cljc` `:no-separate-boa-vendor-qualification` entry is a
  NEGATIVE/boundary catalog entry (never itself drafted/submitted) that
  exists solely so the governor's fabricated-vendor-qualification check
  has a citable spec-basis for REJECTING any proposal that invents one.
- No modeling of "coming under Board of Audit jurisdiction" as
  something an entity applies for or registers into — jurisdiction
  attaches automatically by statute (Article 22/23 criteria) based on
  the entity's own funding/ownership/contract facts. This is why
  `registry/classify-audit-scope` is framed as a pure CLASSIFICATION
  function over the entity's own facts, never a registration/
  application op, and why `:filing/draft`/`:filing/submit` name the
  operator's OWN compliance-package act, never a submission the Board
  of Audit receives or approves (see Core Contract above).

## What this is NOT

- **Not Board of Audit (会計検査院) itself, and not the
  government of Japan.** See [`docs/business-model.md`](docs/business-model.md)
  for the boundary with `com-etzhayyim-ooyake`, `matsurigoto`,
  `com-etzhayyim-toritsugi`, `legal-entity.etzhayyim.com`,
  `cloud-itonami-M6910`, and the country-level `cloud-itonami-iso3166-jpn`.
- **Not legal or tax advice.** Every regulatory claim must cite the
  official Board of Audit source and route final filings to
  Japan-licensed counsel or a registered agent where the law requires
  licensed representation.

## Capability layer

Resolves via [`kotoba-lang/iso3166`](https://github.com/kotoba-lang/iso3166)
(code `JPN-AUDIT`, `:parent "JPN"`, cross-referenced to ooyake's
`gov.jpn.audit`). Required capabilities:

- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
