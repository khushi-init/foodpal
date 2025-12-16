package client.searchUI;

import client.utils.searchUtils.SearchFunctions;
import javafx.scene.control.ListCell;

public class FunctionListCell extends ListCell<SearchFunctions>{
    @Override
    protected void updateItem(SearchFunctions function, boolean empty){
        super.updateItem(function, empty);
        if(empty || function == null){
            setText(null);
        } else {
            setText(function.name.toUpperCase());
        }
    }
}
