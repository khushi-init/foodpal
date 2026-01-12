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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.Injector;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import javafx.util.Pair;

public class MyFXML {

    private Injector injector;

    /**
     * Constructor of the MyFXML module
     * @param injector - The injector provided
     */
    @Inject
    public MyFXML(Injector injector) {
        this.injector = injector;
    }

    /**
     * Returns a module controller, and it's parent in a pair
     * @param c - The class of the FXML controller class
     * @param parts - The file location as many strings
     * @return - The Pair of the controller instance and the parent
     * @param <T> - The class of the desired controller
     */
    public <T> Pair<T, Parent> load(Class<T> c, String... parts) {
        try {
            FXMLLoader loader = new FXMLLoader(getLocation(parts), null, null, new MyFactory(), StandardCharsets.UTF_8);
            Parent parent = loader.load();
            T ctrl = loader.getController();
            return new Pair<>(ctrl, parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a module controller, and the respective Node as a Pair
     * @param c - The class of the FXML node controller class
     * @param parts - The file location as many strings
     * @return - The Pair of the controller instance and the node
     * @param <T> - The class of the desired controller
     */
    public <T> Pair<T, Node> loadNode(Class<T> c, String... parts) {
        try {
            FXMLLoader loader = new FXMLLoader(getLocation(parts), null, null, new MyFactory(), StandardCharsets.UTF_8);
            Node node = loader.load();
            T ctrl = loader.getController();
            return new Pair<>(ctrl, node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a path as a string to a URL
     * @param parts - The path as many strings
     * @return - The URL derived from the string path
     */
    private URL getLocation(String... parts) {
        String path = Path.of("", parts).toString();
        return MyFXML.class.getClassLoader().getResource(path);
    }

    private class MyFactory implements BuilderFactory, Callback<Class<?>, Object> {

        @Override
        @SuppressWarnings("rawtypes")
        public Builder<?> getBuilder(Class<?> type) {
            return new Builder() {
                @Override
                public Object build() {
                    return injector.getInstance(type);
                }
            };
        }

        @Override
        public Object call(Class<?> type) {
            return injector.getInstance(type);
        }
    }
}