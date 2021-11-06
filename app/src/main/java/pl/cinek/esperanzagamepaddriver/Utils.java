package pl.cinek.esperanzagamepaddriver;

import android.content.Context;
import android.content.Intent;

public class Utils {

	public static void openTvAppsDrawer(Context context) {
		try {
			context.startActivity(new Intent().setClassName("com.google.android.tvlauncher", "com.google.android.tvlauncher.appsview.AppsViewActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}