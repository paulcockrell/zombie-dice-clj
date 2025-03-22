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

(defn init "Initialize the game" []
  (kaplay/init config)
  (let [dice-pot (dice/init-dice)]
    (.log js/console (clj->js (dice/roll-dice 3 dice-pot))))
  (.log js/console "Zombie Dice initialized"))
