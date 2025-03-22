(ns zombiedice.frontend.app
  (:require [zombiedice.frontend.kaplay :as kaplay]))

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
  (.log js/console "Zombie Dice initialized"))
