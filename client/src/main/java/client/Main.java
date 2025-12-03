/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import static com.google.inject.Guice.createInjector;

import client.scenes.*;
import client.utils.ServerUtils;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Main extends Application {

    // Debug variable to disable checking for an active server when launching client. Will be useful for early
    // development. Set to false or remove altogether before finalizing.
    private final boolean debug = true;

    public static final Injector INJECTOR = createInjector(new MyModule());
    public static final MyFXML FXML = new MyFXML(INJECTOR);

//    public static void main(String[] args) throws URISyntaxException, IOException {
//        launch();
//    }

    @Override
    public void start(Stage primaryStage) {

        System.out.println("Opening very cool amazing recipe app!");

        ServerUtils serverUtils = INJECTOR.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable() && !debug) {
            String msg = "Server needs to be started before the client, but it does not seem to be available. Shutting down.";
            System.err.println(msg);
            return;
        }

        // All scenes must be initialized here as such
        Pair<RecipesWindowCtrl, Parent> recipesWindow = FXML.load(RecipesWindowCtrl.class, "client", "scenes", "RecipesWindow.fxml");
        Pair<ShoppingListCtrl, Parent> shoppingList = FXML.load(ShoppingListCtrl.class, "client", "scenes", "ShoppingList.fxml");
        Pair<IngredientsWindowCtrl, Parent> ingredientsWindow = FXML.load(IngredientsWindowCtrl.class, "client", "scenes", "IngredientsWindow.fxml");
        PrimaryCtrl prime = INJECTOR.getInstance(PrimaryCtrl.class);
        prime.init(primaryStage, recipesWindow, shoppingList, ingredientsWindow);
    }
}