(ns puzzle-crypt.core
  (:gen-class)
  (require [clojure.tools.cli :as cli])
  (require [clojure.java.io :as io])
  (require [byte-streams :refer [to-byte-array possible-conversions]])
  (require [lock-key.core :refer [decrypt decrypt-as-str decrypt-from-base64
                                  encrypt encrypt-as-base64]])
  (:import (java.io InputStream)))

(def byte-chunk-padding 0)
(def cli-options
  [["-s" "--shards NUM" "The number of Shards you would like to split your file into."
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-k" "--key STRING" "The key to use for encrypting each shard."]])

(def convert-or-propagate (fn [char]
   (try
     (into-array Byte/TYPE char)
     (catch Exception e
       (println (str "Caught exception:" (.getMessage e) " " (apply max char)))))))

(defn byte-seq
  "Similar to https://clojuredocs.org/clojure.core/line-seq, but is byte-oriented instead of line-oriented.
  Taken from https://gist.github.com/cjlarose/bf9f5004f18098fbe358"
  [^InputStream rdr]
  (lazy-seq
    (let [ch (unchecked-byte (.read rdr))]
      (if (= ch -1)
        '()
        (cons ch (byte-seq rdr))))))

(defn partition-byte-seq
  "Partitions a byte-seq into a maximum of n sub-sequences."
  [input-byte-seq file-length n]
  (let [chunk-size (long (Math/ceil (/ file-length n)))
        padding (- (* n chunk-size) file-length)
        padded-input-byte-seq (lazy-cat input-byte-seq (repeat padding byte-chunk-padding))]
    (partition chunk-size padded-input-byte-seq)))

(defn encrypt-byte-chunks
  "Encrypts a partitioned sequence of bytes."
  [byte-chunk-coll key]
  (let [byte-chunk-arrays (map convert-or-propagate byte-chunk-coll)]
    (pmap encrypt-as-base64 byte-chunk-arrays (repeat key))))

(defn encrypt-file-as-chunks
  "Shards a file into a given number of chunks and encrypts them."
  [file-path number-of-shards encryption-key]
  (let [file (io/file file-path)
        file-length (.length file)
        file-input-stream (byte-seq (byte-streams/to-input-stream file))
        chunks (partition-byte-seq file-input-stream file-length number-of-shards)]
    (encrypt-byte-chunks chunks encryption-key)))

;; We should explicitly drop the padding, not try and filter based on content.
(defn filter-zeroes
  [coll]
  (take-while #(not (= % 0)) coll))
;;doing this will limit the data types we can use.

(defn decrypt-string-chunks
  [string-chunks decryption-key]
  (let [decrypted-string-chunks (map decrypt-from-base64 string-chunks (repeat decryption-key))
        byte-chunks (map convert-or-propagate decrypted-string-chunks)]
    (map filter-zeroes byte-chunks)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (assert (:key options) "`--key` must be specified")
    (if errors
      (println errors)
      (time (doall (encrypt-file-as-chunks (first arguments) (:shards options) (:key options)))))))