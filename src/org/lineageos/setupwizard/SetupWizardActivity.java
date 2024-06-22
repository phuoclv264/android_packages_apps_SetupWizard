/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017,2019 The LineageOS Project
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

package org.lineageos.setupwizard;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_LOAD;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_SCRIPT_URI;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;
import org.lineageos.setupwizard.wizardmanager.WizardManager;

public class SetupWizardActivity extends BaseSetupWizardActivity {
    private static final String TAG = SetupWizardActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }

        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1);

        if (SetupWizardUtils.hasGMS(this)) {
            SetupWizardUtils.disableHome(this);
            finish();
        } else {
            SetupWizardUtils.finishSetupWizard(this);
            finish();
        }
    }
}
