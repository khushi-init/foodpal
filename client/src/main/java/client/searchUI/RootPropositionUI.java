package client.searchUI;

import client.scenes.SearchWindowCtrl;
import client.scenes.ErrorCtrl;
import client.utils.searchUtils.Proposition;
import client.utils.searchUtils.SearchFunctions;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class RootPropositionUI implements ParentPropositionUI{
    private PropositionUI child;
    private SearchWindowCtrl sceneCtrl;

    private final ErrorCtrl errorCtrl;

    /**
     * Constructor...
     * @param s the scene controller
     * @param errorCtrl - An errorCtrl object for reporting errors.
     */
    public RootPropositionUI(SearchWindowCtrl s, ErrorCtrl errorCtrl){
        // this.child = null;
        this.child = new CombinedPropositionUI(this);
        this.child.setSelectedFunction(SearchFunctions.AND);
        this.errorCtrl = errorCtrl;
        sceneCtrl = s;
    }

    public void setChild(PropositionUI child){
        this.child = child;
    }

    /**
     * Here for compatibility, it only shows an error and does not actually delete the child.
     * @param child Dummy, here for compatibility
     */
    public void deleteChild(PropositionUI child){
        errorCtrl.showGenericError("Cannot delete root proposition!");
    }

    /**
     * Calls the scene controller to update the tree visualisation
     */
    public void updateView(){
        sceneCtrl.updateView();
    }

    public Pane getView(){
        if(child == null) return new VBox(); //return an emtpy container
        return child.getView();
    }

    /**
     * Extracts a Proposition from this UI instance
     * @return Proposition as constructed by the user in the UI
     */
    public Proposition mapToProposition(){
        return this.child.mapToProposition();
    }
}
