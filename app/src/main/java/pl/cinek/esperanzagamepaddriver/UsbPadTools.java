package pl.cinek.esperanzagamepaddriver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbPadTools implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String PERMISSION_ACTION = "pl.cinek.esperanzagamepaddriver.PERMISSION_ACTION";
	public static final long DPAD_HOLD_DELAY_MS = 350;
	public static final long DPAD_HOLD_REPEAT_MS = 100;

	Context context;
	MyThread myThread;
	UsbManager usbManager;
	UsbDevice usbDevice;
	UsbDeviceConnection openDevice;
	UsbInterface usbInterface;
	UsbEndpoint usbEndpoint;
	List<Integer> buttonsPressed = new ArrayList<>();
	List<Integer> lastButtonsPressed = new ArrayList<>();
	SharedPreferences sp;
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
				loadDevices();
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
			} else if (PERMISSION_ACTION.equals(intent.getAction())) {
				continueAfterPermission();
			}
		}
	};
	boolean usb_button_a_with_dpad_center, usb_button_x_with_dpad_center, usb_button_b_with_back, usb_button_y_with_back;
	float l_thumb_sensitivity, r_thumb_sensitivity;
	float last_AXIS_X;
	float last_AXIS_Y;
	List<GamePadButton> gamePadButtons = new ArrayList<>();
	GamePadButton DPAD_UP = new GamePadButton("DPAD_UP", null, KeyEvent.KEYCODE_DPAD_UP, 103);
	GamePadButton DPAD_RIGHT = new GamePadButton("DPAD_RIGHT", null, KeyEvent.KEYCODE_DPAD_RIGHT, 106);
	GamePadButton DPAD_DOWN = new GamePadButton("DPAD_DOWN", null, KeyEvent.KEYCODE_DPAD_DOWN, 108);
	GamePadButton DPAD_LEFT = new GamePadButton("DPAD_LEFT", null, KeyEvent.KEYCODE_DPAD_LEFT, 105);
	GamePadButton BUTTON_A = new GamePadButton("BUTTON_A", null, KeyEvent.KEYCODE_BUTTON_A, 304);
	GamePadButton BUTTON_B = new GamePadButton("BUTTON_B", null, KeyEvent.KEYCODE_BUTTON_B, 305);
	GamePadButton BUTTON_X = new GamePadButton("BUTTON_X", null, KeyEvent.KEYCODE_BUTTON_X, 307);
	GamePadButton BUTTON_Y = new GamePadButton("BUTTON_Y", null, KeyEvent.KEYCODE_BUTTON_Y, 308);
	GamePadButton BACK = new GamePadButton("BACK", null, KeyEvent.KEYCODE_BACK, 158);
	GamePadButton DPAD_CENTER = new GamePadButton("DPAD_CENTER", null, KeyEvent.KEYCODE_DPAD_CENTER, 353);
	GamePadButton BUTTON_L1 = new GamePadButton("BUTTON_L1", null, KeyEvent.KEYCODE_BUTTON_L1, 310);
	GamePadButton BUTTON_R1 = new GamePadButton("BUTTON_R1", null, KeyEvent.KEYCODE_BUTTON_R1, 311);
	GamePadButton BUTTON_SELECT = new GamePadButton("BUTTON_SELECT", Prefs.USB_ACTION_SELECT, KeyEvent.KEYCODE_BUTTON_SELECT, 0);
	GamePadButton BUTTON_START = new GamePadButton("BUTTON_START", Prefs.USB_ACTION_START, KeyEvent.KEYCODE_BUTTON_START, 0);
	GamePadButton BUTTON_THUMBL = new GamePadButton("BUTTON_THUMBL", null, KeyEvent.KEYCODE_BUTTON_THUMBL, 317);
	GamePadButton BUTTON_THUMBR = new GamePadButton("BUTTON_THUMBR", null, KeyEvent.KEYCODE_BUTTON_THUMBR, 318);

	GamePadButton AXIS_LTRIGGER = new GamePadButton("AXIS_LTRIGGER", Prefs.USB_ACTION_LTRIGGER, KeyEvent.KEYCODE_BUTTON_L2, 0);//powiino być MotionEvent.AXIS_LTRIGGER ale nie da sie zaimplementować inject MotionEvent
	GamePadButton AXIS_RTRIGGER = new GamePadButton("AXIS_RTRIGGER", Prefs.USB_ACTION_RTRIGGER, KeyEvent.KEYCODE_BUTTON_R2, 0);//MotionEvent.AXIS_RTRIGGER

	public UsbPadTools(Context context) {
		this.context = context;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.registerOnSharedPreferenceChangeListener(this);
		loadsPrefs();
		gamePadButtons.add(BACK);
		gamePadButtons.add(DPAD_CENTER);
		gamePadButtons.add(DPAD_UP);
		gamePadButtons.add(DPAD_RIGHT);
		gamePadButtons.add(DPAD_DOWN);
		gamePadButtons.add(DPAD_LEFT);
		gamePadButtons.add(BUTTON_A);
		gamePadButtons.add(BUTTON_B);
		gamePadButtons.add(BUTTON_X);
		gamePadButtons.add(BUTTON_Y);
		gamePadButtons.add(BUTTON_L1);
		gamePadButtons.add(BUTTON_R1);
		gamePadButtons.add(BUTTON_SELECT);
		gamePadButtons.add(BUTTON_START);

		gamePadButtons.add(BUTTON_THUMBL);
		gamePadButtons.add(BUTTON_THUMBR);

		gamePadButtons.add(AXIS_LTRIGGER);
		gamePadButtons.add(AXIS_RTRIGGER);
		IntentFilter iF = new IntentFilter(PERMISSION_ACTION);
		iF.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		iF.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		context.registerReceiver(receiver, iF);
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		loadDevices();
	}

	public void destroy() {
		sp.unregisterOnSharedPreferenceChangeListener(this);
		context.unregisterReceiver(receiver);
	}

	public void loadDevices() {
		HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
		for (String key : devices.keySet()) {
			UsbDevice usbDevice = devices.get(key);
			int vendorId = usbDevice.getVendorId();//121
			int productId = usbDevice.getProductId();//6
			String hexString = Integer.toHexString(vendorId);
			String str = "0000000000" + Integer.toHexString(productId);
			String substring = str.substring(str.length() - 4);
			if (hexString.equals("79") && substring.equals("0006")) {
				this.usbDevice = usbDevice;
				if (usbManager.hasPermission(usbDevice))
					continueAfterPermission();
				else
					usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, new Intent(PERMISSION_ACTION), 0));
			}
		}
	}

	public void continueAfterPermission() {
		if (myThread != null)
			myThread.stopRunning();
		openDevice = usbManager.openDevice(usbDevice);
		usbInterface = usbDevice.getInterface(0);
		for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
			UsbEndpoint endpoint = usbInterface.getEndpoint(i);
			if (endpoint.getType() == 3 && endpoint.getDirection() == 128)
				usbEndpoint = endpoint;
		}
		if (usbEndpoint != null && openDevice != null && openDevice.claimInterface(usbInterface, true)) {
			myThread = new MyThread();
			myThread.start();
		}
	}

	public boolean checkBit(byte data, int position) {
		return getBit(data, position) == 1;
	}

	public byte getBit(byte data, int position) {
		return (byte) ((data >> position) & 1);
	}

	public String process(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			if (i < 5)
				sb.append(" ").append(String.valueOf(data[i]));
			else
				sb.append(" ").append(String.format("%8s", Integer.toBinaryString(data[i] & 255)).replace(' ', '0'));
		}
		sb.append("\n\n");

		long time = SystemClock.uptimeMillis();

		int leftX = data[0] & 255;
		int leftY = data[1] & 255;
		float AXIS_X = (leftX - 128) / 128f;
		float AXIS_Y = (leftY - 128) / 128f;
		sb.append("AXIS_X=").append(AXIS_X).append("\n");
		sb.append("AXIS_Y=").append(AXIS_Y).append("\n");

		if (AXIS_X != last_AXIS_X || AXIS_Y != last_AXIS_Y) {
			//to nie bardzo działa niestety
			MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
			MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
			coords.setAxisValue(MotionEvent.AXIS_X, AXIS_X);
			coords.setAxisValue(MotionEvent.AXIS_Y, AXIS_Y);
			MotionEvent motionEvent = MotionEvent.obtain(time, time,
					MotionEvent.ACTION_MOVE, 1, new MotionEvent.PointerProperties[]{properties},
					new MotionEvent.PointerCoords[]{coords}, 0, 0,
					1.0f, 1.0f, 0,
					0, 0, 0);
			TestActivity activity = TestActivity.mThis;
			if (activity != null)
				activity.runOnUiThread(() -> activity.dispatchGenericMotionEvent(motionEvent));
		}

		last_AXIS_X = AXIS_X;
		last_AXIS_Y = AXIS_Y;

		int rightX = data[3] & 255;
		int rightY = data[4] & 255;
		float AXIS_Z = (rightX - 128) / 128f;
		float AXIS_RZ = (rightY - 128) / 128f;
		sb.append("AXIS_Z=").append(AXIS_Z).append("\n");
		sb.append("AXIS_RZ=").append(AXIS_RZ).append("\n");

		sb.append("\n");

		buttonsPressed.clear();

		byte bits1 = data[5];
		boolean checkDpadButtonBits = !checkBit(bits1, 0) && !checkBit(bits1, 3);
		byte arrows = (byte) (getBit(bits1, 1) + 2 * getBit(bits1, 2));
		DPAD_UP.setPressed((checkDpadButtonBits && arrows == 0) || AXIS_Y < -l_thumb_sensitivity || AXIS_RZ < -r_thumb_sensitivity);
		DPAD_RIGHT.setPressed((checkDpadButtonBits && arrows == 1) || AXIS_X > l_thumb_sensitivity || AXIS_Z > r_thumb_sensitivity);
		DPAD_DOWN.setPressed((checkDpadButtonBits && arrows == 2) || AXIS_Y > l_thumb_sensitivity || AXIS_RZ > r_thumb_sensitivity);
		DPAD_LEFT.setPressed((checkDpadButtonBits && arrows == 3) || AXIS_X < -l_thumb_sensitivity || AXIS_Z < -r_thumb_sensitivity);

		BUTTON_A.setPressed(checkBit(bits1, 6));
		BUTTON_X.setPressed(checkBit(bits1, 7));
		DPAD_CENTER.setPressed((BUTTON_A.pressed && usb_button_a_with_dpad_center) || (BUTTON_X.pressed && usb_button_x_with_dpad_center));
		BUTTON_Y.setPressed(checkBit(bits1, 4));
		BUTTON_B.setPressed(checkBit(bits1, 5));
		BACK.setPressed((BUTTON_B.pressed && usb_button_b_with_back) || (BUTTON_Y.pressed && usb_button_y_with_back));

		byte bits2 = data[6];
		BUTTON_L1.setPressed(checkBit(bits2, 0));
		BUTTON_R1.setPressed(checkBit(bits2, 1));
		AXIS_LTRIGGER.setPressed(checkBit(bits2, 2));
		AXIS_RTRIGGER.setPressed(checkBit(bits2, 3));
		BUTTON_SELECT.setPressed(checkBit(bits2, 4));
		BUTTON_START.setPressed(checkBit(bits2, 5));
		BUTTON_THUMBL.setPressed(checkBit(bits2, 6));
		BUTTON_THUMBR.setPressed(checkBit(bits2, 7));

		byte bits3 = data[7];
		for (GamePadButton button : gamePadButtons) {
			if (button.pressed && !button.lastPressed) {
				button.clickCount++;
				int clickCountNow = button.clickCount;
				if (button == DPAD_UP || button == DPAD_DOWN || button == DPAD_LEFT || button == DPAD_RIGHT)
					new Thread(() -> {
						try {
							Thread.sleep(DPAD_HOLD_DELAY_MS);
							while (button.pressed && clickCountNow == button.clickCount) {
								button.runAction(context, KeyEvent.ACTION_DOWN);
								Thread.sleep(DPAD_HOLD_REPEAT_MS);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}).start();
				button.runAction(context, KeyEvent.ACTION_DOWN);
			}
			if (!button.pressed && button.lastPressed) {
				button.clickCount++;
				button.runAction(context, KeyEvent.ACTION_UP);
			}
			if (button.pressed)
				sb.append(button.name).append("\n");
		}
		lastButtonsPressed.clear();
		lastButtonsPressed.addAll(buttonsPressed);
		return sb.toString();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadsPrefs();
	}

	public void loadsPrefs() {
		usb_button_a_with_dpad_center = sp.getBoolean(Prefs.USB_BUTTON_A_WITH_DPAD_CENTER, true);
		usb_button_x_with_dpad_center = sp.getBoolean(Prefs.USB_BUTTON_X_WITH_DPAD_CENTER, true);
		usb_button_b_with_back = sp.getBoolean(Prefs.USB_BUTTON_B_WITH_BACK, true);
		usb_button_y_with_back = sp.getBoolean(Prefs.USB_BUTTON_Y_WITH_BACK, true);
		l_thumb_sensitivity = Float.parseFloat(sp.getString(Prefs.USB_L_THUMB_SENSITIVITY, "0.5"));
		r_thumb_sensitivity = Float.parseFloat(sp.getString(Prefs.USB_R_THUMB_SENSITIVITY, "0.5"));
	}

	public class MyThread extends Thread {

		boolean running = true;

		@Override
		public void run() {
			try {
				while (running) {
					byte[] data = new byte[8];
					int bytesCount = openDevice.bulkTransfer(usbEndpoint, data, data.length, 1000);
					if (bytesCount >= 0) {
						String str = process(data);
						TestActivity activity = TestActivity.mThis;
						if (activity != null)
							activity.runOnUiThread(() -> activity.usbDataText.setText(str));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void stopRunning() {
			this.running = false;
		}
	}

}