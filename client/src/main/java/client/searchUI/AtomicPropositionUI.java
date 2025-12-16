package client.searchUI;

import java.util.ArrayList;

import client.utils.searchUtils.AtomicProposition;
import client.utils.searchUtils.Proposition;
import client.utils.searchUtils.SearchFunctions;
import javafx.geometry.Insets;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class AtomicPropositionUI extends PropositionUI {
    // private ComboBox<SearchFunctions> dropdown;
    private ArrayList<TextField> argsInput;
    
    /**
     * Constructor...
     * @param parent ...
     */
    public AtomicPropositionUI(ParentPropositionUI parent){
        super(parent, SearchFunctions.getAtomicFunctions());
        // ObservableList<SearchFunctions> functions = FXCollections.observableArrayList(SearchFunctions.getAtomicFunctions());
        // this.dropdown = new ComboBox<SearchFunctions>();
        dropdown.setOnAction((event) -> {
            setArgumentsInput();
            updateView();
            // System.out.println("Changed atomic function");
        });
        // dropdown.setCellFactory(lc -> new FunctionListCell());
        // dropdown.setItems(functions);
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
    public Proposition mapToProposition() throws IllegalArgumentException{
        ArrayList<String> args = new ArrayList<>(this.argsInput.stream().map(x -> x.getText()).toList());
        SearchFunctions function = getSelectedFunction();
        return new AtomicProposition(function, args);
    }

    public void setArgumentsInput(){
        this.argsInput.clear();
        SearchFunctions function = getSelectedFunction();
        for(int i = 0; i < function.amountOfArguments; i++){
            TextField x = new TextField();
            String type = function.paramTypes[i].getSimpleName();
            x.setPromptText(type);
            argsInput.add(x);
        }
    }

}
