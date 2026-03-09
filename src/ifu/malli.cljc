(ns ifu.malli
  (:require [malli.core :as m]
            [malli.error :as me]))

(defn validator
  "Return a validator fn for use with ifu.core/parse from a malli schema."
  [schema]
  (fn [data]
    (when-let [explanation (m/explain schema data)]
      (let [h (me/humanize explanation)]
        (mapv
          (fn [{:keys [in]}]
            {:in in :message (first (get-in h (vec in)))})
          (:errors explanation))))))
