(ns puzzle-crypt.core
  (:gen-class)
  (require [clojure.tools.cli :as cli])
  (require [clojure.java.io :as io])
  (require [lock-key.core :refer [decrypt decrypt-as-str decrypt-from-base64
                                   encrypt encrypt-as-base64]]))

(def byte-chunk-padding 0)

(def cli-options
  [["-s" "--shards NUM" "The number of Shards you would like to split your file into."
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-k" "--key STRING" "The key to use for encrypting each shard."]])

(defn byte-seq
  "Similar to https://clojuredocs.org/clojure.core/line-seq, but is byte-oriented instead of line-oriented.
  Taken from https://gist.github.com/cjlarose/bf9f5004f18098fbe358"
  [^java.io.BufferedReader rdr]
  (lazy-seq
    (let [ch (.read rdr)]
      (if (= ch -1)
        '()
        (cons ch (byte-seq rdr))))))

(defn partition-byte-seq
  "Partitions a byte-seq into a maximum of n sub-sequences."
  [input-byte-seq n]
  (partition n n (repeat byte-chunk-padding) input-byte-seq))

(defn encrypt-byte-chunks
  "Encrypts a partitioned sequence of bytes."
  [byte-chunk-coll key]
  (let [byte-chunk-arrays (map #(into-array Byte/TYPE %) byte-chunk-coll)]
    (map encrypt-as-base64 byte-chunk-arrays (repeat key))))

(defn encrypt-file-as-chunks
  "Shards a file into a given number of chunks and encrypts them."
  [file-path number-of-shards encryption-key]
  (let [file-length (.length (io/file file-path))
        file-input-stream (byte-seq (io/reader file-path))
        shard-size (long (Math/ceil (/ file-length number-of-shards)))
        chunks (partition-byte-seq file-input-stream shard-size)]
    (encrypt-byte-chunks chunks encryption-key)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (assert (:key options) "`--key` must be specified")
    (if errors
      (println errors)
      (println (encrypt-file-as-chunks (first arguments) (:shards options) (:key options))))))