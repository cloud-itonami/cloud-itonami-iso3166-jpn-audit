(ns auditreadiness.governor
  "Audit-Readiness Compliance Governor -- the independent compliance
  layer that earns the AuditReadiness-LLM the right to commit. The LLM
  has no notion of whether an entity's own ground-truth funding/
  ownership facts actually place it in Board of Audit Act Article 22
  mandatory scope or Article 23 discretionary scope (or neither),
  whether Article 24-26 document-submission/field-audit cooperation
  readiness is actually in place, whether a subsidy recipient's record-
  retention duty (Act No. 179/1955 + its own applicable 交付要綱) is
  actually satisfied, whether a proposal is quietly inventing a
  Board-of-Audit-specific vendor qualification that does not exist (a
  documented fabrication trap -- only 全省庁統一資格 applies), or when a
  draft stops being a draft and becomes the operator's own real-world
  audit-readiness compliance package, so this MUST be a separate system
  able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:audit-readiness-compliance-governor`
  (blueprint.edn).

  This blueprint's own text (docs/business-model.md Trust Controls:
  'any actual filing, registration, or compliance-program submission
  requires Audit-Readiness Compliance Governor clearance and always
  escalates to human sign-off'; 'a false or fabricated regulatory-
  requirement claim is a HARD hold that cannot be overridden by human
  approval alone') names exactly the checks below.

  Checks, in priority order, ALL HARD violations except the
  confidence/actuation gate: a human approver CANNOT override the hard
  ones. The confidence/actuation gate is SOFT: it asks a human to look
  (low confidence / actuation), and the human may approve -- but see
  `auditreadiness.phase`: for `:stake :actuation/draft-filing`/
  `:actuation/submit-filing` NO phase ever allows auto-commit either.
  Two independent layers agree that actuation is always a human call.

    1. Spec-basis                    -- did the compliance-track
                                         proposal cite an OFFICIAL
                                         source (`auditreadiness.facts`),
                                         or invent one?
    2. Audit-scope classification    -- for `:filing/submit`, has the
       (FLAGSHIP)                       engagement's audit-scope been
                                         classified at all, AND does the
                                         claimed `:audit-scope`
                                         (`:mandatory`/`:discretionary`/
                                         `:none`) actually match an
                                         INDEPENDENT recompute of
                                         `auditreadiness.registry/
                                         classify-audit-scope` over the
                                         engagement's own ground-truth
                                         facts (Article 22/23 criteria)?
                                         A genuine classification/
                                         routing check, not a yes/no
                                         gate.
    3. Cooperation-readiness missing -- for `:filing/submit`, when the
                                         engagement's audit-scope is NOT
                                         `:none` (i.e. it IS subject to
                                         Board of Audit examination
                                         under Article 22 or 23),
                                         INDEPENDENTLY verify
                                         `:cooperation-contact-
                                         designated?` is true (Article
                                         24-26 readiness). A no-op when
                                         the engagement's own
                                         classification is `:none`.
    4. Subsidy-retention             -- for `:filing/submit`, CONDITIONAL
       noncompliant                     on the engagement's own
                                         `:subsidy-recipient?` flag:
                                         verify `:record-retention-
                                         verified?` is true AND the
                                         engagement's own recorded
                                         retention years meets or
                                         exceeds its own applicable
                                         program's retention-years
                                         requirement. Never a hardcoded
                                         universal number -- always the
                                         engagement's own
                                         `:program-retention-years`. A
                                         no-op for a non-subsidy-
                                         recipient engagement.
    5. Fabricated Board-of-Audit     -- for `:compliance/assess`/
       vendor qualification             `:filing/draft`/`:filing/submit`,
                                         does the PROPOSAL claim a
                                         Board-of-Audit-specific vendor
                                         qualification distinct from
                                         全省庁統一資格 is required? A
                                         documented fabrication trap --
                                         no such qualification exists;
                                         only the standard Unified
                                         Qualification applies (see
                                         `auditreadiness.facts`'
                                         `:no-separate-boa-vendor-
                                         qualification` boundary entry).
    6. Confidence floor / actuation
       gate                            -- LLM confidence below
                                         threshold, OR the op is
                                         `:filing/draft`/`:filing/submit`
                                         (REAL acts) -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off the engagement's own `:drafted?`/`:submitted?` facts (never a
  `:status` value) -- this actor manages exactly ONE filing track
  (`auditreadiness.registry/audit-readiness-track`), so no per-track
  indirection is needed here either."
  (:require [auditreadiness.registry :as registry]
            [auditreadiness.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real audit-readiness compliance package and finalizing
  (submitting) it for the operator's own records are the two
  real-world actuation events this actor performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:compliance/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent Board of Audit jurisdiction/cooperation/retention/vendor-
  qualification requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:compliance/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案はコンプライアンス要件として扱えない"}]))))

(defn- audit-scope-classification-violations
  "For `:filing/submit`, the engagement's audit-scope MUST already be
  classified (`:audit-scope-classified? true`), and the claimed
  `:audit-scope` MUST match an INDEPENDENT recompute of
  `registry/classify-audit-scope` over the engagement's own ground-
  truth facts. The flagship check -- a genuine multi-branch
  classification, not a yes/no gate."
  [{:keys [op subject track]} st]
  (when (and (= op :filing/submit) (= track registry/audit-readiness-track))
    (let [e (store/engagement st subject)]
      (cond
        (not (:audit-scope-classified? e))
        [{:rule :audit-scope-undetermined
          :detail (str subject " は会計検査院法第22条/第23条に基づく検査対象区分が未判定 -- 提出提案は進められない")}]

        (not= (:audit-scope e) (registry/classify-audit-scope e))
        [{:rule :audit-scope-classification-mismatch
          :detail (str subject " の申告区分(" (:audit-scope e)
                      ")が独立再判定値(" (registry/classify-audit-scope e) ")と一致しない")}]

        :else nil))))

(defn- cooperation-readiness-violations
  "For `:filing/submit`, when the engagement's audit-scope is NOT
  `:none` (it IS subject to Board of Audit examination under Article 22
  or 23), INDEPENDENTLY verify `:cooperation-contact-designated?` is
  true -- Article 24-26 readiness. A no-op when the engagement's own
  classification is `:none`."
  [{:keys [op subject track]} st]
  (when (and (= op :filing/submit) (= track registry/audit-readiness-track))
    (let [e (store/engagement st subject)]
      (when (and (not= :none (:audit-scope e))
                 (not (true? (:cooperation-contact-designated? e))))
        [{:rule :cooperation-readiness-missing
          :detail (str subject " は会計検査院法第24条-第26条(計算書等提出/実地検査/資料提出・質問)への対応体制窓口が未指定 -- 提出提案は進められない")}]))))

(defn- subsidy-retention-violations
  "For `:filing/submit`, CONDITIONAL on the engagement's own
  `:subsidy-recipient?` flag: verify `:record-retention-verified?` is
  true AND the engagement's own recorded retention years meets or
  exceeds its OWN applicable program's retention-years requirement.
  Never a hardcoded universal number. A no-op for a non-subsidy-
  recipient engagement."
  [{:keys [op subject track]} st]
  (when (and (= op :filing/submit) (= track registry/audit-readiness-track))
    (let [e (store/engagement st subject)]
      (when (true? (:subsidy-recipient? e))
        (when (or (not (true? (:record-retention-verified? e)))
                  (and (number? (:program-retention-years e))
                       (number? (:recorded-retention-years e))
                       (< (:recorded-retention-years e) (:program-retention-years e))))
          [{:rule :subsidy-retention-noncompliant
            :detail (str subject " は補助金等に係る予算の執行の適正化に関する法律(昭和30年法律第179号)+適用交付要綱に基づく帳簿・証拠書類の保存要件が未充足 -- 提出提案は進められない")}])))))

(defn- fabricated-vendor-qualification-violations
  "For `:compliance/assess`/`:filing/draft`/`:filing/submit`, a
  proposal that claims a Board-of-Audit-specific vendor qualification
  distinct from 全省庁統一資格 exists/is required is a HARD violation
  (`auditreadiness.facts`'s `:no-separate-boa-vendor-qualification`
  entry is the citable spec-basis for REJECTING this claim, not for
  asserting it)."
  [{:keys [op]} proposal]
  (when (contains? #{:compliance/assess :filing/draft :filing/submit} op)
    (when (true? (:claims-boa-specific-vendor-qualification-required? (:value proposal)))
      [{:rule :fabricated-boa-vendor-qualification
        :detail "会計検査院固有の物品役務調達資格(全省庁統一資格と別建て)を要求する提案は認められない -- 会計検査院が調達主体となる場合も他省庁と同じ全省庁統一資格の枠組みで運用され、会計検査院固有の別建て資格は存在しない"}])))

(defn- already-drafted-violations
  "Refuses to draft the SAME engagement's audit-readiness compliance
  package twice."
  [{:keys [op subject track]} st]
  (when (and (= op :filing/draft) (= track registry/audit-readiness-track))
    (when (store/engagement-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "Refuses to submit the SAME engagement's audit-readiness compliance
  package twice."
  [{:keys [op subject track]} st]
  (when (and (= op :filing/submit) (= track registry/audit-readiness-track))
    (when (store/engagement-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors an AuditReadiness-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (audit-scope-classification-violations request st)
                           (cooperation-readiness-violations request st)
                           (subsidy-retention-violations request st)
                           (fabricated-vendor-qualification-violations request proposal)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :track      (:track request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
