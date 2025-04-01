(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [reagent.core :as r]
            [reagent.dom.client :as rc]))

(defonce game-state
  (r/atom
   {:current-dice []
    :remaining-dice []
    :current-player ""
    :players {}
    :round 0
    :state :initializing}))

(defn add-player!
  "Add player to the game states players key"
  [game-state name]
  (swap! game-state (fn [{:keys [players]}]
                      {:current-player (keyword name)
                       :players (conj players {(keyword name) (player/init-player)})}))
  (.log js/console (clj->js @game-state)))

(defn add-dice!
  ([game-state dice]
   (add-dice! game-state [] dice))
  ([game-state current-dice remaining-dice]
   (swap! game-state assoc :current-dice current-dice)
   (swap! game-state assoc :remaining-dice remaining-dice)))

(defn reset-game!
  "Reset the game state"
  [game-state]
  (add-player! game-state "Paul")
  (add-dice! game-state (dice/init-dice)))

(defn roll
  "Take 3 dice from the pot of dice and roll them. Returns list of current
  (rolled) dice and remaining dice. Updates the game state"
  [game-state]
  (let [[current-dice remaining-dice] (dice/take-dice (:remaining-dice @game-state) 3)]
    (add-dice! game-state (dice/roll-dices current-dice) remaining-dice)))

(defn list-current-dice [game-state]
  (let [dices (:current-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li (str "Color: " (:color dice) ", face: " (:face dice))])]))

(defn list-remaining-dice [game-state]
  (.log js/console "render list-remaining-dice")
  (let [dices (:remaining-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li dice])]))

(defn count-brains [game-state]
  (count (filter (fn [x]
                   (= :brains (:face x))) (:current-dice @game-state))))

(defn eat-brains! [game-state]
  (let [current-player (:current-player @game-state)]
    (swap! game-state update-in [:players (keyword current-player) :brains] (fn [current-brains]
                                                                              (+ current-brains (count-brains game-state))))))

(defn show-current-brains [game-state]
  (let [current-player (:current-player @game-state)]
    (get-in @game-state [:players current-player :brains])))

(defn app []
  [:div
   [:button {:on-click (fn []
                         (roll game-state)
                         (eat-brains! game-state))}
    "Roll dice..."]
   [:button {:on-click (fn [] (reset-game! game-state))}
    "Restart"]
   [:div  "Current: " [list-current-dice game-state]]
   [:div "Remaining: " [list-remaining-dice game-state]]
   [:div "Brains: " [show-current-brains game-state]]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (app))))

(defn init []
  (reset-game! game-state)
  (mount-root)
  (.log js/console "Zombie Dice initialized"))

