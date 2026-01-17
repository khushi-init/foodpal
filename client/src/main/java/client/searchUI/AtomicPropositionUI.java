package client.searchUI;

import java.util.ArrayList;

import client.data.TranslationManager;
import client.utils.searchUtils.AtomicProposition;
import client.utils.searchUtils.Proposition;
import client.utils.searchUtils.SearchFunctions;
import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class AtomicPropositionUI extends PropositionUI {
    private ArrayList<TextField> argsInput;//an ArrayList of text inputs, one input for each argument
    private TranslationManager tm;

    /**
     * Constructor, initialize the TextField argument inputs
     * @param parent ...
     *               @param tm the translation manager used to localize UI text
     */
    @Inject
    public AtomicPropositionUI(ParentPropositionUI parent, TranslationManager tm){
        super(parent, SearchFunctions.getAtomicFunctions());
        this.tm=tm;
        dropdown.setOnAction((event) -> {
            setArgumentsInput();
            updateView();
        });
        this.argsInput = new ArrayList<>();
        setSelectedFunction(SearchFunctions.HAS);
        setArgumentsInput();
    }

    @Override
    public Pane getView() {
        HBox view = new HBox(1);
        view.setPadding(new Insets(topMargin, 0, 0, indentation));

        view.getChildren().add(this.dropdown);
        for(TextField x : argsInput){
            view.getChildren().add(x);
        }
        view.getChildren().add(deleteButton);
        return view;
    }

    /**
     * Converts this propositionUI to an actual executable proposition.
     * @return corresponding proposition
     */
    @Override
    public Proposition mapToProposition() throws IllegalArgumentException{
        ArrayList<String> args = new ArrayList<>(this.argsInput.stream().map(x -> x.getText()).toList());
        SearchFunctions function = getSelectedFunction();
        return new AtomicProposition(function, args);
    }

    public void setArgumentsInput(){
        this.argsInput.clear();
        SearchFunctions function = getSelectedFunction();
        for(int i = 0; i < function.amountOfArguments; i++){
            //create a TextField for every argument, with placeholder representing the expected type
            TextField x = new TextField();
            String type = function.paramTypes[i].getSimpleName();
            x.setPromptText(type);
            argsInput.add(x);
        }
    }

}
