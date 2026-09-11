(ns auditreadiness.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [auditreadiness.facts :as facts]))

(deftest mandatory-audit-scope-has-spec-basis
  (let [sb (facts/spec-basis :mandatory-audit-scope)]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (= 6 (count (:mandatory-categories sb))))))

(deftest discretionary-audit-scope-has-spec-basis
  (let [sb (facts/spec-basis :discretionary-audit-scope)]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (= 60 (:continuing-designation-count sb)))
    (is (= 4816 (:annual-designation-count-fy2025 sb)))
    (is (= 63 (:annual-designation-contractor-subcategory-count sb)))))

(deftest audit-cooperation-duties-has-spec-basis
  (let [sb (facts/spec-basis :audit-cooperation-duties)]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))))

(deftest subsidy-record-retention-is-not-a-blanket-rule
  (testing "the retention duty is program-conditional, never a hardcoded universal number"
    (let [sb (facts/spec-basis :subsidy-record-retention)]
      (is (some? sb))
      (is (string? (:provenance sb)))
      (is (seq (:required-evidence sb)))
      (is (false? (:blanket-rule? sb)) "must not be modeled as a universal Board-of-Audit-Act-wide mandate"))))

(deftest no-separate-boa-vendor-qualification-is-not-a-filing-track
  (testing "the boundary entry is spec-basis-citable but never itself drafted/submitted"
    (let [sb (facts/spec-basis :no-separate-boa-vendor-qualification)]
      (is (some? sb))
      (is (string? (:provenance sb)))
      (is (empty? (:required-evidence sb)))
      (is (false? (:filing-track? sb)))
      (is (false? (facts/filing-track? :no-separate-boa-vendor-qualification))))))

(deftest filing-track-defaults-true-for-regulatory-entries
  (is (true? (facts/filing-track? :mandatory-audit-scope)))
  (is (true? (facts/filing-track? :discretionary-audit-scope)))
  (is (true? (facts/filing-track? :audit-cooperation-duties)))
  (is (true? (facts/filing-track? :subsidy-record-retention)))
  (is (false? (facts/filing-track? :unknown-track)) "no spec-basis at all -> not a filing track"))

(deftest unknown-track-has-no-spec-basis
  (is (nil? (facts/spec-basis :unknown-track)))
  (is (nil? (facts/spec-basis :zzz))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis :mandatory-audit-scope)
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? :mandatory-audit-scope all)))
    (is (not (facts/required-evidence-satisfied? :mandatory-audit-scope (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? :unknown-track all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage [:mandatory-audit-scope :discretionary-audit-scope :unknown-track])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["unknown-track"] (:missing-tracks c)))))

(deftest catalog-has-exactly-five-entries
  (is (= #{:mandatory-audit-scope :discretionary-audit-scope :audit-cooperation-duties
           :subsidy-record-retention :no-separate-boa-vendor-qualification}
         facts/valid-tracks)))
