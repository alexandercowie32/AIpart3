(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  :dependencies[org.clojars.cognesence/ops-search])

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
(def world
  '#{
     (at lift fifth)
     (at Person ground)
     ;(going Person)
     (in Person nil)
     (waiting Person false)
     (moving lift false)
     (contains lift nil)
     ;(selected lift ground)
     (manipulable lift)
     (container  lift)
     (agent Person)
     (containable Person)
     (place destination)
     (above ground first)
     (above first second)
     (above second third)
     (above third fourth)
     (above fourth fifth)
     (below first ground)
     (below second first)
     (below third second)
     (below fourth third)
     (below fifth fourth)
     (place ground)
     (place first)
     (place second)
     (place third)
     (place fourth)
     (place fifth)
     }
  )


(def ops
  '{:call-lift {
                :pre ((agent ?P)
                      (manipulable ?lift)
                      (moving lift ?called)
                      (not ?called)
                      (at ?lift ?floor)
                      (at ?P ?p-floor)
                      (not (= ?floor ?p-floor))
                      )
                :add((moving ?lift true))
                :del((moving ?lift false))
                :txt(?P called ?lift from ?p-floor)
                :cmd(call ?lift)
                }
    :going-up{ :pre((manipulable ?lift)
                    (moving ?lift ?move)
                    (true? ?move)
                    (at ?lift ?floor)
                    (above ?floor ?above))
              :add((at ?lift ?above))
              :del((at ?lift ?floor))
              :txt(?lift moves up from ?floor to ?above)
              :cmd(ascend lift)

              }
    :going-down{:pre((manipulable ?lift)
                     (moving ?lift ?move)
                     (true? ?move)
                     (at ?lift ?floor)
                     (below ?floor ?below))
                :add((at ?lift ?below))
                :del((at ?lift ?floor))
                :txt(?lift moves down from ?floor to ?below)
                :cmd(descend lift)

                }
    :going-up-filled{:pre((manipulable ?lift)
                          (agent ?P)
                          (contains ?lift ?P)
                          (moving ?lift ?move)
                          (true? ?move)
                          (at ?lift ?floor)
                          (above ?floor ?above))
                     :add((at ?lift ?above)
                          (at ?P ?above))
                     :del((at ?lift ?floor)
                          (at ?P ?floor))
                     :txt(?lift moves ?P up from ?floor to ?above)
                     :cmd(ascend person)

                     }
    :going-down-filled{:pre((manipulable ?lift)
                            (agent ?P)
                            (contains ?lift ?P)
                            (moving ?lift ?move)
                            (true? ?move)
                            (at ?lift ?floor)
                            (above ?floor ?below))
                       :add((at ?lift ?below)
                            (at ?P ?below))
                       :del((at ?lift ?floor)
                            (at ?P ?floor))
                       :txt(?lift moves ?P down from ?floor to ?below)
                       :cmd(descend person)

                       }
    :stop-lift{:pre((manipulable ?lift)
                    (moving ?lift ?move)
                    (true? ?move))
               :add((moving ?lift false))
               :del((moving ?lift ?move))
               :txt(?lift has stopped moving)
               :cmd(stop lift)

          }

    :wait-called{
                 :pre((manipulable ?lift)
                      (agent ?P)
                      (waiting ?P ?wait)
                      (not ?wait)
                      (at ?lift ?floor)
                      (moving ?lift ?move)
                      (true? ?move)
                      (selected ?lift ?selected)
                  )
                 :add((waiting ?P true))
                 :del((at ?lift ?floor))
                 :txt(waiting for ?lift to reach ?selected floor)
                 :cmd(waiting at ?selected)

             }
    :enter{
           :pre((manipulable ?lift)
                (container ?lift)
                (containable ?person)
                ())
           }
    :select-floor
    :wait-selected
    :exit
    }
  )
