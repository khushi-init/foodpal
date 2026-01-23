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
* **Bonus Feature(s):**

---

## 6. Live Language Switch
> **Goal:** To allow users to switch the application's language at runtime without having to restart the application.

* **Expected Grade:** Excellent
* **Implementation Details:**
* **Bonus Feature(s):**
