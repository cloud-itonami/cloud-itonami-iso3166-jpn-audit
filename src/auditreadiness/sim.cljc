(ns auditreadiness.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a clean mandatory-scope
  engagement through intake -> assess mandatory-audit-scope -> assess
  audit-cooperation-duties -> audit-readiness compliance-package filing
  draft (escalate/approve/commit) -> filing submit (escalate/approve/
  commit), then shows HARD-hold scenarios grounded in the dossier:
  fabrication defense (unregistered track + fabricated Board-of-Audit
  vendor qualification), audit-scope-undetermined, audit-scope-
  classification-mismatch, cooperation-readiness-missing, subsidy-
  retention-noncompliant, a subsidy-retention no-op for a non-subsidy-
  recipient, and double-draft/double-submit."
  (:require [langgraph.graph :as g]
            [auditreadiness.registry :as registry]
            [auditreadiness.store :as store]
            [auditreadiness.operation :as op]))

(def operator {:actor-id "op-1" :actor-role :audit-readiness-operator :phase 3})
(def track registry/audit-readiness-track)

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn -main [& _]
  (let [db (store/seed-db)
        actor (op/build db)]
    (println "== engagement/intake eng-1 (clean, mandatory scope) ==")
    (println (exec-op actor "t1" {:op :engagement/intake :subject "eng-1"
                                  :patch {:id "eng-1" :operator "Setouchi Public Infrastructure KK"}} operator))

    (println "== compliance/assess eng-1/mandatory-audit-scope (escalates -- human approves) ==")
    (println (exec-op actor "t2" {:op :compliance/assess :subject "eng-1" :track :mandatory-audit-scope} operator))
    (println (approve! actor "t2"))

    (println "== compliance/assess eng-1/audit-cooperation-duties (escalates -- human approves) ==")
    (println (exec-op actor "t2b" {:op :compliance/assess :subject "eng-1" :track :audit-cooperation-duties} operator))
    (println (approve! actor "t2b"))

    (println "== filing/draft eng-1 audit-readiness package (always escalates -- actuation/draft-filing) ==")
    (let [r (exec-op actor "t3" {:op :filing/draft :subject "eng-1" :track track} operator)]
      (println r)
      (println "-- human operator approves --")
      (println (approve! actor "t3")))

    (println "== filing/submit eng-1 audit-readiness package (always escalates -- actuation/submit-filing) ==")
    (let [r (exec-op actor "t4" {:op :filing/submit :subject "eng-1" :track track} operator)]
      (println r)
      (println "-- human operator approves --")
      (println (approve! actor "t4")))

    (println "== compliance/assess eng-2/mandatory-audit-scope (no spec-basis -> HARD hold) ==")
    (println (exec-op actor "t5" {:op :compliance/assess :subject "eng-2" :track :mandatory-audit-scope :no-spec? true} operator))

    (println "== compliance/assess eng-2/no-separate-boa-vendor-qualification (fabrication -> HARD hold) ==")
    (println (exec-op actor "t5b" {:op :compliance/assess :subject "eng-2" :track :no-separate-boa-vendor-qualification :fabricate-vendor-qualification? true} operator))

    (println "== filing/draft + filing/submit eng-3 (audit-scope-undetermined -> HARD hold) ==")
    (println (exec-op actor "t6" {:op :filing/draft :subject "eng-3" :track track} operator))
    (println (approve! actor "t6"))
    (println (exec-op actor "t7" {:op :filing/submit :subject "eng-3" :track track} operator))

    (println "== filing/draft + filing/submit eng-4 (audit-scope-classification-mismatch -> HARD hold) ==")
    (println (exec-op actor "t8" {:op :filing/draft :subject "eng-4" :track track} operator))
    (println (approve! actor "t8"))
    (println (exec-op actor "t9" {:op :filing/submit :subject "eng-4" :track track} operator))

    (println "== filing/draft + filing/submit eng-5 (cooperation-readiness-missing -> HARD hold) ==")
    (println (exec-op actor "t10" {:op :filing/draft :subject "eng-5" :track track} operator))
    (println (approve! actor "t10"))
    (println (exec-op actor "t11" {:op :filing/submit :subject "eng-5" :track track} operator))

    (println "== filing/draft + filing/submit eng-6 (subsidy-retention-noncompliant -> HARD hold) ==")
    (println (exec-op actor "t12" {:op :filing/draft :subject "eng-6" :track track} operator))
    (println (approve! actor "t12"))
    (println (exec-op actor "t13" {:op :filing/submit :subject "eng-6" :track track} operator))

    (println "== filing/draft + filing/submit eng-7 (subsidy-retention is a NO-OP for non-recipient -- clean escalate) ==")
    (println (exec-op actor "t14" {:op :filing/draft :subject "eng-7" :track track} operator))
    (println (approve! actor "t14"))
    (let [r (exec-op actor "t15" {:op :filing/submit :subject "eng-7" :track track} operator)]
      (println r)
      (println (approve! actor "t15")))

    (println "== filing/draft eng-1 AGAIN (double-draft -> HARD hold) ==")
    (println (exec-op actor "t16" {:op :filing/draft :subject "eng-1" :track track} operator))

    (println "== filing/submit eng-1 AGAIN (double-submit -> HARD hold) ==")
    (println (exec-op actor "t17" {:op :filing/submit :subject "eng-1" :track track} operator))

    (println "== audit ledger ==")
    (doseq [f (store/ledger db)] (println f))

    (println "== draft records ==")
    (doseq [r (store/draft-history db)] (println r))

    (println "== submit records ==")
    (doseq [r (store/submit-history db)] (println r))))
