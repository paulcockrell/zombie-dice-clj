(ns zombiedice.state.state-manager)

;; Initial state
;; Player 1 rolls 3 dice
;; Player 1 collects any brain dice
;; > If Player has 13 brains they win the game
;; Player 1 collects any shotgun dice
;; > If the player has 3 shotgun blasts they loose the brains they have collected for the current round
;; Player 1 Rerolls if has any footprint dice.
;; > Take extra dice as you must always roll 3 dice.
;; > Player may simply pass, and the turn moves to next player
