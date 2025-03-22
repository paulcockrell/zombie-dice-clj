(ns zombiedice.frontend.app
  (:require [zombiedice.frontend.kaplay :as kaplay]
            [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]))

(def config
  {:width 1920
   :height 1080
   :letterbox true
   :background [0 0 0]
   :global false
   :touchToMouse true
   :buttons {:roll {:keyboard ["space"] :mouse "left"}}
   :debugKey "d"
   :debug true})

(defn init-round [[current-dice remaining-dice]]
  (.log js/console (-> (player/init-player "Bob")
                       (player/inc-round)
                       (player/set-dice current-dice remaining-dice)
                       (clj->js)))
  (.log js/console (clj->js (dice/roll-dices current-dice))))

(defn init
  "Initialize the game"
  []
  (kaplay/init config)
  (let [dice (-> (dice/init-dice)
                 (dice/take-dice 3))]
    (init-round dice))
  (.log js/console "Zombie Dice initialized"))
