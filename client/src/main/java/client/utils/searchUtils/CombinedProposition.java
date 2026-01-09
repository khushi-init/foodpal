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
        if(recipe == null) throw new IllegalArgumentException("Cannot evaluate null recipe");
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
        if(this.function.amountOfArguments == -1) return "Function " + this.function.name.toUpperCase() + " requires at least 1 child proposition.";
        return "Function " + this.function.name + 
            " requires exactly " + 
            this.function.amountOfArguments + 
            " child proposition" + 
            (this.function.amountOfArguments == 1 ? "" : "s") +  
            ".";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((function == null) ? 0 : function.hashCode());
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CombinedProposition other = (CombinedProposition) obj;
        if (function != other.function)
            return false;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        return true;
    }

    public SearchFunctions getFunction() {
        return function;
    }

    public void setFunction(SearchFunctions function) {
        this.function = function;
    }

    public ArrayList<Proposition> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayList<Proposition> arguments) {
        this.arguments = arguments;
    }

    
}
