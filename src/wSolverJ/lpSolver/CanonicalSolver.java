package wSolverJ.lpSolver;

import java.util.*;

/**
 * the linear program canonical form solver
 * Created by Xavier on 15/12/15.
 */
class CanonicalSolver {


    CanonicalExpr p2ObjectiveEPart = new CanonicalExpr();
    double p2ObjectiveCPart = 0.0;

    CanonicalExpr p1ObjectiveEPart = null;
    double p1ObjectiveCPart = 0.0;


    ArrayList<CanonicalConstraint> constraints = new ArrayList<>();

    HashMap<CanonicalVariable, Double> varValues = null;

    CanonicalConstraint addConstraint(CanonicalExpr expr, double cnst, CanonicalVariable basicVar){
        CanonicalConstraint constraint = new CanonicalConstraint(expr, cnst, basicVar);
        constraints.add(constraint);
        return constraint;
    }

    void setObjective(int phase, CanonicalExpr expr, double cnst){
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

        for(Map.Entry<CanonicalVariable, Double> elem : p2ObjectiveEPart.elements.entrySet()){
            if(elem.getValue() < 0)continue;

            if(constraints.stream().filter(c->
                c.lExpression.elements.containsKey(elem.getKey())
                    && c.lExpression.elements.get(elem.getKey()) > 0).count() == 0){
                return true;
            }
        }
        return false;
    }

    boolean solve(int phase){
        if(unboundCriterion()){
            return false;
        }

        while(! optimalityCriterion(phase)){
            CanonicalVariable var = evaluatePivot(phase);
            iterate(var);
        }

        return true;
    }

    private CanonicalVariable evaluatePivot(int phase){
        switch (phase){
            case 1:
                return p1ObjectiveEPart.elements.entrySet().stream()
                        .max( (elem1, elem2) -> Double.compare(elem1.getValue(), elem2.getValue())).get().getKey();

            case 2:
                return p2ObjectiveEPart.elements.entrySet().stream()
                        .max( (elem1, elem2) -> Double.compare(elem1.getValue(), elem2.getValue())).get().getKey();
            default:
                throw new Error("Canonical phase incorrect!");
        }
    }

    private void iterate(CanonicalVariable variable){
        CanonicalConstraint constraintMin = constraints.stream()
                .filter(c -> c.lExpression.elements.containsKey(variable)
                    && c.lExpression.elements.get(variable) > 0)
                .min(
                    (c1, c2) -> Double.compare(
                            c1.rConstant / c1.lExpression.elements.get(variable),
                            c2.rConstant / c2.lExpression.elements.get(variable)) ).get();

        pivotIn(variable, constraintMin);
    }

    private void transform(double cf, CanonicalExpr expr, Map<CanonicalVariable, Double> elems){
        for(Map.Entry<CanonicalVariable, Double> elem : elems.entrySet()){
            CanonicalVariable var = elem.getKey();
            expr.setElement(
                    expr.getElementCoeff(var) - cf * elem.getValue(), var);
        }
    }


    void removeArtificial(){
        ArrayList<CanonicalConstraint> redundantConstraints = new ArrayList<>();
        for(CanonicalConstraint constraint : constraints){
            if(constraint.basic.isNotArtificial)continue;
            CanonicalVariable varNonArti = findNonArtiV(constraint);
            if(varNonArti == null){
                redundantConstraints.add(constraint);
                continue;
            }

            pivotIn(varNonArti, constraint);
        }

        constraints.removeAll(redundantConstraints);

        for(CanonicalConstraint constraint : constraints){
            constraint.lExpression.elements.entrySet().removeIf(elem -> ! elem.getKey().isNotArtificial);
        }

        p2ObjectiveEPart.elements.entrySet().removeIf(elem -> ! elem.getKey().isNotArtificial);
    }


    private void pivotIn(CanonicalVariable variable, CanonicalConstraint constraint){
        double coeff = constraint.lExpression.elements.get(variable);
        constraint.lExpression.elements.replaceAll( (var, c)-> c / coeff);

        constraint.rConstant /= coeff;
        constraint.basic = variable;

        constraints.stream().filter(c -> c != constraint).forEach( c -> {
            double cf = c.lExpression.getElementCoeff(variable);
            transform(cf, c.lExpression, constraint.lExpression.elements);
            c.rConstant -= cf * constraint.rConstant;
        });

        double coeffObj = p2ObjectiveEPart.getElementCoeff(variable);
        transform(coeffObj, p2ObjectiveEPart, constraint.lExpression.elements);
        p2ObjectiveCPart += coeffObj * constraint.rConstant;

        if(p1ObjectiveEPart != null){
            coeffObj = p1ObjectiveEPart.getElementCoeff(variable);
            transform(coeffObj, p1ObjectiveEPart, constraint.lExpression.elements);
            p1ObjectiveCPart += coeffObj * constraint.rConstant;
        }
    }

    private CanonicalVariable findNonArtiV(CanonicalConstraint c){
        for(CanonicalVariable var : c.lExpression.elements.keySet()){
            if(var.isNotArtificial){
                return var;
            }
        }
        return null;
    }

    void getVariableValues(){
        varValues = new HashMap<>();

        for(CanonicalConstraint constraint : constraints){
            varValues.put(constraint.basic, constraint.rConstant);
        }
    }
}
