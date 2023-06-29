# GrearRolledOne backlog

## June 29 2023 (small group)
* Continue on `computeProbWin` and `valueIterate`:
    * `computeProbWin`: consider subcases: both < goal; 1 > goal & 2 < goal; 1 < goal & 2 > goal; and both > goal
    * `valueIterate`: change to `l = (l+1)%2` and `onesRolled = 0` when compute opponent's pWin

## June 24 2023 (small group)
* Work on `computeProbWin`
    * This func still needs some more modifications
* Future work:
    * Modify `valueIterate` according to `computeProbWin`, using just 5 loops

## June 22 2023 (whole group)
* Prof Neller found errors in implementation, `pWin` and `isRoll` should have 5 dimensions (missing is First Player's turn)

## June 15 2023 (whole group)
* Working on `valueIteration()`
* Action Items:
    * Todd: Possible experimentation with approximately optimal policies
    * Quan, Phong, Linh: Compute optimal play for GRO in advance of the meeting and share results
* Advance (based on prob of winning after `valueIteration`):
    * Which player has a starting disadvantage
    * How points should they start with ("komi" = compensation points) in order to have as fair a game as possible?


## June 9 2023 (small group)
* Try to deal with question: "What is the probability of the first player winning at the beginning of the game?"
* Reference on Pig Game:
    * Code: https://github.com/QuanHNguyen232/GBurg_courses/tree/main/CS-371/hw5
    * Game rule: [cs371/hw5](http://cs.gettysburg.edu/~tneller/cs371/hw5.html) -> http://cs.gettysburg.edu/~tneller/nsf/pig/pig.pdf

## June 1 2023 (whole group)
* Complete `computepExceed()` function