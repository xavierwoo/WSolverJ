package wSolverJ.canonicalSolver;

import java.util.HashMap;
import java.util.Set;

/**
 * Linear expression
 * Created by Xavier on 15/12/15.
 */
public class CanonicalExpr {
    public HashMap<Variable, Double> elements = new HashMap<>();

    public void setElement(double coefficient, Variable variable){
        if(Double.compare(coefficient, 0.0) == 0){
            elements.remove(variable);
        }else{
            elements.put(variable, coefficient);
        }
    }

    public Double getElementCoeff(Variable variable){
        return elements.containsKey(variable) ? elements.get(variable) : 0.0;
    }

    public Set<Variable> getVariableSet(){
        return elements.keySet();
    }

//    public void removeArtiVar(){
//        elements.entrySet().removeIf(var -> ! var.getKey().isNotArtificial);
//    }
}
