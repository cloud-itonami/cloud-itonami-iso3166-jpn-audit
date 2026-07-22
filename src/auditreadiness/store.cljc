(ns auditreadiness.store
  "SSoT for the JPN-AUDIT (Board of Audit) audit-readiness actor, behind
  a `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior cloud-itonami actor in this fleet uses.

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store, using `langchain-store.core` for BOTH
                        the entity field-spec (`map->tx`/`pull->map`/
                        `pull-pattern`) AND the shared EDN-blob codec +
                        identity schema + event-log helpers, instead of
                        a hand-rolled `enc`/`dec*` (ADR-2607141600).

  Both implement the same protocol and pass the same contract
  (test/auditreadiness/store_contract_test.clj).

  The primary entity here is an `engagement` -- one operator's
  audit-readiness compliance engagement, carrying:

    - the ground-truth facts `auditreadiness.registry/classify-audit-
      scope` independently recomputes from: `:state-capital-share`,
      `:is-nhk?`, `:receives-state-subsidy?`,
      `:board-designated-contractor?` -- plus the engagement's OWN
      claimed classification, `:audit-scope` and `:audit-scope-
      classified?`. `auditreadiness.governor`'s audit-scope-
      classification check holds if the claim is missing or disagrees
      with the independent recompute.
    - `:cooperation-contact-designated?` -- Board of Audit Act
      Articles 24-26 cooperation-readiness (`:audit-cooperation-
      duties`), unconditional whenever `:audit-scope` is not `:none`.
    - the CONDITIONAL subsidy-record-retention gate: `:subsidy-
      recipient?` / `:program-retention-years` / `:recorded-retention-
      years` / `:record-retention-verified?`, grounded in
      `:subsidy-record-retention` -- a no-op gate when `:subsidy-
      recipient?` is false (retention derives from Act No. 179/1955 +
      the applicable program's own 交付要綱, never a hardcoded number
      -- see facts.cljc).
    - `:claims-boa-specific-vendor-qualification-required?` -- a
      test-only fabrication-injection field on the PROPOSAL (not
      normally set on the entity itself); see
      `auditreadiness.auditreadinessllm`'s `:fabricate-vendor-
      qualification?` flag.
    - the single actionable filing track's actuation state:
      `:drafted?`/`:draft-number`/`:submitted?`/`:submit-number` for
      `auditreadiness.registry/audit-readiness-track`
      (`:audit-readiness-package`) -- this actor manages exactly ONE
      filing track, so no per-track field-name indirection is needed.

  `:compliance/assess` proposals are stored per underlying regulatory
  catalog track (`:mandatory-audit-scope`/`:discretionary-audit-scope`/
  `:audit-cooperation-duties`/`:subsidy-record-retention`/`:no-
  separate-boa-vendor-qualification`) via `assessment-of`, keyed
  [engagement-id catalog-track] -- kept for the operator's own audit
  trail / checklist export, independent of which governor gates key off
  the engagement's own boolean fields.

  The ledger stays append-only on every backend."
  (:require [auditreadiness.registry :as registry]
            [langchain.db :as d]
            [langchain-store.core :as ls]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (assessment-of [s engagement-id track] "committed track assessment, or nil")
  (ledger [s])
  (draft-history [s] "the append-only filing-draft history")
  (submit-history [s] "the append-only filing-submit history")
  (next-draft-sequence [s track])
  (next-submit-sequence [s track])
  (engagement-drafted? [s engagement-id])
  (engagement-submitted? [s engagement-id])
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set covering the happy path
  (mandatory scope, clean draft/submit) plus the governor's own
  dossier-grounded checks: a fabrication-defense-only subject (eng-2,
  used to exercise `:no-spec?`/`:fabricate-vendor-qualification?` test
  flags), an audit-scope-undetermined case (eng-3), an audit-scope-
  classification-mismatch case (eng-4), a cooperation-readiness-missing
  case (eng-5), a subsidy-retention-noncompliant case (eng-6), and a
  subsidy-retention-noop case for a non-subsidy-recipient (eng-7)."
  []
  {:engagements
   {"eng-1" {:id "eng-1" :operator "Setouchi Public Infrastructure KK"
             :state-capital-share 0.6 :is-nhk? false
             :receives-state-subsidy? false :board-designated-contractor? false
             :audit-scope :mandatory :audit-scope-classified? true
             :cooperation-contact-designated? true
             :subsidy-recipient? false :program-retention-years nil
             :recorded-retention-years nil :record-retention-verified? false
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}
    "eng-2" {:id "eng-2" :operator "Atlantis Contract Services LLC"
             :state-capital-share 0.0 :is-nhk? false
             :receives-state-subsidy? false :board-designated-contractor? true
             :audit-scope :discretionary :audit-scope-classified? true
             :cooperation-contact-designated? true
             :subsidy-recipient? false :program-retention-years nil
             :recorded-retention-years nil :record-retention-verified? false
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? true
             :drafted? false :submitted? false :status :intake}
    "eng-3" {:id "eng-3" :operator "Nishi Regional Development KK"
             :state-capital-share 0.0 :is-nhk? false
             :receives-state-subsidy? true :board-designated-contractor? false
             :audit-scope nil :audit-scope-classified? false
             :cooperation-contact-designated? true
             :subsidy-recipient? true :program-retention-years 5
             :recorded-retention-years 5 :record-retention-verified? true
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}
    "eng-4" {:id "eng-4" :operator "Higashi Development Partners KK"
             :state-capital-share 0.0 :is-nhk? false
             :receives-state-subsidy? true :board-designated-contractor? false
             :audit-scope :mandatory :audit-scope-classified? true
             :cooperation-contact-designated? true
             :subsidy-recipient? true :program-retention-years 5
             :recorded-retention-years 5 :record-retention-verified? true
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}
    "eng-5" {:id "eng-5" :operator "Kita Public Works Vendors KK"
             :state-capital-share 0.0 :is-nhk? false
             :receives-state-subsidy? false :board-designated-contractor? true
             :audit-scope :discretionary :audit-scope-classified? true
             :cooperation-contact-designated? false
             :subsidy-recipient? false :program-retention-years nil
             :recorded-retention-years nil :record-retention-verified? false
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}
    "eng-6" {:id "eng-6" :operator "Minami Subsidized Research Institute"
             :state-capital-share 0.0 :is-nhk? false
             :receives-state-subsidy? true :board-designated-contractor? false
             :audit-scope :discretionary :audit-scope-classified? true
             :cooperation-contact-designated? true
             :subsidy-recipient? true :program-retention-years 5
             :recorded-retention-years 3 :record-retention-verified? false
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}
    "eng-7" {:id "eng-7" :operator "Chuo Equipment Supply KK"
             :state-capital-share 0.7 :is-nhk? false
             :receives-state-subsidy? false :board-designated-contractor? false
             :audit-scope :mandatory :audit-scope-classified? true
             :cooperation-contact-designated? true
             :subsidy-recipient? false :program-retention-years nil
             :recorded-retention-years nil :record-retention-verified? false
             :claims-boa-specific-vendor-qualification-required? false
             :sells-to-board-of-audit? false
             :drafted? false :submitted? false :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------
;; Both backends' `commit-record!` build the draft/submit record the
;; same way -- these two pure helpers are the shared step, mirroring
;; every sibling actor's `draft-filing!`/`submit-filing!` shape.

(defn- do-draft! [engagement-id track seq-n]
  (registry/register-draft engagement-id track seq-n))

(defn- do-submit! [engagement-id track seq-n]
  (registry/register-submit engagement-id track seq-n))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (assessment-of [_ engagement-id track] (get-in @a [:assessments engagement-id track]))
  (ledger [_] (:ledger @a))
  (draft-history [_] (:draft-records @a))
  (submit-history [_] (:submit-records @a))
  (next-draft-sequence [_ track] (get-in @a [:draft-sequences track] 0))
  (next-submit-sequence [_ track] (get-in @a [:submit-sequences track] 0))
  (engagement-drafted? [_ engagement-id]
    (boolean (get-in @a [:engagements engagement-id :drafted?])))
  (engagement-submitted? [_ engagement-id]
    (boolean (get-in @a [:engagements engagement-id :submitted?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :assessment/set
      (let [[engagement-id track] path]
        (swap! a assoc-in [:assessments engagement-id track] payload))

      :engagement/mark-drafted
      (let [[engagement-id track] path
            seq-n (next-draft-sequence s track)
            result (do-draft! engagement-id track seq-n)]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:draft-sequences track] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge
                                  {:drafted? true :draft-number (get result "draft_number")})
                       (update :draft-records registry/append result))))
        result)

      :engagement/mark-submitted
      (let [[engagement-id track] path
            seq-n (next-submit-sequence s track)
            result (do-submit! engagement-id track seq-n)]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:submit-sequences track] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge
                                  {:submitted? true :submit-number (get result "submit_number")})
                       (update :submit-records registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {}
                           :ledger [] :draft-sequences {} :draft-records []
                           :submit-sequences {} :submit-records []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

;; Entity field-spec drives map<->tx<->pull for `engagement` via
;; `langchain-store.core` -- no hand-rolled `engagement->tx`/
;; `pull->engagement` pair (ADR-2607141600 increment 2, `underwriting.store`
;; reference-entity-adopter pattern). None of these fields need `:blob?`
;; (all scalars/booleans/keywords); booleans use `:coerce boolean` so a
;; never-set attribute reads back as `false`, matching MemStore.
(def ^:private engagement-spec
  {:id                                                  {:attr :engagement/id}
   :operator                                            {:attr :engagement/operator}
   :state-capital-share                                 {:attr :engagement/state-capital-share}
   :is-nhk?                                              {:attr :engagement/is-nhk? :coerce boolean}
   :receives-state-subsidy?                              {:attr :engagement/receives-state-subsidy? :coerce boolean}
   :board-designated-contractor?                         {:attr :engagement/board-designated-contractor? :coerce boolean}
   :audit-scope                                          {:attr :engagement/audit-scope}
   :audit-scope-classified?                              {:attr :engagement/audit-scope-classified? :coerce boolean}
   :cooperation-contact-designated?                      {:attr :engagement/cooperation-contact-designated? :coerce boolean}
   :subsidy-recipient?                                   {:attr :engagement/subsidy-recipient? :coerce boolean}
   :program-retention-years                              {:attr :engagement/program-retention-years}
   :recorded-retention-years                             {:attr :engagement/recorded-retention-years}
   :record-retention-verified?                           {:attr :engagement/record-retention-verified? :coerce boolean}
   :claims-boa-specific-vendor-qualification-required?   {:attr :engagement/claims-boa-specific-vendor-qualification-required? :coerce boolean}
   :sells-to-board-of-audit?                             {:attr :engagement/sells-to-board-of-audit? :coerce boolean}
   :drafted?                                             {:attr :engagement/drafted? :coerce boolean}
   :draft-number                                         {:attr :engagement/draft-number}
   :submitted?                                           {:attr :engagement/submitted? :coerce boolean}
   :submit-number                                        {:attr :engagement/submit-number}
   :status                                               {:attr :engagement/status}})

(def ^:private schema
  (merge
   (ls/identity-schema [:engagement/id :assessment/key :ledger/seq
                        :draft-record/seq :submit-record/seq
                        :draft-sequence/track :submit-sequence/track])))

(defn- assessment-key [engagement-id track] (str engagement-id "::" (name track)))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (ls/pull->map engagement-spec :id
                  (d/pull (d/db conn) (ls/pull-pattern engagement-spec) [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(ls/pull->map engagement-spec :id
                             (d/pull (d/db conn) (ls/pull-pattern engagement-spec) [:engagement/id %])))
         (sort-by :id)))
  (assessment-of [_ engagement-id track]
    (ls/dec* (d/q '[:find ?p . :in $ ?k
                   :where [?a :assessment/key ?k] [?a :assessment/payload ?p]]
                 (d/db conn) (assessment-key engagement-id track))))
  (ledger [_] (ls/read-stream conn :ledger/seq :ledger/fact))
  (draft-history [_] (ls/read-stream conn :draft-record/seq :draft-record/record))
  (submit-history [_] (ls/read-stream conn :submit-record/seq :submit-record/record))
  (next-draft-sequence [_ track]
    (or (d/q '[:find ?n . :in $ ?t
              :where [?e :draft-sequence/track ?t] [?e :draft-sequence/next ?n]]
            (d/db conn) track)
        0))
  (next-submit-sequence [_ track]
    (or (d/q '[:find ?n . :in $ ?t
              :where [?e :submit-sequence/track ?t] [?e :submit-sequence/next ?n]]
            (d/db conn) track)
        0))
  (engagement-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(ls/map->tx engagement-spec value)])

      :assessment/set
      (let [[engagement-id track] path]
        (d/transact! conn [{:assessment/key (assessment-key engagement-id track)
                            :assessment/payload (ls/enc payload)}]))

      :engagement/mark-drafted
      (let [[engagement-id track] path
            seq-n (next-draft-sequence s track)
            result (do-draft! engagement-id track seq-n)
            next-n (inc seq-n)]
        (d/transact! conn
                     [(ls/map->tx engagement-spec {:id engagement-id :drafted? true
                                                   :draft-number (get result "draft_number")})
                      {:draft-sequence/track track :draft-sequence/next next-n}
                      {:draft-record/seq (count (draft-history s)) :draft-record/record (ls/enc (get result "record"))}])
        result)

      :engagement/mark-submitted
      (let [[engagement-id track] path
            seq-n (next-submit-sequence s track)
            result (do-submit! engagement-id track seq-n)
            next-n (inc seq-n)]
        (d/transact! conn
                     [(ls/map->tx engagement-spec {:id engagement-id :submitted? true
                                                   :submit-number (get result "submit_number")})
                      {:submit-sequence/track track :submit-sequence/next next-n}
                      {:submit-record/seq (count (submit-history s)) :submit-record/record (ls/enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (ls/append-blob! conn :ledger/seq :ledger/fact (count (ledger s)) fact)
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv #(ls/map->tx engagement-spec %) (vals engagements)))) s))

(defn datomic-store
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  []
  (datomic-store (demo-data)))
