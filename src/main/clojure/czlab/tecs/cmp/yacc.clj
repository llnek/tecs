;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.tecs.cmp.yacc

  (:require [czlab.basal.format :as f]
            [czlab.basal.log :as log]
            [czlab.basal.core :as c]
            [czlab.basal.io :as i]
            [clojure.java.io :as io]
            [czlab.basal.str :as s]
            [clojure.string :as cs])

  (:use [clojure.walk])

  (:import [java.io StringWriter File LineNumberReader]
           [java.util.concurrent.atomic AtomicInteger]
           [czlab.tecs.p11 Node ASTNode ASTGentor]
           [czlab.basal.core GenericMutable]
           [java.net URL]
           [java.util Stack ArrayList List Map]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private ^AtomicInteger static-cntr (AtomicInteger.))
(def ^:private ^AtomicInteger class-cntr (AtomicInteger.))
(def ^:private static-vars (GenericMutable. {}))
(def ^:private class-vars (GenericMutable. {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- printj "" [^ASTNode x]
  (let [w (StringWriter.)
        ;_ (.dumpXML x w)
        _ (.dumpEDN x w)]
    (c/do-with
      [s (.toString w)] (c/prn!! s))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(declare toAST)
(defn- convObj "" [obj]
  (cond
    (c/ist? Node obj)
    (toAST obj)
    (c/ist? List obj)
    (mapv #(convObj %) obj)
    :else (str obj)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- toAST "" [^ASTNode node]
  (let [v (.jjtGetValue node)
        k (.toString node)
        stag (keyword k)
        props (.-props node)
        nested (.-nested node)
        hasC? (pos? (.jjtGetNumChildren node))
        ctx (GenericMutable. {:tag stag})]
    (when (some? v)
      (->> (convObj v)
           (c/setf! ctx :value)))
    ;;handle props
    (when (pos? (.size props))
      (->> (c/preduce<map>
             #(let [[k v] %2
                    kee (keyword k)]
                (assoc! %1
                        kee (convObj v))) props)
           (c/setf! ctx :attrs)))
    ;;handle nested
    (when (pos? (.size nested))
      (c/preduce<map>
        (fn [sum [k v]]
           (->>
             (convObj v)
             (c/setf! ctx (keyword k))) sum) nested))
    (when hasC?
      (loop [len (.jjtGetNumChildren node)
             pos 0
             nodes []]
        (if-not (< pos len)
          (c/setf! ctx :children nodes)
          (->> (toAST (.jjtGetChild node pos))
               (conj nodes)
               (recur len (inc pos))))))
    (deref ctx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(declare eval-expr eval-term)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- speek "" [^Stack s]
  (if-not (.empty s) (.peek s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- spop "" [^Stack s]
  (if-not (.empty s) (.pop s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- fakeUnary "" [unary]
  (doto {:tag :OP :value unary :rank 99 :bind "right"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-op ""
  [{:keys [value rank bind] :as node} ^List out ^Stack stk]

  (let [top (speek stk)]
    (if (and (some? top)
             (not= "(" (:value top))
             (or (< rank (:rank top))
                 (and (= rank (:rank top))
                      (= "left" bind))))
      (do
        (.add out (.pop stk))
        (eval-op node out stk))
      (.push stk node))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;unwind the stack until we find the opening "(" during
;;which operators are popped off to the output queue
(defn- eval-group "" [group ^List out ^Stack stk]
  (eval-expr group out stk)
  (loop [top (speek stk)]
    (let [{:keys [tag value]} top]
      (cond
        (nil? top) (c/trap! Exception "bad group")
        (and (= :OP tag)
             (= "(" value))
        (.pop stk)
        :else
        (do (.add out (.pop stk))
            (recur (speek stk) ))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-unary "" [unary term ^List out ^Stack stk]
  (eval-term term out stk)
  (loop [top (speek stk)]
    (let [{:keys [tag value]} top]
      (cond
        (nil? top) (c/trap! Exception "bad unary")
        (and (= :OP tag)
             (= "(" value))
        (do (.pop stk)
            (.add out (fakeUnary unary)))
        :else
        (do (.add out (.pop stk))
            (recur (speek stk) ))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-subr-call ""
  [{:keys [target params] :as node} ^List out ^Stack stk]
  (let [[z m](.split ^String target "\\.")]
    (c/prn!! "z = %s" z)
    (c/prn!! "m = %s" m)
    (doseq [e params]
      (eval-expr e out stk))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- fakeGroup-B "" []
  (doto {:tag :OP :value "("}))
(defn- fakeGroup-E "" []
  (doto {:tag :OP :value ")"}))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-term ""
  [{:keys [literal group call
           unary varr index] :as node} ^List out ^Stack stk]
  (c/prn!! "term=%s" node)
  (cond
    (some? unary)
    (do (.push stk (fakeGroup-B))
        (eval-unary unary (:term node) out stk))
    (some? literal)
    (.add out node)
    (some? varr)
    (do (.add out node)
        (if (some? index)
          (eval-expr index out stk)))
    (some? group)
    (do (.push stk (fakeGroup-B))
        (eval-group group out stk))
    (some? call)
    (eval-subr-call call out stk)
    :else nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-expr "" [expr out stk]
  (doseq [c (:children expr)
          :let [tag (:tag c)]]
    (condp = tag
      :Term (eval-term c out stk)
      :OP (eval-op c out stk)
      (c/trap! Exception "bad expr"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-do-stmt "" [{:keys [call] :as stmt}]
  (let [out (ArrayList.)
        stk (Stack.)]
    (eval-subr-call call out stk)
    (doseq [x out]
      (c/prn!! "do= %s" x))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- eval-let-stmt "" [{:keys [varr lhs rhs] :as stmt}]
  (c/prn!! "varr = %s" varr)
  (if (some? lhs)
    (let [out (ArrayList.)
          stk (Stack.)]
      (eval-expr lhs out stk)
      (doseq [x out]
        (c/prn!! "lhs= %s" x))))
  (if (some? rhs)
    (let [out (ArrayList.)
          stk (Stack.)]
      (eval-expr rhs out stk)
      (doseq [x out]
        (c/prn!! "rhs= %s" x)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- doFuncVar "" [{:keys [attrs statements] :as node}]
  (let [{:keys [args name type
                qualifier vars]}
        attrs
        pc (AtomicInteger.)
        lc (AtomicInteger.)
        px (GenericMutable. {})
        lx (GenericMutable. {})
        args (partition 2 args)
        vars (partition 2 vars)]
    (c/prn!! "doFuncVar %s" name)
    (doseq [s statements
            :let [t (:tag s)]]
      (cond
        (= :WhileStatement t) nil
        (= :LetStatement t)
        (eval-let-stmt s)
        (= :DoStatement t)
        (eval-do-stmt s)
        (= :IfStatement t) nil
        (= :ReturnStatement t) nil ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- doClassVar "" [{:keys [attrs] :as node}]
  (let [{:keys [type
                vars
                qualifier]} attrs
        dft {:type type}
        [ctx ^AtomicInteger ctr]
        (condp = qualifier
          "static" [static-vars static-cntr]
          "field"  [class-vars class-cntr]
          (c/trap! Exception "bad qualifier"))]
    (if (string? vars)
      (->> {:index (.getAndIncrement ctr)}
           (merge dft)
           (c/setf! ctx vars))
      (doseq [s (seq vars)]
        (->> {:index (.getAndIncrement ctr)}
             (merge dft)
             (c/setf! ctx s))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- compilej "" [^ASTNode x]
  (let [root (toAST x)
        s (f/writeEdnStr root)]
    (doseq [c (:children root)
            :let [tag (:tag c)]]
      (cond
        (= :ClassVarDec tag)
        (doClassVar c)
        (= :SubroutineDec tag)
        (doFuncVar c)))
    (c/prn!! (f/writeEdnStr @static-vars))
    (c/prn!! (f/writeEdnStr @class-vars))
    s))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- tokenj "" [furl]
  (with-open [inp (.openStream ^URL furl)]
    (let [p (ASTGentor. inp)]
      (c/prn!! "Parsing file...")
      (ASTGentor/parseOneUnit))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- compFile ""
  [fp outDir]
  (c/prn!! "Processing file: %s" fp)
  (c/do-with
    [out (->> (io/as-url fp) tokenj compilej)]
    (let [nm (.getName ^File fp)
          t (io/file outDir
                     (str nm ".clj"))]
      (c/prn!! "Writing file: %s" t)
      (spit t out))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- scanDir "" [dir out]
  (doseq [f (i/listFiles dir ".jack")]
    (compFile f out)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn -main "" [& args]

  (let [[src des & more] args]
    (if (and (s/hgl? src)
             (s/hgl? des)
             (empty? more))
      (try
        (let [f (io/file src)
              d (io/file des)]
          (if (.isDirectory f)
            (scanDir f d)
            (compFile f d)))
        (catch Throwable e
          (.printStackTrace e)))
      (c/prn!! "Usage: cmp <jack-file> <out-dir>"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF



