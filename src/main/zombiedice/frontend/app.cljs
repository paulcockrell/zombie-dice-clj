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
  (let [my-dice
        (-> (dice/init-dice)
            (dice/take-dice 3)
            (dice/roll-dices)
            ((fn [d] (.log js/console (clj->js d))))
            (clj->js))]
    (.log js/console my-dice))
  (.log js/console "Zombie Dice initialized"))
