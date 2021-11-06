package pl.cinek.esperanzagamepaddriver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PrefsFragment extends PreferenceFragmentCompat {

	Activity activity;
	SharedPreferences sp;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.settings);
		activity = getActivity();
		sp = PreferenceManager.getDefaultSharedPreferences(activity);
	}

}
