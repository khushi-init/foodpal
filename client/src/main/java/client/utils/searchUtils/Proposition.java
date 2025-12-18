package client.utils.searchUtils;

import commons.Recipe;

public interface Proposition {
    /**
     * Checks if the (logical) proposition holds
     * @param recipe
     * @return boolean representing if the proposition holds.
     */
    public boolean evaluate(Recipe recipe);
}
