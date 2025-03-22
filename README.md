# Zombie Dice

ClojureScript implementation of the classic Zombie Dice game.

## Dice

There are 13 dice total

The dice have pictures on them. The pictures mean you ate a brain, your victim
got away, or they shot you back.

The dice are colored green, yellow and red. 3 are red, 4 are yellow, and 6 are
green. As you may have figured out, red dice are colored red because they are
"high risk" dice.

All dice, no matter what color, have a shot, feet, and brains. The difference in them is the color and thus outcome.

The meaning behind the colors is the probability of how bad your outcome will be. For example, say you pull out 3 red dice, you are more than likely going to be shot 3 times.

So dice go as follows:

- Green: more likely to roll a brain
- Yellow: little less likely to roll a brain
- Red: more likely to get shot

Brain is _good_. Shot is _bad_. Footsteps mean _roll_ that specific die again.

Dice configuration:

- Red
  - 3 sides shotgun
  - 2 sides footsteps
  - 1 side brains
- Yellow
  - 2 sides shotgun
  - 2 sides footsteps
  - 2 sides brains
- Green
  - 1 sides shotgun
  - 2 sides footsteps
  - 3 sides brains

## How to win

First player to reach 13 brains is the winner.

## How to play

1. The first player grabs 3 dice and rolls.
1. If any footsteps are rolled, they signify rolling that same die again.
1. If you are shot or ate any brains, put these to the right or on your player
   board. This helps keep track of what you have for this round.
1. With that being said, each roll must be with 3 dice every time. If you put
   a brain die and a shot die aside, then you need to grab 2 more dice from the
   container to make 3 dice.
1. The player continues to roll until they get 1 or 2 shots.
1. If the player is close to 3 shots, they should call for the next player to
   play. Any brains accumulated through the rolls are safe to be tallied unless
   3 shots have been rolled. If 3 shots have been rolled, the player will loose
   all their brains they have eaten this round. So it is better to keep safe
   and pass the turn on to the next player. This way you keep whatever you have
   eaten.
1. If you are shot 3 times and lost all your brains for that round, it's not
   the end of the world, you can still try again next turn. Anything tallied
   from a previous round that wasn't shot 3 times is safe.
1. If it is your turn, you can always stop rolling any time you want, but keep
   in mind you won't get much brains not being greedy!
