(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  )

(def lift-state-1
  '#{(at lift ground)
     (at User fifth)
     (opening doors false)
     (waiting User false)
     (moving lift false)
     (occupied lift nil)})

(def lift-state-2
  '#{(at lift fifth)
     (at User ground)
     (opening doors false)
     (waiting User false)
     (moving lift false)
     (occupied lift nil)})


(def elevator-world
  '#{
     (elevator lift)
     (user Person)
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
                         (occupied ?lift nil)
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
                           (occupied ?lift nil)
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

    ;moving the elevator and user up one floor
    traverse-upward-occupied{
                             :pre((elevator ?lift)
                                  (user ?U)
                                  (occupied ?lift ?U)
                                  (moving ?lift true)
                                  (waiting ?U true)
                                  (at ?lift ?l-floor)
                                  (upwards ?l-floor ?above))
                             :add((at ?l-lift ?above)
                                  (at ?U ?above))
                             :del((at ?lift ?l-floor)
                                  (at ?U ?l-floor))
                             :txt(?lift moves upward from ?floor to ?above with ?U inside)
                             :cmd(traversing upwards user)
                             }

    ;moving the elevator and user down one floor
    traverse-downward-occupied{
                               :pre((elevator ?lift)
                                    (user ?U)
                                    (waiting ?U true)
                                    (occupied ?lift ?U)
                                    (moving ?lift true)
                                    (at ?lift ?l-floor)
                                    (downwards ?l-floor ?below))
                               :add((at ?lift ?below)
                                    (at ?U ?below))
                               :del((at ?lift ?l-floor)
                                    (at ?U ?l-floor))
                                :txt(?lift moves downward from ?floor to ?below with ?U inside)
                                :cmd(traversing downwards user)
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
                        (user ?U)
                        (occupied ?lift nil)
                        (at ?U ?u-floor)
                        (at ?lift ?u-floor)
                        (waiting ?U true)
                        (moving ?lift false)
                        (opening ?doors ?open)
                        (true? ?open))
                   :add((occupied ?lift ?U)
                        (waiting ?U false))
                   :del((occupied ?lift nil)
                        (waiting ?U true))
                   :txt(?user entered ?lift)
                   :cmd(enter ?lift)
                   }

    ;user selects the floor and elevator begins to move
    user-destination{
                     :pre((user ?U)
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
                       (user ?U)
                       (occupied ?lift ?U)
                       (waiting ?U true)
                       (at ?lift ?l-floor)
                       (moving ?lift false)
                       (opening ?doors true))
                  :add((occupied ?lift nil)
                       (at ?U ?l-floor)
                       (waiting ?U false))
                  :del((occupied ?lift ?person)
                       (waiting ?U true)
                       (at ?U ?u-floor))
                  :txt(?person exited ?lift at ?u-floor)
                  :cmd(exit ?lift)
                  }


    ;elevator doors open when user is ready to exit/enter lift
    doors-open{
               :pre ((elevator ?lift)
                     (moving ?lift false)
                     (opening ?doors false))
               :add (opening ?doors true)
               :del (opening ?doors false)
               :txt (opening ?lift doors)
               :cmd (opening doors)
               }

    ;elevator doors close once user has entered/exited lift
    doors-closed{
                 :pre ((elevator ?lift)
                       (moving ?lift false)
                       (opening ?doors true))
                 :add (opening ?doors false)
                 :del (opening ?doors true)
                 :txt (closing ?lift doors)
                 :cmd (closing doors)
                 }

    }
  )
