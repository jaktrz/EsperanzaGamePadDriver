package pl.cinek.esperanzagamepaddriver;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

	public static MyAccessibilityService mThis;
	SharedPreferences sp;

	public static void performAction(int action) {
		if (mThis != null)
			mThis.performGlobalAction(action);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThis = this;
		sp = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onDestroy() {
		mThis = null;
		super.onDestroy();
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_EXPLORER) {
			if (event.getAction() == KeyEvent.ACTION_UP)
				Utils.openTvAppsDrawer(this);
			return true;
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_MUSIC) {
			if (event.getAction() == KeyEvent.ACTION_UP)
				startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			return true;
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENVELOPE || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
			if (event.getAction() == KeyEvent.ACTION_UP)
				performAction(GLOBAL_ACTION_BACK);
			return true;
		}
		return super.onKeyEvent(event);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
	}

	@Override
	public void onServiceConnected() {
	}

	@Override
	public void onInterrupt() {
	}

}