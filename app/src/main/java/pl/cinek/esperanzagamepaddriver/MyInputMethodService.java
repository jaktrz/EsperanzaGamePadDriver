package pl.cinek.esperanzagamepaddriver;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

	public static MyInputMethodService mThis;
	UsbPadTools usbPadTools;

	public static void keyEvent(KeyEvent keyEvent) {
		if (MyInputMethodService.mThis != null)
			MyInputMethodService.mThis.getCurrentInputConnection().sendKeyEvent(keyEvent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThis = this;
		usbPadTools = new UsbPadTools(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		usbPadTools.destroy();
		mThis = null;
	}

	@Override
	public View onCreateInputView() {
		KeyboardView inputView = new KeyboardView(this, null);
		inputView.setOnKeyboardActionListener(this);
		return inputView;
	}

	//Disable fullscreen keybaord
	@Override
	public void onUpdateExtractingVisibility(EditorInfo ei) {
		setExtractViewShown(false);
	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeUp() {
	}

}