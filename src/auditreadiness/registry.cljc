(ns auditreadiness.registry
  "Pure-function audit-scope CLASSIFICATION + filing-draft/filing-submit
  record construction for the JPN-AUDIT (Board of Audit) actor.

  `classify-audit-scope` is the flagship routing logic this actor exists
  to provide: given an entity's own ground-truth funding/ownership/
  contract facts, determine whether it falls under Board of Audit Act
  Article 22 mandatory scope (`:mandatory`), Article 23 discretionary
  scope (`:discretionary`), or neither (`:none`) -- see
  `auditreadiness.facts`' `:mandatory-audit-scope`/`:discretionary-
  audit-scope` entries for the underlying statutory criteria. This is a
  genuine multi-branch CLASSIFICATION, not a yes/no gate, and it is
  deliberately NOT modeled as a registration/application op: coming
  under Board of Audit jurisdiction attaches automatically by statute to
  any entity whose own facts meet the Article 22/23 criteria -- there is
  nothing to 'apply for' (see facts.cljc docstring, fabrication trap 3).
  `auditreadiness.governor`'s audit-scope-classification check
  independently RECOMPUTES this function against the engagement's own
  stored facts and holds if the engagement's claimed `:audit-scope`
  disagrees, or if no classification has been recorded at all.

  There is no single reference-number standard the Board of Audit
  assigns to an audit-readiness compliance package this actor produces
  for its own audit trail -- this namespace does NOT invent one; it
  builds a sequence number and validates the record's required fields,
  the same honest, non-fabricating discipline `auditreadiness.facts`
  uses.

  This namespace is pure data + pure functions -- no I/O, no network
  call to the Board of Audit or any government portal. It builds the
  RECORD an operator would keep (their own audit-readiness
  self-attestation package), not a submission TO the Board of Audit
  itself -- the Board never receives applications; it examines by
  statute (see `auditreadiness.operation`'s `:filing/submit`, always
  human-gated -- see README Core Contract)."
  (:require [clojure.string :as str]))

(def audit-readiness-track
  "The single actionable filing track this actor drafts/submits records
  for: the operator's own audit-readiness compliance package (record-
  keeping standard + cooperation-readiness self-attestation). NOT a
  `auditreadiness.facts` catalog key -- it is this actor's own internal
  packaging concept, deliberately kept out of the regulatory catalog so
  'what this actor calls its own deliverable' is never conflated with
  'what the law actually requires' (facts.cljc stays the sole
  regulatory-citation source)."
  :audit-readiness-package)

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn- track-code [track]
  (str/upper-case (name track)))

(defn classify-audit-scope
  "The flagship classification: does `entity`'s OWN ground-truth facts
  bring it into Board of Audit Act Article 22 mandatory scope, Article
  23 discretionary scope, or neither? Pure function over the entity's
  own `:state-capital-share`/`:is-nhk?`/`:receives-state-subsidy?`/
  `:board-designated-contractor?` fields -- never a lookup into an
  external registry (there is none to look up)."
  [{:keys [state-capital-share is-nhk? receives-state-subsidy? board-designated-contractor?]}]
  (cond
    (or (true? is-nhk?)
        (and (number? state-capital-share) (>= state-capital-share 0.5)))
    :mandatory

    (or (true? receives-state-subsidy?) (true? board-designated-contractor?))
    :discretionary

    :else
    :none))

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT for `track`
  (always `audit-readiness-track` today) -- the operator's own act of
  preparing an audit-readiness compliance package. Pure function --
  does not touch the Board of Audit or any government portal."
  [engagement-id track sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and track (not= track ""))
    (throw (ex-info "draft: track required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str "JPN-AUDIT-" (track-code track) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "track" (name track)
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT for
  `track` -- the operator's own act of finalizing its audit-readiness
  compliance package for its own records (always human-gated
  upstream). This is NOT a submission to the Board of Audit -- the
  Board does not accept applications (see facts.cljc fabrication trap
  3); it is the operator's own book-of-record act."
  [engagement-id track sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and track (not= track ""))
    (throw (ex-info "submit: track required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str "JPN-AUDIT-" (track-code track) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "track" (name track)
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
