package wSolverJ.canonicalSolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * the linear program canonical form solver
 * Created by Xavier on 15/12/15.
 */
public class CanonicalSolver {

    int testCount = 0;

    public CanonicalExpr p2ObjectiveEPart = new CanonicalExpr();
    public double p2ObjectiveCPart = 0.0;

    public CanonicalExpr p1ObjectiveEPart = null;
    public double p1ObjectiveCPart = 0.0;


    public ArrayList<Constraint> constraints = new ArrayList<>();

    public Constraint addConstraint(CanonicalExpr expr, double cnst, Variable basicVar){
        Constraint constraint = new Constraint(expr, cnst, basicVar);
        constraints.add(constraint);
        return constraint;
    }

    public void setObjective(int phase, CanonicalExpr expr, double cnst){
        switch (phase){
            case 1:
                p1ObjectiveEPart = expr;
                p1ObjectiveCPart = cnst;
                break;
            case 2:
                p2ObjectiveEPart = expr;
                p2ObjectiveCPart = cnst;
                break;
            default:
                throw new Error("Canonical phase incorrect!");
        }

    }

    private boolean optimalityCriterion(int phase){
        switch (phase){
            case 1:
                return p1ObjectiveEPart.elements.entrySet().stream().filter(it -> it.getValue() > 0).count() == 0;
            case 2:
                return p2ObjectiveEPart.elements.entrySet().stream().filter(it -> it.getValue() > 0).count() == 0;
            default:
                throw new Error("Canonical phase incorrect!");
        }
    }

    private boolean unboundCriterion(){

        for(Map.Entry<Variable, Double> elem : p2ObjectiveEPart.elements.entrySet()){
            if(elem.getValue() < 0)continue;

            if(constraints.stream().filter(c->
                c.lExpression.elements.containsKey(elem.getKey())
                    && c.lExpression.elements.get(elem.getKey()) > 0).count() == 0){
                return true;
            }
        }
        return false;
    }

    public boolean solve(int phase){
        if(unboundCriterion()){
            return false;
        }

        while(! optimalityCriterion(phase)){
            Variable var = evaluatePivot(phase);
            pivot(var);
        }

        return true;
    }

    private Variable evaluatePivot(int phase){
        switch (phase){
            case 1:
                return p1ObjectiveEPart.elements.entrySet().stream()
                        .max( (elem1, elem2) -> Double.compare(elem1.getValue(), elem2.getValue())).get().getKey();

            case 2:
                //return p2ObjectiveEPart.elements.entrySet().stream().filter(elem -> elem.getKey().isNotArtificial)
                return p2ObjectiveEPart.elements.entrySet().stream()
                        .max( (elem1, elem2) -> Double.compare(elem1.getValue(), elem2.getValue())).get().getKey();
            default:
                throw new Error("Canonical phase incorrect!");
        }
    }

    private void pivot(Variable variable){

//        testCount++;
//        System.out.println(testCount);

        Constraint constraintMin = constraints.stream()
                .filter(c -> c.lExpression.elements.containsKey(variable)
                    && c.lExpression.elements.get(variable) > 0)
                .min(
                    (c1, c2) -> Double.compare(
                            c1.rConstant / c1.lExpression.elements.get(variable),
                            c2.rConstant / c2.lExpression.elements.get(variable)) ).get();

        double coeff = constraintMin.lExpression.elements.get(variable);

        constraintMin.lExpression.elements.replaceAll( (var, c)-> c / coeff );

        constraintMin.rConstant /= coeff;

        constraintMin.basic = variable;

        constraints.stream().filter(c -> c != constraintMin).forEach( c -> {
            double cf = c.lExpression.getElementCoeff(variable);
            transform(cf, c.lExpression, constraintMin.lExpression.elements);
            c.rConstant -= cf * constraintMin.rConstant;
        });

        double coeffObj = p2ObjectiveEPart.getElementCoeff(variable);
        transform(coeffObj, p2ObjectiveEPart, constraintMin.lExpression.elements);
        p2ObjectiveCPart += coeffObj * constraintMin.rConstant;

        if(p1ObjectiveEPart != null){
            coeffObj = p1ObjectiveEPart.getElementCoeff(variable);
            transform(coeffObj, p1ObjectiveEPart, constraintMin.lExpression.elements);
            p1ObjectiveCPart += coeffObj * constraintMin.rConstant;
        }
    }

    private void transform(double cf, CanonicalExpr expr, Map<Variable, Double> elems){
        for(Map.Entry<Variable, Double> elem : elems.entrySet()){
            Variable var = elem.getKey();
            expr.setElement(
                    expr.getElementCoeff(var) - cf * elem.getValue(), var);
        }
    }


    public void removeArtificial(){
        //HashSet<Variable> harmlessArtiVars = new HashSet<>();
        ArrayList<Constraint> redundantConstraints = new ArrayList<>();
        for(Constraint constraint : constraints){
            if(constraint.basic.isNotArtificial)continue;
            Variable varNonArti = findNonArtiV(constraint);
            if(varNonArti == null){
                //harmlessArtiVars.add(constraint.basic);
                redundantConstraints.add(constraint);
                continue;
            }

            double coeff = constraint.lExpression.getElementCoeff(varNonArti);
            constraint.lExpression.elements.replaceAll( (var, c) -> c / coeff);
            constraint.rConstant /= coeff;
            constraint.basic = varNonArti;

            constraints.stream().filter(c -> c != constraint).forEach( c -> {
                double cf = c.lExpression.getElementCoeff(varNonArti);
                transform(cf, c.lExpression, constraint.lExpression.elements);
                c.rConstant -= cf * constraint.rConstant;
            });


            double coeffObj = p2ObjectiveEPart.getElementCoeff(varNonArti);
            transform(coeffObj, p2ObjectiveEPart, constraint.lExpression.elements);
            p2ObjectiveCPart += coeffObj * constraint.rConstant;

            if(p1ObjectiveEPart != null){
                coeffObj = p1ObjectiveEPart.getElementCoeff(varNonArti);
                transform(coeffObj, p1ObjectiveEPart, constraint.lExpression.elements);
                p1ObjectiveCPart += coeffObj * constraint.rConstant;
            }
        }


        constraints.removeAll(redundantConstraints);

        for(Constraint constraint : constraints){
            constraint.lExpression.elements.entrySet().removeIf(elem -> ! elem.getKey().isNotArtificial);
        }

        p2ObjectiveEPart.elements.entrySet().removeIf(elem -> ! elem.getKey().isNotArtificial);

    }

    public void removeArtificial_o(){

        //collect the artificial variables that can be removed
        HashSet<Variable> moveAbleArtiV = new HashSet<>();
        for(Iterator<Map.Entry<Variable, Double>> elemIt = p2ObjectiveEPart.elements.entrySet().iterator();
                elemIt.hasNext(); ){
            Map.Entry<Variable, Double> entry = elemIt.next();
            if(! entry.getKey().isNotArtificial){
                moveAbleArtiV.add(entry.getKey());
                elemIt.remove();
            }
        }

        //remove non basic artificial variables
        HashMap<Constraint, Variable> unMoveAbleArtiVInC = new HashMap<>();
        for(Constraint c : constraints){
            for(Iterator<Map.Entry<Variable, Double>> elemIt = c.lExpression.elements.entrySet().iterator();
                    elemIt.hasNext(); ){
                Map.Entry<Variable, Double> entry = elemIt.next();

                if(! entry.getKey().isNotArtificial){
                    if(moveAbleArtiV.contains(entry.getKey())){
                        elemIt.remove();
                    }else{
                        unMoveAbleArtiVInC.put(c, entry.getKey());
                    }
                }
            }
        }

        //remove the rest artificial variables
        for(Map.Entry<Constraint, Variable> entry : unMoveAbleArtiVInC.entrySet()){
            Variable nonArtiV = findNonArtiV(entry.getKey());
            if(nonArtiV != null){
                //double coef = entry.getKey().lExpression.elements.get(entry.getValue());

                constraints.stream().filter(c->c != entry.getKey()).forEach( c-> {
                    double cf = c.lExpression.getElementCoeff(entry.getValue());
                    transform(cf, c.lExpression, entry.getKey().lExpression.elements);
                    c.rConstant -= cf * entry.getKey().rConstant;
                });

                double coeffObj = p2ObjectiveEPart.getElementCoeff(entry.getValue());
                transform(coeffObj, p2ObjectiveEPart, entry.getKey().lExpression.elements);
                p2ObjectiveCPart += coeffObj * entry.getKey().rConstant;
            }
        }
    }

    private Variable findNonArtiV(Constraint c){
        for(Variable var : c.lExpression.elements.keySet()){
            if(var.isNotArtificial){
                return var;
            }
        }
        return null;
    }
}
