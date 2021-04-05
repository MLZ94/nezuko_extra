/*
 * Copyright (C) 2017-2019 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nezuko.extra.tabs;



import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.provider.Settings;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import android.hardware.fingerprint.FingerprintManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragment;
import com.android.internal.util.custom.fod.FodUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.nezuko.extra.preferences.CustomSeekBarPreference;

public class Lockscreen extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";

    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private PreferenceCategory mFODIconPickerCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        mFODIconPickerCategory = findPreference(FOD_ICON_PICKER_CATEGORY);

        if (!mFingerprintManager.isHardwareDetected()){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }
        if (mFODIconPickerCategory != null && !FodUtils.hasFodSupport(getContext())) {
            prefScreen.removePreference(mFODIconPickerCategory);
        }
        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);

    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        
        if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mPulseBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, value);
            return true;
        }

        return false;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.NEZUKO;
    }
}
