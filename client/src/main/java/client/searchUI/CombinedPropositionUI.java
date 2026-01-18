package client.searchUI;

import java.util.ArrayList;

import client.data.TranslationManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import client.utils.searchUtils.*;

public class CombinedPropositionUI extends PropositionUI implements ParentPropositionUI{
    private ArrayList<PropositionUI> children;
    private Button addAtomicButton;
    private Button addCombinedButton;

    private TranslationManager tm;

    /**
     * Constructor, initialize add buttons
     * @param parent ...
     *               @param tm the translation manager used to localize UI text
     */
    public CombinedPropositionUI(ParentPropositionUI parent, TranslationManager tm) {
        super(parent, SearchFunctions.getCombinedFunctions());
        this.tm = tm;

        this.children = new ArrayList<>();
        setSelectedFunction(SearchFunctions.AND);

        this.addAtomicButton = new Button();
        this.addCombinedButton = new Button();

        applyTexts();
        tm.bundleProperty().addListener((obs, oldB, newB) -> applyTexts());

        addAtomicButton.setOnAction(event -> {
            children.add(new AtomicPropositionUI(this, tm));
            updateView();
        });

        addCombinedButton.setOnAction(event -> {
            children.add(new CombinedPropositionUI(this, tm));
            updateView();
        });
    }

    private void applyTexts() {
        addAtomicButton.setText(tm.tr("search.button.addAtomic"));
        addCombinedButton.setText(tm.tr("search.button.addCombined"));
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
