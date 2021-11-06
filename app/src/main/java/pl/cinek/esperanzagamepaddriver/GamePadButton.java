package pl.cinek.esperanzagamepaddriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;

import androidx.preference.PreferenceManager;

import java.util.Objects;

public class GamePadButton {

	public String name, actionPrefName;
	public int keyCode;
	public boolean lastPressed;
	public boolean pressed;
	public int clickCount;
	int scanCode;
	int repeatCount;
	long downTime;
	boolean resetClock = true;

	public GamePadButton(String name, String actionPrefName, int keyCode, int scanCode) {
		this.name = name;
		this.actionPrefName = actionPrefName;
		this.keyCode = keyCode;
		this.scanCode = scanCode;
	}

	public void setPressed(boolean newPressed) {
		lastPressed = pressed;
		pressed = newPressed;
	}

	public void runAction(Context context, int action) {
		if (action == KeyEvent.ACTION_DOWN) {
			if (resetClock) {
				downTime = SystemClock.uptimeMillis();
				repeatCount = 0;
				resetClock = false;
			} else
				repeatCount++;
		}
		if (action == KeyEvent.ACTION_UP) {
			repeatCount = 0;
			resetClock = true;
		}
		if (actionPrefName != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
			String buttonAction = sp.getString(actionPrefName, "default");
			if (buttonAction.equals("none"))
				return;
			if (buttonAction.equals("settings")) {
				if (action == KeyEvent.ACTION_UP)
					context.startActivity(new Intent(context, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				return;
			}
			if (buttonAction.equals("apps")) {
				if (action == KeyEvent.ACTION_UP)
					Utils.openTvAppsDrawer(context);
				return;
			}
			if (!buttonAction.equals("default")) {
				if (action == KeyEvent.ACTION_UP)
					MyAccessibilityService.performAction(Integer.parseInt(buttonAction));
				return;
			}
		}
		if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
			long eventTime = SystemClock.uptimeMillis();
			KeyEvent keyEvent = new KeyEvent(downTime, eventTime, action,
					keyCode, repeatCount, 0,
					7, scanCode, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD | InputDevice.SOURCE_DPAD | InputDevice.SOURCE_GAMEPAD);
			MyInputMethodService.keyEvent(keyEvent);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GamePadButton that = (GamePadButton) o;
		return Objects.equals(keyCode, that.keyCode);
	}

	@Override
	public int hashCode() {
		return keyCode;
	}

}