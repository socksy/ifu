(ns ifu.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [ifu.core :as ifu]))

(deftest colorize-test
  (is (= "\033[1;31mhello\033[0m" (ifu/colorize "hello" "1;31"))))

(deftest source-line-test
  (is (= "second" (ifu/source-line "first\nsecond\nthird" 2)))
  (is (= "" (ifu/source-line "one" 5))))

(deftest pointer-test
  (is (= "  ^^^" (ifu/pointer 3 3)))
  (is (= "^" (ifu/pointer 1 0))))

(deftest render-test
  (let [result (ifu/render {:file "test.edn"
                            :source "foo\nbar baz\nqux"
                            :row 2 :col 5 :end-col 8
                            :message "unexpected value"
                            :hint "expected keyword"})]
    (testing "contains file location"
      (is (str/includes? result "test.edn:2:5")))
    (testing "contains source line"
      (is (str/includes? result "bar baz")))
    (testing "contains message"
      (is (str/includes? result "unexpected value")))
    (testing "contains hint"
      (is (str/includes? result "expected keyword")))
    (testing "contains pointer"
      (is (str/includes? result "^^^")))))

(deftest render-without-hint-test
  (let [result (ifu/render {:file "x.edn" :source "hello" :row 1 :col 1
                            :message "bad"})]
    (is (not (str/includes? result "nil")))))

(deftest severity-test
  (testing "warning uses yellow"
    (let [result (ifu/render {:file "x.edn" :source "hello" :row 1 :col 1
                              :message "hmm" :severity :warning})]
      (is (str/includes? result "warning"))
      (is (str/includes? result "\033[1;33m"))))
  (testing "info uses blue"
    (let [result (ifu/render {:file "x.edn" :source "hello" :row 1 :col 1
                              :message "fyi" :severity :info})]
      (is (str/includes? result "info"))
      (is (str/includes? result "\033[1;34m"))))
  (testing "note uses green"
    (let [result (ifu/render {:file "x.edn" :source "hello" :row 1 :col 1
                              :message "btw" :severity :note})]
      (is (str/includes? result "note"))
      (is (str/includes? result "\033[1;32m")))))

(deftest multiline-test
  (let [result (ifu/render {:file "test.edn"
                            :source "aaa\nbbb\nccc\nddd"
                            :row 2 :col 1 :end-row 3 :end-col 4
                            :message "spans two lines"})]
    (testing "contains both source lines"
      (is (str/includes? result "bbb"))
      (is (str/includes? result "ccc")))
    (testing "contains pointer on last line"
      (is (str/includes? result "^^^")))))

(deftest related-test
  (let [result (ifu/render {:file "a.edn" :source "foo bar" :row 1 :col 5 :end-col 8
                            :message "bad value"
                            :related [{:file "b.edn" :source "baz qux" :row 1 :col 1 :end-col 4
                                       :message "defined here"}]})]
    (testing "contains related file"
      (is (str/includes? result "b.edn:1:1")))
    (testing "contains related source"
      (is (str/includes? result "baz qux")))
    (testing "contains related message"
      (is (str/includes? result "defined here")))))
