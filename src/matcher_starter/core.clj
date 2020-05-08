(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
(def state1 '#{(at lift fifth)
               (at Person ground)
               ;(going Person)

               (waiting Person false)
               (moving lift false)
               (contains lift nil)})
(def world
  '#{

     ;(selected lift ground)

     (container  lift)
     (agent Person)

     (place ground)
     (place first)
     (place second)
     (place third)
     (place fourth)
     (place fifth)
     ;(place destination)
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


(def ops-alex
  '{call-lift {
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
    going-up{ :pre((container ?lift)
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
    going-down{:pre((container ?lift)
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
    going-up-filled{:pre((container ?lift)
                          (agent ?P)
                          (contains ?lift ?P)
                          (moving ?lift true)
                          (at ?lift ?floor)
                          (above ?floor ?above))
                     :add((at ?lift ?above)
                          (at ?P ?above))
                     :del((at ?lift ?floor)
                          (at ?P ?floor))
                     :txt(?lift moves ?P up from ?floor to ?above)
                     :cmd(ascend person)

                     }
    going-down-filled{:pre((container ?lift)
                            (agent ?P)
                            (contains ?lift ?P)
                            (moving ?lift true)
                            (at ?lift ?floor)
                            (above ?floor ?below))
                       :add((at ?lift ?below)
                            (at ?P ?below))
                       :del((at ?lift ?floor)
                            (at ?P ?floor))
                       :txt(?lift moves ?P down from ?floor to ?below)
                       :cmd(descend person)

                       }
    stop-lift{:pre((container ?lift)
                    (moving ?lift true))
               :add((moving ?lift false))
               :del((moving ?lift true))
               :txt(?lift has stopped moving)
               :cmd(stop lift)

          }


    wait-called{
                 :pre((container ?lift)
                      (agent ?P)
                      (waiting ?P false)
                      (at ?lift ?floor)
                      (at ?P ?p-floor)
                      (moving ?lift true)

                  )
                 :add((waiting ?P true))
                 :del((waiting ?P false))
                 :txt(waiting for ?lift to reach ?p-floor floor)
                 :cmd(waiting at ?selected)

             }
    enter{
           :pre(

                (container ?lift)
                (agent ?person)
                (contains ?lift nil)
                (at ?person ?p-floor)
                (at ?lift ?p-floor)
                (moving ?lift false)
                )
           :add((contains ?lift ?person)
                )
           :del((contains ?lift nil)
                )
           :txt(?person entered ?lift)
           :cmd(enter ?lift)

           }

    select-floor{
                  :pre(
                       (agent ?person)
                       (container ?lift)
                       (contains ?lift ?person)
                       (moving ?lift false))
                  :add((moving ?lift true))
                  :del((moving ?lift false))
                  :txt(floor selected)
                  :cmd(select floor)
                  }
    wait-selected{
                   :pre(
                           (agent ?person)
                           (container ?lift)
                        (contains ?lift ?person)
                           (moving ?lift true)
                           (waiting ?person false))
                   :add((waiting ?person true))
                   :del((waiting ?person false))
                   :txt(waiting to reach selected floor)
                   :cmd(wait in lift)
                   }
    exit{
          :pre(
               (container ?lift)
               (agent ?person)
               (contains ?lift ?person)
               (at ?person ?p-floor)
               (at ?lift ?p-floor)
               (moving ?lift false))
          :add((contains ?lift nil))
          :del((contains ?lift ?person))
          :txt(?person exited ?lift)
          :cmd(exit ?lift)
          }
    }
  )
