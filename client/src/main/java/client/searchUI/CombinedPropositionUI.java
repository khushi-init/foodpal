package client.searchUI;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import client.utils.searchUtils.*;

public class CombinedPropositionUI extends PropositionUI implements ParentPropositionUI{
    private ArrayList<PropositionUI> children;
    private Button addAtomicButton;
    private Button addCombinedButton;

    /**
     * Constructor, initialize add buttons
     * @param parent ...
     */
    public CombinedPropositionUI(ParentPropositionUI parent){
        super(parent, SearchFunctions.getCombinedFunctions());
        this.children = new ArrayList<PropositionUI>();
        setSelectedFunction(SearchFunctions.AND);
        this.addAtomicButton = new Button("+ Atomic");
        this.addAtomicButton.setOnAction((event) -> {
            //on click, add a new atomic proposition (with this as its parent) to children and rerender the tree
            this.children.add(new AtomicPropositionUI(this));
            updateView();
        });
        this.addCombinedButton = new Button("+ Combined");
        this.addCombinedButton.setOnAction((event) -> {
            //on click, add a new combined proposition (with this as its parent) to children and rerender the tree
            this.children.add(new CombinedPropositionUI(this));
            updateView();
        });
    }
    
    @Override
    public VBox getView() {
        
        VBox view = new VBox(1);
        view.setPadding(boxInsets);
        view.setPadding(new Insets(topMargin, 0, 0, indentation));
        HBox header = new HBox(1);
        header.getChildren().add(dropdown);
        header.getChildren().add(addCombinedButton);
        header.getChildren().add(addAtomicButton);
        header.getChildren().add(deleteButton);
        view.getChildren().add(header);
        for(PropositionUI x: children){
            view.getChildren().add(x.getView());
        }
        return view;
    }

    /**
     * Converts this object to an executable proposition.
     * @return Proposition
     */
    @Override
    public Proposition mapToProposition() throws IllegalArgumentException{
        ArrayList<Proposition> props = new ArrayList<>(this.children.stream().map(x -> x.mapToProposition()).toList());
        SearchFunctions function = getSelectedFunction();
        return new CombinedProposition(function, props);
    }

    /**
     * Passes the update signal to the top of the tree and rerenders the tree.
     */
    public void updateView(){
        parent.updateView();
    }

    @Override
    public void deleteChild(PropositionUI child) {
        this.children.remove(child);
        updateView();
    }
}
