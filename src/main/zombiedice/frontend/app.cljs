(ns zombiedice.frontend.app
  (:require [zombiedice.frontend.kaplay :as kaplay]
            [zombiedice.entities.dice :as dice]))

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

(defn init
  "Initialize the game"
  []
  (kaplay/init config)
  (let [[current-dice remaining-dice]
        (-> (dice/init-dice)
            (dice/take-dice 3))]
    (.log js/console "current dice: " (clj->js current-dice) " remaining dice: " (clj->js remaining-dice))
    (.log js/console (clj->js (dice/roll-dices current-dice))))
  (.log js/console "Zombie Dice initialized"))
