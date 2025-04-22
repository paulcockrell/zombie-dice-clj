(ns zombiedice.state.state-manager
  [:require
   [zombiedice.entities.dice :as dice]
   [zombiedice.entities.player :as player]
   [reagent.core :as r]])

;; Basic state rules
;;
;; Player 1 rolls 3 dice
;; Player 1 collects any brain dice
;; > If Player has 13 brains they win the game
;; Player 1 collects any shotgun dice
;; > If the player has 3 shotgun blasts they loose the brains they have collected for the current round
;; Player 1 Rerolls if has any footprint dice.
;; > Take extra dice as you must always roll 3 dice.
;; > Player may simply pass, and the turn moves to next player

(def initial-game-state
  {:current-dice []
   :remaining-dice []
   :players []
   :throws () ;; list of maps recording dice faces
   :action :adding-players
   :round 1})

(defonce game-state
  (r/atom initial-game-state))

(defn save-game-state! [game-state new-game-state]
  (reset! game-state new-game-state))

(def allowed-actions #{:adding-players :in-game :turn-over :game-over})

(def transition-rules
  {:adding-players #{:in-game}
   :in-game        #{:in-game :turn-over :game-over}
   :turn-over      #{:in-game}
   :game-over      #{:adding-players}})

(defn valid-transition? [from to]
  (contains? (get transition-rules from #{}) to))

(defn get-action [game-state]
  (:action @game-state))

(defn set-action [game-state new-action]
  (let [current-action (:action game-state)]
    (cond
      (not (allowed-actions new-action))
      (prn "Invalid action" new-action)

      (not (valid-transition? current-action new-action))
      (prn "Invalid state transition:" current-action "→" new-action)

      :else
      (assoc game-state :action new-action))))

(defn get-round [game-state]
  (:round game-state))

(defn get-players [game-state]
  (:players game-state))

(defn get-players-sorted [game-state]
  (sort-by :position (get-players game-state)))

(defn get-active-players [game-state]
  (filter (fn [p] (> 13 (:brains p))) (get-players game-state)))

(defn get-current-player [game-state]
  (first (get-players game-state)))

(defn get-current-dice [game-state]
  (:current-dice game-state))

(defn invalid-name-size? [name]
  (or
   (< (count name) 2)
   (> (count name) 10)))

(defn name-taken? [game-state name]
  (some (partial = name) (map :name (get-players game-state))))

(defn max-players? [game-state]
  (<= 5 (count (get-players game-state))))

(defn valid-name? [game-state name]
  (cond
    (invalid-name-size? name) false
    (name-taken? game-state name) false
    (max-players? game-state) false
    :else true))

(defn add-player
  "Add player to the game states players key"
  [game-state name]
  (if (valid-name? game-state name)
    (let [players (get-players game-state)
          position (+ 1 (count players))
          new-player (player/init-player :name name :position position)]
      (assoc game-state :players (conj players new-player)))
    (do (prn "Invalid name. Must be between 2 and 10 characters and not be taken.")
        game-state)))

(defn add-dice
  ([game-state dice]
   (add-dice game-state [] dice))
  ([game-state current-dice remaining-dice]
   (assoc game-state :current-dice current-dice :remaining-dice remaining-dice)))

(defn roll-dice
  "Take 3 dice from the pot of dice and roll them. Returns list of current
  (rolled) dice and remaining dice. If your current dice have 'feet' these will
  be kept for the next roll and the others replaced from the pot"
  [game-state]

  (let [last-round-feet-dice (dice/filter-feet (:current-dice game-state))
        number-of-new-dices-to-take (if (< 0 (count last-round-feet-dice))
                                      (- 3 (count last-round-feet-dice))
                                      3)
        [current-dice remaining-dice] (dice/take-dice (:remaining-dice game-state) number-of-new-dices-to-take)
        new-dice (into current-dice (dice/get-colors last-round-feet-dice))]
    (add-dice game-state (dice/roll-dices new-dice) remaining-dice)))

(defn record-throw [game-state]
  (let [dice (get-current-dice game-state)
        feet-count (dice/count-feet dice)
        shot-count (dice/count-shots dice)
        brain-count (dice/count-brains dice)
        current-throws (:throws game-state)
        new-throw {:throw (+ 1 (count current-throws)) :feet feet-count :shots shot-count :brains brain-count}]
    (assoc game-state :throws (cons new-throw current-throws))))

(defn reset-throws [game-state]
  (assoc game-state :throws ()))

(defn get-throws [game-state]
  (:throws @game-state))

(defn- merge-sum [maps]
  (reduce (fn [acc m]
            (merge-with + acc m))
          {}
          maps))

(defn get-throw-totals [game-state]
  (merge-sum (:throws game-state)))

(defn get-current-player-brains [game-state]
  (:brains (get-current-player game-state)))

(defn update-player-brains
  "Saves current round brains to player brain tally"
  [game-state]
  (let [current-player (get-current-player game-state)
        players (get-players game-state)
        brains (:brains (get-throw-totals game-state))
        action (:action game-state)]
    (if (= action :in-game)
      ;; Record players brains as they yielded thier turn while 'in-game'
      (assoc game-state :players (assoc players 0 (player/add-brains current-player brains)))
      ;; Disregard players brains as they yielded their turn after loosing their turn
      game-state)))

(defn process-hand [game-state]
  (-> game-state
      (roll-dice)
      (record-throw)))

(defn move-current-player-to-last [game-state]
  (let [players (get-active-players game-state)]
    (assoc game-state :players (vec (concat (rest players) [(first players)])))))

(defn update-round-counter [game-state]
  (if (= 1 (:position (get-current-player game-state)))
    (assoc game-state :round (inc (:round game-state)))
    game-state))

(defn start-game! [game-state]
  (let [new-state (set-action @game-state :in-game)]
    (save-game-state! game-state new-state)))

(defn loose-turn [game-state]
  (prn "Oh no you got shot too many times, you loose all your brains from this round!")
  (set-action game-state :turn-over))

(defn win-game [game-state]
  (prn (str "Player " (:name (get-current-player game-state)) " has won the game!"))
  (set-action game-state :game-over))

(defn check-hand [game-state]
  (let [current-player-brains (get-current-player-brains game-state)
        throw-brains (:brains (get-throw-totals game-state))
        throw-shots (:shots (get-throw-totals game-state))
        player-total-brains (+ current-player-brains throw-brains)]
    (cond
      (<= 3 throw-shots) (loose-turn game-state)
      (<= 13 player-total-brains) (win-game game-state)
      :else game-state)))
