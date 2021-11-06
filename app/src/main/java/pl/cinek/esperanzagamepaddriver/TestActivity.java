package pl.cinek.esperanzagamepaddriver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint({"SetTextI18n", "StaticFieldLeak"})
public class TestActivity extends AppCompatActivity {

	public static TestActivity mThis;
	TextView usbDataText, keyEventText;
	ScrollView scrollView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mThis = this;
		setContentView(R.layout.activity_test);
		usbDataText = findViewById(R.id.usbDataText);
		keyEventText = findViewById(R.id.keyEventText);
		//scrollView = findViewById(R.id.scrollView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mThis = null;
	}

	@Override
	public void onBackPressed() {
	}

	public void processKeyEvent(KeyEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append(event.toString());
		Log.e("processKeyEvent", "event=" + sb.toString());
		runOnUiThread(() -> {
			keyEventText.setText(sb.toString() + "\n" + keyEventText.getText());//.replace(", ", "\n")
			//scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		processKeyEvent(event);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		processKeyEvent(event);
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		processKeyEvent(event);
		if (keyCode == KeyEvent.KEYCODE_BACK)
			finish();
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		processKeyEvent(event);
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		processKeyEvent(event);
		return super.onKeyShortcut(keyCode, event);
	}

}