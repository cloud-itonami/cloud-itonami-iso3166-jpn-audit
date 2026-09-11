(ns auditreadiness.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [auditreadiness.registry :as registry]))

(deftest classify-mandatory-via-state-capital-share
  (is (= :mandatory (registry/classify-audit-scope
                     {:state-capital-share 0.5 :is-nhk? false
                      :receives-state-subsidy? false :board-designated-contractor? false})))
  (is (= :mandatory (registry/classify-audit-scope
                     {:state-capital-share 0.99 :is-nhk? false
                      :receives-state-subsidy? false :board-designated-contractor? false}))))

(deftest classify-mandatory-via-nhk
  (is (= :mandatory (registry/classify-audit-scope
                     {:state-capital-share 0.0 :is-nhk? true
                      :receives-state-subsidy? false :board-designated-contractor? false}))))

(deftest classify-discretionary-via-subsidy-or-contractor-designation
  (is (= :discretionary (registry/classify-audit-scope
                         {:state-capital-share 0.0 :is-nhk? false
                          :receives-state-subsidy? true :board-designated-contractor? false})))
  (is (= :discretionary (registry/classify-audit-scope
                         {:state-capital-share 0.0 :is-nhk? false
                          :receives-state-subsidy? false :board-designated-contractor? true}))))

(deftest classify-none-when-no-criteria-met
  (is (= :none (registry/classify-audit-scope
                {:state-capital-share 0.0 :is-nhk? false
                 :receives-state-subsidy? false :board-designated-contractor? false}))))

(deftest classify-mandatory-takes-priority-over-discretionary
  (testing "an entity that is BOTH majority-state-owned AND a subsidy recipient is :mandatory (Article 22 is unconditional)"
    (is (= :mandatory (registry/classify-audit-scope
                       {:state-capital-share 0.6 :is-nhk? false
                        :receives-state-subsidy? true :board-designated-contractor? true})))))

(deftest classify-handles-missing-state-capital-share
  (is (= :none (registry/classify-audit-scope
                {:state-capital-share nil :is-nhk? false
                 :receives-state-subsidy? false :board-designated-contractor? false}))))

(deftest register-draft-and-submit
  (let [d (registry/register-draft "eng-1" registry/audit-readiness-track 0)
        s (registry/register-submit "eng-1" registry/audit-readiness-track 0)]
    (is (= "JPN-AUDIT-AUDIT-READINESS-PACKAGE-DFT-000000" (get d "draft_number")))
    (is (= "JPN-AUDIT-AUDIT-READINESS-PACKAGE-SUB-000000" (get s "submit_number")))
    (is (nil? (get-in d ["certificate" "proof"])))
    (is (= "draft-unsigned" (get-in s ["certificate" "status"])))))

(deftest register-requires-ids
  (is (thrown? Exception (registry/register-draft "" registry/audit-readiness-track 0)))
  (is (thrown? Exception (registry/register-submit "eng-1" "" 0))))
