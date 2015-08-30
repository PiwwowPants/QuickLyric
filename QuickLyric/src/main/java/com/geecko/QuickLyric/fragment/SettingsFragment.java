/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.geecko.QuickLyric.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.geecko.QuickLyric.MainActivity;
import com.geecko.QuickLyric.R;
import com.geecko.QuickLyric.utils.NightTimeVerifier;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String NIGHT_START_TIME_DIALOG_TAG = "StartPickerDialog";
    private static final String NIGHT_END_TIME_DIALOG_TAG = "EndPickerDialog";

    private int[] nightTimeStart = new int[]{42, 0};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("pref_contribute").setOnPreferenceClickListener(this);
        // findPreference("pref_beta").setOnPreferenceClickListener(this);
        findPreference("pref_issues").setOnPreferenceClickListener(this);
        findPreference("pref_about").setOnPreferenceClickListener(this);
        findPreference("pref_theme").setOnPreferenceChangeListener(this);
        findPreference("pref_force_screen_on").setOnPreferenceChangeListener(this);
        findPreference("pref_opendyslexic").setOnPreferenceChangeListener(this);
        findPreference("pref_night_mode").setOnPreferenceChangeListener(this);
        findPreference("pref_notifications").setOnPreferenceChangeListener(this);
        findPreference("pref_providers").setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch (pref.getKey()) {
            case "pref_theme":
                if (!newValue.equals(pref.getSharedPreferences().getString("pref_theme", "0"))) {
                    if (NightTimeVerifier.check(getActivity())
                            && pref.getSharedPreferences().getBoolean("pref_night_mode", false))
                        break;
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "pref_force_screen_on":
                if (!newValue.equals(pref.getSharedPreferences().getBoolean("pref_force_screen_on", false))) {
                    getActivity().findViewById(R.id.switcher).setKeepScreenOn((Boolean) newValue);
                    getActivity().findViewById(R.id.lrc_view).setKeepScreenOn((Boolean) newValue);
                }
                break;
            case "pref_opendyslexic":
                if (!newValue.equals(pref.getSharedPreferences().getBoolean("pref_opendyslexic", false))) {
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "pref_notifications":
                if (newValue.equals("0")) {
                    ((NotificationManager) getActivity()
                            .getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
                } else {
                    SharedPreferences current = getActivity().getSharedPreferences("current_music", Context.MODE_PRIVATE);
                    Intent intent = new Intent();
                    intent.setAction("com.geecko.QuickLyric.SHOW_NOTIFICATION");
                    intent.putExtra("artist", current.getString("artist", "Michael Jackson"));
                    intent.putExtra("track", current.getString("track", "Bad"));
                    intent.putExtra("playing", current.getBoolean("playing", false));
                    getActivity().sendBroadcast(intent);
                }
                break;
            case "pref_night_mode":
                if ((Boolean) newValue) {
                    boolean twentyFourHourStyle = DateFormat.is24HourFormat(getActivity());
                    TimePickerDialog tpd = TimePickerDialog
                            .newInstance(this, 21, 0, twentyFourHourStyle);
                    tpd.setCancelable(false);
                    tpd.setTitle(getActivity().getString(R.string.nighttime_start_dialog_title));
                    tpd.show(getFragmentManager(), NIGHT_START_TIME_DIALOG_TAG);
                } else if (NightTimeVerifier.check(getActivity())) {
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        AlertDialog.Builder dialog;
        switch (preference.getKey()) {
            case "pref_about":
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setView(getActivity().getLayoutInflater().inflate(R.layout.about_dialog, (ViewGroup) getView(), false));
                dialog.create().show();
                break;
            case "pref_contribute":
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://github.com/geecko86/QuickLyric"));
                if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(browserIntent);
                break;
            case "pref_beta":
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setView(getActivity().getLayoutInflater().inflate(R.layout.beta_dialog, (ViewGroup) getView(), false));
                dialog.create().show();
                break;
            case "pref_issues":
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"quicklyricapp@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issues with QuickLyric");
                if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(emailIntent);
                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.isHidden())
            return;
        View fragmentView = getView();
        TypedValue typedValue = new TypedValue();
        view.getContext().getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
        if (fragmentView != null)
            fragmentView.setBackgroundColor(typedValue.data);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int h, int min) {
        if (nightTimeStart[0] >= 25) {
            nightTimeStart = new int[]{h, min};
            boolean twentyFourHourStyle = DateFormat.is24HourFormat(getActivity());
            TimePickerDialog tpd = TimePickerDialog
                    .newInstance(this, 6, 0, twentyFourHourStyle);
            tpd.setCancelable(false);
            tpd.setTitle(getActivity().getString(R.string.nighttime_end_dialog_title));
            tpd.show(getFragmentManager(), NIGHT_END_TIME_DIALOG_TAG);
        } else {
            SharedPreferences current = getActivity().getSharedPreferences("night_time", Context.MODE_PRIVATE);
            current.edit().putInt("startHour", nightTimeStart[0])
                    .putInt("startMinute", nightTimeStart[1])
                    .putInt("endHour", h)
                    .putInt("endMinute", min)
                    .apply();

            nightTimeStart[0] = 42;

            if (NightTimeVerifier.check(getActivity())) {
                getActivity().finish();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setAction("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            this.onViewCreated(getView(), null);
    }
}
