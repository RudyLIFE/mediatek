/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.android.simmelock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;//To invoke the telephony function
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.IccCard;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;

public class SIMPAddLockSetting extends SimLockBaseActivity {
    private static final int DIALOG_IMSILENGTHINCORRECT = 1;
    private static final int DIALOG_ADDLOCKFAIL = 2;
    private static final int DIALOG_ADDLOCKSUCCEED = 3;
    private static final int DIALOG_PASSWORDLENGTHINCORRECT = 4;
    private static final int DIALOG_PASSWORDWRONG = 5;
    private static final int ADDLOCK_ICC_SML_COMPLETE = 120;

    private EditText etImsi = null;
    private EditText etPwd = null;
    private EditText etPwdConfirm = null;
    private Spinner s1;

    private String mSimImsi = null;
    private boolean mImsiReadSim = false;
    private boolean clickFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simpaddlocksetting);

        etImsi = (EditText) findViewById(R.id.idsimpaddlockEditInputIMSI);
        // Let the user to choose "put in" for just read MCCMNC from the SIM
        // card
        s1 = (Spinner) findViewById(R.id.spinnersimp1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Input_mode,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        AdapterView.OnItemSelectedListener l = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 == 0) {
                    etImsi.setVisibility(View.VISIBLE);
                    etImsi.requestFocus();
                    // set the regulation of EditText
                    SMLCommonProcess.limitEditText(etImsi, 15);
                    mImsiReadSim = false;
                } else {
                    mImsiReadSim = true;
                    etImsi.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        };
        s1.setOnItemSelectedListener(l);

        etPwd = (EditText) findViewById(R.id.idsimpaddlockEditInputPassword);
        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        SMLCommonProcess.limitEditTextPassword(etPwd, 8);
        etPwdConfirm = (EditText) findViewById(R.id.idsimpaddlockEditInputPasswordAgain);
        etPwdConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        SMLCommonProcess.limitEditTextPassword(etPwdConfirm, 8);
        etImsi.setOnLongClickListener(mOnLongClickListener);
        etPwd.setOnLongClickListener(mOnLongClickListener);
        etPwdConfirm.setOnLongClickListener(mOnLongClickListener);

        // Press the CONFIRM Button
        Button btnConfirm = (Button) findViewById(R.id.idsimpaddlockButtonConfirm);
        // Yu for ICS
        btnConfirm.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                log("clickFlag: " + clickFlag);
                if (clickFlag) {
                    return;
                } else {
                    clickFlag = true;
                }
                // do something after verify all the input things,for example:
                // write a password to the modem
                if ((!mImsiReadSim) && (15 > etImsi.getText().length())) {
                    showDialog(DIALOG_IMSILENGTHINCORRECT);
                } else if (mImsiReadSim && ((mSimImsi == null) || (15 > mSimImsi.length()))) {
                    showDialog(DIALOG_IMSILENGTHINCORRECT);
                } else if ((etPwd.getText().length() < 4) || ((etPwd.getText().length() > 8))) {
                    showDialog(DIALOG_PASSWORDLENGTHINCORRECT);
                } else if (etPwd.getText().toString().equals(etPwdConfirm.getText().toString()) == false) {
                    showDialog(DIALOG_PASSWORDWRONG);
                } else {
                    Message callback = Message.obtain(mHandler, ADDLOCK_ICC_SML_COMPLETE);
                    setIccNetworkLockEnabled(4, 2, etPwd.getText().toString(),
                            mImsiReadSim ? mSimImsi : etImsi.getText().toString(), null, null, callback);
                }
            }
        });

        // Press the CANCEL Button
        Button btnCancel = (Button) findViewById(R.id.idsimpaddlockButtonCancel);
        btnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // do nothing to quit the edit page
                SIMPAddLockSetting.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSimReady()) {
            log("Add NP lock fail : SIM not ready!");
            return;
        }

        // To get the IMSI from SIM card
        if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            mSimImsi = telephonyManager.getSubscriberId();
        } else {
            TelephonyManagerEx telephonyManagerEx = TelephonyManagerEx.getDefault();
            mSimImsi = telephonyManagerEx.getSubscriberId(simNumber);
        }

        if (mSimImsi == null) {
            log("Fail to read SIM IMSI!");
        } else {
            log("Read SIM IMSI: " + mSimImsi);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.strAttention)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setOnKeyListener(this);

        switch (id) {
        case DIALOG_ADDLOCKFAIL: // Fail
            builder.setMessage(R.string.strAddLockFail).setPositiveButton(R.string.strYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etImsi.setText("");
                            etPwd.setText("");
                            etPwdConfirm.setText("");
                            dialog.cancel();
                            clickFlag = false;
                        }
                    });
            return builder.create();
        case DIALOG_PASSWORDLENGTHINCORRECT:// Length is incorrect
            builder.setMessage(R.string.strPasswordLengthIncorrect).setPositiveButton(R.string.strYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etPwd.setText("");
                            etPwdConfirm.setText("");
                            dialog.cancel();
                            clickFlag = false;
                        }
                    });
            return builder.create();
        case DIALOG_IMSILENGTHINCORRECT:// Length is incorrect
            builder.setMessage(R.string.strIMSILengthIncorrect).setPositiveButton(R.string.strYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etImsi.setText("");
                            dialog.cancel();
                            clickFlag = false;
                        }
                    });
            return builder.create();
        case DIALOG_ADDLOCKSUCCEED:// Succeed
            builder.setMessage(R.string.strAddLockSucceed).setPositiveButton(R.string.strYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etImsi.setText("");
                            etPwd.setText("");
                            etPwdConfirm.setText("");
                            dialog.cancel();
                            clickFlag = false;
                            SIMPAddLockSetting.this.finish();
                        }
                    });
            return builder.create();
        case DIALOG_PASSWORDWRONG:// Wrong password
            builder.setMessage(R.string.str_simme_passwords_dont_match).setPositiveButton(R.string.strYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etPwd.setText("");
                            etPwdConfirm.setText("");
                            dialog.cancel();
                            clickFlag = false;
                        }
                    });
            return builder.create();
        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
            case ADDLOCK_ICC_SML_COMPLETE:
                if (ar.exception != null) {
                    showDialog(DIALOG_ADDLOCKFAIL);
                } else {
                    showDialog(DIALOG_ADDLOCKSUCCEED);
                }
                break;
            default:
                break;
            }
        }
    };
}
