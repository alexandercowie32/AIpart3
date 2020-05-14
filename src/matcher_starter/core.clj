(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  )

(def lift-state-1
  '#{(at lift ground)
     (at Person fifth)
     (open doors false)
     (waiting Person false)
     (moving lift false)
     (occupied lift nil)})

(def lift-state-2
  '#{(at lift fifth)
     (at Person ground)
     (open doors false)
     (waiting Person false)
     (moving lift false)
     (occupied lift nil)})


(def elevator-world
  '#{
     (elevator lift)
     (user Person)
     (obstacle doors)
     (location ground)
     (location first)
     (location second)
     (location third)
     (location fourth)
     (location fifth)
     (upwards ground first)
     (upwards first second)
     (upwards second third)
     (upwards third fourth)
     (upwards fourth fifth)
     (downwards first ground)
     (downwards second first)
     (downwards third second)
     (downwards fourth third)
     (downwards fifth fourth)
     }
  )


(def elevator-operators
  '{
    ;request the elevator to the current floor called by the user
    request-elevator{
                     :pre ((user ?U)
                           (elevator ?lift)
                           (occupied ?lift nil)
                           (moving lift false)
                           (at ?lift ?l-floor)
                           (at ?U ?u-floor))
                     :add((moving ?lift true))
                     :del((moving ?lift false))
                     :txt(?U called ?lift from ?u-floor)
                     :cmd(request ?lift)
                     }

    ;moving the elevator upwards while it is not occupied
    traverse-upward{
                    :pre((elevator ?lift)
                         (user ?U)
                         (waiting ?U true)
                         (moving ?lift true)
                         (at ?lift ?l-floor)
                         (upwards ?l-floor ?above))
                    :add((at ?lift ?above))
                    :del((at ?lift ?l-floor))
                    :txt(?lift moves up from ?l-floor to ?above)
                    :cmd(traversing upwards lift)
                    }

    ;moving the elevator downwards while it is not occupied
    traverse-downward{
                      :pre((elevator ?lift)
                           (user ?U)
                           (waiting ?U true)
                           (moving ?lift true)
                           (at ?lift ?l-floor)
                           (downwards ?l-floor ?below))
                      :add((at ?lift ?below))
                      :del((at ?lift ?l-floor))
                      :txt(?lift moves down from ?l-floor to ?below)
                      :cmd(traversing downwards lift)
                      }





    ;stop the elevator from moving
    stop-elevator{
                  :pre((elevator ?lift)
                       (moving ?lift true))
                  :add((moving ?lift false))
                  :del((moving ?lift true))
                  :txt(?lift has stopped moving)
                  :cmd(stop lift)
                  }

    ;wait for the lift to reach the floor the user is on
    wait-called{
                :pre((elevator ?lift)
                     (user ?U)
                     (waiting ?U false)
                     (at ?lift ?l-floor)
                     (at ?U ?u-floor)
                     (moving ?lift true))
                :add((waiting ?U true))
                :del((waiting ?U false))
                :txt(waiting for ?lift to reach ?u-floor floor)
                :cmd(waiting at ?u-floor)
                }

    ;user enters the elevator
    enter-elevator{
                   :pre((elevator ?lift)
                        (obstacle ?doors)
                        (user ?U)
                        (occupied ?lift nil)
                        (at ?U ?u-floor)
                        (at ?lift ?u-floor)
                        (waiting ?U true)
                        (moving ?lift false)
                        (open ?doors true)
                        )
                   :add((occupied ?lift ?U)
                        (waiting ?U false))
                   :del((occupied ?lift nil)
                        (waiting ?U true))
                   :txt(?U entered ?lift)
                   :cmd(enter ?lift)
                   }

    ;user selects the floor and elevator begins to move
    user-destination{
                     :pre((user ?U)
                          (obstacle doors)
                          (open ?doors false)
                          (elevator ?lift)
                          (occupied ?lift ?U)
                          (moving ?lift false))
                     :add((moving ?lift true))
                     :del((moving ?lift false))
                     :txt(floor selected)
                     :cmd(select floor)
                     }

    ;user waits for elevator to take them to their desired location
    user-wait-selected{
                  :pre((user ?U)
                       (elevator ?lift)
                       (obstacle ?doors)
                       (open ?doors false)
                       (occupied ?lift ?U)
                       (moving ?lift true)
                       (waiting ?U false))
                  :add((waiting ?U true))
                  :del((waiting ?U false))
                  :txt(waiting to reach selected floor)
                  :cmd(wait in lift)
                  }

    ;user exits the elevator
    exit-elevator{
                  :pre((elevator ?lift)
                       (obstacle ?doors)
                       (user ?U)
                       (occupied ?lift ?U)
                       (waiting ?U true)
                       (at ?lift ?l-floor)
                       (at ?U ?u-floor)
                       (moving ?lift false)
                       (open ?doors true)
                       )
                  :add((occupied ?lift nil)
                       (at ?U ?l-floor)
                       (waiting ?U false))
                  :del((occupied ?lift ?person)
                       (waiting ?U true)
                       (at ?U ?u-floor))
                  :txt(?U exited ?lift at ?u-floor)
                  :cmd(exit ?lift)
                  }


    ;elevator doors open when user is ready to exit/enter lift
    doors-open{
               :pre ((elevator ?lift)
                     (obstacle ?doors)
                     (moving ?lift false)
                     (open ?doors false))
               :add ((open ?doors true))
               :del ((open ?doors false))
               :txt (open ?lift doors)
               :cmd (open doors)
               }

    ;elevator doors close once user has entered/exited lift
    doors-closed{
                 :pre ((elevator ?lift)
                       (obstacle ?doors)
                       (moving ?lift false)
                       (open ?doors true))
                 :add ((open ?doors false))
                 :del ((open ?doors true))
                 :txt (closing ?lift doors)
                 :cmd (closing doors)
                 }

    }
  )
