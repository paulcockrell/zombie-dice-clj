(ns zombiedice.frontend.app
  (:require [zombiedice.frontend.kaplay :as kaplay]
            [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [zombiedice.state.state-manager :as state-manager]
            [zombiedice.scenes.round :as round]))

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

(def k (kaplay/init config))

(defn create-player [name] ())
  ;; (.log js/console (-> (player/init-player "Bob")
  ;;                      (player/inc-round)
  ;;                      (player/set-dice current-dice remaining-dice)
  ;;                      (clj->js)))
  ;; (.log js/console (clj->js (dice/roll-dices current-dice))))

(defn init []
  ;; Load scenes
  (kaplay/scene k :round round/make-round)

  ;; Setup game state and start game
  (let [game-state (-> state-manager/default-game-state
                       (state-manager/add-dice (dice/init-dice))
                       (state-manager/add-player (player/init-player "Paul")))]

    (kaplay/go k :round game-state))

  (.log js/console "Zombie Dice initialized"))
