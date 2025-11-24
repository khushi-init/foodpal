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
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import commons.Recipe;
import org.glassfish.jersey.client.ClientConfig;

import commons.Quote;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";

    /**
     * Returns true if a server is running on port 8080
     * @return - Boolean value representing an active server
     */
    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig()) //
                    .target(SERVER) //
                    .request(APPLICATION_JSON) //
                    .get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                return false;
            }
        }
        return true;
    }

    // WARNING THE FOLLOWING METHODS ARE TEMPLATES AND NOT PART OF THE BASE CLIENT, THEY ARE TO BE
    // REWORKED AS PART OF OTHER ISSUES

    public void getRecipesTheHardWay() throws IOException, URISyntaxException {
        java.net.URL url = new URI("http://localhost:8080/api/recipes").toURL();
        java.io.InputStream is = url.openConnection().getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    public List<Recipe> getRecipes() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/recipes") //
                .request(APPLICATION_JSON) //
                .get(new GenericType<List<Recipe>>() {});
    }

    public Quote addRecipe(Recipe recipe) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/quotes") //
                .request(APPLICATION_JSON) //
                .post(Entity.entity(recipe, APPLICATION_JSON), Quote.class);
    }
}