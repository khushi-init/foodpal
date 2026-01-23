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
import java.util.Optional;

import client.data.TranslationManager;
import client.scenes.ErrorCtrl;
import com.google.inject.Inject;
import commons.Ingredient;
import commons.Recipe;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

    private final String server;
    private final int statusOK = 200;
    private final int statusCreation = 201;
    private final int statusNoContent = 204;
    private final int statusSameName = 409;

    private final ErrorCtrl errorCtrl;
    private final TranslationManager tm;

    /**
     * Constructor for server utils. Just look at it
     * @param errorCtrl - Injected error ctrl
     * @param configService - Injected configService
     */
    @Inject
    public ServerUtils(ErrorCtrl errorCtrl, ConfigService configService) {
        this.errorCtrl = errorCtrl;

        server = configService.get().getUrl();
    }

    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig())
                    .target(server)
                    .request(APPLICATION_JSON)
                    .get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                return false;
            }
        }
        return true;
    }

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
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/recipes")
                .request(APPLICATION_JSON)
                .get(new GenericType<List<Recipe>>() {});
    }

    public List<Ingredient> getIngredients() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/ingredients")
                .request(APPLICATION_JSON)
                .get(new GenericType<List<Ingredient>>() {});
    }

    public Recipe getRecipe(Long id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("/api/recipes/" + id)
                .request(APPLICATION_JSON)
                .get(new GenericType<Recipe>() {});
    }

    public Ingredient getIngredient(Long id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("/api/ingredients/" + id)
                .request(APPLICATION_JSON)
                .get(new GenericType<Ingredient>() {});
    }

    public Recipe addRecipe(Recipe recipe) {
        Response response = null;
        try {
            response = ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/recipes")
                    .request(APPLICATION_JSON)
                    .post(Entity.entity(recipe, APPLICATION_JSON));

            if (response.getStatus() == statusCreation) {
                return response.readEntity(Recipe.class);
            } else if (response.getStatus() == statusSameName) {
                errorCtrl.showErrorPopup(
                        tm.tr("error.title"),
                        tm.tr("error.addingRecipeHeader"),
                        tm.tr("error.addingRecipeMessage")
                );
                return null;
            } else {
                System.err.println(
                        tm.tr("error.addRecipeFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                response.readEntity(String.class);
                return null;
            }
        } catch (ProcessingException e) {
            System.err.println(tm.tr("error.addRecipeNetwork") + ": " + e.getMessage());
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public Optional<Ingredient> addIngredient(Ingredient ingredient) {
        try (Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/ingredients")
                .request(APPLICATION_JSON)
                .post(Entity.entity(ingredient, APPLICATION_JSON))) {

            if (response.getStatus() == statusCreation) {
                return Optional.of(response.readEntity(Ingredient.class));
            }
            errorCtrl.showErrorPopup(
                    tm.tr("error.title"),
                    tm.tr("error.createIngredientHeader"),
                    tm.tr("error.createIngredientMessage") + " " + response.getStatus()
            );
            return Optional.empty();
        } catch (ProcessingException e) {
            errorCtrl.showErrorPopup(
                    tm.tr("error.title"),
                    tm.tr("error.createIngredientHeader"),
                    tm.tr("error.failConnectServer")
            );
            return Optional.empty();
        }
    }

    public void downloadRecipe(Long recipeId) {
        try {
            Response response = ClientBuilder.newClient()
                    .target(server)
                    .path("api/recipes/download/" + recipeId)
                    .request()
                    .get(Response.class);

            if (response.getStatus() == statusOK) {
                String userDownloads = System.getProperty("user.home") + "/Downloads/";
                String fileName = "recipe_" + recipeId + ".md";
                Path filePath = Paths.get(userDownloads + fileName);

                Files.write(filePath, response.readEntity(byte[].class));
                errorCtrl.displayInfo(
                        tm.tr("info.downloadRecipeSuccess") + " " + filePath,
                        tm.tr("button.close"),
                        tm.tr("status.success")
                );
            }
        } catch (Exception e) {
            errorCtrl.showGenericError(tm.tr("error.downloadRecipeFailed"));
        }
    }

    public boolean deleteRecipe(Long recipeId) {
        Response response = null;
        try {
            response = ClientBuilder.newClient()
                    .target(server)
                    .path("api/recipes/" + recipeId)
                    .request()
                    .delete();

            if (response.getStatus() != statusNoContent) {
                System.err.println(
                        tm.tr("error.deleteRecipeFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                return false;
            }
            return true;
        } catch (ProcessingException e) {
            System.err.println(tm.tr("error.deleteRecipeNetwork") + ": " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    public boolean deleteIngredient(Long ingredientId) {
        Response response = null;
        try {
            response = ClientBuilder.newClient()
                    .target(server)
                    .path("api/ingredients/" + ingredientId)
                    .request()
                    .delete();

            if (response.getStatus() != statusNoContent) {
                errorCtrl.showGenericError(
                        tm.tr("error.deleteIngredientFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                return false;
            }
            return true;
        } catch (ProcessingException e) {
            errorCtrl.showGenericError(
                    tm.tr("error.deleteIngredientNetwork") + ": " + e.getMessage()
            );
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    public Recipe updateRecipe(Recipe recipe) {
        Response response = null;
        try {
            response = ClientBuilder.newClient()
                    .target(server)
                    .path("api/recipes/" + recipe.getId())
                    .request()
                    .put(Entity.entity(recipe, APPLICATION_JSON));

            int status = response.getStatus();
            if (status == statusNoContent) {
                return recipe;
            } else if (status == statusOK) {
                return response.readEntity(Recipe.class);
            } else {
                System.err.println(
                        tm.tr("error.updateRecipeFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + status
                );
                return null;
            }
        } catch (ProcessingException e) {
            System.err.println(tm.tr("error.updateRecipeNetwork") + ": " + e.getMessage());
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public boolean deleteIngredient(long recipeId, long ingredientId) {
        Response response = null;
        try {
            response = ClientBuilder.newClient()
                    .target(server)
                    .path("api/recipes/" + recipeId + "/ingredients/" + ingredientId)
                    .request()
                    .delete();

            if (response.getStatus() != statusNoContent && response.getStatus() != statusOK) {
                errorCtrl.showGenericError(
                        tm.tr("error.deleteIngredientFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                return false;
            }
            return true;
        } catch (ProcessingException e) {
            errorCtrl.showGenericError(
                    tm.tr("error.deleteIngredientNetwork") + ": " + e.getMessage()
            );
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public Optional<Ingredient> editIngredient(Ingredient replacement) {
        try (Response response = ClientBuilder.newClient()
                .target(server)
                .path("api/ingredients")
                .request()
                .put(Entity.entity(replacement, APPLICATION_JSON))) {

            if (response.getStatus() != statusOK) {
                errorCtrl.showGenericError(
                        tm.tr("error.updateIngredientFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                return Optional.empty();
            }
            return Optional.of(response.readEntity(Ingredient.class));
        } catch (ProcessingException e) {
            errorCtrl.showGenericError(
                    tm.tr("error.updateIngredientNetwork") + ": " + e.getMessage()
            );
            return Optional.empty();
        }
    }

    public Optional<Integer> getIngredientUsage(long ingredientId) {
        try (Response response = ClientBuilder.newClient()
                .target(server)
                .path("api/ingredients/recipecount/" + ingredientId)
                .request()
                .get()) {

            if (response.getStatus() != statusOK) {
                errorCtrl.showGenericError(
                        tm.tr("error.fetchIngredientUsageFailed") + " " +
                                tm.tr("error.httpStatus") + ": " + response.getStatus()
                );
                return Optional.empty();
            }
            return Optional.of(response.readEntity(Integer.class));
        } catch (ProcessingException e) {
            errorCtrl.showGenericError(
                    tm.tr("error.fetchIngredientUsageNetwork") + ": " + e.getMessage()
            );
            return Optional.empty();
        }
    }
}