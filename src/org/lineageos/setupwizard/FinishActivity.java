/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2020 The LineageOS Project
 * Copyright (C) 2018-2019 e.foundation
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

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_COMPLETE;
import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_BUTTON_BACKLIGHT;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_SEND_METRICS;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.animation.Animator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.EnableAccessibilityController;

import lineageos.providers.LineageSettings;

import static android.os.Binder.getCallingUserHandle;
import static org.lineageos.setupwizard.Manifest.permission.FINISH_SETUP;

public class FinishActivity extends BaseSetupWizardActivity {

    public static final String TAG = FinishActivity.class.getSimpleName();

    private ImageView mReveal;

    private EnableAccessibilityController mEnableAccessibilityController;

    private SetupWizardApp mSetupWizardApp;

    private final Handler mHandler = new Handler();

    private volatile boolean mIsFinishing = false;


    // Component name of default weather provider.
    private static final String OWM =
            "org.lineageos.openweathermapprovider/org.lineageos.openweathermapprovider"
                    + ".OpenWeatherMapProviderService";

    private static final int THEME = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=" + savedInstanceState);
        }
        mSetupWizardApp = (SetupWizardApp) getApplication();
        mReveal = (ImageView) findViewById(R.id.reveal);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        setNextText(R.string.start);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.finish_activity;
    }

    @Override
    public void finish() {
        super.finish();
        if (!isResumed() || mResultCode != RESULT_CANCELED) {
            overridePendingTransition(R.anim.translucent_enter, R.anim.translucent_exit);
        }
    }

    @Override
    public void onNavigateNext() {
        applyForwardTransition(TRANSITION_ID_NONE);
        startFinishSequence();
    }

    private void finishSetup() {
        if (!mIsFinishing) {
            mIsFinishing = true;
            //setupRevealImage();// /e/ remove
            // eelo 20180527 - fp - end animation cannot be displayed smoothly on some devices (like zerofltexx)
            // Thus byass animation process.
            // eelo add
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    completeSetup();
                }
            });
            // /e/

        }
    }

    private void startFinishSequence() {
        Intent i = new Intent(ACTION_SETUP_COMPLETE);
        i.setPackage(getPackageName());
        sendBroadcastAsUser(i, getCallingUserHandle(), FINISH_SETUP);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideBackButton();
        hideNextButton();
        finishSetup();
    }

    private void setupRevealImage() {
        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(this);
        wallpaperManager.forgetLoadedWallpaper();
        final Bitmap wallpaper = wallpaperManager.getBitmap();
        Bitmap cropped = null;
        if (wallpaper != null) {
            cropped = Bitmap.createBitmap(wallpaper, 0,
                    0, Math.min(p.x, wallpaper.getWidth()),
                    Math.min(p.y, wallpaper.getHeight()));
        }
        if (cropped != null) {
            mReveal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mReveal.setImageBitmap(cropped);
        } else {
            mReveal.setBackground(wallpaperManager
                    .getBuiltInDrawable(p.x, p.y, false, 0, 0));
        }
        animateOut();
    }

    private void animateOut() {
        int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
        int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
        int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);
        anim.setDuration(900);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        completeSetup();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    private void completeSetup() {
        if (mEnableAccessibilityController != null) {
            mEnableAccessibilityController.onDestroy();
        }
        setupWeatherProvider();
        setupTheme();
        handleEnableMetrics(mSetupWizardApp);
        handleNavKeys(mSetupWizardApp);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(mSetupWizardApp);
        wallpaperManager.forgetLoadedWallpaper();
        finishAllAppTasks();
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(),
                Activity.RESULT_OK);
        startActivityForResult(intent, NEXT_REQUEST);
    }

    private void setupWeatherProvider() {
        LineageSettings.Secure.putString(getContentResolver(),
                        LineageSettings.Secure.WEATHER_PROVIDER_SERVICE, OWM);
    }

    private void setupTheme() {
        LineageSettings.System.putInt(getContentResolver(),
                        LineageSettings.System.BERRY_GLOBAL_STYLE, THEME);
    }

    private static void handleEnableMetrics(SetupWizardApp setupWizardApp) {
        LineageSettings.Secure.putInt(setupWizardApp.getContentResolver(),
            LineageSettings.Secure.STATS_COLLECTION, 0);
    }

    private static void handleNavKeys(SetupWizardApp setupWizardApp) {
        if (setupWizardApp.getSettingsBundle().containsKey(DISABLE_NAV_KEYS)) {
            writeDisableNavkeysOption(setupWizardApp,
                    setupWizardApp.getSettingsBundle().getBoolean(DISABLE_NAV_KEYS));
        }
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final boolean virtualKeysEnabled = LineageSettings.System.getIntForUser(
                    context.getContentResolver(), LineageSettings.System.FORCE_SHOW_NAVBAR, 0,
                    UserHandle.USER_CURRENT) != 0;
        if (enabled != virtualKeysEnabled) {
            LineageSettings.System.putIntForUser(context.getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }

        /* Save/restore button timeouts to disable them in softkey mode */
        if (enabled) {
            LineageSettings.Secure.putInt(context.getContentResolver(),
                    LineageSettings.Secure.BUTTON_BRIGHTNESS, 0);
        } else {
            int currentBrightness = LineageSettings.Secure.getInt(context.getContentResolver(),
                    LineageSettings.Secure.BUTTON_BRIGHTNESS, 100);
            int oldBright = prefs.getInt(KEY_BUTTON_BACKLIGHT,
                    currentBrightness);
            LineageSettings.Secure.putInt(context.getContentResolver(),
                    LineageSettings.Secure.BUTTON_BRIGHTNESS, oldBright);
        }
    }
}
