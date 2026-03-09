(ns ifu.core
  (:require [clojure.string :as str]
            [edamame.core :as eda]))

(defn colorize [s code]
  (str "\033[" code "m" s "\033[0m"))

(def ^:private severities
  {:error   ["error"   "1;31" "31"]
   :warning ["warning" "1;33" "33"]
   :info    ["info"    "1;34" "34"]
   :note    ["note"    "1;32" "32"]})

(defn source-line [source row]
  (nth (str/split source #"\n" -1) (dec row) ""))

(defn pointer [col len]
  (str (apply str (repeat (dec col) \space)) (apply str (repeat (max 1 len) \^))))

(defn- lpad [n width]
  (let [s (str n)] (str (apply str (repeat (- width (count s)) \space)) s)))

(defn- source-block
  "Returns [gutter-and-source-lines pointer-line] for a span."
  [source row col end-row end-col gw │ pc]
  (let [all-lines (str/split source #"\n" -1)
        end-row   (or end-row row)
        margin    (apply str (repeat (+ gw 2) \space))
        last-l    (nth all-lines (dec end-row) "")
        pcol      (if (= end-row row) col 1)
        ptr       (colorize (pointer pcol (- (or end-col (inc (count last-l))) pcol)) pc)]
    [(for [r (range row (inc end-row))
           line [(str margin │)
                 (str " " (lpad r gw) " " │ " " (nth all-lines (dec r) ""))]]
       line)
     (str margin │ " " ptr)]))

(defn render
  [{:keys [file source row col end-row end-col message hint severity related]}]
  (let [[label lc pc]    (severities (or severity :error))
        gw               (count (str (or end-row row)))
        │                (colorize "│" "36")
        [src-lines ptr]  (source-block source row col end-row end-col gw │ pc)]
    (str/join "\n"
      (concat
        ;; error: unexpected value
        ;;   --> test.edn:2:5
        [(str (colorize label lc) ": " (colorize message "1"))
         (str (colorize "  -->" "36") " " file ":" row ":" col)]
        ;;    │
        ;;  2 │ bar baz
        src-lines
        ;;    │     ^^^ expected keyword
        [(str ptr (when hint (str " " hint)))]
        ;;   ::: other.edn:5:3
        ;;      │
        ;;    5 │ (def x ...)
        ;;      │ ^^^ defined here
        (mapcat
          (fn [{rf :file rs :source rr :row rc :col rec :end-col rm :message}]
            (let [[rl rp] (source-block rs rr rc nil rec (count (str rr)) │ pc)]
              (concat
                [(str (colorize "  ::: " "36") rf ":" rr ":" rc)]
                rl
                [(str rp " " rm)])))
          related)
        [""]))))

(defn meta-at
  "Follow a path through a parsed structure, returning the form at
  that path (with its metadata)."
  [parsed path]
  (reduce
    (fn [form seg]
      (cond
        (and (map? form) (keyword? seg)) (get form seg)
        (and (sequential? form) (int? seg)) (nth form seg nil)
        :else (reduced nil)))
    parsed path))

(defn diagnostics
  "Transducer from {:in path :message string} errors to ifu diagnostic
  maps. parsed should carry source-position metadata (e.g. from
  edamame with {:all true})."
  [parsed source file]
  (map
    (fn [{:keys [in message]}]
      (let [form   (when (seq in) (meta-at parsed in))
            parent (when (> (count in) 1) (meta-at parsed (butlast in)))
            m      (or (meta form) (meta parent) (meta parsed))]
        {:file    file
         :source  source
         :row     (or (:row m) 1)
         :col     (or (:col m) 1)
         :end-col (:end-col m)
         :message message}))))

(defn parse
  "Parse an EDN string and validate it. Returns parsed data if valid,
  throws ex-info with rendered diagnostics if not.

  validator is (fn [data] -> [{:in [:path] :message \"...\"}] | nil)."
  [source file validator]
  (let [parsed (eda/parse-string source {:all true})
        errors (validator parsed)
        diags  (into [] (diagnostics parsed source file) errors)]
    (if (seq diags)
      (throw (ex-info (str/join "\n" (map render diags))
                      {:diagnostics diags}))
      parsed)))
