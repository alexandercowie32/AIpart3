(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(def start-state
  '#{(at lift ground)
     (at Person fifth)
     (waiting Person false)
     (moving lift false)
     (contains lift nil)})

(def world
  '#{
     (container  lift)
     (agent Person)
     (opening doors false)
     (place ground)
     (place first)
     (place second)
     (place third)
     (place fourth)
     (place fifth)
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
     }
  )

;probably could consolidate the moving ups and the moving downs,
;regardless of occupancy
(def ops
  '{call-lift {;call the lift to the floor the agent is on
                :pre ((agent ?P)
                      (container ?lift)
                      (moving lift false)
                      (at ?lift ?floor)
                      (at ?P ?p-floor))
                :add((moving ?lift true))
                :del((moving ?lift false))
                :txt(?P called ?lift from ?p-floor)
                :cmd(call ?lift)
                }
    going-up{;moving the lift when it is not occupied
             :pre((container ?lift)
                  (contains ?lift nil)
                  (agent ?P)
                  (waiting ?P true)
                  (moving ?lift true)
                  (at ?lift ?floor)
                  (above ?floor ?above))
             :add((at ?lift ?above))
             :del((at ?lift ?floor))
             :txt(?lift moves up from ?floor to ?above)
             :cmd(ascend lift)
              }
    going-down{;moving the lift when not occupied
               :pre((container ?lift)
                    (contains ?lift nil)
                    (agent ?P)
                    (waiting ?P true)
                    (moving ?lift true)
                    (at ?lift ?floor)
                    (below ?floor ?below))
               :add((at ?lift ?below))
               :del((at ?lift ?floor))
               :txt(?lift moves down from ?floor to ?below)
               :cmd(descend lift)
                }
    going-up-filled{;moving the occupant up one floor
                    :pre((container ?lift)
                         (agent ?P)
                         (contains ?lift ?P)
                         (moving ?lift true)
                         (waiting ?P true)
                         (at ?lift ?floor)
                         (above ?floor ?above))
                    :add((at ?lift ?above)
                         (at ?P ?above))
                    :del((at ?lift ?floor)
                         (at ?P ?floor))
                    :txt(?lift moves ?P up from ?floor to ?above)
                    :cmd(ascend person)
                    }
    going-down-filled{;moving the occupant down one floor
                      :pre((container ?lift)
                           (agent ?P)
                           (waiting ?P true)
                           (contains ?lift ?P)
                           (moving ?lift true)
                           (at ?lift ?floor)
                           (below ?floor ?below))
                       :add((at ?lift ?below)
                            (at ?P ?below))
                       :del((at ?lift ?floor)
                            (at ?P ?floor))
                       :txt(?lift moves ?P down from ?floor to ?below)
                       :cmd(descend person)
                       }
    stop-lift{;stop the lift moving
              :pre((container ?lift)
                    (moving ?lift true))
               :add((moving ?lift false))
               :del((moving ?lift true))
               :txt(?lift has stopped moving)
               :cmd(stop lift)
          }
    wait-called{;wait for the lift to reach the floor the agent is on
                 :pre((container ?lift)
                      (agent ?P)
                      (waiting ?P false)
                      (at ?lift ?floor)
                      (at ?P ?p-floor)
                      (moving ?lift true))
                 :add((waiting ?P true))
                 :del((waiting ?P false))
                 :txt(waiting for ?lift to reach ?p-floor floor)
                 :cmd(waiting at ?p-floor)
             }
    enter{;person enters the lift
           :pre((container ?lift)
                (agent ?person)
                (contains ?lift nil)
                (at ?person ?p-floor)
                (at ?lift ?p-floor)
                (waiting ?person true)
                (moving ?lift false)
                (opening ?doors true))
           :add((contains ?lift ?person)
                (waiting ?person false))
           :del((contains ?lift nil)
                (waiting ?person true))
           :txt(?person entered ?lift)
           :cmd(enter ?lift)
           }
    select-floor{;person selects the floor and lift starts moving
                  :pre((agent ?person)
                       (container ?lift)
                       (contains ?lift ?person)
                       (moving ?lift false))
                  :add((moving ?lift true))
                  :del((moving ?lift false))
                  :txt(floor selected)
                  :cmd(select floor)
                  }
    wait-selected{;person waits for lift to take them to their floor
                   :pre((agent ?person)
                        (container ?lift)
                        (contains ?lift ?person)
                        (moving ?lift true)
                        (waiting ?person false))
                   :add((waiting ?person true))
                   :del((waiting ?person false))
                   :txt(waiting to reach selected floor)
                   :cmd(wait in lift)
                   }
    exit{;person exits the lift
          :pre((container ?lift)
               (agent ?person)
               (contains ?lift ?person)
               (waiting ?person true)
               (at ?person ?p-floor)
               (at ?lift ?p-floor)
               (moving ?lift false)
               (opening ?doors true))
          :add((contains ?lift nil)
               (waiting ?person false))
          :del((contains ?lift ?person)
               (waiting ?person true))
          :txt(?person exited ?lift)
          :cmd(exit ?lift)
          }

    ;lift doors open
    doors-open{
               :pre ((container ?lift)
                     (moving ?lift false)
                     (opening ?doors false))
               :add (opening ?doors true)
               :del (opening ?doors false)
               :txt (opening ?lift doors)
               :cmd (opening doors)
               }

    ;lift doors close
    doors-closed{
                 :pre ((container ?lift)
                       (moving ?lift false)
                       (opening ?doors true))
                 :add (opening ?doors false)
                 :del (opening ?doors true)
                 :txt (closing ?lift doors)
                 :cmd (closing doors)
                 }

    }
  )
