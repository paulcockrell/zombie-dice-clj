(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [reagent.core :as r]
            [reagent.dom.client :as rc]
            [zombiedice.frontend.components :as components]
            [zombiedice.state.state-manager :as state]
            [cljs.core :as c]))

(defn yield-turn! [game-state]
  (let [new-game-state
        (-> @game-state
            (state/update-player-brains)
            (state/move-current-player-to-last)
            (state/reset-shots)
            (state/reset-brains)
            (state/update-round-counter)
            (state/add-dice (dice/init-dice)))]
    (state/save-game-state! game-state new-game-state)))

(defn reset-game!
  "Reset the game state"
  [game-state]
  (let [new-game-state
        (-> state/initial-game-state
            (state/add-dice (dice/init-dice)))]
    (state/save-game-state! game-state new-game-state)))

(defn play-hand! [game-state]
  (let [new-game-state
        (-> @game-state
            (state/process-hand)
            (state/check-hand))]
    (state/save-game-state! game-state new-game-state)))

(defn list-current-dice [game-state]
  (let [dices (state/get-current-dice @game-state)]
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
                              (= 0 (count (state/get-players @game-state)))}
   "Yield turn"])

(defn roll-dice-btn [game-state]
  [:button.button.is-primary {:on-click (fn [] (play-hand! game-state)) :disabled (or
                                                                                   (= 0 (count (state/get-active-players @game-state)))
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
      (str (:name (state/get-current-player @game-state)))]]]
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
      (str (state/get-round-brains @game-state))]]]
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
      (str (state/get-shots @game-state))]]]
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
        (state/add-player @game-state @new-player)]
    (state/save-game-state! game-state new-game-state)))

(defn player-input [value]
  [:input {:type "text"
           :value @value
           :class "input"
           :placeholder "Player name"
           :on-change #(reset! value (-> % .-target .-value))
           :on-key-press
           (fn [e]
             (when (= (.-key e) "Enter")
               (update-player-list state/game-state value)
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

   (let [players (state/get-players @game-state)]
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
    [show-current-hand state/game-state]]])

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
     [header-component state/game-state]]
    [:div.columns.is-multiline
     [current-player-component state/game-state]
     [mobile-component]
     [list-players-component state/game-state]]]
   [footer-component]
   [:section.section
    [:div.buttons
     [roll-dice-btn state/game-state]
     [yield-turn-btn state/game-state]
     [reset-game-btn state/game-state]]]
   [:section.section
    [state/show-current-player state/game-state]
    [state/show-round-brains state/game-state]
    [state/show-round-shots state/game-state]]
   [:section.section
    [show-current-hand state/game-state]
    [list-remaining-dice state/game-state]
    [state/list-players state/game-state]]])

;; new design start

(defn score-board-table
  "Render a table of players in the game with their accumulated brain
  consumption tally"
  [game-state]
  (let [players (state/get-players @game-state)]
    (if (< 0 (count players))
      [:table {:class "w-full caption-bottom text-sm"}
       [:thead {:class "[&_tr]:border-b"}
        [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
         [:th {:class "h-10 px-2 text-left align-middle font-medium text-muted-foreground w-[100px]"}
          "Name"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Position"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Rank"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Brains"]]]
       [:tbody {:class "[&_tr:last-child]:border-0"}
        (for [{:keys [name brains]} players]
          [:tr {:key name :class "border-b bg-primary/10"}
           [:td {:class "p-2 align-middle font-medium"} name]
           [:td {:class "p-2 align-middle text-right"} "1"]
           [:td {:class "p-2 align-middle text-right"} "3"]
           [:td {:class "p-2 align-middle text-right"} brains]])]]
      [:table {:class "w-full caption-bottom text-sm"}
       [:tbody {:class "[&_tr:last-child]:border-0"}
        [:tr {:class "border-b"}
         [:td "No players - add between 2 and 8 to start"]]]])))

(defn current-round-stats-table []
  [:table {:class "w-full caption-bottom text-sm"}
   [:thead {:class "[&_tr]:border-b"}
    [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
     [:th {:class "h-10 px-2 text-left align-middle font-medium text-muted-foreground w-[100px]"}
      "Throw"]
     [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
      "Feet"]
     [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
      "Shotguns"]
     [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
      "Brains"]]]
   [:tbody {:class "[&_tr:last-child]:border-0"}
    [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
     [:td {:class "p-2 align-middle font-medium"} "1"]
     [:td {:class "p-2 align-middle text-right"} "2"]
     [:td {:class "p-2 align-middle text-right"} "1"]
     [:td {:class "p-2 align-middle text-right"} "1"]]]
   [:tfoot {:class "border-t bg-primary/10 font-medium [&>tr]:last:border-b-0"}
    [:tr {:class "border-b"}
     [:td {:class "p-2 align-middle" :col-span 2} "Total"]
     [:td {:class "p-2 align-middle text-right"} "1"]
     [:td {:class "p-2 align-middle text-right"} "1"]]]])

(defn update-player-list [game-state new-player]
  (let [new-game-state
        (state/add-player @game-state @new-player)]
    (state/save-game-state! game-state new-game-state)))

(defn add-player-component
  [game-state]
  (let [name (r/atom "")]
    (fn []
      [:div
       {:class "grid grid-flow-col grid-rows-1 gap-4"}
       [components/input {:placeholder "Player name"
                          :value @name
                          :on-change #(reset! name (-> % .-target .-value))
                          :on-key-press
                          (fn [e]
                            (when (= (.-key e) "Enter")
                              (update-player-list game-state name)
                              (reset! name "")))}]
       [components/button {:label "Add"
                           :variant :primary
                           :on-click
                           (fn []
                             (update-player-list game-state name)
                             (reset! name ""))}]])))

(defn zombie-dice-ui [game-state]
  [:div {:class "p-4 space-y-4 max-w-md mx-auto"}
   [:h1 {:class "text-xl font-bold text-center"} "Zombie Dice"]

     ;; Players List
   [components/card
    [:<>
     [components/section-title "Score board"]
     [components/section-subtitle "The first to eat 13 brains wins the game"]
     [score-board-table game-state]
     [components/divider-horizontal]
     [:div
      {:class "grid grid-flow-col grid-rows-1 gap-4"}
      [add-player-component game-state]]]]

   [components/button {:label "(Re)start game"
                       :full-width true
                       :on-click #(js/alert "Label is start game while not in play, and restart game while in play")}]

   [:div
    {:class "grid grid-cols-2 grid-rows-2 gap-0"}
    [:div
     {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tl-lg border-primary text-left even:border sm:border sm:px-4 sm:py-2"}
     [:span
      {:class "text-xs text-gray-600"} "Round"]
     [:span
      {:class "text-lg font-bold leading-none sm:text-lg text-center"} "5"]]
    [:div
     {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tr-lg border-primary text-left sm:border sm:border-l-0 sm:px-4 sm:py-2"}
     [:span
      {:class "text-xs text-gray-600"} "Turn"]
     [:span
      {:class "text-lg font-bold leading-none sm:text-lg text-center"} "2 of 3"]]
    [:div
     {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-bl-lg border-primary text-left sm:border sm:border-t-0 sm:px-4 sm:py-2"}
     [:span
      {:class "text-xs text-gray-600"} "Throw"]
     [:span
      {:class "text-lg font-bold leading-none sm:text-lg text-center"} "1"]]
    [:div
     {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-br-lg border-primary text-left sm:border sm:border-t-0 sm:border-l-0 sm:px-4 sm:py-2"}
     [:span
      {:class "text-xs text-gray-600"} "Dice remaining"]
     [:span
      {:class "text-lg font-bold leading-none sm:text-lg text-center"} "6"]]]

   [components/card
    [:<>
     [components/section-title "Current zombie - Moss"]
     [current-round-stats-table]]]

   [components/card
    [:<>
     [components/section-title "Dice thrown"]
     [:div {:class "flex justify-center gap-4 text-4xl"}
      [:img {:src "/images/dice-green-brains.png"
             :alt "Descriptive text"
             :style {:width "80px"
                     :height "auto"}}]
      [:img {:src "/images/dice-yellow-footsteps.png"
             :alt "Descriptive text"
             :style {:width "80px"
                     :height "auto"}}]
      [:img {:src "/images/dice-red-explosion.png"
             :alt "Descriptive text"
             :style {:width "80px"
                     :height "auto"}}]]]]

     ;; Action Buttons
   [:div {:class "flex flex-col sm:flex-row gap-2 justify-around"}
    [components/button {:label "Take/Roll dice"
                        :variant :primary
                        :on-click #(js/alert "Button initally shows Take dice, where you know the colors of the dice, it then changes to Roll dice so you can throw")}]
    [components/button {:label "Yield turn"
                        :variant :outline
                        :on-click #(js/alert "Yield Turn")}]]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (zombie-dice-ui state/game-state))))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (prn "Zombie Dice starting")
  (reset-game! state/game-state)
  (mount-root))

(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
