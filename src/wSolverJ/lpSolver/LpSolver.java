package wSolverJ.lpSolver;

import wSolverJ.canonicalSolver.CanonicalExpr;
import wSolverJ.canonicalSolver.CanonicalSolver;
import wSolverJ.canonicalSolver.Variable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * linear program solver
 * Created by Xavier on 15/12/16.
 */
public class LpSolver {

    private class ReplaceVariable {
        Variable varA;
        Variable varB;

        ReplaceVariable(Variable va, Variable vb) {
            varA = va;
            varB = vb;
        }
    }

    private LpExpression objectiveFunEPart = new LpExpression();
    private double objectiveFunCPart = 0.0;
    private double objReversal = 1.0;
    private ArrayList<LpConstraint> lpConstraints = new ArrayList<>();


    private CanonicalSolver canonicalSolver = new CanonicalSolver();

    private HashMap<DVariable, ReplaceVariable> dVarToVar = new HashMap<>();

    /**
     * Solve the problem
     * @return the state of the result:
     * -1 for no feasible solution, 1 for unbounded problem and 0 for optimal solution.
     */
    public int solve() {

        determineReplaceVars();
        toCanonical();
        if(findFeasibleSolution()){
            System.out.println("A feasible initial solution is found!");
        }else{
            System.out.println("There is no feasible solution!");
            return -1;
        }

        canonicalSolver.removeArtificial();

        if(canonicalSolver.solve(2)){
            System.out.println("Optimal solution is found! Objective: " + canonicalSolver.p2ObjectiveCPart);
            return 0;
        }else{
            System.out.println("The problem is unbounded!");
            return 1;
        }
    }

    private boolean findFeasibleSolution(){

        return canonicalSolver.p1ObjectiveEPart == null
                ||
             (canonicalSolver.solve(1) && Double.compare(canonicalSolver.p1ObjectiveCPart, 0.0) == 0);
    }

    private void toCanonical() {
        HashSet<Variable> basicVars = new HashSet<>();
        CanonicalExpr objPhase2 = getCanonicalObject();
        CanonicalExpr objPhase1 = new CanonicalExpr();
        double objPhase1C = 0.0;

        for(LpConstraint lpConstraint : lpConstraints){
            if(lpConstraint.lpExpr.elements.keySet().size()==1
                    && lpConstraint.type== LpConstraint.CType.GE
                    && Double.compare(lpConstraint.constant, 0.0)==0){
                continue;
            }

            CanonicalExpr cExpr = new CanonicalExpr();
            double sign = lpConstraint.constant >= 0 ? 1 : -1;
            for (Map.Entry<DVariable, Double> LpElem : lpConstraint.lpExpr.elements.entrySet()) {
                DVariable dVariable = LpElem.getKey();
                double coeff = sign * LpElem.getValue();

                ReplaceVariable rVar = dVarToVar.get(dVariable);
                cExpr.setElement(coeff, rVar.varA);
                if (rVar.varB != null) {
                    cExpr.setElement(0 - coeff, rVar.varB);
                }
            }

            switch (lpConstraint.type) {
                case GE://add surplus variable
                    cExpr.setElement(0 - sign, new Variable());
                    break;
                case LE://add slack variable
                    cExpr.setElement(sign, new Variable());
                    break;

            }

            Variable basicVar = findBasic(cExpr, objPhase2, basicVars);
            double constraintC = sign * lpConstraint.constant;
            if(basicVar == null){
                for(Variable var : cExpr.elements.keySet()){
                    objPhase1.setElement(objPhase1.getElementCoeff(var) + cExpr.getElementCoeff(var), var);
                }

                basicVar = new Variable(false);

                cExpr.setElement(1.0, basicVar);
                objPhase1C -= constraintC;
            }

            canonicalSolver.addConstraint(cExpr, constraintC, basicVar);
            basicVars.add(basicVar);
        }

        if(! objPhase1.elements.isEmpty()){
            canonicalSolver.setObjective(1, objPhase1, objPhase1C);
        }

        canonicalSolver.setObjective(2, objPhase2, objectiveFunCPart);
    }

    Variable findBasic(CanonicalExpr cExp, CanonicalExpr obj, HashSet<Variable> basicVars){
        for(Map.Entry<Variable, Double> entry : cExp.elements.entrySet()){
            if(entry.getValue().compareTo(1.0)==0
                    && ! obj.elements.containsKey(entry.getKey())
                    && ! basicVars.contains(entry.getKey())){
                return entry.getKey();
            }
        }
        return null;
    }

    private CanonicalExpr getCanonicalObject() {
        CanonicalExpr objExpr = new CanonicalExpr();
        for (Map.Entry<DVariable, Double> LpElem : objectiveFunEPart.elements.entrySet()) {
            DVariable dVariable = LpElem.getKey();
            double coeff = LpElem.getValue();
            ReplaceVariable rVar = dVarToVar.get(dVariable);
            objExpr.setElement(objReversal * coeff, rVar.varA);
            if (rVar.varB != null) {
                objExpr.setElement(0 - coeff, rVar.varB);
            }
        }
        return objExpr;
    }

    private void determineReplaceVars() {
        Map<Boolean, List<LpConstraint>> cGroup = lpConstraints.stream()
                .collect(Collectors.partitioningBy(c -> c.lpExpr.elements.size() == 1 && c.type == LpConstraint.CType.GE));

        for (LpConstraint lpConstraint : cGroup.get(true)) {
            DVariable dvar = lpConstraint.lpExpr.elements.keySet().iterator().next();
            if (!dVarToVar.containsKey(dvar)) {
                dVarToVar.put(dvar, new ReplaceVariable(new Variable(), null));
            }
        }


        for (LpConstraint lpConstraint : cGroup.get(false)) {
            lpConstraint.lpExpr.elements.keySet().stream().filter(dVariable -> !dVarToVar.containsKey(dVariable))
                    .forEach(dVariable -> dVarToVar.put(dVariable, new ReplaceVariable(new Variable(), new Variable())));
        }
    }


    /**
     * Set the objective function of the problem
     * @param type 1 for maximize problem, -1 for minimize problem
     * @param exp the objective function
     * @param constant the constant part of the objective function
     */
    public void setObjective(int type, LpExpression exp, double constant) {

        switch (type){
            case 1 :
                objReversal = 1.0;
                break;
            case 2:
                objReversal = -1.0;
                break;
            default:
                throw new Error("The parameter \"type\" can only be 1 or -1");
        }

        objectiveFunEPart = exp;
        objectiveFunCPart = constant;
    }

    /**
     * Add an equality constraint
     * @param exp the expression on the left
     * @param constant the constant on the right
     */
    public void addEQ(LpExpression exp, double constant) {
        lpConstraints.add(new LpConstraint(exp, LpConstraint.CType.EQ, constant));
    }

    /**
     * Add a less equal inequality constraint
     * @param exp the expression on the left
     * @param constant the expression on the right
     */
    public void addLE(LpExpression exp, double constant) {
        lpConstraints.add(new LpConstraint(exp, LpConstraint.CType.LE, constant));
    }

    /**
     * Add a greater equal inequality constraint
     * @param exp the expression on the left
     * @param constant the expression on the right
     */
    public void addGE(LpExpression exp, double constant) {
        lpConstraints.add(new LpConstraint(exp, LpConstraint.CType.GE, constant));
    }
}
