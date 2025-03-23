(ns zombiedice.scenes.round
  (:require [zombiedice.frontend.kaplay :as kaplay]
            [zombiedice.state.state-manager :as state-manager]))

(defn make-round [k game-state]
  (let [state (state-manager/update-state-playing game-state)]
    (.log js/console (clj->js state))
    (kaplay/add k [(kaplay/text k "ZOMBIE DICE" {:font "mainia" :size 96})
                   (kaplay/pos k (.-x (kaplay/center k)) 200)])))
