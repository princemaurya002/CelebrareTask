package com.princemaurya.celebrare;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.graphics.Paint;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView currentTextView;
    private FrameLayout canvasLayout;
    private Stack<TextAction> undoStack = new Stack<>();
    private Stack<TextAction> redoStack = new Stack<>();
    private int fontSize = 16;
    private String fontFamily = "sans-serif";
    private boolean isBold = false, isItalic = false, isUnderlined = false;
    private int textAlign = View.TEXT_ALIGNMENT_TEXT_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasLayout = findViewById(R.id.canvas_layout);
        MaterialButton addTextButton = findViewById(R.id.add_text_button);
        ImageButton undoButton = findViewById(R.id.undo_button);
        ImageButton redoButton = findViewById(R.id.redo_button);
        ImageButton boldButton = findViewById(R.id.bold_text);
        ImageButton italicButton = findViewById(R.id.italic_text);
        ImageButton underlineButton = findViewById(R.id.underline_text);
        ImageButton alignmentButton = findViewById(R.id.alignment_text);
        MaterialAutoCompleteTextView fontDropdown = findViewById(R.id.font_dropdown);
        ImageButton increaseFontButton = findViewById(R.id.increase_text);
        ImageButton decreaseFontButton = findViewById(R.id.decrease_text);

        addTextButton.setOnClickListener(v -> addTextToCanvas());
        undoButton.setOnClickListener(v -> undoAction());
        redoButton.setOnClickListener(v -> redoAction());
        boldButton.setOnClickListener(v -> toggleBold());
        italicButton.setOnClickListener(v -> toggleItalic());
        underlineButton.setOnClickListener(v -> toggleUnderline());
        alignmentButton.setOnClickListener(v -> toggleAlignment());

        increaseFontButton.setOnClickListener(v -> adjustFontSize(2));
        decreaseFontButton.setOnClickListener(v -> adjustFontSize(-2));

        fontDropdown.setOnItemClickListener((parent, view, position, id) -> selectFont(fontDropdown.getText().toString()));

        if (canvasLayout == null) {
            Log.e("MainActivity", "canvasLayout is null after findViewById()");
        }

    }

    private void addTextToCanvas() {
        if (canvasLayout == null) {
            Log.e("MainActivity", "canvasLayout is null");
            Toast.makeText(this, "Canvas is not initialized!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a dialog to input text
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Enter Text");

        // Create an EditText for user input
        final EditText input = new EditText(this);
        input.setHint("Type your text here...");
        builder.setView(input);

        // Set dialog buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String userText = input.getText().toString().trim();
            if (userText.isEmpty()) {
                Toast.makeText(this, "Text cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and configure the TextView
            TextView textView = new TextView(this);
            textView.setText(userText);
            textView.setTextSize(fontSize);
            textView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
            textView.setTextColor(Color.BLACK);
            textView.setPadding(10, 10, 10, 10);
            textView.setTextAlignment(textAlign);
            textView.setBackgroundColor(Color.TRANSPARENT);

            // Add touch listener for drag functionality
            textView.setOnTouchListener(new View.OnTouchListener() {
                float xCoOrdinate, yCoOrdinate;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            xCoOrdinate = v.getX() - event.getRawX();
                            yCoOrdinate = v.getY() - event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            v.animate()
                                    .x(event.getRawX() + xCoOrdinate)
                                    .y(event.getRawY() + yCoOrdinate)
                                    .setDuration(0)
                                    .start();
                            break;
                        default:
                            return false;
                    }
                    return true;
                }
            });

            // Add the TextView to the canvas
            canvasLayout.addView(textView);
            currentTextView = textView;

            // Update undo/redo stacks
            undoStack.push(new TextAction(ActionType.ADD, textView));
            redoStack.clear();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }

    private void undoAction() {
        if (!undoStack.isEmpty()) {
            TextAction lastAction = undoStack.pop();
            redoStack.push(lastAction);
            if (lastAction.actionType == ActionType.ADD) {
                canvasLayout.removeView(lastAction.textView);
            }
        } else {
            Toast.makeText(this, "No more actions to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoAction() {
        if (!redoStack.isEmpty()) {
            TextAction lastAction = redoStack.pop();
            undoStack.push(lastAction);
            if (lastAction.actionType == ActionType.ADD) {
                canvasLayout.addView(lastAction.textView);
            }
        } else {
            Toast.makeText(this, "No more actions to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleBold() {
        if (currentTextView != null) {
            isBold = !isBold;
            currentTextView.setTypeface(null, isBold ? Typeface.BOLD : Typeface.NORMAL);
            undoStack.push(new TextAction(ActionType.STYLE, currentTextView));
            redoStack.clear();
        }
    }

    private void toggleItalic() {
        if (currentTextView != null) {
            isItalic = !isItalic;
            currentTextView.setTypeface(null, isItalic ? Typeface.ITALIC : Typeface.NORMAL);
            undoStack.push(new TextAction(ActionType.STYLE, currentTextView));
            redoStack.clear();
        }
    }

    private void toggleUnderline() {
        if (currentTextView != null) {
            isUnderlined = !isUnderlined;
            currentTextView.setPaintFlags(isUnderlined ? currentTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG : currentTextView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            undoStack.push(new TextAction(ActionType.STYLE, currentTextView));
            redoStack.clear();
        }
    }

    private void selectFont(String fontName) {
        if (currentTextView != null) {
            fontFamily = fontName;
            currentTextView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
            undoStack.push(new TextAction(ActionType.FONT, currentTextView));
            redoStack.clear();
        }
    }

    private void adjustFontSize(int adjustment) {
        if (currentTextView != null) {
            fontSize += adjustment;
            currentTextView.setTextSize(fontSize);
            undoStack.push(new TextAction(ActionType.SIZE, currentTextView));
            redoStack.clear();
        }
    }

    private void toggleAlignment() {
        if (currentTextView != null) {
            if (textAlign == View.TEXT_ALIGNMENT_TEXT_START) {
                textAlign = View.TEXT_ALIGNMENT_CENTER;
            } else if (textAlign == View.TEXT_ALIGNMENT_CENTER) {
                textAlign = View.TEXT_ALIGNMENT_TEXT_END;
            } else {
                textAlign = View.TEXT_ALIGNMENT_TEXT_START;
            }
            currentTextView.setTextAlignment(textAlign);
            undoStack.push(new TextAction(ActionType.ALIGNMENT, currentTextView));
            redoStack.clear();
        }
    }

    private static class TextAction {
        ActionType actionType;
        TextView textView;

        TextAction(ActionType actionType, TextView textView) {
            this.actionType = actionType;
            this.textView = textView;
        }
    }

    private enum ActionType {
        ADD,
        STYLE,
        SIZE,
        FONT,
        ALIGNMENT
    }
}

