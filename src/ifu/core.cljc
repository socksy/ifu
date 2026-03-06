(ns ifu.core
  (:require [clojure.string :as str]))

(defn colorize [s code]
  (str "\033[" code "m" s "\033[0m"))

(defn source-line [source row]
  (nth (str/split source #"\n" -1) (dec row) ""))

(defn pointer [col len]
  (str (apply str (repeat (dec col) \space)) (apply str (repeat (max 1 len) \^))))

(defn render
  "Render a miette-style diagnostic from a map with keys:
   :file, :source, :row, :col, :end-col, :message, :hint"
  [{:keys [file source row col end-col message hint]}]
  (let [line (source-line source row)
        gw (count (str row))]
    (str (colorize "error" "1;31") ": " (colorize message "1") "\n"
         (colorize "  -->" "36") " " file ":" row ":" col "\n"
         (apply str (repeat (+ gw 2) \space)) (colorize "│" "36") "\n"
         " " row " " (colorize "│" "36") " " line "\n"
         (apply str (repeat (+ gw 2) \space)) (colorize "│" "36") " "
         (colorize (pointer col (- (or end-col (inc col)) col)) "31")
         (when hint (str " " hint)) "\n")))
