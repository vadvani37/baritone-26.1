package com.automine.goals;

/** Reach ANY of the contained goals (logical OR). Used by mining to target many ore blocks. */
public class GoalComposite implements Goal {

    private final Goal[] goals;

    public GoalComposite(Goal... goals) {
        this.goals = goals;
    }

    public Goal[] goals() {
        return goals;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        for (Goal goal : goals) {
            if (goal.isInGoal(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double min = Double.MAX_VALUE;
        for (Goal goal : goals) {
            min = Math.min(min, goal.heuristic(x, y, z));
        }
        return min;
    }

    @Override
    public String toString() {
        return "GoalComposite{size=" + goals.length + "}";
    }
}
