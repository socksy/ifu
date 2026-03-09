(ns ifu.validation-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [ifu.core :as ifu]
            [ifu.malli :as ifu-malli]
            [ifu.spec :as ifu-spec]))

(s/def ::port int?)
(s/def ::host string?)
(s/def ::config (s/keys :req-un [::port ::host]))

(deftest malli-valid
  (is (= {:port 8080 :host "localhost"}
         (ifu/parse "{:port 8080 :host \"localhost\"}" "f"
           (ifu-malli/validator [:map [:port :int] [:host :string]])))))

(deftest malli-invalid
  (let [ex (try (ifu/parse "{:port \"abc\"}" "config.edn"
                  (ifu-malli/validator [:map [:port :int]]))
               (catch Exception e e))]
    (is (str/includes? (ex-message ex) "config.edn"))
    (is (str/includes? (ex-message ex) "should be an integer"))
    (is (seq (:diagnostics (ex-data ex))))))

(deftest spec-valid
  (is (= {:port 8080 :host "localhost"}
         (ifu/parse "{:port 8080 :host \"localhost\"}" "f"
           (ifu-spec/validator ::config)))))

(deftest spec-invalid
  (let [ex (try (ifu/parse "{:port \"abc\" :host 123}" "config.edn"
                  (ifu-spec/validator ::config))
               (catch Exception e e))]
    (is (str/includes? (ex-message ex) "config.edn"))
    (is (seq (:diagnostics (ex-data ex))))))
