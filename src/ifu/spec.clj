(ns ifu.spec
  (:require [clojure.spec.alpha :as s]))

(defn validator
  "Return a validator fn for use with ifu.core/parse from a spec."
  [spec]
  (fn [data]
    (when-let [ed (s/explain-data spec data)]
      (mapv
        (fn [{:keys [in pred]}]
          {:in in :message (str "failed: " pred)})
        (::s/problems ed)))))
