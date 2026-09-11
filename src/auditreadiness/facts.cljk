(ns auditreadiness.facts
  "Japan Board of Audit (会計検査院) audit-jurisdiction / cooperation-duty /
  subsidy-record-retention / vendor-qualification-boundary catalog -- the
  ONLY source of regulatory-requirement facts this actor is allowed to
  cite (`auditreadiness.governor`'s spec-basis check enforces that every
  proposal touching `:compliance/assess`, `:filing/draft`, or
  `:filing/submit` cites this catalog and nothing invented).

  Every fact below was verified against `jbaudit.go.jp` and
  `japaneselawtranslation.go.jp` / `hourei.ndl.go.jp` (Japan Ministry of
  Justice statute-translation portals) during this repo's research pass
  (2026-07-22/23). Five catalog entries, each with its own owner
  authority and legal basis -- do NOT merge them into one
  undifferentiated 'Board of Audit requirement':

    :mandatory-audit-scope         -- 会計検査院法 (Board of Audit Act,
                                       Act No. 73 of 1947, enacted
                                       pursuant to Article 90 of the
                                       Constitution) Article 22 --
                                       必要的検査対象 (mandatory audit
                                       scope), applies AUTOMATICALLY by
                                       statute, no opt-in/registration:
                                       monthly State revenue/expenditure;
                                       State cash/goods/national-property
                                       transfers; State claims and
                                       national-debt changes; BOJ
                                       handling of State cash/precious
                                       metals/securities; accounts of
                                       entities where the State holds
                                       >=1/2 capital (208 entities per
                                       the Board's current list); NHK.
    :discretionary-audit-scope     -- Board of Audit Act Article 23 --
                                       選択的検査対象 (discretionary audit
                                       scope), when the Board finds it
                                       necessary or the Cabinet requests
                                       it: entities receiving State
                                       subsidies or other financial
                                       assistance. Per the Board's own
                                       current target-list: 60
                                       'continuing designation' entities
                                       (all 47 prefectures + 12 others)
                                       plus 4,816 'annual designation'
                                       entities for FY2025 (municipalities,
                                       cooperatives/legal entities
                                       receiving subsidies, government
                                       contractors/suppliers -- 63
                                       entities in that last category).
    :audit-cooperation-duties      -- Board of Audit Act Articles 24-27
                                       -- what an audited entity must
                                       cooperate with once it is in scope
                                       (Art.22 or Art.23): Art.24 regular
                                       submission of statements of
                                       accounts + documentary evidence;
                                       Art.25 on-site (実地) field audits;
                                       Art.26 submission of books/
                                       documents/materials/reports, or
                                       questioning/summoning relevant
                                       persons; Art.27 immediate reporting
                                       of any discovered crime or property
                                       loss found in audited accounts.
    :subsidy-record-retention      -- a SEPARATE act from the Board of
                                       Audit Act: 補助金等に係る予算の執行の
                                       適正化に関する法律 ('Act on Proper
                                       Execution of Budget Concerning
                                       Subsidies, etc.', Act No. 179 of
                                       1955). Individual ministries'
                                       subsidy manuals (交付要綱, set
                                       per-program) commonly require
                                       subsidy recipients to retain books/
                                       evidentiary documents (帳簿・証拠書類)
                                       for 5 years from the end of the
                                       fiscal year the subsidized project
                                       concludes, available on request
                                       from the subsidy secretariat AND
                                       the Board of Audit. This is a
                                       COMMONLY-OBSERVED PROGRAM-LEVEL
                                       practice, NOT a universal
                                       Board-of-Audit-Act-wide mandate --
                                       `:blanket-rule?` is `false` and the
                                       retained-years figure this actor
                                       checks against always comes from
                                       the engagement's own
                                       `:program-retention-years` field,
                                       never a hardcoded number.
    :no-separate-boa-vendor-qualification -- a NEGATIVE/boundary catalog
                                       entry, `:filing-track? false`.
                                       When the Board of Audit itself acts
                                       as a procuring entity, it is one of
                                       the ministries/agencies covered by
                                       the standard 全省庁統一資格 (Unified
                                       Qualification for All Ministries/
                                       Agencies) system -- same as any
                                       other ministry. There is NO
                                       evidence of a Board-of-Audit-
                                       specific vendor qualification
                                       distinct from this. Exists so
                                       `auditreadiness.governor`'s
                                       fabricated-vendor-qualification
                                       check has a citable spec-basis for
                                       REJECTING any proposal that invents
                                       a Board-of-Audit-specific vendor
                                       credential.

  What this catalog deliberately does NOT claim (fabrication traps this
  repo's research dossier explicitly flagged -- see README/docs):
    - does NOT invent a universal retention period (e.g. 'always 5
      years for everyone') mandated directly by the Board of Audit Act
      -- it doesn't exist; retention derives from Act No. 179/1955 +
      program-specific 交付要綱 and varies by program;
    - does NOT invent a Board-of-Audit-specific vendor/bidder
      qualification distinct from 全省庁統一資格 -- actively researched,
      found no evidence it exists;
    - does NOT model 'coming under Board-of-Audit jurisdiction' as
      something an entity applies for or registers into -- it is
      automatic-by-statute based on the entity's own funding/contract
      facts (Art.22/23 criteria), which is why
      `auditreadiness.registry/classify-audit-scope` is a pure
      CLASSIFICATION function over an entity's own ground-truth facts,
      never a registration/application op.")

(def catalog
  {:mandatory-audit-scope
   {:name "会計検査院法第22条 -- 必要的検査対象"
    :name-en "Board of Audit Act Article 22 -- Mandatory Audit Scope"
    :owner-authority "会計検査院 (Board of Audit) -- 日本国憲法第90条に基づき内閣から独立した機関"
    :legal-basis "日本国憲法第90条 + 会計検査院法(昭和22年法律第73号, 1947年制定)第22条"
    :constitutional-basis "日本国憲法第90条: 国の収入支出の決算は、すべて毎年会計検査院がこれを検査し、内閣は、次の年度に、その検査報告とともに、これを国会に提出しなければならない。"
    :provenance "https://www.japaneselawtranslation.go.jp/en/laws/view/3856"
    :provenance-secondary
    ["https://www.jbaudit.go.jp/english/jbaudit/history.html"
     "https://www.jbaudit.go.jp/jbaudit/target/index.html"]
    :mandatory-categories
    ["毎月の国の収入支出"
     "国の現金・物品の受払、国有財産の移動"
     "国の債権の発生・変更、国債その他の債務の増減"
     "日本銀行が取り扱う国の現金・貴金属・有価証券の受払"
     "国が資本金の1/2以上を出資している法人の会計(現行208法人)"
     "日本放送協会(NHK)"]
    :entity-count-note "国が資本金の1/2以上を出資している法人: 現在208法人(jbaudit.go.jpの対象一覧に基づく)"
    :process-description "対象該当性は会計検査院法第22条の要件(資本出資割合等)によって自動的に発生し、対象法人が申請・登録して得る資格ではない。"
    :required-evidence
    ["対象区分(資本出資割合1/2以上・NHK・国の現金物品取扱い等)の該当性確認記録"
     "会計検査院法第24条に基づく計算書・証拠書類の定期提出体制の記録"]}

   :discretionary-audit-scope
   {:name "会計検査院法第23条 -- 選択的検査対象"
    :name-en "Board of Audit Act Article 23 -- Discretionary Audit Scope"
    :owner-authority "会計検査院 (Board of Audit)"
    :legal-basis "会計検査院法(昭和22年法律第73号)第23条 -- 会計検査院が必要と認めるとき、または内閣の請求があるとき"
    :provenance "https://www.jbaudit.go.jp/jbaudit/target/index.html"
    :provenance-secondary
    ["https://www.jbaudit.go.jp/jbaudit/target/03.html"
     "https://www.japaneselawtranslation.go.jp/en/laws/view/3856"]
    :continuing-designation-count 60
    :continuing-designation-composition "47都道府県 + その他12"
    :annual-designation-count-fy2025 4816
    :annual-designation-note "FY2025指定: 市町村、補助金等を受ける組合・法人、政府調達契約事業者等を含む -- うち事業者(政府調達契約事業者等)の区分は63法人"
    :annual-designation-contractor-subcategory-count 63
    :process-description "国からの補助金その他の財政援助を受ける団体等について、会計検査院が必要と認めたとき検査の対象になる。対象該当性は毎年度、会計検査院自身が指定リスト(継続指定/年度指定)を公表する形で運用され、対象団体が申請して得る資格ではない。"
    :required-evidence
    ["国からの補助金・財政援助受給の事実確認記録"
     "当該年度の会計検査院指定対象リスト(継続指定/年度指定)該当性確認記録"]}

   :audit-cooperation-duties
   {:name "会計検査院法第24条・第25条・第26条・第27条 -- 検査への協力義務"
    :name-en "Board of Audit Act Articles 24-27 -- audited-entity cooperation duties (document submission, field audit, information requests, crime reporting)"
    :owner-authority "会計検査院"
    :legal-basis "会計検査院法(昭和22年法律第73号)第24条(計算書等の提出義務)/第25条(実地検査)/第26条(帳簿書類等の提出要求・質問)/第27条(犯罪・損害の報告義務)"
    :provenance "https://www.japaneselawtranslation.go.jp/en/laws/view/3856"
    :provenance-secondary ["https://www.jbaudit.go.jp/english/jbaudit/history.html"]
    :process-description "被検査対象は定期的に計算書及び証拠書類その他の資料を提出しなければならず(第24条)、会計検査院は職員を派遣して実地検査を行うことができ(第25条)、帳簿・書類・報告の提出を求め、関係者に質問し、または関係者を召喚することができ(第26条)、被検査対象の職員は検査を通じ発見した犯罪・損害を直ちに報告しなければならない(第27条)。"
    :required-evidence
    ["計算書・証拠書類の定期提出体制(担当窓口)の整備記録"
     "実地検査(第25条)受入れ体制の整備記録"
     "帳簿・書類・報告提出要求・質問(第26条)への対応体制の整備記録"]}

   :subsidy-record-retention
   {:name "補助金等に係る予算の執行の適正化に関する法律(昭和30年法律第179号)に基づく帳簿・証拠書類の保存"
    :name-en "Act on Proper Execution of Budget Concerning Subsidies, etc. (Act No. 179 of 1955) -- record retention for subsidy recipients"
    :owner-authority "補助金交付元の各省庁(交付要綱ごとに保存年数を規定) -- 会計検査院はこの保存記録を第22条/第23条の検査時に確認しうる"
    :legal-basis "補助金等に係る予算の執行の適正化に関する法律(昭和30年法律第179号)"
    :provenance "https://hourei.ndl.go.jp/simple/detail?lawId=0000048052&current=-1"
    :blanket-rule? false
    :common-practice-note "個別の交付要綱の実務では、補助事業が完了した年度の終了後5年間、帳簿・証拠書類を保存する例が多い -- ただしこれは各省庁が交付要綱で個別に定める運用であり、Act No. 179/1955そのものが全ての補助金に一律5年間の保存を義務付けているわけではない。保存年数は適用される交付要綱ごとに確認する必要がある。"
    :process-description "補助金等の交付を受けた事業者は、当該補助事業に係る帳簿及び証拠書類を、適用される交付要綱が定める期間(実務上5年間とする例が多いが、交付要綱ごとに異なりうる)保存し、補助金等交付機関及び会計検査院の請求があったときいつでも提示できる状態に置かなければならない。"
    :required-evidence
    ["適用される交付要綱上の保存年数の確認記録"
     "帳簿・証拠書類の保存状況(実際の保存年数が交付要綱の定める年数以上であることの確認)記録"]}

   :no-separate-boa-vendor-qualification
   {:name "会計検査院向け物品役務調達における資格制度 -- 全省庁統一資格のみであり、会計検査院固有の資格は存在しない(境界確認用エントリ)"
    :name-en "Vendor qualification for selling to the Board of Audit as a procuring entity -- ONLY 全省庁統一資格 (Unified Qualification for All Ministries/Agencies) applies; no separate Board-of-Audit-specific vendor qualification exists (boundary entry)"
    :owner-authority "全省庁共通制度 -- 会計検査院はこの統一資格制度の対象官庁の一つとして自らの契約事務を運用する。会計検査院固有の別建て資格制度は確認されていない。"
    :legal-basis "予算決算及び会計令(昭和22年勅令第165号)に基づく全省庁統一の競争参加資格審査制度 -- 会計検査院固有の法的根拠は存在しない"
    :provenance "https://www.jbaudit.go.jp/english/jbaudit/history.html"
    :filing-track? false
    :process-description "会計検査院が調達主体(発注者)として物品・役務を調達する場合も、他省庁と同じ全省庁統一資格の枠組みで運用される。加えて、会計検査院の検査対象になること自体(第22条/第23条)は法律上自動的に発生する事項であり、申請・登録によって『会計検査院の検査対象資格』を取得するという手続は存在しない。この2点(①調達資格は全省庁統一資格に一本化されている、②検査対象化は自動発生であり申請不要)を混同・省略して『会計検査院固有の資格/登録』が必要であるかのように提案することは誤り。"
    :required-evidence []}})

(def valid-tracks (set (keys catalog)))

(defn spec-basis [track] (get catalog track))

(defn coverage
  ([] (coverage (keys catalog)))
  ([tracks]
   (let [have (filter catalog tracks) missing (remove catalog tracks)]
     {:requested (count tracks) :covered (count have)
      :covered-tracks (vec (sort (map name have)))
      :missing-tracks (vec (sort (map name missing)))
      :note "R0 catalog seed -- mandatory-audit-scope + discretionary-audit-scope + audit-cooperation-duties + subsidy-record-retention + no-separate-boa-vendor-qualification, JPN-AUDIT agency scope"})))

(defn required-evidence-satisfied? [track submitted]
  (when-let [{:keys [required-evidence]} (spec-basis track)]
    (= (count required-evidence) (count (filter (set submitted) required-evidence)))))

(defn evidence-checklist [track] (:required-evidence (spec-basis track) []))

(defn filing-track?
  "Does `track`'s catalog entry represent something that is ever itself
  drafted/submitted as a filing (as opposed to a citation-only /
  boundary entry like `:no-separate-boa-vendor-qualification`)? Defaults
  to true when the catalog entry does not say otherwise -- only
  `:no-separate-boa-vendor-qualification` opts out today."
  [track]
  (let [sb (spec-basis track)]
    (boolean (and sb (not (false? (:filing-track? sb)))))))
