package wSolverJ.lpSolver;

import java.util.HashMap;

/**
 * Linear expression
 * Created by Xavier on 15/12/15.
 */
class CanonicalExpr {
    HashMap<CanonicalVariable, Double> elements = new HashMap<>();

    void setElement(double coefficient, CanonicalVariable variable){
        if(Double.compare(coefficient, 0.0) == 0){
            elements.remove(variable);
        }else{
            elements.put(variable, coefficient);
        }
    }

    Double getElementCoeff(CanonicalVariable variable){
        return elements.containsKey(variable) ? elements.get(variable) : 0.0;
    }
}
