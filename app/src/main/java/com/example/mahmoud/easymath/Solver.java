package com.example.mahmoud.easymath;

import java.util.ArrayList;

public final class Solver {

    private static final char[] allowed = {'-', '+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X'};
    private static final String THIRD_ORDER_VARIABLE = "X3";
    private static final String SECOND_ORDER_VARIABLE = "X2";
    private static final String FIRST_ORDER_VARIABLE_1 = "X";
    private static final String FIRST_ORDER_VARIABLE_2 = "X1";
    private static final char PLUS_SIGN = '+';
    private static final char MINUS_SIGN = '-';

    public static ArrayList<String> solve (String equation) {

        ArrayList<String> solution = new ArrayList<>();

        if (isChecked(equation)) {
            if (equation.contains(THIRD_ORDER_VARIABLE)) {
                solution = solveThirdOrderEquation(equation);
            } else if (equation.contains(SECOND_ORDER_VARIABLE)) {
                solution = solveSecondOrderEquation(equation);
            } else if (equation.contains(FIRST_ORDER_VARIABLE_1) || equation.contains(FIRST_ORDER_VARIABLE_2)) {
                solution = solveFirstOrderEquation(equation);
            } else {
                solution = solveExpression(equation);
            }
        }

        return solution;
    }

    private static boolean isChecked (String equation) {
        boolean ok = true;
        for (int i = 0 ; i < equation.length() && ok ; i++) {
            if (!isCharAllowed(equation.charAt(i))) {
                ok = false;
            }
        }
        return ok;
    }

    private static boolean isCharAllowed (char c) {
        boolean ok = false;
        for (int i = 0 ; i < allowed.length && !ok ; i++) {
            if (c == allowed[i]) {
                ok = true;
            }
        }
        return ok;
    }

    private static ArrayList<String> solveExpression (String equation) {
        ArrayList<String> solution = new ArrayList<>();

        while (equation.contains(Character.toString(PLUS_SIGN)) || equation.contains(Character.toString(MINUS_SIGN))) {

            if (equation.contains(Character.toString(PLUS_SIGN))) {
                String const1 = getValueBeforeOperator(equation, PLUS_SIGN);
                String const2 = getValueAfterOperator(equation, PLUS_SIGN);
                int result = Integer.valueOf(const1) + Integer.valueOf(const2);
                equation = equation.replace((const1 + PLUS_SIGN + const2), String.valueOf(result));
            } else {
                String const1 = getValueBeforeOperator(equation, MINUS_SIGN);
                String const2 = getValueAfterOperator(equation, MINUS_SIGN);
                int result = Integer.valueOf(const1) - Integer.valueOf(const2);
                equation = equation.replace(const1 + MINUS_SIGN + const2, String.valueOf(result));
            }

        }

        solution.add(equation);

        return solution;
    }

    private static String getValueBeforeOperator (String expression, char operator) {
        int index = expression.indexOf(operator);
        String coefficient = "";
        char character;

        while (index > 0) {
            index = index - 1;
            character = expression.charAt(index);
            coefficient = character + coefficient;
        }

        return coefficient;
    }

    private static String getValueAfterOperator (String expression, char operator) {
        int index = expression.indexOf(operator);
        String coefficient = "";
        char character;

        while ((index + 1) < expression.length() && expression.charAt(index + 1) != PLUS_SIGN && expression.charAt(index + 1) != MINUS_SIGN) {
            index = index + 1;
            character = expression.charAt(index);
            coefficient = coefficient + character;
        }

        return coefficient;
    }

    private static ArrayList<String> solveFirstOrderEquation (String equation) {
        ArrayList<String> solution = new ArrayList<>();

        ArrayList<String> arrayList2 = getFirstOrderCoefficient(equation);
        int a = Integer.valueOf(arrayList2.get(0));

        int b = 0;
        if (arrayList2.get(1).length() != 0) {
            b = Integer.valueOf(arrayList2.get(1));
        }

        if (b != 0.0) {
            solution.add(String.valueOf(round(-1.0*b)/a));
        } else {
            solution.add("0.0");
        }

        return solution;
    }

    private static ArrayList<String> solveSecondOrderEquation (String equation) {
        ArrayList<String> solution = new ArrayList<>();

        ArrayList<String> arrayList1 = getSecondOrderCoefficient(equation);
        int a = Integer.valueOf(arrayList1.get(0));

        int b = 0;
        int c = 0;
        if (arrayList1.get(1).length() != 0) {
            if (arrayList1.get(1).contains(FIRST_ORDER_VARIABLE_1) || arrayList1.get(1).contains(FIRST_ORDER_VARIABLE_2)) {

                ArrayList<String> arrayList2 = getFirstOrderCoefficient(arrayList1.get(1));
                b = Integer.valueOf(arrayList2.get(0));

                if (arrayList2.get(1).length() != 0) {
                    c = Integer.valueOf(arrayList2.get(1));
                }

            } else {
                c = Integer.valueOf(arrayList1.get(1));
            }
        }

        double x1;
        double x2;
        if (((b*b) - (4*a*c)) > 0) {
            x1 = round((-1.0*b) / (2.0*a));
            x2 = round(Math.sqrt((b*b) - (4*a*c)) / (2.0*a));
            solution.add(String.valueOf(x1 + x2));
            solution.add(String.valueOf(x1 - x2));
        } else if (((b*b) - (4*a*c)) < 0) {
            x1 = round((-1.0*b) / (2.0*a));
            x2 = round(Math.sqrt((4*a*c) - (b*b)) / (2.0*a));
            if (b != 0.0) {
                solution.add(String.valueOf(x1) + "+" + String.valueOf(x2) + "J");
                solution.add(String.valueOf(x1) + "-" + String.valueOf(x2) + "J");
            } else {
                solution.add("+" + String.valueOf(x2) + "J");
                solution.add("-" + String.valueOf(x2) + "J");
            }
        } else {
            x1 = round((-1.0*b) / (2.0*a));
            if (b != 0.0) {
                solution.add(String.valueOf(x1));
                solution.add(String.valueOf(x1));
            } else {
                solution.add("0.0");
                solution.add("0.0");
            }
        }

        return solution;
    }

    private static ArrayList<String> solveThirdOrderEquation (String equation) {
        ArrayList<String> solution = new ArrayList<>();

        ArrayList<String> arrayList1 = getThirdOrderCoefficient(equation);
        int a = Integer.valueOf(arrayList1.get(0));

        int b = 0;
        int c = 0;
        int d = 0;

        if (arrayList1.get(1).length() != 0) {

            if (arrayList1.get(1).contains(SECOND_ORDER_VARIABLE)) {

                ArrayList<String> arrayList2 = getSecondOrderCoefficient(arrayList1.get(1));
                b = Integer.valueOf(arrayList2.get(0));

                if (arrayList2.get(1).length() != 0) {
                    if (arrayList2.get(1).contains(FIRST_ORDER_VARIABLE_1) || arrayList2.get(1).contains(FIRST_ORDER_VARIABLE_2)) {
                        ArrayList<String> arrayList3 = getFirstOrderCoefficient(arrayList2.get(1));
                        c = Integer.valueOf(arrayList3.get(0));
                        if (arrayList3.get(1).length() != 0) {
                            d = Integer.valueOf(arrayList3.get(1));
                        }
                    } else {
                        d = Integer.valueOf(arrayList2.get(1));
                    }
                }

            } else if (arrayList1.get(1).contains(FIRST_ORDER_VARIABLE_1) || arrayList1.get(1).contains(FIRST_ORDER_VARIABLE_2)) {
                ArrayList<String> arrayList2 = getFirstOrderCoefficient(arrayList1.get(1));
                c = Integer.valueOf(arrayList2.get(0));
                if (arrayList2.get(1).length() != 0) {
                    d = Integer.valueOf(arrayList2.get(1));
                }
            } else {
                d = Integer.valueOf(arrayList1.get(1));
            }
        }

        if (a != 1) {
            b = b / a;
            c = c / a;
            d = d / a;
        }

        double p = (c / 3.0) - (b * b / 9.0);
        double q = (b * b * b / 27.0) - (b * c / 6.0) + (d / 2.0);
        double D = p * p * p + q * q;

        if (D > 0) {
            double r = Math.cbrt(-q + Math.sqrt(D));
            double s = Math.cbrt(-q - Math.sqrt(D));
            solution.add(String.valueOf(round((r+s)-(b/3.0))));
        } else if (D < 0) {
            double ang = Math.acos(-q / Math.sqrt(-p * p * p));
            double r = 2.0 * Math.sqrt(-p);

            for (int k = -1 ; k <= 1 ; k++) {
                double theta = (ang - 2.0 * Math.PI * k) / 3.0;
                solution.add(String.valueOf((r*Math.cos(theta))-(b/3.0)));
            }
        } else {
            double r = Math.cbrt(-q);
            solution.add(String.valueOf(round((2.0*r)-(b/3.0))));
            solution.add(String.valueOf(round((-1.0*r)-(b/3.0))));
        }

        return solution;

    }

    private static ArrayList<String> getThirdOrderCoefficient (String expression) {
        return getCoefficient(expression, THIRD_ORDER_VARIABLE);
    }

    private static ArrayList<String> getSecondOrderCoefficient (String expression) {
        return getCoefficient(expression, SECOND_ORDER_VARIABLE);
    }

    private static ArrayList<String> getFirstOrderCoefficient (String expression) {
        if (expression.contains(FIRST_ORDER_VARIABLE_2)) {
            return getCoefficient(expression, FIRST_ORDER_VARIABLE_2);
        } else {
            return getCoefficient(expression, FIRST_ORDER_VARIABLE_1);
        }
    }

    private static double round (double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static ArrayList<String> getCoefficient (String expression, String orderVariable) {

        ArrayList<String> arrayList = new ArrayList<>();

        int index = expression.indexOf(orderVariable);
        boolean stop = false;
        String coefficient = "";
        char character;

        while (index > 0 && !stop) {
            index = index - 1;
            character = expression.charAt(index);
            coefficient = character + coefficient;
            if (character == PLUS_SIGN || character == MINUS_SIGN) {
                stop = true;
            }
        }

        if (coefficient.length() == 0) {
            arrayList.add("1");
            arrayList.add(expression.replace(coefficient + orderVariable, ""));
        } else if (coefficient.length() == 1 && (coefficient.charAt(0) == PLUS_SIGN || coefficient.charAt(0) == MINUS_SIGN)) {
            if (coefficient.charAt(0) == PLUS_SIGN) {
                arrayList.add("1");
            } else if (coefficient.charAt(0) == MINUS_SIGN) {
                arrayList.add("-1");
            }
            arrayList.add(expression.replace(coefficient + orderVariable, ""));
        } else {
            arrayList.add(coefficient);
            arrayList.add(expression.replace(coefficient + orderVariable, ""));
        }

        return arrayList;
    }

}