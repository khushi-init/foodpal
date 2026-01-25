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

   FoodPal makes use of *WebSockets* to update content **dynamically** the second it changes!
   * A change in the recipe title is instantly visible to all client's sidebars! The same goes for additions & deletions of recipes.
   * Editing *any* detail of a recipe will instantly change it in all clients that have it open.
   *  *Only* updated content is sent every update, don't worry about your bandwidth!
* **Bonus Feature(s):**
> Because that's not all!
   * Everything seen about recipes above also applies to ingredients in the *Ingredients Tab*!
   * FoodPal is able to recover from a server disconnect and automatically resubscribes to all websockets it lost connection to.


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
	- When a recipe is opened, a togglable star appears which lets the user (un)star a recipe.
	- Underneath the recipes list there is a favorites toggle to only show favorites.
	- Favorite recipes have a star next to their name in the recipes list.
	- The list of favorites is stored locally.
	- A search bar allowing simple space-separated queries. For example: "pizza cheese" shows the recipes containing both pizza AND cheese in its title, or any of its ingredients, or any of its preparation steps. A recipe called pizza with an ingredient which has cheese in its name would satisfy this query.
	- The results of a query are always displayed in the standard list of recipes.
	- Pressing escape cancels the current search and resets the search input box.
	- When the favorites filter is enabled while searching, only results that satisfy the query AND are marked as favorite are shown.
  - Favorites are stored as references in the local config file, so renaming a recipe does not influence the starred state of the recipe
  - A warning is sent when someone else deleted your favorite recipe.
* **Bonus Feature:** 
  - We should get excellent for this epic because of the following feature:
  - Clicking the spiky wheel next to the search field opens an advanced query generator. This allows for using more specific filters.
	- Filters are either atomic or combined. Atomic filters check a recipe for a certain property (like HASING, checks if a recipe has an ingredient that contains the String passed into HASING), while combined filters allow for combining filters (they can be nested).
	- Combined filters are AND, OR
	- Atomic filters are HASING, HASNAME, HASSTEP, HAS (equivalent to OR(HASING, HASNAME, HASSTEP)), NOT (equivalent to the negation of HAS), NOTING, NOTNAME, NOTSTEP, MINING (minimal amount of an ingredient), MAXING, MINSTEPS (minimum amount of steps), MAXSTEPS, HASLANG.
	- All filters are case insensitive.
  - A short explanation on how to use this feature can be found when clicking the question mark in the search window.
	- Results of the query are displayed in the same way as normal search.
	- In the UI, for every argument the expected type is specified.
	- Examples:
		- Recipe 1 called Pizza, containing 20 g cheese and 100 g dough, having 0 preparation steps
		- Recipe 2 called Cake, containing 100 g sugar, having 3 preparation steps.
		- OR(AND(MINING(cheese, 20), MAXSTEPS(0)), HASING(sugar)) would yield both recipes
		- OR(AND(MINING(cheese, 20), MINSTEPS(1)), HASING(sugar)) would yield recipe 2
		- AND(HASNAME(a), MAXSTEPS(5)) would yield both recipes, because they both have 'a' in their names
		- AND(HASNAME(pizza), MINSTEPS(1)) would yield no recipes
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
* **Expected Grade:** Very Good
* **Implementation Details:**
  *In the main window, a language selector is displayed as a dropdown containing flag icons of all supported languages. This allows the user to immediately see which language is currently active at a glance.
  * Clicking the language indicator opens a list of available languages, represented by their respective flags.
  * When a language is selected, the application interface updates to the chosen language, and the selected language remains when navigating between different windows.
  * The selected application language is persisted and automatically restored after restarting the application. If the user has not selected a language before, English is used as the default.
  * When creating or editing a recipe, the user can assign a specific language to that recipe to indicate the language of the ingredients and preparation instructions.
  * Recipes can be filtered by language using the advanced search functionality:
    * Next to the search bar, an icon opens the extended filtering menu.
    * In this menu, the user can select the `Atomic` search option.
    * Within the Atomic search, the `HASLANG` filter can be used to specify the desired recipe language.
    * The language shortcut (e.g. en, nl, etc.) must be entered manually and can be found by clicking the **?** icon, which displays a hint with all supported shortcuts.
    * When filtering just one language opt for changing first dropdown to `AND`
    * When filtering multiple languages at once chose `OR` from first dropdown and then multiple languages bars with clicking on `Atomic` twice or more 
  * Language filtering affects which recipes are displayed, based on the language assigned to each recipe.
* **Bonus Feature(s):**
    * A language preview feature is available: when hovering over a language flag in the selector, the user can preview how the application interface would appear in that language before selecting it.
    * Multiple additional languages (Slovak, Greek, and Portuguese) were added as a proof of concept beyond the required languages.
* **Why we should get very good:**
  * The language selector is intuitive and visually clear through the use of flag icons and immediate feedback
  * Persisting the application language across restarts improves usability and accessibility.
  * Assigning a language to individual recipes enables meaningful filtering and better organization of multilingual content.
  * The advanced filtering system using atomic search works nicely for filtering languages of recipes, however selected filters are not currently persisted between sessions.  
  * The language preview and additional supported languages show extra effort beyond the requirements.
