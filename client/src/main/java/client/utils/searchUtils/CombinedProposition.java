package client.utils.searchUtils;

import java.util.ArrayList;

import commons.Recipe;

public class CombinedProposition implements Proposition{
    private SearchFunctions function;
    private ArrayList<Proposition> arguments;

    /**
     * Constructor...
     * @param function The SearchFunctions Enum entry associated with the function
     * @param arguments The propositions passed to the logical operator
     */
    public CombinedProposition(SearchFunctions function, ArrayList<Proposition> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * Checks if the logical proposition holds for a given recipe
     * @param recipe
     * @return boolean representing if the proposition holds.
     */
    public boolean evaluate(Recipe recipe) throws IllegalArgumentException{
        if(!verifyArguments()){
            String message = generateIllegalArgumentExceptionMessage();
            throw new IllegalArgumentException(message);
        }
        ArrayList<Object> objectArgs = new ArrayList<>(arguments.stream().map(x -> (Object) x).toList());
        return this.function.evaluate.apply(recipe, objectArgs);
    }

    /**
     * toString method...
     * @return String
     */
    public String toString(){
        return "{" + this.function.name + ", " + this.arguments.toString() + "}";
    }

    /**
     * Checks if the amount of child propositions matches the amount defined by the function
     * @return boolean
     */
    public boolean verifyArguments(){
        if(this.function.amountOfArguments == -1) return this.arguments.size() >= 1;
        return this.function.amountOfArguments == this.arguments.size();
    }

    /**
     * Generates a message explaining the amount of child propositions is incorrect
     * @return String
     */
    public String generateIllegalArgumentExceptionMessage(){
        if(this.function.amountOfArguments == -1) return "Function " + this.function.name + " requires at least 1 child proposition.";
        return "Function " + this.function.name + 
            " requires exactly " + 
            this.function.amountOfArguments + 
            " child proposition" + 
            (this.function.amountOfArguments == 1 ? "" : "s") +  
            ".";
    }
}
