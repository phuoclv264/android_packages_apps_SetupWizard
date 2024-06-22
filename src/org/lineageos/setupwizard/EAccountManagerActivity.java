/*
 * Copyright (C) 2018 e.foundation
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

import android.content.Intent;
import android.view.View;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;

public class EAccountManagerActivity extends SubBaseActivity {

    public static final String TAG = EAccountManagerActivity.class.getSimpleName();
    
    private AccountManager accountManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.skip);
    }

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);
        findViewById(R.id.setup_e_account_manager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAccountManagerSetup();
            }
        });
        findViewById(R.id.create_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetupWizardApp.ACTION_CREATE_E_ACCOUNT);
                startSubactivity(intent, SetupWizardApp.REQUEST_CODE_CREATE_E_ACCOUNT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            onNavigateNext();
        }
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_e_account_manager;
    }

    @Override
    protected int getTitleResId() {
        return R.string.e_account_manager_setup_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_account_manager_screen;
    }

    private void launchAccountManagerSetup() {
        try {
            accountManager = AccountManager.get(this);
            accountManager.addAccount("e.foundation.webdav.eelo", null, null, null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    // An eelo account has been added, continue to the next screen
                    onNavigateNext();
                }
            }, null);
        } 
        catch (Exception e) {} 
        finally {}
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }
}