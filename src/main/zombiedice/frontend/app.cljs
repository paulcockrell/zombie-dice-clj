(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [reagent.core :as r]
            [reagent.dom.client :as rc]
            [cljs.core :as c]))

(def initial-game-state
  {:current-dice []
   :remaining-dice []
   :players []
   :round 0
   :brains 0
   :shots 0})

(defonce game-state
  (r/atom initial-game-state))

(defn save-game-state! [game-state new-game-state]
  (reset! game-state new-game-state))

(defn get-players [game-state]
  (:players game-state))

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
    (let [players (get-players game-state)]
      (prn (str "Adding player " name))
      (assoc game-state :players (conj players (player/init-player name))))
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

;; Brain functions

(defn get-round-brains [game-state]
  (:brains game-state))

(defn get-current-player-brains [game-state]
  (:brains (get-current-player game-state)))

(defn update-round-brains [game-state]
  (let [current-brains (get-round-brains game-state)
        new-brains (dice/count-brains (get-current-dice game-state))]
    (assoc game-state :brains (+ current-brains new-brains))))

(defn reset-brains [game-state]
  (assoc game-state :brains 0))

(defn update-player-brains
  "Saves current round brains to player brain tally"
  [game-state]
  (let [current-player (get-current-player game-state)
        players (get-players game-state)
        brains (get-round-brains game-state)]
    (assoc game-state :players
           (assoc players 0 (player/add-brains current-player brains)))))

;; Shot functions

(defn get-shots [game-state]
  (:shots game-state))

(defn update-round-shots [game-state]
  (let [current-shots (get-shots game-state)
        new-shots (dice/count-shots (get-current-dice game-state))]
    (assoc game-state :shots (+ current-shots new-shots))))

(defn reset-shots [game-state]
  (assoc game-state :shots 0))

;; Player functions

(defn list-players [game-state]
  (let [players (get-players @game-state)]
    [:div "Players:"
     [:ul
      (for [{:keys [name brains]} players]
        ^{:key (random-uuid)} [:li (str name " has eaten " brains " brains.")])]]))

(defn show-current-player [game-state]
  (let [current-player (get-current-player @game-state)]
    [:div "Current player: "
     (str (:name current-player))]))

(defn show-round-brains [game-state]
  [:div "Current brains eaten: "
   (get-round-brains @game-state)])

(defn show-round-shots [game-state]
  [:div "Current shots taken: "
   (get-shots @game-state)])

(defn process-hand [game-state]
  (-> game-state
      (roll-dice)
      (update-round-shots)
      (update-round-brains)))

(defn move-current-player-to-last [game-state]
  (let [players (get-active-players game-state)]
    (assoc game-state :players (vec (concat (rest players) [(first players)])))))

(defn update-round-counter [game-state]
  (assoc game-state :round (inc (:round game-state))))

(defn yield-turn! [game-state]
  (let [new-game-state
        (-> @game-state
            (update-player-brains)
            (move-current-player-to-last)
            (reset-shots)
            (reset-brains)
            (update-round-counter)
            (add-dice (dice/init-dice)))]
    (save-game-state! game-state new-game-state)))

(defn reset-game!
  "Reset the game state"
  [game-state]
  (let [new-game-state
        (-> initial-game-state
            (add-dice (dice/init-dice)))]
    (save-game-state! game-state new-game-state)))

(defn loose-round [game-state]
  (prn "Oh no you got shot too many times, you loose all your brains from this round!")
  (-> game-state
      (move-current-player-to-last)
      (reset-shots)
      (reset-brains)
      (update-round-counter)
      (add-dice (dice/init-dice))))

(defn win-round [game-state]
  (prn (str "Player " (:name (get-current-player game-state)) " has won the game!"))
  game-state)

(defn check-hand [game-state]
  (let [player-total-brains (+ (get-current-player-brains game-state) (get-round-brains game-state))
        shots (get-shots game-state)]
    (cond
      (<= 3 shots) (loose-round game-state)
      (<= 13 player-total-brains) (win-round game-state)
      :else game-state)))

(defn play-hand! [game-state]
  (let [new-game-state
        (-> @game-state
            (process-hand)
            (check-hand))]
    (save-game-state! game-state new-game-state)))

(defn list-current-dice [game-state]
  (let [dices (get-current-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li (str "Color: " (:color dice) ", face: " (:face dice))])]))

(defn list-remaining-dice [game-state]
  (let [dices (:remaining-dice @game-state)]
    [:div "Remaining dice: "
     [:ul
      (for [dice dices]
        ^{:key (random-uuid)} [:li dice])]]))

(defn show-current-hand [game-state]
  [:div "Current hand: "
   (list-current-dice game-state)])

(defn reset-game-btn [game-state]
  [:button.button.is-danger {:on-click (fn [] (reset-game! game-state))}
   "Restart"])

(defn yield-turn-btn [game-state]
  [:button.button.is-warning {:on-click
                              (fn []
                                (yield-turn! game-state))
                              :disabled
                              (= 0 (count (get-players @game-state)))}
   "Yield turn"])

(defn roll-dice-btn [game-state]
  [:button.button.is-primary {:on-click (fn [] (play-hand! game-state)) :disabled (or
                                                                                   (= 0 (count (get-active-players @game-state)))
                                                                                   (= 0 (count (:remaining-dice @game-state))))}
   "Roll dice..."])

(defn navbar-component []
  [:nav
   {:class "navbar py-4"}
   [:div
    {:class "container is-fluid"}
    [:div
     {:class "navbar-brand"}
     [:a
      {:class "navbar-item", :href "#"}
      [:img
       {:class "image",
        :src "https://bulma.io/images/bulma-logo.png",
        :alt "",
        :width "96px"}]]
     [:a
      {:class "navbar-burger",
       :role "button",
       :aria-label "menu",
       :aria-expanded "false"}
      [:span {:aria-hidden "true"}]
      [:span {:aria-hidden "true"}]
      [:span {:aria-hidden "true"}]]]
    [:div
     {:class "navbar-menu"}
     [:div
      {:class "navbar-start"}
      [:a {:class "navbar-item", :href "#"} "About"]
      [:a {:class "navbar-item", :href "#"} "Company"]
      [:a {:class "navbar-item", :href "#"} "Services"]
      [:a {:class "navbar-item", :href "#"} "Testimonials"]]
     [:div
      {:class "navbar-item"}
      [:div
       {:class "field has-addons"}
       [:div
        {:class "control"}
        [:input
         {:class "input",
          :type "search",
          :placeholder "Search",
          :aria-label "Search"}]]
       [:div
        {:class "control"}
        [:button
         {:class "button", :type "submit"}
         [:svg
          {:xmlns "http://www.w3.org/2000/svg",
           :fill "none",
           :viewBox "0 0 24 24",
           :stroke "currentColor",
           :style {:width "24px", :height "24px"}}
          [:path
           {:stroke-linecap "round",
            :stroke-linejoin "round",
            :stroke-width "2",
            :d "M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"}]]]]]]]]])

(defn header-component [game-state]
  [:div
   {:class "container"}
   [:div
    {:class "mb-6 pb-3 columns is-multiline"}
    [:div
     {:class "column is-12 is-6-desktop mx-auto has-text-centered"}
     [:h2
      {:class "mb-4 is-size-1 is-size-3-mobile has-text-weight-bold"}
      "Zombie Dice Online"]
     [:p
      {:class "subtitle has-text-grey mb-5"}
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque\n              massa nibh, pulvinar vitae aliquet nec, accumsan aliquet orci."]
     [:div
      {:class "buttons is-centered"}
      [roll-dice-btn game-state]
      [yield-turn-btn game-state]
      [reset-game-btn game-state]]]]])

(defn current-player-component [game-state]
  [:div
   {:class "column is-12 is-4-desktop"}
   [:h2
    {:class "is-size-4 has-text-weight-bold mb-2"}
    "Current round stats"]
   [:div
    {:class "ml-3"}
    [:h4
     {:class "is-size-4 has-text-weight-bold mb-2"}
     (str "Round # " (:round  @game-state))]]
   [:div
    {:class "mb-6 is-flex"}
    [:span
     [:svg
      {:width "48",
       :height "48",
       :viewBox "0 0 48 48",
       :fill "none",
       :xmlns "http://www.w3.org/2000/svg"}
      [:path
       {:d
        "M25.6 22.9C25.7 23 25.8 23 26 23H33C33.6 23 34 22.6 34 22C34 21.8 34 21.7 33.9 21.6L30.4 14.6C30.1 14.1 29.5 13.9 29 14.2C28.9 14.3 28.7 14.4 28.6 14.6L25.1 21.6C24.9 22 25.1 22.6 25.6 22.9ZM29.5 17.2L31.4 21H27.6L29.5 17.2ZM18.5 14C16 14 14 16 14 18.5C14 21 16 23 18.5 23C21 23 23 21 23 18.5C23 16 21 14 18.5 14ZM18.5 21C17.1 21 16 19.9 16 18.5C16 17.1 17.1 16 18.5 16C19.9 16 21 17.1 21 18.5C21 19.9 19.9 21 18.5 21ZM22.7 25.3C22.3 24.9 21.7 24.9 21.3 25.3L18.5 28.1L15.7 25.3C15.3 24.9 14.7 24.9 14.3 25.3C13.9 25.7 13.9 26.3 14.3 26.7L17.1 29.5L14.3 32.3C13.9 32.7 13.9 33.3 14.3 33.7C14.7 34.1 15.3 34.1 15.7 33.7L18.5 30.9L21.3 33.7C21.7 34.1 22.3 34.1 22.7 33.7C23.1 33.3 23.1 32.7 22.7 32.3L19.9 29.5L22.7 26.7C23.1 26.3 23.1 25.7 22.7 25.3ZM33 25H26C25.4 25 25 25.4 25 26V33C25 33.6 25.4 34 26 34H33C33.6 34 34 33.6 34 33V26C34 25.4 33.6 25 33 25ZM32 32H27V27H32V32Z",
        :fill "#00d1b2"}]
      [:circle {:cx "24", :cy "24", :r "23.5", :stroke "#00d1b2"}]]]
    [:div
     {:class "ml-3"}
     [:h4
      {:class "is-size-4 has-text-weight-bold mb-2"}
      "Current player"]
     [:p
      {:class "subtitle has-text-grey"}
      (str (:name (get-current-player @game-state)))]]]
   [:div
    {:class "mb-6 is-flex"}
    [:span
     [:svg
      {:width "48",
       :height "48",
       :viewBox "0 0 48 48",
       :fill "none",
       :xmlns "http://www.w3.org/2000/svg"}
      [:path
       {:d
        "M25.6 22.9C25.7 23 25.8 23 26 23H33C33.6 23 34 22.6 34 22C34 21.8 34 21.7 33.9 21.6L30.4 14.6C30.1 14.1 29.5 13.9 29 14.2C28.9 14.3 28.7 14.4 28.6 14.6L25.1 21.6C24.9 22 25.1 22.6 25.6 22.9ZM29.5 17.2L31.4 21H27.6L29.5 17.2ZM18.5 14C16 14 14 16 14 18.5C14 21 16 23 18.5 23C21 23 23 21 23 18.5C23 16 21 14 18.5 14ZM18.5 21C17.1 21 16 19.9 16 18.5C16 17.1 17.1 16 18.5 16C19.9 16 21 17.1 21 18.5C21 19.9 19.9 21 18.5 21ZM22.7 25.3C22.3 24.9 21.7 24.9 21.3 25.3L18.5 28.1L15.7 25.3C15.3 24.9 14.7 24.9 14.3 25.3C13.9 25.7 13.9 26.3 14.3 26.7L17.1 29.5L14.3 32.3C13.9 32.7 13.9 33.3 14.3 33.7C14.7 34.1 15.3 34.1 15.7 33.7L18.5 30.9L21.3 33.7C21.7 34.1 22.3 34.1 22.7 33.7C23.1 33.3 23.1 32.7 22.7 32.3L19.9 29.5L22.7 26.7C23.1 26.3 23.1 25.7 22.7 25.3ZM33 25H26C25.4 25 25 25.4 25 26V33C25 33.6 25.4 34 26 34H33C33.6 34 34 33.6 34 33V26C34 25.4 33.6 25 33 25ZM32 32H27V27H32V32Z",
        :fill "#00d1b2"}]
      [:circle {:cx "24", :cy "24", :r "23.5", :stroke "#00d1b2"}]]]
    [:div
     {:class "ml-3"}
     [:h4
      {:class "is-size-4 has-text-weight-bold mb-2"}
      "Brains eaten"]
     [:p
      {:class "Brains eaten"}
      (str (get-round-brains @game-state))]]]
   [:div
    {:class "mb-6 is-flex"}
    [:span
     [:svg
      {:width "48",
       :height "48",
       :viewBox "0 0 48 48",
       :fill "none",
       :xmlns "http://www.w3.org/2000/svg"}
      [:path
       {:d
        "M25.6 22.9C25.7 23 25.8 23 26 23H33C33.6 23 34 22.6 34 22C34 21.8 34 21.7 33.9 21.6L30.4 14.6C30.1 14.1 29.5 13.9 29 14.2C28.9 14.3 28.7 14.4 28.6 14.6L25.1 21.6C24.9 22 25.1 22.6 25.6 22.9ZM29.5 17.2L31.4 21H27.6L29.5 17.2ZM18.5 14C16 14 14 16 14 18.5C14 21 16 23 18.5 23C21 23 23 21 23 18.5C23 16 21 14 18.5 14ZM18.5 21C17.1 21 16 19.9 16 18.5C16 17.1 17.1 16 18.5 16C19.9 16 21 17.1 21 18.5C21 19.9 19.9 21 18.5 21ZM22.7 25.3C22.3 24.9 21.7 24.9 21.3 25.3L18.5 28.1L15.7 25.3C15.3 24.9 14.7 24.9 14.3 25.3C13.9 25.7 13.9 26.3 14.3 26.7L17.1 29.5L14.3 32.3C13.9 32.7 13.9 33.3 14.3 33.7C14.7 34.1 15.3 34.1 15.7 33.7L18.5 30.9L21.3 33.7C21.7 34.1 22.3 34.1 22.7 33.7C23.1 33.3 23.1 32.7 22.7 32.3L19.9 29.5L22.7 26.7C23.1 26.3 23.1 25.7 22.7 25.3ZM33 25H26C25.4 25 25 25.4 25 26V33C25 33.6 25.4 34 26 34H33C33.6 34 34 33.6 34 33V26C34 25.4 33.6 25 33 25ZM32 32H27V27H32V32Z",
        :fill "#00d1b2"}]
      [:circle {:cx "24", :cy "24", :r "23.5", :stroke "#00d1b2"}]]]
    [:div
     {:class "ml-3"}
     [:h4
      {:class "is-size-4 has-text-weight-bold mb-2"}
      "Shots taken"]
     [:p
      {:class "subtitle has-text-grey"}
      (str (get-shots @game-state))]]]
   [:div
    {:class "mb-6 is-flex"}
    [:span
     [:svg
      {:width "48",
       :height "48",
       :viewBox "0 0 48 48",
       :fill "none",
       :xmlns "http://www.w3.org/2000/svg"}
      [:path
       {:d
        "M25.6 22.9C25.7 23 25.8 23 26 23H33C33.6 23 34 22.6 34 22C34 21.8 34 21.7 33.9 21.6L30.4 14.6C30.1 14.1 29.5 13.9 29 14.2C28.9 14.3 28.7 14.4 28.6 14.6L25.1 21.6C24.9 22 25.1 22.6 25.6 22.9ZM29.5 17.2L31.4 21H27.6L29.5 17.2ZM18.5 14C16 14 14 16 14 18.5C14 21 16 23 18.5 23C21 23 23 21 23 18.5C23 16 21 14 18.5 14ZM18.5 21C17.1 21 16 19.9 16 18.5C16 17.1 17.1 16 18.5 16C19.9 16 21 17.1 21 18.5C21 19.9 19.9 21 18.5 21ZM22.7 25.3C22.3 24.9 21.7 24.9 21.3 25.3L18.5 28.1L15.7 25.3C15.3 24.9 14.7 24.9 14.3 25.3C13.9 25.7 13.9 26.3 14.3 26.7L17.1 29.5L14.3 32.3C13.9 32.7 13.9 33.3 14.3 33.7C14.7 34.1 15.3 34.1 15.7 33.7L18.5 30.9L21.3 33.7C21.7 34.1 22.3 34.1 22.7 33.7C23.1 33.3 23.1 32.7 22.7 32.3L19.9 29.5L22.7 26.7C23.1 26.3 23.1 25.7 22.7 25.3ZM33 25H26C25.4 25 25 25.4 25 26V33C25 33.6 25.4 34 26 34H33C33.6 34 34 33.6 34 33V26C34 25.4 33.6 25 33 25ZM32 32H27V27H32V32Z",
        :fill "#00d1b2"}]
      [:circle {:cx "24", :cy "24", :r "23.5", :stroke "#00d1b2"}]]]
    [:div
     {:class "ml-3"}
     [:h4
      {:class "is-size-4 has-text-weight-bold mb-2"}
      "Round dice left"]
     [:p
      {:class "subtitle has-text-grey"}
      (str (count (:remaining-dice @game-state)))]]]])

(defn update-player-list [game-state new-player]
  (let [new-game-state
        (add-player @game-state @new-player)]
    (save-game-state! game-state new-game-state)))

(defn player-input [value]
  [:input {:type "text"
           :value @value
           :class "input"
           :placeholder "Player name"
           :on-change #(reset! value (-> % .-target .-value))
           :on-key-press
           (fn [e]
             (when (= (.-key e) "Enter")
               (update-player-list game-state value)
               (reset! value "")))}])

(defn player-input-save [game-state value]
  [:button.button.is-primary {:on-click
                              (fn []
                                (update-player-list game-state value)
                                (reset! value ""))}
   "Add player"])

(defn new-player-component [game-state]
  (let [name (r/atom "")]
    (fn []
      [:div.field.has-addons
       [:div.control
        [player-input name]]
       [:div.control
        [player-input-save game-state name]]])))

(defn list-players-component [game-state]
  [:div
   {:class "column is-12 is-4-desktop"}
   [:h2
    {:class "is-size-4 has-text-weight-bold mb-2"}
    "Players"]
   [new-player-component game-state]

   (let [players (get-players @game-state)]
     (for [{:keys [name brains]} players]
       [:div
        {:class "mb-6 is-flex" :key (random-uuid)}
        [:span
         [:svg
          {:width "48",
           :height "48",
           :viewBox "0 0 48 48",
           :fill "none",
           :xmlns "http://www.w3.org/2000/svg"}
          [:path
           {:d
            "M25.6 22.9C25.7 23 25.8 23 26 23H33C33.6 23 34 22.6 34 22C34 21.8 34 21.7 33.9 21.6L30.4 14.6C30.1 14.1 29.5 13.9 29 14.2C28.9 14.3 28.7 14.4 28.6 14.6L25.1 21.6C24.9 22 25.1 22.6 25.6 22.9ZM29.5 17.2L31.4 21H27.6L29.5 17.2ZM18.5 14C16 14 14 16 14 18.5C14 21 16 23 18.5 23C21 23 23 21 23 18.5C23 16 21 14 18.5 14ZM18.5 21C17.1 21 16 19.9 16 18.5C16 17.1 17.1 16 18.5 16C19.9 16 21 17.1 21 18.5C21 19.9 19.9 21 18.5 21ZM22.7 25.3C22.3 24.9 21.7 24.9 21.3 25.3L18.5 28.1L15.7 25.3C15.3 24.9 14.7 24.9 14.3 25.3C13.9 25.7 13.9 26.3 14.3 26.7L17.1 29.5L14.3 32.3C13.9 32.7 13.9 33.3 14.3 33.7C14.7 34.1 15.3 34.1 15.7 33.7L18.5 30.9L21.3 33.7C21.7 34.1 22.3 34.1 22.7 33.7C23.1 33.3 23.1 32.7 22.7 32.3L19.9 29.5L22.7 26.7C23.1 26.3 23.1 25.7 22.7 25.3ZM33 25H26C25.4 25 25 25.4 25 26V33C25 33.6 25.4 34 26 34H33C33.6 34 34 33.6 34 33V26C34 25.4 33.6 25 33 25ZM32 32H27V27H32V32Z",
            :fill "#00d1b2"}]
          [:circle {:cx "24", :cy "24", :r "23.5", :stroke "#00d1b2"}]]]
        [:div
         {:class "ml-3"}
         [:h4
          {:class "is-size-4 has-text-weight-bold mb-2"}
          name]
         [:p
          {:class "subtitle has-text-grey"}
          "has eaten " [:span {:class "has-text-primary"} brains] " brains."]]]))])

(defn mobile-component []
  [:div
   {:class "column is-12 is-4-desktop"}
   [:h2
    {:class "is-size-4 has-text-weight-bold mb-2"}
    [show-current-hand game-state]]])

(defn footer-component []
  [:footer
   {:class "section"}
   [:div
    {:class "container"}
    [:div
     {:class
      "pb-5 is-flex is-flex-wrap-wrap is-justify-content-between is-align-items-center"}
     [:div
      {:class "mr-auto mb-2"}
      [:a
       {:class "is-inline-block", :href "#"}
       [:img
        {:class "image",
         :src "https://bulma.io/images/bulma-logo.png",
         :alt "",
         :width "96px"}]]]
     [:div
      [:ul
       {:class
        "is-flex is-flex-wrap-wrap is-align-items-center is-justify-content-center"}
       [:li
        {:class "mr-4"}
        [:a {:class "button is-white", :href "#"} "About"]]
       [:li
        {:class "mr-4"}
        [:a {:class "button is-white", :href "#"} "Company"]]
       [:li
        {:class "mr-4"}
        [:a {:class "button is-white", :href "#"} "Services"]]
       [:li
        [:a {:class "button is-white", :href "#"} "Testimonials"]]]]]]
   [:div {:class "pt-5", :style {:border-top "1px solid #dee2e6"}}]
   [:div
    {:class "container"}
    [:div
     {:class
      "is-flex-tablet is-justify-content-between is-align-items-center"}
     [:p "All rights reserved © My App 20XX"]
     [:div {:class "py-2 is-hidden-tablet"}]
     [:div
      {:class "ml-auto"}
      [:a
       {:class "mr-4 is-inline-block", :href "#"}
       [:img {:src "../images/app-page/socials/facebook.svg", :alt ""}]]
      [:a
       {:class "mr-4 is-inline-block", :href "#"}
       [:img {:src "../images/app-page/socials/twitter.svg", :alt ""}]]
      [:a
       {:class "mr-4 is-inline-block", :href "#"}
       [:img {:src "../images/app-page/socials/github.svg", :alt ""}]]
      [:a
       {:class "mr-4 is-inline-block", :href "#"}
       [:img {:src "../images/app-page/socials/instagram.svg", :alt ""}]]
      [:a
       {:class "is-inline-block", :href "#"}
       [:img
        {:src "../images/app-page/socials/linkedin.svg", :alt ""}]]]]]])

(defn app []
  [:div
   [:div.columns.is-multiline]
   [navbar-component]
   [:section.section
    [:div.container
     [header-component game-state]]
    [:div.columns.is-multiline
     [current-player-component game-state]
     [mobile-component]
     [list-players-component game-state]]]
   [footer-component]
   [:section.section
    [:div.buttons
     [roll-dice-btn game-state]
     [yield-turn-btn game-state]
     [reset-game-btn game-state]]]
   [:section.section
    [show-current-player game-state]
    [show-round-brains game-state]
    [show-round-shots game-state]]
   [:section.section
    [show-current-hand game-state]
    [list-remaining-dice game-state]
    [list-players game-state]]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (app))))

(defn init []
  (reset-game! game-state)
  (mount-root)
  (.log js/console "Zombie Dice initialized"))

