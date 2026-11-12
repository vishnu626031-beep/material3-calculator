package com.vishnu.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Small expression engine for the calculator.
 * Keeps a plain expression string such as "12+3*4" and evaluates it
 * with the usual operator precedence (* and / before + and -).
 */
final class CalculatorEngine {

    private String expr = "";
    private String display = "0";
    private String preview = "";
    private boolean error = false;
    private boolean justEvaluated = false;

    String getPreview() { return preview; }

    String getDisplay() { return display; }

    boolean isError() { return error; }

    void clear() {
        expr = "";
        display = "0";
        preview = "";
        error = false;
        justEvaluated = false;
    }

    void backspace() {
        if (error) { clear(); return; }
        justEvaluated = false;
        if (expr.isEmpty()) { display = "0"; preview = ""; return; }
        expr = expr.substring(0, expr.length() - 1);
        refresh();
    }

    void digit(String d) {
        if (error) clear();
        if (justEvaluated) { expr = ""; justEvaluated = false; }
        String cur = currentNumber();
        if (cur.isEmpty()) {
            expr = expr + d;
        } else if (cur.equals("0")) {
            expr = expr.substring(0, expr.length() - 1) + d;
        } else {
            expr = expr + d;
        }
        refresh();
    }

    void dot() {
        if (error) clear();
        if (justEvaluated) { expr = ""; justEvaluated = false; }
        String cur = currentNumber();
        if (cur.contains(".")) return;
        if (cur.isEmpty()) {
            expr = expr + "0.";
        } else {
            expr = expr + ".";
        }
        refresh();
    }

    void operator(char o) {
        if (error) clear();
        justEvaluated = false;
        if (expr.isEmpty()) expr = "0";
        char last = expr.charAt(expr.length() - 1);
        if (last == '.') {
            expr = expr.substring(0, expr.length() - 1) + o;
        } else if (isOp(last)) {
            expr = expr.substring(0, expr.length() - 1) + o;
        } else {
            expr = expr + o;
        }
        refresh();
    }

    void percent() {
        if (error) clear();
        String cur = currentNumber();
        if (cur.isEmpty()) return;
        double v;
        try {
            v = Double.parseDouble(cur);
        } catch (NumberFormatException e) {
            return;
        }
        String r = format(v / 100.0);
        expr = expr.substring(0, expr.length() - cur.length()) + r;
        justEvaluated = false;
        refresh();
    }

    void equals() {
        if (error) return;
        String e = expr;
        while (!e.isEmpty() && (isOp(e.charAt(e.length() - 1)) || e.charAt(e.length() - 1) == '.')) {
            e = e.substring(0, e.length() - 1);
        }
        if (e.isEmpty()) { clear(); return; }
        double v = evaluate(e);
        if (Double.isNaN(v)) {
            preview = pretty(e) + " =";
            display = "Can't divide by zero";
            error = true;
            justEvaluated = false;
            return;
        }
        preview = pretty(e) + " =";
        display = format(v);
        expr = display;
        justEvaluated = true;
        error = false;
    }

    // ---------------------------------------------------------------- internals

    private void refresh() {
        if (justEvaluated) return;
        if (expr.isEmpty()) {
            display = "0";
            preview = "";
            return;
        }
        String cur = currentNumber();
        if (!cur.isEmpty()) {
            display = cur;
        } else {
            String trimmed = expr.substring(0, expr.length() - 1);
            if (trimmed.isEmpty()) {
                display = "0";
            } else {
                double v = evaluate(trimmed);
                display = Double.isNaN(v) ? "0" : format(v);
            }
        }
        preview = hasOperator(expr) ? pretty(expr) : "";
    }

    private String currentNumber() {
        int i = expr.length();
        while (i > 0) {
            char c = expr.charAt(i - 1);
            if ((c >= '0' && c <= '9') || c == '.') i--;
            else break;
        }
        return expr.substring(i);
    }

    private static boolean hasOperator(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (isOp(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean isOp(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static String pretty(String s) {
        return s.replace('*', '\u00D7').replace('/', '\u00F7').replace('-', '\u2212');
    }

    static double evaluate(String s) {
        ArrayList<Double> nums = new ArrayList<>();
        ArrayList<Character> ops = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.') {
                int j = i;
                while (j < s.length() && ((s.charAt(j) >= '0' && s.charAt(j) <= '9') || s.charAt(j) == '.')) j++;
                String token = s.substring(i, j);
                try {
                    nums.add(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    return Double.NaN;
                }
                i = j;
            } else {
                if (nums.isEmpty()) return Double.NaN;
                ops.add(c);
                i++;
            }
        }
        if (nums.isEmpty()) return 0;
        if (ops.size() != nums.size() - 1) return Double.NaN;

        for (int k = 0; k < ops.size(); ) {
            char c = ops.get(k);
            if (c == '*' || c == '/') {
                double a = nums.get(k);
                double b = nums.get(k + 1);
                if (c == '/' && b == 0) return Double.NaN;
                double r = (c == '*') ? a * b : a / b;
                nums.set(k, r);
                nums.remove(k + 1);
                ops.remove(k);
            } else {
                k++;
            }
        }
        double acc = nums.get(0);
        for (int k = 0; k < ops.size(); k++) {
            acc = (ops.get(k) == '+') ? acc + nums.get(k + 1) : acc - nums.get(k + 1);
        }
        return acc;
    }

    static String format(double v) {
        if (Double.isNaN(v)) return "Error";
        if (Double.isInfinite(v)) return "Infinity";
        if (v == 0) return "0";
        double a = Math.abs(v);
        if (a >= 1e12 || a < 1e-6) {
            String s = String.format(Locale.US, "%.10E", v);
            int e = s.indexOf('E');
            String mantissa = s.substring(0, e);
            String exponent = s.substring(e + 1);
            if (mantissa.contains(".")) {
                mantissa = mantissa.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            int ex = Integer.parseInt(exponent);
            return mantissa + "e" + ex;
        }
        BigDecimal bd = new BigDecimal(v).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString();
    }
}
