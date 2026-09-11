(ns auditreadiness.auditreadinessllm
  "AuditReadiness-LLM client -- the *contained intelligence node* for
  the JPN-AUDIT (Board of Audit) audit-readiness compliance actor.

  It normalizes engagement intake, drafts a per-track (`:mandatory-
  audit-scope`/`:discretionary-audit-scope`/`:audit-cooperation-
  duties`/`:subsidy-record-retention`/`:no-separate-boa-vendor-
  qualification`) compliance evidence checklist, drafts the
  audit-readiness compliance-package filing-draft action, and drafts
  the compliance-package filing-submit action. CRITICAL: it is a
  smart-but-untrusted advisor. It returns a *proposal* (with a
  rationale + the fields it cited), never a committed record or a real
  Board-of-Audit-facing act. Every output is censored downstream by
  `auditreadiness.governor` before anything touches the SSoT, and
  `:filing/draft`/`:filing/submit` proposals NEVER auto-commit at any
  phase -- see README Core Contract.

  Like every sibling actor's advisor, this is a deterministic mock so
  the actor graph runs offline and the governor contract is exercised
  end-to-end. Two test-only injection flags exist purely to exercise the
  governor's fabrication defenses without needing an actual bad LLM:
  `:no-spec?` (assess an unregistered track) and `:fabricate-vendor-
  qualification?` (assess `:no-separate-boa-vendor-qualification` while
  WRONGLY claiming a Board-of-Audit-specific vendor qualification is
  required -- the governor's fabricated-vendor-qualification check must
  catch this)."
  (:require [auditreadiness.facts :as facts]
            [auditreadiness.registry :as registry]
            [auditreadiness.store :as store]))

(defn- normalize-intake
  [_db {:keys [patch]}]
  {:summary    (str "engagement intake record updated: " (pr-str (keys patch)))
   :rationale  "入力 patch の正規化のみ。新規事実の生成なし。"
   :cites      (vec (keys patch))
   :effect     :engagement/upsert
   :value      patch
   :stake      nil
   :confidence 0.97})

(defn- assess-track
  "Per-track (`:mandatory-audit-scope`/`:discretionary-audit-scope`/
  `:audit-cooperation-duties`/`:subsidy-record-retention`/`:no-
  separate-boa-vendor-qualification`) compliance evidence checklist
  draft. `:no-spec?` injects the failure mode we must defend against:
  proposing a checklist for a track with NO official spec-basis.
  `:fabricate-vendor-qualification?` injects the OTHER failure mode:
  assessing `:no-separate-boa-vendor-qualification` while wrongly
  claiming a Board-of-Audit-specific vendor qualification IS required."
  [_db {:keys [track no-spec? fabricate-vendor-qualification?]}]
  (let [track (if no-spec? :unknown-track track)
        sb (facts/spec-basis track)]
    (cond
      (nil? sb)
      {:summary    (str (name track) " の公式spec-basisが見つかりません")
       :rationale  "auditreadiness.facts に未登録のトラック。要件を推測で作らない。"
       :cites      []
       :effect     :assessment/set
       :value      {:track track :checklist [] :spec-basis nil}
       :stake      nil
       :confidence 0.9}

      (and fabricate-vendor-qualification? (= track :no-separate-boa-vendor-qualification))
      {:summary    (str (name track) " について会計検査院固有の資格が必要と主張する提案(検証用の誤った提案)")
       :rationale  "この提案は意図的に誤りを含む -- governorのfabricated-boa-vendor-qualification検査を試験するため"
       :cites      [(:legal-basis sb) (:provenance sb)]
       :effect     :assessment/set
       :value      {:track track :checklist (:required-evidence sb) :spec-basis (:provenance sb)
                    :claims-boa-specific-vendor-qualification-required? true}
       :stake      nil
       :confidence 0.5}

      :else
      {:summary    (str (name track) " (" (:owner-authority sb) ") 向け必要書類 "
                        (count (:required-evidence sb)) " 件を提案")
       :rationale  (str "公式ソース: " (:provenance sb) " / 法的根拠: " (:legal-basis sb))
       :cites      [(:legal-basis sb) (:provenance sb)]
       :effect     :assessment/set
       :value      {:track track
                    :checklist (:required-evidence sb)
                    :spec-basis (:provenance sb)
                    :legal-basis (:legal-basis sb)}
       :stake      nil
       :confidence 0.9})))

(defn- propose-draft
  "Draft the actual audit-readiness compliance-package FILING-DRAFT
  action. ALWAYS `:stake :actuation/draft-filing`."
  [db {:keys [subject]}]
  (let [e (store/engagement db subject)
        track registry/audit-readiness-track]
    {:summary    (str subject " 向け監査対応コンプライアンス・パッケージ提出ドラフト提案"
                      (when e (str " (operator=" (:operator e) ")")))
     :rationale  (if e
                   (str "track=" (name track) " audit-scope=" (:audit-scope e))
                   "engagementが見つかりません")
     :cites      (if e [subject (name track)] [])
     :effect     :engagement/mark-drafted
     :value      {:engagement-id subject :track track}
     :stake      :actuation/draft-filing
     :confidence (if e 0.9 0.3)}))

(defn- propose-submit
  "Draft the actual audit-readiness compliance-package FILING-SUBMIT
  action. ALWAYS `:stake :actuation/submit-filing` -- the operator's
  own real-world act of finalizing its audit-readiness compliance
  package for its own records (NOT a submission to the Board of Audit
  itself -- see registry.cljc). Reflects readiness across the gates the
  governor independently re-verifies: audit-scope classification
  (unconditional once the entity is engaged), cooperation readiness
  (unconditional when in-scope), and subsidy-record retention
  (conditional on `:subsidy-recipient?`)."
  [db {:keys [subject]}]
  (let [e (store/engagement db subject)
        track registry/audit-readiness-track
        scope-ok? (and (:audit-scope-classified? e)
                       (= (:audit-scope e) (registry/classify-audit-scope e)))
        cooperation-ok? (or (= :none (:audit-scope e)) (true? (:cooperation-contact-designated? e)))
        retention-ok? (or (not (:subsidy-recipient? e))
                          (and (true? (:record-retention-verified? e))
                               (or (not (number? (:program-retention-years e)))
                                   (not (number? (:recorded-retention-years e)))
                                   (>= (:recorded-retention-years e) (:program-retention-years e)))))]
    {:summary    (str subject " 向け監査対応コンプライアンス・パッケージ提出提案"
                      (when e (str " (operator=" (:operator e) ")")))
     :rationale  (if e
                   (str "audit-scope=" (:audit-scope e)
                        " audit-scope-classified?=" (:audit-scope-classified? e)
                        " cooperation-contact-designated?=" (:cooperation-contact-designated? e)
                        " subsidy-recipient?=" (:subsidy-recipient? e)
                        " record-retention-verified?=" (:record-retention-verified? e))
                   "engagementが見つかりません")
     :cites      (if e [subject (name track)] [])
     :effect     :engagement/mark-submitted
     :value      {:engagement-id subject :track track}
     :stake      :actuation/submit-filing
     :confidence (if (and e scope-ok? cooperation-ok? retention-ok?) 0.9 0.3)}))

(defprotocol Advisor
  (-advise [this db request] "Return a proposal map for `request`."))

(defrecord MockAdvisor []
  Advisor
  (-advise [_ db {:keys [op] :as request}]
    (case op
      :engagement/intake   (normalize-intake db request)
      :compliance/assess   (assess-track db request)
      :filing/draft        (propose-draft db request)
      :filing/submit       (propose-submit db request)
      {:summary "unknown op" :rationale "unsupported" :cites []
       :effect :noop :value {} :stake nil :confidence 0.0})))

(defn mock-advisor [] (->MockAdvisor))

(defn trace [request proposal]
  {:t :advisor-proposal
   :op (:op request)
   :subject (:subject request)
   :track (:track request)
   :summary (:summary proposal)
   :confidence (:confidence proposal)
   :stake (:stake proposal)})
