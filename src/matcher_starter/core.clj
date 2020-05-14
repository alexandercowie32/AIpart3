(ns matcher-starter.core
  (:require [org.clojars.cognesence.breadth-search.core :refer :all]
            [org.clojars.cognesence.matcher.core :refer :all]
            [org.clojars.cognesence.ops-search.core :refer :all])
  )
;version 1: first version

(def state-v1
  '#{(at lift ground)
     (at Person fifth)
     (waiting Person false)
     (moving lift false)
     (contains lift nil)})
(def world-v1
  '#{
     (container  lift)
     (agent Person)
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
(def ops-v1
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
               (moving ?lift false))
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
              (moving ?lift false))
         :add((contains ?lift nil)
              (waiting ?person false))
         :del((contains ?lift ?person)
              (waiting ?person true))
         :txt(?person exited ?lift)
         :cmd(exit ?lift)
         }
    }
  )

;version 2: adds doors to more accurately
;emulate a real lift/elevator
(def start-state
  '#{(at elevator ground)
     (at Person fifth)
     (waiting Person false)
     (moving elevator false)
     (contains elevator nil)
     (open door true)
     })
(def world
  '#{
     (container elevator)
     (obstacle door)
     (agent Person)
     (floor ground)
     (floor first)
     (floor second)
     (floor third)
     (floor fourth)
     (floor fifth)
     (moving-up ground first)
     (moving-up first second)
     (moving-up second third)
     (moving-up third fourth)
     (moving-up fourth fifth)
     (moving-down first ground)
     (moving-down second first)
     (moving-down third second)
     (moving-down fourth third)
     (moving-down fifth fourth)

     }
  )

;probably could consolidate the moving ups and the moving downs,
;regardless of occupancy

(def operators
  '{button-pressed {;emulates calling an elevator by an agent
                    :pre ((agent ?Person)
                          (obstacle ?door)
                          (container ?elevator)
                          (contains ?lift nil)
                          (moving elevator false)
                          (at ?elevator ?floor)
                          (open ?door false)
                          (at ?Person ?person-floor))
                    :add((moving ?elevator true))
                    :del((moving ?elevator false))
                    :txt(?Person called ?elevator from ?person-floor floor.)
                    :cmd(call ?elevator)
                    }
    stop-elevator{;stop the lift moving
                  :pre((container ?elevator)
                       (obstacle ?door)

                       (moving ?elevator true)
                       (open ?door false))
                  :add((moving ?elevator false))
                  :del((moving ?elevator true))
                  :txt(The elevator has stopped.)
                  :cmd(stop elevator)
                  }
    open-door {;opens elevator's door
               :pre ((container ?elevator)
                     (obstacle ?door)
                     (open ?door false)
                     (moving ?elevator false))
               :add ((open ?door true))
               :del ((open ?door false))
               :txt (Doors are opening...)
               :cmd (open door)
               }
    close-door {
                :pre ((container ?elevator)
                      (obstacle ?door)
                      (open ?door true)
                      (moving ?elevator false))
                :add ((open ?door false))
                :del ((open ?door true))
                :txt (Doors are closing...)
                :cmd (close door)
                }
    empty-going-up{;moving the lift when it is not occupied
                   :pre((container ?elevator)
                        (obstacle ?door)
                        (open ?door false)
                        (contains ?elevator nil)
                        (agent ?Person)
                        (waiting ?Person true)
                        (moving ?elevator true)
                        (at ?elevator ?floor)
                        (moving-up ?floor ?moving-up))
                   :add((at ?elevator ?moving-up))
                   :del((at ?elevator ?floor))
                   :txt(Elevator goes up from ?floor floor to ?moving-up floor.)
                   :cmd(ascend elevator)
                   }
    empty-going-down{;moving the lift when not occupied
                     :pre((container ?elevator)
                          (obstacle ?door)
                          (open ?door false)
                          (contains ?elevator nil)
                          (agent ?Person)
                          (waiting ?Person true)
                          (moving ?elevator true)
                          (at ?elevator ?floor)
                          (moving-down ?floor ?moving-down))
                     :add((at ?elevator ?moving-down))
                     :del((at ?elevator ?floor))
                     :txt(Elevator goes down from ?floor floor to ?moving-down floor.)
                     :cmd(descend elevator)
                     }
    going-up-with-person{;moving the occupant up one floor
                         :pre((container ?elevator)
                              (obstacle ?door)
                              (open ?door false)
                              (agent ?Person)
                              (contains ?elevator ?Person)
                              (moving ?elevator true)
                              (waiting ?Person true)
                              (at ?elevator ?floor)
                              (moving-up ?floor ?moving-up))
                         :add((at ?elevator ?moving-up)
                              (at ?Person ?moving-up))
                         :del((at ?elevator ?floor)
                              (at ?Person ?floor))
                         :txt(Elevator goes up from ?floor floor to ?moving-up floor with ?Person inside.)
                         :cmd(ascend person)
                         }
    going-down-with-person{;moving the occupant down one floor
                           :pre((container ?elevator)
                                (obstacle ?door)
                                (open ?door false)
                                (agent ?Person)
                                (waiting ?Person true)
                                (contains ?elevator ?Person)
                                (moving ?elevator true)
                                (at ?elevator ?floor)
                                (moving-down ?floor ?moving-down))
                           :add((at ?elevator ?moving-down)
                                (at ?Person ?moving-down))
                           :del((at ?elevator ?floor)
                                (at ?Person ?floor))
                           :txt(Elevator goes down from ?floor floor to ?moving-down floor with ?Person inside.)
                           :cmd(descend person)
                           }

    wait-for-elevator{;wait for the lift to reach the floor the agent is on
                      :pre((container ?elevator)
                           (obstacle ?door)
                           (open ?door false)
                           (agent ?Person)
                           (waiting ?Person false)
                           (at ?elevator ?floor)
                           (at ?Person ?person-floor)
                           (moving ?elevator true))
                      :add((waiting ?Person true))
                      :del((waiting ?Person false))
                      :txt(waiting for ?elevator to reach ?person-floor floor.)
                      :cmd(waiting at ?person-floor)
                      }
    person-enters-elevator{;person enters the lift
                           :pre((container ?elevator)
                                (obstacle ?door)
                                (open ?door true)
                                (agent ?person)
                                (contains ?elevator nil)
                                (at ?person ?person-floor)
                                (at ?elevator ?person-floor)
                                (waiting ?person true)
                                (moving ?elevator false))
                           :add((contains ?elevator ?person)
                                (waiting ?person false))
                           :del((contains ?elevator nil)
                                (waiting ?person true))
                           :txt(?person entered the ?elevator)
                           :cmd(enter ?elevator)
                           }
    person-selects-floor{;person selects the floor and lift starts moving
                         :pre((agent ?person)
                              (waiting ?person false)
                              (obstacle ?door)
                              (open ?door false)
                              (container ?elevator)
                              (contains ?elevator ?person)
                              (moving ?elevator false))
                         :add((moving ?elevator true)
                              (waiting ?person true))
                         :del((moving ?elevator false)
                              (waiting ?person false))
                         :txt(floor selected)
                         :cmd(select floor)
                         }
    wait-selected{;person waits for lift to take them to their floor
                  :pre((agent ?person)
                       (container ?elevator)
                       (obstacle ?door)
                       (open ?door false)
                       (contains ?elevator ?person)
                       (moving ?elevator true)
                       (waiting ?person false))
                  :add((waiting ?person true))
                  :del((waiting ?person false))
                  :txt(waiting to reach selected floor)
                  :cmd(wait in elevator)
                  }
    person-exits-elevator{;person exits the lift
                          :pre((container ?elevator)
                               (obstacle ?door)
                               (agent ?person)
                               (contains ?elevator ?person)
                               (waiting ?person true)
                               (at ?person ?person-floor)
                               (at ?elevator ?person-floor)
                               (moving ?elevator false)
                               (open ?door true))
                          :add((contains ?elevator nil)
                               (waiting ?person false))
                          :del((contains ?elevator ?person)
                               (waiting ?person true))
                          :txt(?person exited ?elevator)
                          :cmd(exit ?elevator)
                          }
    }
  )




;version 3: a revision of the
;second version to simplify motion
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
