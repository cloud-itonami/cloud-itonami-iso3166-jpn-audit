# Business Model: Independent Board-of-Audit Readiness Compliance Service — Japan (Board of Audit)

Implementation: `src/auditreadiness/` — see README.md's Implementation
section. The Trust Controls below are enforced in code by
`auditreadiness.governor` (spec-basis/no-fabrication HARD check,
audit-scope-classification HARD check (flagship — unconditional once
engaged), cooperation-readiness-missing HARD check (conditional on
in-scope), subsidy-retention-noncompliant HARD check (conditional on
`:subsidy-recipient?`), fabricated-Board-of-Audit-vendor-qualification
HARD check, confidence-floor/actuation gate, double-draft/double-submit
guards) and `auditreadiness.phase` (`:filing/submit` absent from every
phase's `:auto` set).

## Classification

- Repository: `cloud-itonami-iso3166-jpn-audit`
- ISO 3166 (agency-level): `JPN-AUDIT`, parent `JPN`
- Ooyake cross-reference: `gov.jpn.audit` (Board of Audit / 会計検査院)
- Activity: financial record-keeping and documentation standards a public-sector contractor should maintain in anticipation of a Board of Audit (会計検査院) inspection under the Board of Audit Act (会計検査院法)
- Social impact: [:audit-readiness-clarity :public-spend-transparency :record-keeping-compliance]

## Customer

- an operator preparing financial records for a Board of Audit (会計検査) inspection
- an operator wanting a documented record-keeping standard suitable for public-contract audit readiness
- a foreign contractor unfamiliar with Japan's government-audit documentation expectations

## Offer

- audit-readiness self-assessment checklist against Board of Audit expectations
- financial record-keeping standard template for public-contract documentation
- ongoing regulatory-change monitoring for Board of Audit guidance updates
- compliance-audit export package for the operator's own records

## Revenue

- per-engagement compliance-review fee
- recurring regulatory-change monitoring subscription
- compliance-audit export package

## Trust Controls

- any actual filing, registration, or compliance-program submission
  requires Audit-Readiness Compliance Governor clearance and always escalates to human
  sign-off (`:filing/submit` is never automated at any phase)
- a false or fabricated regulatory-requirement claim is a HARD hold that
  cannot be overridden by human approval alone — it must be corrected
  against a cited Board of Audit source first
- claiming an entity's Board of Audit jurisdiction classification
  (mandatory Article 22 / discretionary Article 23 / neither) without it
  matching an independent recompute from the entity's own ground-truth
  funding/ownership facts is a HARD hold that cannot be overridden
- claiming a subsidy recipient's record-retention duty is satisfied
  without independent verification against that recipient's own
  applicable program (交付要綱) requirement is a HARD hold — retention is
  never checked against a hardcoded universal number
- claiming a Board-of-Audit-specific vendor qualification exists,
  distinct from the standard 全省庁統一資格, is a HARD hold that cannot be
  overridden — no such qualification exists
- this service does **not** provide legal or tax advice; characterization
  and filing on the client's behalf beyond checklist/draft assistance
  routes to Japan-licensed counsel or a registered agent
- every requirement cites the official Board of Audit source or
  regulation, never invented

## Boundary with adjacent actors (read before forking)

- **`cloud-itonami-iso3166-jpn`**: the COUNTRY-level coordinator (general
  Japan public-sector market entry). This repo is a narrower, deeper
  AGENCY-level leaf — most operators need the country-level blueprint plus
  only the agency-level blueprints that actually apply to their contract.
- **`com-etzhayyim-ooyake`** (etzhayyim/root): read-only civic-wayfinding
  mirror of government structure, non-commercial, barred from acting as or
  for the government (G3 impersonation ban). This blueprint is commercial
  and never claims to be Board of Audit or an official channel.
- **`matsurigoto`** (etzhayyim/root): sovereign e-government statecraft —
  literally the government. This blueprint is an independent operator that
  engages with Board of Audit under its public rules — never the
  agency itself.
- **`com-etzhayyim-toritsugi`** (etzhayyim/root): guides a consenting
  INDIVIDUAL citizen through their OWN procedure, non-profit,
  donation-only. This blueprint's client is a business operator, not an
  individual citizen, and it is commercial.
- **`cloud-itonami-M6910`**: helps a client BECOME a legal entity
  (incorporation, ISIC 6910) — a prior, different regulatory phase (company
  law). This blueprint assumes incorporation is already done and handles
  Board of Audit-specific compliance (a different regulatory domain).

## Domain note (reconciled during implementation)

This blueprint's original text (pre-implementation, landed 2026-07-10)
already described the correct scope — Board of Audit (会計検査院)
audit-readiness/record-keeping compliance, not general Japan market
entry — and the verified research dossier available for the
implementation pass (constitutional/statutory basis: Article 90 of the
Constitution + Board of Audit Act; audit-scope classification under
Articles 22/23; cooperation duties under Articles 24-27; the SEPARATE
subsidy-record-retention act, Act No. 179 of 1955; and the negative
finding that no Board-of-Audit-specific vendor qualification exists)
matched that scope closely, so no scope pivot was needed — unlike
`cloud-itonami-iso3166-jpn-mof` and `cloud-itonami-iso3166-jpn-moe`,
whose pre-implementation text diverged from their own dossiers.

Two framing points were made explicit during implementation, because
the pre-existing text's generic "gated filing / registration /
compliance-program submission" language (inherited from this actor
family's shared template) could otherwise be misread as "the operator
applies to / registers with the Board of Audit":

1. Coming under Board of Audit jurisdiction is never something an
   entity applies for — it attaches automatically by statute (Article
   22/23 criteria) to any entity whose own funding/ownership/contract
   facts meet them. `src/auditreadiness/registry.cljc`'s
   `classify-audit-scope` is accordingly a pure CLASSIFICATION function
   over the entity's own facts, never a registration/application op.
2. `:filing/draft`/`:filing/submit` name the operator's OWN act of
   drafting and finalizing its own audit-readiness compliance package
   (a self-attestation record) for its own records — never a submission
   the Board of Audit itself receives or approves. See README.md's Core
   Contract section for the full framing.

The subsidy record-retention duty (Offer: "financial record-keeping
standard template") is implemented as CONDITIONAL and program-dependent
(grounded in Act No. 179/1955 + the applicable program's own 交付要綱),
never as a fixed universal number, per the research dossier's explicit
finding that no Board-of-Audit-Act-wide blanket retention period
exists.
