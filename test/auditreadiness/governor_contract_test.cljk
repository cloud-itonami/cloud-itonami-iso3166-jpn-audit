(ns auditreadiness.governor-contract-test
  "The governor contract as executable tests -- this vertical's own
  Trust Controls implemented faithfully, and the integration test
  running the compiled StateGraph end-to-end. The single invariant
  under test:

    AuditReadiness-LLM never drafts or submits an audit-readiness
    compliance package the Audit-Readiness Compliance Governor would
    reject, `:filing/draft`/`:filing/submit` NEVER auto-commit at any
    phase, `:engagement/intake` MAY auto-commit when clean, and every
    decision (commit OR hold) leaves exactly one ledger fact."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [auditreadiness.registry :as registry]
            [auditreadiness.store :as store]
            [auditreadiness.operation :as op]))

(defn- fresh []
  (let [db (store/seed-db)]
    [db (op/build db)]))

(def operator {:actor-id "op-1" :actor-role :audit-readiness-operator :phase 3})
(def track registry/audit-readiness-track)

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- draft!
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-draft") {:op :filing/draft :subject subject :track track} operator)
  (approve! actor (str tid-prefix "-draft")))

(deftest clean-intake-auto-commits
  (testing "integration: engagement/intake at phase 3 auto-commits through the full compiled graph"
    (let [[db actor] (fresh)
          res (exec-op actor "t1"
                    {:op :engagement/intake :subject "eng-1"
                     :patch {:id "eng-1" :operator "Setouchi Public Infrastructure KK"}} operator)]
      (is (= :commit (get-in res [:state :disposition])))
      (is (= "Setouchi Public Infrastructure KK" (:operator (store/engagement db "eng-1"))) "SSoT actually updated")
      (is (= 1 (count (store/ledger db)))))))

(deftest compliance-assess-always-needs-approval
  (testing "assess is never in any phase's :auto set -- always human approval, even when clean"
    (let [[db actor] (fresh)
          res (exec-op actor "t2" {:op :compliance/assess :subject "eng-1" :track :mandatory-audit-scope} operator)]
      (is (= :interrupted (:status res)))
      (let [r2 (approve! actor "t2")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (some? (store/assessment-of db "eng-1" :mandatory-audit-scope)))))))

(deftest fabricated-track-is-held
  (testing "a compliance/assess proposal with no official spec-basis -> HOLD"
    (let [[db actor] (fresh)
          res (exec-op actor "t3"
                    {:op :compliance/assess :subject "eng-1" :track :mandatory-audit-scope :no-spec? true} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:no-spec-basis} (-> (store/ledger db) first :basis)))
      (is (nil? (store/assessment-of db "eng-1" :mandatory-audit-scope)) "no assessment written"))))

(deftest fabricated-boa-vendor-qualification-is-held-and-unoverridable
  (testing "claiming a Board-of-Audit-specific vendor qualification exists -> HARD hold (flagship fabrication-trap defense)"
    (let [[db actor] (fresh)
          res (exec-op actor "t3b"
                    {:op :compliance/assess :subject "eng-2" :track :no-separate-boa-vendor-qualification
                     :fabricate-vendor-qualification? true} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:fabricated-boa-vendor-qualification} (-> (store/ledger db) first :basis)))
      (is (nil? (store/assessment-of db "eng-2" :no-separate-boa-vendor-qualification)) "no assessment written"))))

(deftest audit-scope-undetermined-is-held-and-unoverridable
  (testing "eng-3 has :audit-scope-classified? false -> HARD hold (flagship classification check, unconditional)"
    (let [[db actor] (fresh)
          _ (draft! actor "t5pre" "eng-3")
          res (exec-op actor "t5" {:op :filing/submit :subject "eng-3" :track track} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:audit-scope-undetermined} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest audit-scope-classification-mismatch-is-held-and-unoverridable
  (testing "eng-4 claims :mandatory but ground-truth facts recompute to :discretionary -> HARD hold (flagship classification check)"
    (let [[db actor] (fresh)]
      (is (= :discretionary (registry/classify-audit-scope (store/engagement db "eng-4")))
          "sanity: eng-4's own ground-truth facts recompute to :discretionary")
      (let [_ (draft! actor "t6pre" "eng-4")
            res (exec-op actor "t6" {:op :filing/submit :subject "eng-4" :track track} operator)]
        (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
        (is (not= :interrupted (:status res)))
        (is (some #{:audit-scope-classification-mismatch} (-> (store/ledger db) last :basis)))
        (is (empty? (store/submit-history db)))))))

(deftest cooperation-readiness-missing-is-held-for-in-scope-engagement
  (testing "eng-5 is :discretionary scope but has no cooperation contact designated -> HARD hold, CONDITIONAL on being in-scope"
    (let [[db actor] (fresh)
          _ (draft! actor "t7pre" "eng-5")
          res (exec-op actor "t7" {:op :filing/submit :subject "eng-5" :track track} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:cooperation-readiness-missing} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest cooperation-check-is-a-noop-for-mandatory-scope-engagement-with-contact-designated
  (testing "eng-1 (:mandatory scope, cooperation-contact-designated? true) never triggers the cooperation gate"
    (let [[db actor] (fresh)
          _ (draft! actor "t7bpre" "eng-1")
          res (exec-op actor "t7b" {:op :filing/submit :subject "eng-1" :track track} operator)]
      (is (= :interrupted (:status res)) "clean submit still escalates for human approval, but is NOT held")
      (is (not (some #{:cooperation-readiness-missing} (mapcat :basis (store/ledger db))))))))

(deftest subsidy-retention-noncompliant-is-held-for-subsidy-recipient
  (testing "eng-6 is a subsidy recipient with insufficient/unverified retention -> HARD hold, CONDITIONAL on :subsidy-recipient?"
    (let [[db actor] (fresh)
          _ (draft! actor "t8pre" "eng-6")
          res (exec-op actor "t8" {:op :filing/submit :subject "eng-6" :track track} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:subsidy-retention-noncompliant} (-> (store/ledger db) last :basis)))
      (is (empty? (store/submit-history db))))))

(deftest subsidy-retention-check-is-a-noop-for-non-subsidy-recipient
  (testing "eng-7 (:subsidy-recipient? false) never triggers the retention gate, even with nil retention fields"
    (let [[db actor] (fresh)
          _ (draft! actor "t8bpre" "eng-7")
          res (exec-op actor "t8b" {:op :filing/submit :subject "eng-7" :track track} operator)]
      (is (= :interrupted (:status res)))
      (is (not (some #{:subsidy-retention-noncompliant} (mapcat :basis (store/ledger db))))))))

(deftest submit-always-escalates-then-human-decides
  (testing "integration: a clean fully-classified submit still ALWAYS interrupts for human approval"
    (let [[db actor] (fresh)
          _ (draft! actor "t10pre" "eng-1")
          r1 (exec-op actor "t10" {:op :filing/submit :subject "eng-1" :track track} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, submit record drafted"
        (let [r2 (approve! actor "t10")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:submitted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/submit-history db))) "one draft submit record"))))))

(deftest draft-always-escalates-then-human-decides
  (testing "a clean draft still ALWAYS interrupts for human approval"
    (let [[db actor] (fresh)
          r1 (exec-op actor "t11" {:op :filing/draft :subject "eng-1" :track track} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, draft record drafted"
        (let [r2 (approve! actor "t11")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:drafted? (store/engagement db "eng-1"))))
          (is (= 1 (count (store/draft-history db))) "one draft record"))))))

(deftest engagement-double-draft-is-held
  (testing "drafting the same engagement's audit-readiness package twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (draft! actor "t12pre" "eng-1")
          res (exec-op actor "t12" {:op :filing/draft :subject "eng-1" :track track} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-drafted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/draft-history db))) "still only the one earlier draft"))))

(deftest engagement-double-submit-is-held
  (testing "submitting the same engagement's audit-readiness package twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (draft! actor "t13pre" "eng-1")
          _ (exec-op actor "t13a" {:op :filing/submit :subject "eng-1" :track track} operator)
          _ (approve! actor "t13a")
          res (exec-op actor "t13" {:op :filing/submit :subject "eng-1" :track track} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-submitted} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/submit-history db))) "still only the one earlier submit"))))

(deftest every-decision-leaves-one-ledger-fact
  (testing "write-only-through-ledger: N operations -> N ledger facts"
    (let [[db actor] (fresh)]
      (exec-op actor "a" {:op :engagement/intake :subject "eng-1"
                          :patch {:id "eng-1" :operator "Setouchi Public Infrastructure KK"}} operator)
      (exec-op actor "b" {:op :compliance/assess :subject "eng-1" :track :mandatory-audit-scope :no-spec? true} operator)
      (is (= 2 (count (store/ledger db)))
          "one commit + one hold, both recorded"))))
