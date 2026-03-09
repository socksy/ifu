;; Validating EDN config files with malli + ifu
;;
;; deps.edn:
;;   {:deps {io.github.socksy/ifu {:mvn/version "0.2.2"}
;;           metosin/malli        {:mvn/version "0.17.0"}}}

(require '[ifu.malli :as im])

(def Config
  [:map
   [:port :int]
   [:host :string]
   [:debug {:optional true} :boolean]])

;; Throws with rendered diagnostics pointing at the bad values
(im/parse Config "{:port \"abc\"\n :host 123}" "config.edn")
