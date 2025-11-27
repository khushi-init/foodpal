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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import commons.Recipe;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";
    private static final int statusOK = 200;
    private static final int statusCreation = 201;
    private static final int statusNoContent = 204;

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
    // REWORKED AS PART OF OTHER ISSUES. Until implemented, ignore javadoc warnings

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


    /**
     * Sends a POST request to the server to create a new recipe
     * @param recipe the recipe object to be saved
     * @return newly created recipe object returned by the server
     */
    public Recipe addRecipe(Recipe recipe) {

        Response response = null; // Declare Response outside the try block if needed elsewhere
        try {
            response = ClientBuilder.newClient(new ClientConfig())
                    .target(SERVER).path("api/recipes")
                    .request(APPLICATION_JSON)
                    // POST the entity and wait for the Response object
                    .post(Entity.entity(recipe, APPLICATION_JSON));

            // check for success (201 Created)
            if (response.getStatus() == statusCreation) {
                // read the JSON response body into a Recipe object
                return response.readEntity(Recipe.class);
            } else {
                // log non-success status and return null
                System.err.println("Failed to add recipe. Server returned status: " + response.getStatus());
                // It's crucial to read the entity even on failure to avoid connection issues
                response.readEntity(String.class); // Consume the body
                return null;
            }
        } catch (ProcessingException e) {
            // handle network/processing error
            System.err.println("Network/Processing error while adding recipe: " + e.getMessage());
            return null;
        } finally {
            // always close the response object to free up resources
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Download a recipe markdown file.
     * @param recipeId the id of the recipe to download.
     */
    public void downloadRecipe(Long recipeId) {
        try {
            Response response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path("api/recipes/download/" + recipeId)
                    .request()
                    .get(Response.class);

            if (response.getStatus() == statusOK) {
                // The path to save the file to
                String userDownloads = System.getProperty("user.home") + "/Downloads/";
                String fileName = "recipe_" + recipeId + ".md";
                Path filePath = Paths.get(userDownloads + fileName);

                // Write the file to the path
                Files.write(filePath, response.readEntity(byte[].class));
                System.out.println("Downloaded recipe " + recipeId + " to " + filePath);
            }

        } catch (Exception e) {
            System.out.println("An error has occurred!");
        }
    }

    /**
     * Sends a DELETE request to the server to delete the recipe of the provided id
     * @param recipeId - The id of the recipe to destroy
     * @return - A boolean indicating whether the deletion was successful or not
     */
    public boolean deleteRecipe(Long recipeId) {
        Response response = null; // Declare Response outside the try block if needed elsewhere
        try {
            response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path("api/recipes/" + recipeId)
                    .request()
                    .delete();
            if (response.getStatus() != statusNoContent) {
                System.err.println("Failed to delete recipe. Server returned status: " + response.getStatus());
                return false;
            }
            return true;

        } catch (ProcessingException e) {
            System.err.println("Network/Processing error while deleting recipe: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

}