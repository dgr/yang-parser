(ns parse.transform
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.string :as str]
    [instaparse.core :as insta]
    [schema.core :as s]
    [tupelo.core :as t]
    [tupelo.misc :as tm]
    [tupelo.schema :as tsk]
    [tupelo.string :as ts]
    ))
(t/refer-tupelo)


; An identifier MUST NOT start with (('X'|'x') ('M'|'m') ('L'|'l')) (i.e. 'xml' in any case)
(defn fn-identifier [& args]
  (let [result  (str/join args)
        first-3 (str/lower-case (strcat (take 3 result))) ]
    (when (= "xml" first-3)
      (throw (IllegalArgumentException. (format "Identifier cannot begin with 'xml': %s " result))))
    [:identifier result] ))


(defn fn-string [& args] [:string (tm/collapse-whitespace (str/join args))] )
(defn fn-boolean [arg] (java.lang.Boolean. arg))


(defn yang-transform
  [parse-tree]
  (insta/transform
    {
     :string-compound str
     :string-simple   str
     :string          fn-string
     :identifier      fn-identifier
     :boolean         fn-boolean
     :namespace       (fn fn-namespace [arg] [:namespace arg])
     :prefix          (fn fn-prefix [arg] [:prefix arg])
     :organization    (fn fn-organization [arg] [:organization arg])
     :contact         (fn fn-contact [arg] [:contact arg])
     :description     (fn fn-description [arg] [:description arg])
     :presence        (fn fn-presence [arg] [:presence arg])
     :revision        (fn fn-revision [& args]
                        (prepend :revision args))
     :iso-date        (fn fn-iso-date [& args]
                        [:iso-date (str/join args)])
     :reference       (fn fn-reference [arg] [:reference arg])
     :identity        (fn fn-identity [& args]
                        (prepend :identity args))
     :typedef         (fn fn-typedef [& args]
                        (prepend :typedef args))
     :container       (fn fn-container [& args]
                        (spy :100 args)
                        (prepend :container args))


     :base            (fn fn-base [arg] [:base arg])

     :error-message   (fn fn-description [arg] [:error-message (tm/collapse-whitespace arg)])
     :length          (fn fn-length [arg] [:length arg])

     :enum-simple     (fn fn-enum-simple [& args] [:enum [:name (first args)]])
     :enum-composite  (fn fn-enum-composite [& args]
                        (let [name    (first args)
                              content (rest args)]
                          (t/prepend :enum [:name name] content)))

     :type-simple     (fn fn-type-simple [& args]
                        (spy :fn-type-simple args)
                        (t/prepend :type args))
     :type-composite  (fn fn-type-composite [& args]
                        (t/prepend :type args))
     :leaf            (fn fn-leaf [& args]
                        (spy :fn-leaf args)
                        (t/prepend :leaf args))
     } parse-tree))

