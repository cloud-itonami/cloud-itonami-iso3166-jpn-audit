# Operator Guide

Implementation: `src/auditreadiness/` — see README.md's Implementation
section for the full StateGraph shape (`auditreadiness.operation`) and
the governor's checks (`auditreadiness.governor`).

## First Deployment

1. Confirm the client already uses (or has completed the equivalent of)
   `cloud-itonami-iso3166-jpn` for general Japan market-entry; this repo is
   an agency-specific supplement, not a substitute.
2. Register the client's intake (`:engagement/intake`): business type,
   and the entity's own ground-truth facts that drive Board of Audit
   Act Article 22/23 audit-scope classification (state-capital share,
   NHK status, State-subsidy receipt, board-designated-contractor
   status) — never the entity's own guess at "am I audited", always the
   underlying facts the governor independently reclassifies from.
3. Run the advisor in read-only mode against Board of Audit's
   (会計検査院) published guidance (`:compliance/assess` against the
   `auditreadiness.facts` catalog).
4. Compare the checklist against the client's current documentation,
   including — only if the client is a subsidy recipient — its
   applicable program's own 交付要綱 record-retention requirement (never
   a hardcoded universal number).
5. Enable gated filing/compliance-draft assistance once the
   Audit-Readiness Compliance Governor contract is trusted; actual
   finalization of the client's own audit-readiness compliance package
   (`:filing/submit`) always requires human sign-off, at every rollout
   phase.

## Minimum Production Controls

- client-owned data store for compliance documents
- clear provenance (official Board of Audit source citation) for every
  requirement surfaced
- approval workflow for any filing, registration, or compliance-program
  submission
- named referral relationship with Japan-licensed counsel or a registered
  agent for anything beyond checklist/draft assistance
- monthly audit export

## Certification

Certified operators must prove data provenance, audit traceability, that
automated actions cannot bypass the Audit-Readiness Compliance Governor, and a working
referral relationship with Japan-licensed counsel or a registered agent for
whatever licensed representation Japanese law requires for actual
Board of Audit filings.
