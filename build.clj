(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'io.github.socksy/ifu)
(def version (format "0.2.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]
                :pom-data [[:licenses
                            [:license
                             [:name "MPL-2.0"]
                             [:url "https://www.mozilla.org/en-US/MPL/2.0/"]]]]
                :scm {:url "https://github.com/socksy/ifu"
                      :connection "scm:git:git://github.com/socksy/ifu.git"
                      :developerConnection "scm:git:ssh://git@github.com/socksy/ifu.git"
                      :tag (str "v" version)}})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (println (str "Built " jar-file))
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn deploy [_]
  (jar nil)
  (dd/deploy {:installer :remote
              :artifact (b/resolve-path jar-file)
              :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))
