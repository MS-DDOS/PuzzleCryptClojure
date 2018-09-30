(ns puzzle-crypt.core
  (:gen-class)
  (require [clojure.tools.cli :as cli])
  (require [clojure.java.io :as io])
  (require [lock-key.core :refer [decrypt decrypt-as-str decrypt-from-base64
                                   encrypt encrypt-as-base64]]))

(def secret "hello world!")
(def lock "password")
(def encrypted-value (encrypt secret lock))

(def cli-options
  [["-s" "--shards NUM" "The number of Shards you would like to split your file into."
    :default 1
    :parse-fn #(Integer/parseInt %)]])

(defn byte-seq
  "Similar to https://clojuredocs.org/clojure.core/line-seq, but is byte-oriented instead of line-oriented.
  Taken from https://gist.github.com/cjlarose/bf9f5004f18098fbe358"
  [^java.io.BufferedReader rdr]
  (lazy-seq
    (let [ch (.read rdr)]
      (if (= ch -1)
        '()
        (cons ch (byte-seq rdr))))))

(defn print-as-chunks
  [input-byte-seq n]
  (partition n n nil input-byte-seq))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (let [file (io/file (first arguments))
          file-input-stream (byte-seq (io/reader (first arguments)))
          fileLength (.length file)
          shards-needed (inc (long (/ fileLength (:shards options))))]
      (println (print-as-chunks file-input-stream shards-needed))
      (println "File length is:" fileLength)
      (println "Shards needed is:" shards-needed))

    (println args)
    (println options)
    (println arguments)
    (println errors)
    (println summary)
    (println encrypted-value)
    (println (decrypt encrypted-value lock))))