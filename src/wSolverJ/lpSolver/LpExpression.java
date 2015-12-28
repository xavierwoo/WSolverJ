package wSolverJ.lpSolver;

import java.util.HashMap;

/**
 * linear program expression
 * Created by Xavier on 15/12/16.
 */
public class LpExpression {
    HashMap<DVariable, Double> elements = new HashMap<>();

    /**
     * Set the elements in the expression.
     * It will erase the former coefficient if its called repeatedly for the same variable.
     * @param coefficient the coefficient
     * @param dVariable the variable
     */
    public void setElements(Double coefficient, DVariable dVariable){
        if(Double.compare(coefficient, 0.0)==0){
            elements.remove(dVariable);
        }else{
            elements.put(dVariable, coefficient);
        }
    }
}
