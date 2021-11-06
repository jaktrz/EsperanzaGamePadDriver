package pl.cinek.esperanzagamepaddriver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint({"SetTextI18n", "StaticFieldLeak"})
public class MainActivity extends AppCompatActivity {

	Button appSettingsButton, gamepadTestButton, keyboardSettingsButton, keyboardSelectorButton, accessibilitySettingsButton;
	InputMethodManager imm;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		setContentView(R.layout.activity_main);
		appSettingsButton = findViewById(R.id.appSettingsButton);
		gamepadTestButton = findViewById(R.id.gamepadTestButton);
		keyboardSettingsButton = findViewById(R.id.keyboardSettingsButton);
		keyboardSelectorButton = findViewById(R.id.keyboardSelectorButton);
		accessibilitySettingsButton = findViewById(R.id.accessibilitySettingsButton);
		appSettingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
		gamepadTestButton.setOnClickListener(v -> startActivity(new Intent(this, TestActivity.class)));
		keyboardSettingsButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
		keyboardSelectorButton.setOnClickListener(v -> imm.showInputMethodPicker());
		accessibilitySettingsButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
	}

}