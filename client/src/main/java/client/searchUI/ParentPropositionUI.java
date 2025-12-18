package client.searchUI;

public interface ParentPropositionUI {
    /**
     * Deletes the specified child (if it exists)
     * @param child the child to be deleted
     */
    public void deleteChild(PropositionUI child);
    /**
     * Passes the update signal to the top of the tree and rerenders it.
     */
    public void updateView();
}
