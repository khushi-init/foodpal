package client.utils.searchUtils;

import java.util.ArrayList;
import java.util.List;

import commons.Recipe;

public class AtomicProposition implements Proposition{

    private ArrayList<String> arguments;
    private SearchFunctions function;

    /**
     * Constructor...
     * @param function The SearchFunctions Enum entry associated with the function
     * @param arguments ArrayList of String arguments. If the String represents a Double value, the Double value is parsed during execution
     */
    public AtomicProposition(SearchFunctions function, ArrayList<String> arguments){
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * Checks if the atomic proposition holds for a given recipe
     * @param recipe
     * @return boolean representing if the proposition holds.
     */
    public boolean evaluate (Recipe recipe) throws IllegalArgumentException {
        if(!verifyArguments(function, arguments)){
            String message = generateIllegalArgumentExceptionMessage();
            throw new IllegalArgumentException(message);
        }
        ArrayList<Object> objectArgs = new ArrayList<>(arguments.stream().map(x -> (Object) x).toList());
        return this.function.evaluate.apply(recipe, objectArgs);
    }

    /**
     * Checks if the provided arguments match the amount and type of the expected arguments
     * @param function The function to check on
     * @param arguments The arguments provided by the user
     * @return boolean
     */
    public boolean verifyArguments(SearchFunctions function, ArrayList<String> arguments){
        //if the amount of arguments is not right, return false:
        if(function.amountOfArguments != arguments.size()){
            return false;
        }

        //check for special values (Double, Integer) if the provided arguments can be converted to the special values:
        for(int i = 0; i < function.amountOfArguments; i++){
            if(function.paramTypes[i] == Double.class){
                try {
                    Double.parseDouble(arguments.get(i));
                } catch (Exception e){
                    return false;
                }
            }

            if(function.paramTypes[i] == Integer.class){
                try {
                    Integer.parseInt(arguments.get(i));
                } catch (Exception e){
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Generates an error message that can be displayed with an IllegalArgumentException
     * @return String containing expected and actual arguments
     */
    public String generateIllegalArgumentExceptionMessage(){
        String expectedTypesMessage = String.join(
                ", ", 
                List.of(this.function.paramTypes)
                .stream()
                .map(x -> x.getSimpleName())
                .toList()
        );

        return "The amount and/or the types of your arguments are not accepted by function "
                + this.function.name.toUpperCase()
                + ".\nExpected amount was "
                + function.amountOfArguments
                + ", expected types were ["
                + expectedTypesMessage
                + "].\nYou entered the following arguments: "
                + this.arguments.toString();
    }

    /**
     * toString method...
     * @return String
     */
    public String toString(){
        return "{" + this.function.name + ", " + this.arguments.toString() + "}";
    }

}
