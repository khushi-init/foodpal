# Welcome to FoodPal

To run the FoodPal app from the command line, you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven wrapper (`mvnw`). You can then execute

	mvn -pl server -am spring-boot:run

to run the server and

	mvn -pl client -am javafx:run

to run the client. Please note that the server needs to be running, before you can start the client.

Get the FoodPal app running from the command line first to ensure you have the required tools on your sytem.

Once it is working, you can try importing the project into your favorite IDE. Especially the client is a bit more tricky to set up there due to the dependency on a JavaFX SDK.
To help you get started, you can find additional instructions in the corresponding README of the client project.

# FoodPal Application Overview

FoodPal is a novel, distributed cooking organizer app that runs in a client/server setting. Our implementation is structured around the following six core epics:

---

## 1. Basic Requirements
> **Goal:** To have a basic recipe management system that allows users to create, view, and modify recipes on a central server.

* **Expected Grade:** Excellent
* **Implementation Details:**
* **Bonus Feature(s):**

---

## 2. Automated Change Synchronization
> **Goal:** To have all clients stay in sync, so that changes made by one user are immediately visible to others.

* **Expected Grade:** Excellent
* **Implementation Details:**
* **Bonus Feature(s):**

---

## 3. Nutritional Value
> **Goal:** To provide nutritional information for recipes and allow users to scale them to different serving sizes.

* **Expected Grade:** Excellent
* **Implementation Details:**

    **Global Ingredient Management**
  *  **Ingredient List Access:** Clicking the carrot icon at the top right switches the view to the global Ingredient List.
  * **Management Tools:** Users can add or delete ingredients via the bottom-left buttons, consistent with the recipe window interface.
  * **Inline Editing:** Ingredients can be modified by clicking the pencil icon next to their name.
  * **Real-Time Sorting:** The ingredient list automatically maintains alphabetical order after any addition or modification.
  * **Nutritional Data:** Each entry displays Carbohydrates, Fat, and Protein per 100g, alongside automatically calculated Inferred Calories and a usage counter showing how many recipes include that item.

   **Recipe Integration & Integrity**
  * **Smart Addition:** When adding ingredients to a recipe, users can select from a dropdown of existing items or create a brand-new ingredient.
  * **Propagation:** Renaming an ingredient automatically updates that name across every recipe where it is used.
  * **Deletion Safeguards:** The system issues a warning if a user attempts to delete an ingredient currently used in recipes. If deleted, it is automatically removed from all associated recipes.
  * **Unit Support:** Supports both **informal units** (text-based) and **formal units** (G, MG, KG, L, ML).

  **Nutritional Scaling & Totals**
  * **Serving Size Control:** A dedicated text field allows users to set and display the recipe's serving size.
  * **Dynamic Calculations:** Total kcal and kcal/100g are calculated for the entire recipe and displayed next to the leaf symbol.
  * **Recipe Scaling:** Users can scale the entire recipe via the bottom text field. This scales ingredient quantities, total calories, and serving sizes simultaneously.
  * **Automatic Normalization:** During scaling, units are automatically normalized if they exceed standard thresholds (e.g., 1000g automatically converts to 1kg).
* **Bonus Feature(s):** **Nutri-Score Integration**
  * **Overview:** A nutritional value-based scoring system for ingredients, visually displayed via the standard Dutch labeling system (A-E) within the Ingredient Window for each Ingredient.
  * **Functionality Enhancement:** Transforms raw macronutrient data into an intelligent health profile. This allows users to instantly identify the "healthiest" ingredients without manual comparisons.
  * **The Logic:** Points are accumulated for "negative" components (high calories, fats, and carbs) and deducted for "positive" components (high protein).
  * **Calculation:** `Final Score = (Calories Points + Fats Points + Carbs Points) - Protein Points`.
  * **Visual Indicator:** The score is mapped to a color-coded grade (A through E), providing a familiar visual cue for informed dietary decision-making.

---

## 4. Searching for Recipes
> **Goal:** To allow users to find recipes quickly using search and a personal favorites list.

* **Expected Grade:** Excellent
* **Implementation Details:**
* **Bonus Feature(s):** 

---

## 5. Shopping List
> **Goal:** To allow users to compile a shopping list from one or more recipes.

* **Expected Grade:** Excellent
* **Implementation Details:**
  * Open the shopping list by clicking on the shopping cart in the right corner, in the red bar.
  * This opens the shopping list menu. You can add, edit and delete ingredients from the list.
  * You can add the ingredients of a specific recipe by clicking the shopping cart with an arrow pointing into the cart in the right corner. This opens the To Be Added menu, where you can modify the ingredients before you add them.
  * When scaling the recipe, the quantity of the ingredients that will be added to the shopping list are adjusted to the scale as well.
  * If you add the same ingredient through adding multiple recipes, it appears multiple times in the shopping list, with the name of the source recipes included.
  * You can reset the list by pressing the reset button.
  * You can download the list by pressing the download button.
* **Bonus Feature(s):**
  * You can mark off ingredients from the shopping list by pressing the box on the left side of the ingredient. This greys out the ingredient, indicating you have already gotten the ingredient. This way, you can prevent adding this ingredient to the list again, since you van see you've already thought of it and have gotten it. The ingredient is also greyed out and crossed out when downloading the shopping list.
  * When downloading the shopping list, the date and time is nicely displayed in the downloaded file.
* **Why we should get excellent:**
  * Ingredients in the shopping list can be checked off, greying them out and crossing them through. This makes it clear which items have already been purchased.
  * Checked-off ingredients help prevent duplicate additions, improving clarity and usability when managing larger shopping lists.
  * The date and time are clearly displayed in the downloaded shopping list, adding useful information to the file.
---

## 6. Live Language Switch
> **Goal:** To allow users to switch the application's language at runtime without having to restart the application.

* **Expected Grade:** Excellent
* **Implementation Details:**
* **Bonus Feature(s):**
