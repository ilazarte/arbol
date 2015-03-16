(ns arbol.internal)

; Seperated for testing purposes
; This api is not guaranteed to be stable, while core is (or should be!).
; Functions may appear or disappear at will between releases and will not affect semver.
(defn partition-every
  "adapted from clojure core version of partition-by
   partition a collection every time a predicate returns true
   => (partition-every keyword? [:a 1 2 3 :b :c 4 5 6 7 :d :e])
   ((:a 1 2 3) (:b) (:c 4 5 6 7) (:d) (:e))"
  [pred coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (let [fst (first s)
            run (cons fst (take-while (complement pred) (next s)))]
        (cons run (partition-every pred (seq (drop (count run) s))))))))

(def fnk? #(or (keyword? %) (fn? %)))

(defn read-selector
  "Read selector parses one step of the selector and
   returns a map with :axis :pred :rest keys
   Axis: first function or keyword in the selector.
   Pred: an ever-pred on all predicates available for that axis.
   A nil predicate indicates no tests are to be run on this path element.
   Rest: the remainder of the everything else besides the current axis/pred."
  [selector]
  (let [sels  (partition-every fnk? selector)   ; [:x number? [odd?]] -> ((:x) (:number [odd?]))
        sel   (first sels)                      
        path  (first sel)
        preds (rest sel)
        pred  (if (empty? preds) (constantly true) (apply every-pred (first preds)))
        rst   (vec (apply concat (rest sels)))]
    {:axis path
     :pred pred
     :rst  rst}))

(defn ispath? [axis mixed]
  (if (fn? axis) 
    (axis mixed)))

(comment 
  "The traverse function depends on two concepts, matching and descending.
   Descending is only the part where a selector axis has been matched so it is popped.
   Matching is only allowed when all selectors and predicates have been validated as true.
   Since the hierarchy can be N-deep, the final selector floats all the way down.")

(defn traverse
  "Recursively traverses structure, in place consumes stack.
   Vectors and sequentials preserve order.
   Maps lose any key order which shouldn't be depended on anyway.
   Each level first adds path information, and then applies test.
   If the selector is an empty vector, it matches root object and returns.
   The overloaded form should probably not be available as a public method.
   Note no predicates are allowed for the key/val itself in a map traversal.
   When the process arrives at the value, the predicates are tested - core types."
  ([mixed selector fx]
    (traverse mixed selector fx []))
  ([mixed selector fx loc]
    (let [parsed (read-selector selector)
          {:keys [axis rst pred]} parsed 
          islast (empty? rst)]
      (cond
        (empty? selector) ; this is a hack for root selector which specified as an empty selector.
        (fx mixed) 
        (map? mixed)
        (let [path      ::map
              nloc      (conj loc path)
              ispath    (ispath? axis mixed)
              ispred    (if ispath (pred mixed)) 
              matches   (and islast ispath ispred)
              descends  (and (not islast) ispath ispred)
              rselector (if descends rst selector) 
              mfx       #(assoc 
                           %1 
                           (first %2) 
                           (traverse 
                             (second %2) 
                             (let [kparsed   (read-selector rselector)
                                   kaxis     (:axis kparsed)
                                   krst      (:rst kparsed)
                                   kislast   (empty? krst)
                                   key       (first %2)
                                   kispath   (= kaxis key)
                                   kdescends (and (not kislast) kispath)
                                   kselector (if kdescends krst rselector)] 
                               kselector) 
                             fx 
                             (conj nloc (first %2))))
              res       (reduce mfx {} mixed)]
          (if matches (fx res) res))
        (set? mixed)
        (let [path      ::set
              nloc      (conj loc path)
              ispath    (ispath? axis mixed)
              ispred    (if ispath (pred mixed)) 
              matches   (and islast ispath ispred)
              descends  (and (not islast) ispath ispred)
              rselector (if descends rst selector)
              vfx       #(traverse % rselector fx nloc)
              res       (mapv vfx mixed)]
        (if matches (fx res) res))
        (vector? mixed)
        (let [path      ::vec
              nloc      (conj loc path)
              ispath    (ispath? axis mixed)
              ispred    (if ispath (pred mixed)) 
              matches   (and islast ispath ispred)
              descends  (and (not islast) ispath ispred)
              rselector (if descends rst selector)
              vfx       #(traverse % rselector fx nloc)
              res       (mapv vfx mixed)]
          (if matches (fx res) res))
        (sequential? mixed)
        (let [path      :seq
              nloc      (conj loc path)
              ispath    (ispath? axis mixed)
              ispred    (if ispath (pred mixed)) 
              matches   (and islast ispath ispred)
              descends  (and (not islast) ispath ispred)
              rselector (if descends rst selector)
              sfx       #(conj %1 (traverse %2 rselector fx nloc))
              nseq      (empty mixed)
              res       (reverse (reduce sfx nseq mixed))]
          (if matches (fx res) res))
        :else
        (let [path      ::scalar
              nloc      (conj loc path)
              ispath    (ispath? axis mixed)
              ispred    (if ispath (pred mixed)) 
              matches   (and islast ispath ispred)]
          (if matches (fx mixed) mixed))))))
