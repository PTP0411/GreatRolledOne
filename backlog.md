# GrearRolledOne backlog

## July 8 2023 (small group)
* New idea for GRO Policies:
    1. policy 1:
        * keep rolling if o < 2
        * need a thres of score_diff, if score_diff >= thres, keep rolling (case i < j)
        * if i > 50 and p = 0, keep rolling until score_diff > thres to consider be safe
        * if p = 1, (case j > 50), keep rolling (check pExceed table)
* Added a simple policy that is only worse ~4% than optimal play
* Create those ideas of policies as `GROPolicyQLP{x}.java`

## July 6 2023 (whole group)
* Found an error in optimality equations (missing "1 -" for rolling insanity)
found a missing player 2 winning hold case in optimality equations made a number of bug fixes in code based on equations validated optimal policy code.
* Clif Presser Blender visualizations here: http://cs.gettysburg.edu/~cpresser/pig/GRO/
* Latest Todd Neller code posted here: http://cs.gettysburg.edu/~tneller/games/gro/code/

* Action items:
    * All: Experiment with approximately optimal policies, especially to find the simplest **human-playable** policies that perform best against optimal play. (Implement your ideas as classes that "implement GROPolicy". Then use `GROPolicyEvaluator`, `GROPolicyCritic`, and `GROPolicyComparison` to gain insight to how your policy differs in performance, most significant play errors, and minimum hold values, respectively.)  Feel free to create your own tools for experimentation as well!


## July 1 2023 (small group)
* Fix issue in `computeProbWin`:
    * Prob is 0.42656903612236774
    * States that game is fair given `onesRolled=0`; `k=0` (turn total=0); `l=0` (1st player):
        ```python
        0.5037387761862688: i=5 j=0 o=0 k=0 l=0
        0.5036805756920554: i=6 j=1 o=0 k=0 l=0
        0.5036858075317838: i=7 j=2 o=0 k=0 l=0
        ...
        ```

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