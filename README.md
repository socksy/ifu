# ifu

Miette-style diagnostic rendering for Clojure/ClojureScript. Takes a map describing a source location and renders a coloured, annotated snippet to the terminal.

```
error: unexpected value
  --> config.edn:2:5
   │
 2 │ bar baz
   │     ^^^ expected keyword
```

## Usage

Add `ifu` to your deps (not yet on clojars — use git dep for now):

```clojure
{:deps {io.github.yourname/ifu {:git/sha "..."}}}
```

Then call `render`:

```clojure
(require '[ifu.core :as ifu])

(println
  (ifu/render {:file    "config.edn"
               :source  "{:port \"abc\"}"
               :row     1
               :col     8
               :end-col 13
               :message "expected integer"
               :hint    "port must be a number"}))
```

## Diagnostic map

| Key | Required | Description |
|-----|----------|-------------|
| `:file` | yes | Filename shown in the header |
| `:source` | yes | Full source text (used to extract lines) |
| `:row` | yes | 1-based start line |
| `:col` | yes | 1-based start column |
| `:end-col` | no | End column (exclusive). Defaults to one past `:col` |
| `:end-row` | no | End line for multi-line spans. Defaults to `:row` |
| `:message` | yes | Error message shown in the header |
| `:hint` | no | Text shown after the pointer |
| `:severity` | no | `:error`, `:warning`, `:info`, or `:note`. Defaults to `:error` |
| `:related` | no | Vector of additional span maps (`:file`, `:source`, `:row`, `:col`, `:end-col`, `:message`) |

## Severity levels

```clojure
(ifu/render {:file "x.edn" :source "bad" :row 1 :col 1
             :message "hmm" :severity :warning})
;; warning: hmm
;;   --> x.edn:1:1
```

Each severity gets its own colour: error (red), warning (yellow), info (blue), note (green).

## Multi-line spans

```clojure
(ifu/render {:file "x.edn" :source "aaa\nbbb\nccc"
             :row 1 :col 1 :end-row 3 :end-col 4
             :message "spans three lines"})
```

Shows all lines in the range with the pointer on the last line.

## Related diagnostics

Point to other locations that help explain the error:

```clojure
(ifu/render {:file "a.edn" :source "foo bar" :row 1 :col 5 :end-col 8
             :message "type mismatch"
             :related [{:file "b.edn" :source "baz qux"
                        :row 1 :col 1 :end-col 4
                        :message "defined here"}]})
```

## Running tests

```
bb test
```
