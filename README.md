# FoodPal

A collaborative recipe manager with real-time synchronization across clients. Built by a team of 6 students as part of the Object-Oriented Programming Project in the BSc Computer Science and Engineering at TU Delft.

**Tech stack:** Java · Spring Boot · JavaFX · WebSockets · H2 · Maven · Checkstyle · GitLab CI

![FoodPal main window](docs/screenshots/main.png)

## Highlights

- **Real-time sync:** changes made by one user appear instantly for every connected client, via WebSockets
- **Custom query language:** advanced search with nested AND/OR filters, e.g. `OR(AND(MINING(cheese, 20), MAXSTEPS(0)), HASING(sugar))`
- **Nutrition tracking:** calorie calculation, recipe scaling with automatic unit normalization, and Nutri-Score grading
- **Shopping list, favorites, Markdown export and live language switching**

## My contributions

- Designed the core data model: `Recipe`, `Ingredient` and the `RecipeIngredient` join entity with a composite key, plus the JPA repositories
- Built the server's REST API (`RecipeController`, `IngredientController`) with GET, POST, PUT and DELETE endpoints, and wrote unit tests for both controllers
- Refactored business logic out of the controllers into a service layer
- Led most of the nutrition feature: the global ingredient list, nutritional values per ingredient, total kcal calculation with live updates, and alphabetical sorting that handles special characters
- Designed the unit system (a `Unit` interface with a formal-unit enum and informal free-text units), the quantity/unit selection dialog, and automatic unit normalization
- Implemented safe deletion of ingredients that are used in recipes, and validation so two recipes cannot share a name

## Running it

Requires Java and Maven (or use the included Maven wrapper, `./mvnw`).

Start the server first:

```
mvn -pl server -am spring-boot:run
```

Then start the client:

```
mvn -pl client -am javafx:run
```

To run the client from an IDE, you also need a JavaFX SDK; see the client README for setup instructions.

---

## Features

### Recipe management

Create, view, edit and delete recipes on a central server.

- Create, edit and delete recipes from the sidebar
- Edit a recipe's name, ingredients and preparation steps from the recipe view
- Add, edit and delete ingredients directly inside a recipe
- Reorder preparation steps with drag and drop
- Favorite, duplicate and assign a language to recipes
- Download recipes as Markdown files for printing or sharing
- All changes are stored on the server and propagated to other clients

### Real-time synchronization

All clients stay in sync, so changes made by one user are immediately visible to everyone.

- Updates are pushed over WebSockets the moment they happen
- Recipe renames, additions and deletions update live in every client's sidebar
- Editing any detail of a recipe updates it in every client that has it open
- Only changed data is sent, not the full recipe
- The global ingredient list is synchronized the same way
- Clients automatically reconnect and resubscribe after a server disconnect

### Ingredients and nutrition

**Global ingredient list**
- Open the ingredient list with the carrot icon in the top right
- Add, edit and delete ingredients; the list stays sorted alphabetically
- Each ingredient shows carbohydrates, fat and protein per 100 g, calculated calories, and how many recipes use it

**Consistency across recipes**
- When adding an ingredient to a recipe, choose an existing one from a dropdown or create a new one
- Renaming an ingredient updates it in every recipe that uses it
- Deleting an ingredient that is in use shows a warning; if confirmed, it is removed from all affected recipes
- Supports both formal units (g, mg, kg, l, ml) and informal, free-text units

**Scaling and totals**
- Set a recipe's serving size
- Total kcal and kcal per 100 g are calculated for the whole recipe
- Scaling a recipe adjusts ingredient quantities, calories and servings together
- Units are normalized automatically when scaling (e.g. 1000 g becomes 1 kg)

**Nutri-Score**
- Each ingredient gets a Nutri-Score grade (A–E), shown with the familiar color-coded Dutch label
- Points are added for calories, fat and carbohydrates and subtracted for protein:
  `score = calorie points + fat points + carb points − protein points`

### Search and favorites

- Star or unstar a recipe from the recipe view; starred recipes are marked in the list
- Toggle a favorites filter to show only starred recipes
- Favorites are stored locally by reference, so renaming a recipe keeps it starred
- You get a warning when another user deletes one of your favorites
- The search bar supports space-separated queries: `pizza cheese` matches recipes where both words appear in the title, ingredients or preparation steps
- Press Escape to clear the search
- Search and the favorites filter can be combined

**Advanced query builder**

The gear icon next to the search bar opens a query builder for more specific searches. Filters can be nested and are case-insensitive.

- **Combined filters:** `AND`, `OR`
- **Atomic filters:**
  - `HASING`, `HASNAME`, `HASSTEP`: an ingredient, the name or a step contains the given text
  - `HAS`: any of the above
  - `NOT`, `NOTING`, `NOTNAME`, `NOTSTEP`: negations of the above
  - `MINING`, `MAXING`: minimum or maximum amount of an ingredient
  - `MINSTEPS`, `MAXSTEPS`: minimum or maximum number of preparation steps
  - `HASLANG`: recipe language

Examples, given a recipe *Pizza* (20 g cheese, 100 g dough, 0 steps) and a recipe *Cake* (100 g sugar, 3 steps):

| Query | Result |
| --- | --- |
| `OR(AND(MINING(cheese, 20), MAXSTEPS(0)), HASING(sugar))` | Pizza, Cake |
| `OR(AND(MINING(cheese, 20), MINSTEPS(1)), HASING(sugar))` | Cake |
| `AND(HASNAME(a), MAXSTEPS(5))` | Pizza, Cake |
| `AND(HASNAME(pizza), MINSTEPS(1))` | none |

A help screen is available via the **?** icon in the search window.

### Shopping list

- Open the shopping list with the cart icon in the top bar
- Add, edit and delete items manually
- Add all ingredients of a recipe at once, with a preview where you can adjust them first
- Scaling a recipe also scales the quantities added to the shopping list
- Items added from multiple recipes show which recipe they came from
- Check off items you already have; they are greyed out and crossed out
- Reset the list, or download it with the date and time included

### Languages

- Switch the interface language at runtime from a flag dropdown, with no restart needed
- Hover over a flag to preview the interface in that language before switching
- The chosen language is saved and restored on the next start (English by default)
- Supports English, Dutch, Slovak, Greek and Portuguese
- Recipes can be tagged with a language and filtered with `HASLANG` in the query builder

---

## Team

Built at TU Delft by Khushi Agrawal, Dimitris Moulakis Koutsis, Marnix van Velzen, Luca Weesie, Loet Wagener and Adel Bežová.
