(ns eu.cassiel.twizzle-test
  (:require [eu.cassiel.twizzle :as tw]
            [midje.sweet :refer :all]))

(facts "Channels"
       (fact "Unused channels return nil"
             (tw/sample (tw/initial) :WHATEVER) => nil)

       (fact "Can sample an initial channel."
             (tw/sample (tw/initial :init {:X 42}) :X) => 42))

(facts "Fades"
       (fact "Start of fade"
             (-> (tw/initial :init {:X 99})
                 (tw/automate-at :X 100 20 0)
                 (tw/locate 100)
                 (tw/sample :X))
             => (roughly 99))

       (fact "Simple fade"
             (-> (tw/initial)
                 (tw/automate-at :X 100 2 1.0)
                 (tw/locate 101)
                 (tw/sample :X))
             => (roughly 0.5))

       (fact "Floating point location fade"
             (-> (tw/initial)
                 (tw/automate-at :X 100 1 1.0)
                 (tw/locate 100.5)
                 (tw/sample :X))
             => (roughly 0.5))

       (fact "End of fade"
             (-> (tw/initial :init {:X 99})
                 (tw/automate-at :X 100 20 55)
                 (tw/locate 120)
                 (tw/sample :X))
             => (roughly 55))

       (fact "Past end of fade"
             (-> (tw/initial :init {:X 99})
                 (tw/automate-at :X 100 20 55)
                 (tw/locate 999)
                 (tw/sample :X))
             => (roughly 55))

       (fact "No effect from future fade"
             (-> (tw/initial)
                 (tw/automate-at :my-param 100 10 9.9)
                 (tw/locate 50)
                 (tw/sample :my-param))
             => nil)

       (fact "Beyond end of fade"
             (-> (tw/initial)
                 (tw/automate-at :my-param 100 10 9.9)
                 (tw/locate 150)
                 (tw/sample :my-param))
             => (roughly 9.9)))

(facts "Relative automation"
       (fact "Mid fade"
             (-> (tw/initial)
                 (tw/locate 100)
                 (tw/automate-in :my-param 10 10 100)
                 (tw/locate 115)
                 (tw/sample :my-param))
             => 50)

       (fact "Relative in past"
             (-> (tw/initial)
                 (tw/locate 100)
                 (tw/automate-in :my-param -20 10 3)
                 (tw/sample :my-param))
             => 3)

       (fact "Relative ahead"
             (-> (tw/initial :init {:my-param -3})
                 (tw/automate-in :my-param 10 10 3)
                 (tw/sample :my-param))
             => -3))

(facts "Location and purging"
       (fact "Purge"
             (-> (tw/initial)
                 (tw/automate-at :my-param 100 10 9.9)
                 (tw/locate 150)
                 (tw/locate 50)
                 (tw/sample :my-param))
             => (roughly 9.9))

       (fact "Multiple purge"
             (-> (tw/initial)
                 (tw/automate-at :X 100 10 99)
                 (tw/automate-at :X 105 10 88)
                 (tw/locate 120)
                 (tw/sample :X))
             => 88)

       (fact "Double locate within fade (1)"
             (-> (tw/initial)
                 (tw/automate-at :my-param 100 10 10.0)
                 (tw/locate 105)
                 (tw/locate 107.5)
                 (tw/sample :my-param))
             => (roughly 7.5))

       (fact "Double locate within fade (2)"
             (-> (tw/initial)
                 (tw/automate-at :my-param 100 10 10.0)
                 (tw/locate 105)
                 (tw/locate 102.5)
                 (tw/sample :my-param))
             => (roughly 2.5))

       (fact "Pre-inserted fade is chased"
             (-> (tw/initial)
                 (tw/locate 100)
                 (tw/automate-at :my-param 50 10 10.0)
                 (tw/sample :my-param))
             => (roughly 10.0))

       (fact "Pre-inserted fade is interpolated"
             (-> (tw/initial)
                 (tw/locate 105)
                 (tw/automate-at :my-param 100 10 10.0)
                 (tw/sample :my-param))
             => (roughly 5.0))

       (fact "Pre-inserted fades are chased"
             (-> (tw/initial)
                 (tw/locate 100)
                 (tw/automate-at :my-param 50 10 10.0)
                 (tw/locate 55)
                 (tw/sample :my-param))
             => (roughly 10.0)))

(facts "Short fades"
       (fact "Can handle fades with zero duration"
             (-> (tw/initial)
                 (tw/automate-at :my-param 50 0 1.0)
                 (tw/locate 51)
                 (tw/sample :my-param))
             => (roughly 1.0))

       (fact "Sample directly on zero-length fade"
             (-> (tw/initial)
                 (tw/automate-at :my-param 50 0 1.0)
                 (tw/locate 50)
                 (tw/sample :my-param))
             => (roughly 1.0))

       (fact "Start of unit fade"
             (-> (tw/initial)
                 (tw/automate-at :my-param 50 1 1.0)
                 (tw/locate 50)
                 (tw/sample :my-param))
             => (roughly 0.0))

       (fact "End of unit fade"
             (-> (tw/initial)
                 (tw/automate-at :my-param 50 1 1.0)
                 (tw/locate 51)
                 (tw/sample :my-param))
             => (roughly 1.0)))

(facts "Complex keys"
       (fact "Vector key"
             (-> (tw/initial :init {[:VOLUME 3] -6})
                 (tw/sample [:VOLUME 3]))
             => -6)

       (fact "Vector key, interp"
             (-> (tw/initial :init {[:VOLUME 3] 0}
                             :interp {[:VOLUME 3] (fn [_ _ p] p)})
                 (tw/automate-at [:VOLUME 3] 100 10 1)
                 (tw/locate 105)
                 (tw/sample [:VOLUME 3]))
             => 1/2)

       (fact "Group initialisation"
             (-> (tw/initial :init (reduce (fn [m k] (assoc m [:VOLUME k] -6))
                                           nil
                                           (range 10)))
                 (tw/sample [:VOLUME 6]))
             => -6)

       (fact "Group interp"
             (-> (tw/initial :interp (reduce (fn [m k] (assoc m
                                                        [:VOLUME k]
                                                        (fn [_ _ p] p)))
                                           nil
                                           (range 10)))
                 (tw/automate-at [:VOLUME 3] 100 10 1)
                 (tw/locate 105)
                 (tw/sample [:VOLUME 3]))
             => 1/2))

(facts "Clearing channels"
       (fact "Clear then post-locate"
             (-> (tw/initial)
                 (tw/automate-at :A 100 10 1.0)
                 (tw/clear :A)
                 (tw/locate 200)
                 (tw/sample :A))
             => nil)

       (fact "Post-locate then clear"
             (-> (tw/initial)
                 (tw/automate-at :A 100 10 1)
                 (tw/locate 200)
                 (tw/clear :A)
                 (tw/sample :A))
             => 1)

       (fact "Mid-fade locate then clear"
             (-> (tw/initial)
                 (tw/automate-at :A 100 10 1)
                 (tw/locate 105)
                 (tw/clear :A)
                 (tw/sample :A))
             => 1/2)

       (fact "Channels distinct"
             (as-> (tw/initial) S
                   (tw/automate-at S :A 100 10 1)
                   (tw/automate-at S :B 100 10 1)
                   (tw/clear S :A)
                   (tw/locate S 105)
                   (map #(tw/sample S %) [:A :B]))
             => [nil 1/2]))

(facts "Interpolation functions"
       (fact "Simple interpolation with init (use interpolation position as value)"
             (-> (tw/initial :init {:A 0}
                             :interp {:A (fn [start end pos] pos)})
                 (tw/automate-at :A 100 10 419)
                 (tw/locate 105)
                 (tw/sample :A))
             => (roughly 0.5))

       (fact "Simple interpolation, no init  (use interpolation position as value)"
             (-> (tw/initial :interp {:A (fn [start end pos] pos)})
                 (tw/automate-at :A 100 10 419)
                 (tw/locate 105)
                 (tw/sample :A))
             => (roughly 0.5))

       (fact "Complex interpolation"
             (letfn [(interp [as bs pos]
                       (->> (interleave as bs)
                            (partition 2)
                            (map (fn [[x y]] (+ x (* pos (- y x)))))))]
               (-> (tw/initial :init {:A [0 1 0]}
                               :interp {:A interp})
                   (tw/automate-at :A 100 10 [1 0 0])
                   (tw/locate 105)
                   (tw/sample :A)))
             => [1/2 1/2 0])

       (fact "Gating interpolator: in"
             (-> (tw/initial :interp {:A (constantly 1.0)})
                 (tw/automate-at :A 100 1 0.0)
                 (tw/locate 100)
                 (tw/sample :A))
             => (roughly 1.0))

       (fact "Gating interpolator: out"
             ;; Beyond the fade range, sample the declared target:
             (-> (tw/initial :interp {:A (constantly 1.0)})
                 (tw/automate-at :A 100 1 0.0)
                 (tw/locate 101)
                 (tw/sample :A))
             => (roughly 0.0)))
