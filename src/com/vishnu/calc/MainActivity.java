package com.vishnu.calc;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A minimal Material 3 style calculator. The whole UI is built in code so the
 * app stays dependency free (no AndroidX / no external libraries).
 */
public class MainActivity extends Activity {

    /** Material 3 baseline colour roles, light and dark. */
    private static final class Palette {
        final int surface, onSurface, onSurfaceVariant, surfaceContainer, surfaceContainerHigh,
                primary, onPrimary, secondaryContainer, onSecondaryContainer,
                tertiaryContainer, onTertiaryContainer;

        Palette(int surface, int onSurface, int onSurfaceVariant, int surfaceContainer,
                int surfaceContainerHigh, int primary, int onPrimary, int secondaryContainer,
                int onSecondaryContainer, int tertiaryContainer, int onTertiaryContainer) {
            this.surface = surface;
            this.onSurface = onSurface;
            this.onSurfaceVariant = onSurfaceVariant;
            this.surfaceContainer = surfaceContainer;
            this.surfaceContainerHigh = surfaceContainerHigh;
            this.primary = primary;
            this.onPrimary = onPrimary;
            this.secondaryContainer = secondaryContainer;
            this.onSecondaryContainer = onSecondaryContainer;
            this.tertiaryContainer = tertiaryContainer;
            this.onTertiaryContainer = onTertiaryContainer;
        }

        static Palette light() {
            return new Palette(0xFFFEF7FF, 0xFF1D1B20, 0xFF49454F, 0xFFF3EDF7, 0xFFECE6F0,
                    0xFF6750A4, 0xFFFFFFFF, 0xFFE8DEF8, 0xFF1D192B, 0xFFFFD8E4, 0xFF31111D);
        }

        static Palette dark() {
            return new Palette(0xFF141218, 0xFFE6E0E9, 0xFFCAC4D0, 0xFF211F26, 0xFF2B2930,
                    0xFFD0BCFF, 0xFF381E72, 0xFF4A4458, 0xFFE8DEF8, 0xFF633B48, 0xFFFFD8E4);
        }
    }

    private Palette p;
    private final CalculatorEngine engine = new CalculatorEngine();
    private TextView previewView;
    private TextView displayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean dark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        p = dark ? Palette.dark() : Palette.light();

        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            w.setStatusBarColor(p.surface);
            w.setNavigationBarColor(p.surface);
        }

        setContentView(buildRoot());
        updateUi();
    }

    // ------------------------------------------------------------------ layout

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(p.surface);
        root.setPadding(dp(10), dp(8), dp(10), dp(10));
        root.setFitsSystemWindows(true);

        root.addView(buildDisplay(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        LinearLayout keypad = new LinearLayout(this);
        keypad.setOrientation(LinearLayout.VERTICAL);
        root.addView(keypad, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.55f));

        addRow(keypad,
                key("AC", p.tertiaryContainer, p.onTertiaryContainer, 20, 1f),
                key("\u232B", p.tertiaryContainer, p.onTertiaryContainer, 22, 1f),
                key("%", p.secondaryContainer, p.onSecondaryContainer, 24, 1f),
                key("\u00F7", p.secondaryContainer, p.onSecondaryContainer, 30, 1f));

        addRow(keypad,
                key("7", p.surfaceContainer, p.onSurface, 26, 1f),
                key("8", p.surfaceContainer, p.onSurface, 26, 1f),
                key("9", p.surfaceContainer, p.onSurface, 26, 1f),
                key("\u00D7", p.secondaryContainer, p.onSecondaryContainer, 30, 1f));

        addRow(keypad,
                key("4", p.surfaceContainer, p.onSurface, 26, 1f),
                key("5", p.surfaceContainer, p.onSurface, 26, 1f),
                key("6", p.surfaceContainer, p.onSurface, 26, 1f),
                key("\u2212", p.secondaryContainer, p.onSecondaryContainer, 30, 1f));

        addRow(keypad,
                key("1", p.surfaceContainer, p.onSurface, 26, 1f),
                key("2", p.surfaceContainer, p.onSurface, 26, 1f),
                key("3", p.surfaceContainer, p.onSurface, 26, 1f),
                key("+", p.secondaryContainer, p.onSecondaryContainer, 30, 1f));

        addRow(keypad,
                key("0", p.surfaceContainer, p.onSurface, 26, 1f),
                key(".", p.surfaceContainer, p.onSurface, 30, 1f),
                key("=", p.primary, p.onPrimary, 32, 2f));

        return root;
    }

    private View buildDisplay() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.BOTTOM | Gravity.END);
        box.setPadding(dp(14), dp(8), dp(14), dp(6));

        previewView = new TextView(this);
        previewView.setTextColor(p.onSurfaceVariant);
        previewView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        previewView.setGravity(Gravity.END);
        previewView.setSingleLine(true);
        previewView.setEllipsize(TextUtils.TruncateAt.START);
        previewView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        previewView.setLetterSpacing(0.02f);
        box.addView(previewView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        displayView = new TextView(this);
        displayView.setTextColor(p.onSurface);
        displayView.setGravity(Gravity.END);
        displayView.setSingleLine(true);
        displayView.setEllipsize(TextUtils.TruncateAt.START);
        displayView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        displayView.setLetterSpacing(-0.02f);
        if (Build.VERSION.SDK_INT >= 26) {
            displayView.setAutoSizeTextTypeUniformWithConfiguration(
                    38, 80, 2, TypedValue.COMPLEX_UNIT_SP);
        } else {
            displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 54);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        box.addView(displayView, lp);
        return box;
    }

    private void addRow(LinearLayout parent, View... keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (View k : keys) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) k.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.setMargins(dp(5), dp(5), dp(5), dp(5));
            row.addView(k, lp);
        }
        parent.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    /** One Material 3 style key: pill shape, tonal fill, state layer ripple. */
    private TextView key(final String label, int bg, int fg, float textSizeSp, float weight) {
        TextView v = new TextView(this);
        v.setText(label);
        v.setTextColor(fg);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        v.setGravity(Gravity.CENTER);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        v.setClickable(true);
        v.setFocusable(true);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(26));
        shape.setColor(bg);

        int stateLayer = (fg & 0x00FFFFFF) | 0x3D000000;
        v.setBackground(new RippleDrawable(
                ColorStateList.valueOf(stateLayer), shape, null));

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                onKey(label);
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
        v.setLayoutParams(lp);
        return v;
    }

    // ------------------------------------------------------------------ actions

    private void onKey(String label) {
        switch (label) {
            case "AC":
                engine.clear();
                break;
            case "\u232B":
                engine.backspace();
                break;
            case "%":
                engine.percent();
                break;
            case "=":
                engine.equals();
                break;
            case ".":
                engine.dot();
                break;
            case "+":
                engine.operator('+');
                break;
            case "\u2212":
                engine.operator('-');
                break;
            case "\u00D7":
                engine.operator('*');
                break;
            case "\u00F7":
                engine.operator('/');
                break;
            default:
                engine.digit(label);
                break;
        }
        updateUi();
    }

    private void updateUi() {
        previewView.setText(engine.getPreview());
        boolean error = engine.isError();
        if (Build.VERSION.SDK_INT >= 26) {
            if (error) {
                displayView.setAutoSizeTextTypeUniformWithConfiguration(
                        16, 26, 2, TypedValue.COMPLEX_UNIT_SP);
            } else {
                displayView.setAutoSizeTextTypeUniformWithConfiguration(
                        38, 80, 2, TypedValue.COMPLEX_UNIT_SP);
            }
        } else {
            displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, error ? 22 : 54);
        }
        displayView.setText(engine.getDisplay());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
