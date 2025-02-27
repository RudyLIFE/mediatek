/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.telephony;

import static android.Manifest.permission.READ_PHONE_STATE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.Rlog;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.MTKPhoneFactory;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.worldphone.ModemSwitchHandler;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.common.telephony.IWorldPhone;
import com.mediatek.telephony.WorldPhoneUtil;

/**
 *@hide
 */
public class WorldPhoneOp01 extends Handler implements IWorldPhone {
    private static Object sLock = new Object();
    private static Context sContext;
    private static Phone sPhone;
    private static Phone[] sGsmPhone;
    private static String sOperatorSpec;
    private static String sPlmnSs;
    private static String[] sImsi;
    private static String[] sGsmPlmnStrings;
    private static int sRilRadioTechnology;
    private static int sRegState;
    private static int sState;
    private static int sUserType;
    private static int sRegion;
    private static int sDenyReason;
    private static int sSuspendId;
    private static int s3gSimSlot;
    private static boolean sWaitForDesignateService;
    private static boolean[] sSuspendWaitImsi;
    private static boolean[] sFirstSelect;
    private static CommandsInterface[] sGsmCi;
    private static UiccController[] sUiccController;
    private static IccRecords[] sIccRecordsInstance;
    private static ModemSwitchHandler sModemSwitchHandler;
    private static final String[] MCCMNC_TABLE_TYPE1 = {
        "46000", "46002", "46007",
        // Lab test IMSI
        "00101", "00211", "00321", "00431", "00541", "00651",
        "00761", "00871", "00902", "01012", "01122", "01232",
        "46004", "46009", "46602", "50270", "46003"
    };
    private static final String[] MCCMNC_TABLE_TYPE3 = {
        "46001", "46006", "45407", "46005", "45502"
    };
    private static final String[] MCC_TABLE_DOMESTIC = {
        "460",
        // Lab test PLMN
        "001", "002", "003", "004", "005", "006",
        "007", "008", "009", "010", "011", "012"
    };
    private static int sTddStandByCounter;
    private static int sFddStandByCounter;
    private static boolean sWaitInTdd;
    private static boolean sWaitInFdd;
    private static int[] FDD_STANDBY_TIMER = {
        60
    };
    private static final int[] TDD_STANDBY_TIMER = {
        0, 5, 10, 20, 30
    };

    public WorldPhoneOp01() {
        logd("Constructor invoked");
        sOperatorSpec = SystemProperties.get("ro.operator.optr", NO_OP);
        logd("Operator Spec:" + sOperatorSpec);
        sPhone = MTKPhoneFactory.getDefaultPhone();
        sGsmPhone = new Phone[PhoneConstants.GEMINI_SIM_NUM];
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            logd("Gemini Project");
            for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
                sGsmPhone[i] = ((PhoneProxy)((GeminiPhone)sPhone).getPhonebyId(i)).getActivePhone();
            }
        } else {
            logd("Single Card Project");
            sGsmPhone[0] = ((PhoneProxy)sPhone).getActivePhone();
        }

        sGsmCi = new CommandsInterface[PhoneConstants.GEMINI_SIM_NUM];
        for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
            sGsmCi[i] = ((PhoneBase)sGsmPhone[i]).mCi;
            sGsmCi[i].setOnPlmnChangeNotification(this, EVENT_GSM_PLMN_CHANGED_1 + i, null);
            sGsmCi[i].setOnGSMSuspended(this, EVENT_GSM_SUSPENDED_1 + i, null);
            sGsmCi[i].registerForOn(this, EVENT_RADIO_ON_1 + i, null);
        }

        IntentFilter intentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            logd("3G Switch Supported");
            intentFilter.addAction(TelephonyIntents.EVENT_3G_SWITCH_DONE);
        } else {
            logd("3G Switch Not Supported");
        }
        sContext = sPhone.getContext();
        sContext.registerReceiver(mReceiver, intentFilter);

        sModemSwitchHandler = new ModemSwitchHandler(sGsmCi);
        ModemSwitchHandler.setModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
        logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getModem()));

        sUiccController = new UiccController[PhoneConstants.GEMINI_SIM_NUM];
        sIccRecordsInstance = new IccRecords[PhoneConstants.GEMINI_SIM_NUM];
        sImsi = new String[PhoneConstants.GEMINI_SIM_NUM];
        sSuspendWaitImsi = new boolean[PhoneConstants.GEMINI_SIM_NUM];
        sFirstSelect = new boolean[PhoneConstants.GEMINI_SIM_NUM];

        resetAllProperties();        
        sTddStandByCounter = 0;
        sFddStandByCounter = 0;
        sWaitInTdd = false;
        sWaitInFdd = false;
        if (Settings.Global.getInt(sContext.getContentResolver(), Settings.Global.WORLD_PHONE_AUTO_SELECT_MODE, 1) == 0) {
            logd("Auto select disable");
            s3gSimSlot = AUTO_SELECT_DISABLE;
        } else {
            logd("Auto select enable");
        }
        FDD_STANDBY_TIMER[sFddStandByCounter] = Settings.Global.getInt(
                sContext.getContentResolver(), Settings.Global.WORLD_PHONE_FDD_MODEM_TIMER, FDD_STANDBY_TIMER[sFddStandByCounter]);
        logd("FDD_STANDBY_TIMER = " + FDD_STANDBY_TIMER[sFddStandByCounter] + "s");
    }

    private boolean isAllowCampOn(String plmnString, int slotId) {
        logd("[isAllowCampOn]+ " + plmnString);
        logd("Slot" + slotId + " is 3G SIM");
        logd("User type:" + sUserType);
        sRegion = getRegion(plmnString);
        if (sUserType == sType1User || sUserType == sType2User) {
            sWaitForDesignateService = true;
            if (sRegion == REGION_DOMESTIC) {
                if (ModemSwitchHandler.getModem() == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                    sDenyReason = CAMP_ON_NOT_DENIED;
                    sWaitForDesignateService = false;
                    removeModemStandByTimer();
                    logd("TDD modem, stop searching TD service");
                    logd("[isAllowCampOn]- Camp on OK");
                    return true;
                } else {
                    // WCDMA limited service, WCDMA limited service, expecting type1 service!
                    sDenyReason = DENY_CAMP_ON_REASON_DOMESTIC_WCDMA;
                    removeModemStandByTimer();
                    setRatMode(SET_RAT_TO_2G, slotId);
                    logd("FDD modem, RAT=2g, expecting TD service");
                    logd("[isAllowCampOn]- Camp on OK");
                    return true;
                }
            } else if (sRegion == REGION_FOREIGN) {
                if (ModemSwitchHandler.getModem() == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                    sDenyReason = DENY_CAMP_ON_REASON_NEED_SWITCH_TO_FDD;
                    logd("TDD modem, expecting TD service");
                    logd("[isAllowCampOn]- Camp on REJECT");
                    return false;
                } else {
                    // WCDMA full service
                    sDenyReason = CAMP_ON_NOT_DENIED;
                    removeModemStandByTimer();
                    setRatMode(SET_RAT_TO_AUTO, slotId);
                    logd("FDD modem, expecting TD service");
                    logd("[isAllowCampOn]- Camp on OK");
                    return true;
                }
            } else {
                logd("Unknow region");
            }
        } else if (sUserType == sType3User) {
            sWaitForDesignateService = false;
            if (ModemSwitchHandler.getModem() == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                // should not enter this state
                logd("Should not happen! TDD modem, Type3 user allow GSM full service");
                logd("[isAllowCampOn]- Camp on OK");
                return true;
            } else {
                // no TD, GSM; WCDMA limited service, RAT = 2g
                sDenyReason = CAMP_ON_NOT_DENIED;
                logd("FDD modem, GSM full service or WCDMA limited service");
                logd("[isAllowCampOn]- Camp on OK");
                return true;
            }
        } else {
            logd("Unknown user type");
        }
        sWaitForDesignateService = true;
        sDenyReason = DENY_CAMP_ON_REASON_UNKNOWN;
        logd("[isAllowCampOn]- Camp on REJECT");
        return false;
    }

    private void handleNoService() {
        logd("[handleNoService]+ Can not find service");
        logd("Type" + sUserType + " user");
        logd(WorldPhoneUtil.regionToString(sRegion));
        if (sUserType == sType1User || sUserType == sType2User) {
            sWaitForDesignateService = true;
            if (ModemSwitchHandler.getModem() == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                // Switch to FDD modem anyway
                logd("TDD modem, expecting TD service");
                if (TDD_STANDBY_TIMER[sTddStandByCounter] >= 0) {
                    if (!sWaitInTdd) {
                        sWaitInTdd = true;
                        logd("Wait " + TDD_STANDBY_TIMER[sTddStandByCounter] + "s. Timer index = " + sTddStandByCounter);
                        postDelayed(mTddStandByTimerRunnable, TDD_STANDBY_TIMER[sTddStandByCounter]*1000);
                    } else {
                        logd("Timer already set:" + TDD_STANDBY_TIMER[sTddStandByCounter] + "s");
                    }
                } else {
                    logd("Standby in TDD modem");
                }
            } else {
                logd("FDD modem, expecting TD service");
                if (FDD_STANDBY_TIMER[sFddStandByCounter] >= 0) {
                    if (sRegion == REGION_FOREIGN) {
                        if (!sWaitInFdd) {
                            sWaitInFdd = true;
                            logd("Wait " + FDD_STANDBY_TIMER[sFddStandByCounter] + "s. Timer index = " + sFddStandByCounter);
                            postDelayed(mFddStandByTimerRunnable, FDD_STANDBY_TIMER[sFddStandByCounter]*1000);
                        } else {
                            logd("Timer already set:" + FDD_STANDBY_TIMER[sFddStandByCounter] + "s");
                        }
                    } else {
                        handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
                    }
                } else {
                    logd("Standby in FDD modem");
                }
            }
        } else if (sUserType == sType3User) {
            sWaitForDesignateService = false;
            if (ModemSwitchHandler.getModem() == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                // Should not enter this state
                logd("Should not happen! Type3 user, TDD modem");
                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
            } else {
                logd("FDD modem -> keep trying in FDD modem");
            }
        } else {
            logd("Unknow user type");
        }
        logd("[handleNoService]-");
        return;
    }

    private Runnable mTddStandByTimerRunnable = new Runnable() {
        public void run() {
            sTddStandByCounter++;
            if (sTddStandByCounter >= TDD_STANDBY_TIMER.length) {
                sTddStandByCounter = TDD_STANDBY_TIMER.length-1;
            }
            logd("TDD time out!");
            handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
        }
    };

    private Runnable mFddStandByTimerRunnable = new Runnable() {
        public void run() {
            sFddStandByCounter++;
            if (sFddStandByCounter >= FDD_STANDBY_TIMER.length) {
                sFddStandByCounter = FDD_STANDBY_TIMER.length-1;
            }
            logd("FDD time out!");
            handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
        }
    };

    private void removeModemStandByTimer() {
        if (sWaitInTdd) {
            logd("Remove TDD wait timer. Set sWaitInTdd = false");
            sWaitInTdd = false;
            removeCallbacks(mTddStandByTimerRunnable);
        }
        if (sWaitInFdd) {
            logd("Remove FDD wait timer. Set sWaitInFdd = false");
            sWaitInFdd = false;
            removeCallbacks(mFddStandByTimerRunnable);
        }
    }

    private void searchForDesignateService(String strPlmn) {
        logd("[searchForDesignateService]+ Search for TD srvice");
        if (strPlmn == null) {
            logd("[searchForDesignateService]- null source");
            return;
        }
        strPlmn = strPlmn.substring(0, 5);
        for (String mccmnc : MCCMNC_TABLE_TYPE1) {
            if (strPlmn.equals(mccmnc)) {
                logd("sUserType:" + sUserType + " sRegion:" + sRegion);
                logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getModem()));
                logd("Find TD service");
                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
                break;
            }    
        }
        logd("[searchForDesignateService]-");
        return;
    }
    
    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult)msg.obj;
        switch (msg.what) {
            case EVENT_GSM_PLMN_CHANGED_1:
                logd("handleMessage : <EVENT_GSM_PLMN_CHANGED>");
                handlePlmnChange(ar, PhoneConstants.GEMINI_SIM_1);
                break;
            case EVENT_GSM_SUSPENDED_1:
                logd("handleMessage : <EVENT_GSM_SUSPENDED>");
                handlePlmnSuspend(ar, PhoneConstants.GEMINI_SIM_1);
                break;
            case EVENT_RADIO_ON_1:
                logd("handleMessage : <EVENT_RADIO_ON>");
                handleRadioOn(PhoneConstants.GEMINI_SIM_1);
                break;
            case EVENT_GSM_PLMN_CHANGED_2:
                logd("handleMessage : <EVENT_GSM_PLMN_CHANGED>");
                handlePlmnChange(ar, PhoneConstants.GEMINI_SIM_2);
                break;
            case EVENT_GSM_SUSPENDED_2:
                logd("handleMessage : <EVENT_GSM_SUSPENDED>");
                handlePlmnSuspend(ar, PhoneConstants.GEMINI_SIM_2);
                break;
            case EVENT_RADIO_ON_2:
                logd("handleMessage : <EVENT_RADIO_ON>");
                handleRadioOn(PhoneConstants.GEMINI_SIM_2);
                break;
            case EVENT_SET_RAT_GSM_ONLY:
                logd("handleMessage : <EVENT_SET_RAT_GSM_ONLY>");
                if (ar.exception == null) {
                    logd("Set RAT=2g ok");
                } else {
                    logd("Set RAT=2g fail " + ar.exception);
                }
                break;
            case EVENT_SET_RAT_WCDMA_PREF:
                logd("handleMessage : <EVENT_SET_RAT_WCDMA_PREF>");
                if (ar.exception == null) {
                    logd("Set RAT=auto ok");
                } else {
                    logd("Set RAT=auto fail " + ar.exception);
                }
                break;
            default:
                logd("Unknown msg:" + msg.what);
        }
    }

    private void handleRadioOn(int slotId) {
        logd("Slot" + slotId);
        if (s3gSimSlot == UNKNOWN_3G_SLOT) {
            s3gSimSlot = get3gCapabilitySim();
        }
        if (s3gSimSlot == slotId) {
            if (sUserType == sType1User || sUserType == sType2User) {
                logd("Modem on, Type12 user");
                setRatMode(SET_RAT_TO_AUTO, slotId);
            } else if (sUserType == sType3User) {
                logd("Modem on, Type3 user");
                setRatMode(SET_RAT_TO_2G, slotId);
            }
        }
    }

    private void handlePlmnChange(AsyncResult ar, int slotId) {
        logd("Slot" + slotId);
        if (s3gSimSlot == UNKNOWN_3G_SLOT) {
            s3gSimSlot = get3gCapabilitySim();
        }
        if (ar.exception == null && ar.result != null) {
            sGsmPlmnStrings = (String[])ar.result;
            for (int i=0; i<sGsmPlmnStrings.length; i++) {
                logd("sGsmPlmnStrings[" + i + "]=" + sGsmPlmnStrings[i]);
            }
            if (s3gSimSlot == slotId && sWaitForDesignateService
                    && sDenyReason != DENY_CAMP_ON_REASON_NEED_SWITCH_TO_FDD) {
                searchForDesignateService(sGsmPlmnStrings[0]);
            }
            // To speed up performance in foreign countries, once get PLMN(no matter which slot)
            // determine region right away and switch modem type if needed
            sRegion = getRegion(sGsmPlmnStrings[0]);
            if (s3gSimSlot != AUTO_SELECT_DISABLE && s3gSimSlot != NO_3G_CAPABILITY && 
                    sRegion == REGION_FOREIGN) {
                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
            }
        } else {
            logd("AsyncResult is wrong " + ar.exception);
        }
    }

    private void handlePlmnSuspend(AsyncResult ar, int slotId) {
        logd("Slot" + slotId);
        if (ar.exception == null && ar.result != null) {
            sSuspendId = ((int[]) ar.result)[0];
            logd("Suspending with Id=" + sSuspendId);
            if (s3gSimSlot == slotId) {
                if (sUserType != sUnknownUser) {
                    resumeCampingProcedure(slotId);
                } else {
                    sSuspendWaitImsi[slotId] = true;
                    logd("User type unknown, wait for IMSI");
                }
            } else {
                logd("Not 3G slot, camp on OK");
                if (sGsmCi[slotId].getRadioState().isOn()) {
                    sGsmCi[slotId].setResumeRegistration(sSuspendId, null);
                } else {
                    logd("Radio off or unavailable, can not send EMSR");
                }
            }
        } else {
            logd("AsyncResult is wrong " + ar.exception);
        }
    }
    
    private final BroadcastReceiver mReceiver = new  BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            logd("[BroadcastReceiver]+");
            String action = intent.getAction();
            logd("Action: " + action);
            int slotId;
            if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                String simStatus = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                slotId = intent.getIntExtra(PhoneConstants.GEMINI_SIM_ID_KEY, 0);
                logd("slotId: " + slotId + " simStatus: " + simStatus);
                if (simStatus.equals(IccCardConstants.INTENT_VALUE_ICC_IMSI)) {
                    if (s3gSimSlot == UNKNOWN_3G_SLOT) {
                        s3gSimSlot = get3gCapabilitySim();
                    }
                    //sIccRecordsInstance[slotId] = ((PhoneBase)sGsmCi[s3gSimSlot]).mIccRecords.get();
                    sUiccController[slotId] = UiccController.getInstance(slotId);
                    sIccRecordsInstance[slotId] = sUiccController[slotId].getIccRecords(UiccController.APP_FAM_3GPP);
                    sImsi[slotId] = sIccRecordsInstance[slotId].getIMSI();
                    logd("sImsi[" + slotId + "]:" + sImsi[slotId]);
                    if (slotId == s3gSimSlot) {
                        logd("3G slot");
                        sUserType = getUserType(sImsi[slotId]);
                        if (sFirstSelect[slotId]) {
                            sFirstSelect[slotId] = false;
                            if (sUserType == sType1User || sUserType == sType2User) {
                                sWaitForDesignateService = true;
                                setRatMode(SET_RAT_TO_AUTO, slotId);
                            } else if (sUserType == sType3User) {
                                sWaitForDesignateService = false;
                                logd("Type3 user, switch to FDD modem");
                                setRatMode(SET_RAT_TO_2G, slotId);
                                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
                            }
                        }
                        if (sSuspendWaitImsi[slotId]) {
                            sSuspendWaitImsi[slotId] = false;
                            logd("IMSI fot slot" + slotId + " now ready, resuming PLMN:" 
                                    + sGsmPlmnStrings[0] + " with ID:" + sSuspendId);
                            resumeCampingProcedure(slotId);
                        }
                    } else {
                        // not 3G slot, do not store into sUserType
                        getUserType(sImsi[slotId]);
                        logd("Not 3G slot");
                    }
                } else if (simStatus.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
                    sImsi[slotId] = "";
                    sFirstSelect[slotId] = true;
                    sSuspendWaitImsi[slotId] = false;
                    if (slotId == s3gSimSlot) {
                        logd("3G Sim removed, no world phone service");
                        sUserType = sUnknownUser;
                        sDenyReason = DENY_CAMP_ON_REASON_UNKNOWN;
                        sWaitForDesignateService = false;
                        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                            s3gSimSlot = UNKNOWN_3G_SLOT;
                        } else {
                            s3gSimSlot = DEFAULT_3G_SLOT;
                        }
                    } else {
                        logd("Slot" + slotId + " is not 3G slot");
                    }
                }
            } else if (action.equals(TelephonyIntents.EVENT_3G_SWITCH_DONE)) {
                if (s3gSimSlot != AUTO_SELECT_DISABLE) {
                    s3gSimSlot = intent.getIntExtra(TelephonyIntents.EXTRA_3G_SIM, 0);
                }
                handle3gSwitched();
            } else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
                ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                if (serviceState != null) {
                    slotId = intent.getIntExtra(PhoneConstants.GEMINI_SIM_ID_KEY, 0);
                    sPlmnSs = serviceState.getOperatorNumeric();
                    sRilRadioTechnology = serviceState.getRilVoiceRadioTechnology();
                    sRegState = serviceState.getRegState();
                    sState = serviceState.getState();
                    logd("s3gSimSlot=" + s3gSimSlot);
                    logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getModem()));
                    logd("slotId: " + slotId + " isRoaming: " + serviceState.getRoaming()
                            + " isEmergencyOnly: " + serviceState.isEmergencyOnly());
                    logd("sPlmnSs: " + sPlmnSs);
                    logd("sState: " + WorldPhoneUtil.stateToString(sState));
                    logd("sRegState: " + WorldPhoneUtil.regStateToString(sRegState));
                    logd("sRilRadioTechnology: " + serviceState.rilRadioTechnologyToString(sRilRadioTechnology));
                    if (slotId == s3gSimSlot) {
                        if (sState == ServiceState.STATE_IN_SERVICE) {
                            logd("sTddStandByCounter set to 0");
                            sTddStandByCounter = 0;
                        } else if (sState == ServiceState.STATE_OUT_OF_SERVICE
                                && sRegState == ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING) {
                            handleNoService();
                        }
                    }
                }
            }
            logd("[BroadcastReceiver]-");
        }
    };

    private void handle3gSwitched() {
        if (s3gSimSlot == NO_3G_CAPABILITY) {
            logd("3G capability turned off");
            removeModemStandByTimer();
            sUserType = sUnknownUser;
        } else if (s3gSimSlot == AUTO_SELECT_DISABLE) {
            logd("Auto Network Selection Disabled");
            removeModemStandByTimer();
        } else {
            logd("3G capability in slot" + s3gSimSlot);
            if (sImsi[s3gSimSlot].equals("")) {
                // may caused by receive 3g switched intent when boot up 
                logd("3G slot IMSI not ready");
                sUserType = sUnknownUser;
                return;
            }
            sUserType = getUserType(sImsi[s3gSimSlot]);
            if (sUserType == sType1User || sUserType == sType2User) {
                sWaitForDesignateService = true;
            } else if (sUserType == sType3User) {
                sWaitForDesignateService = false;
            } else {
                logd("Unknown user type");
            }
            if (sGsmPlmnStrings != null) {
                sRegion = getRegion(sGsmPlmnStrings[0]);
            }
            if (sRegion != REGION_UNKNOWN && sUserType != sUnknownUser) {
                sFirstSelect[s3gSimSlot] = false;
                if (sUserType == sType3User || sRegion == REGION_FOREIGN) {
                    handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
                } else {
                    handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
                }
            }
        }
    }

    public void setNetworkSelectionMode(int mode) {
        if (mode == SELECTION_MODE_AUTO) {
            logd("Network Selection <AUTO>");
            Settings.Global.putInt(sContext.getContentResolver(), Settings.Global.WORLD_PHONE_AUTO_SELECT_MODE, 1);
            s3gSimSlot = get3gCapabilitySim();
        } else {
            logd("Network Selection <MANUAL>");
            Settings.Global.putInt(sContext.getContentResolver(), Settings.Global.WORLD_PHONE_AUTO_SELECT_MODE, 0);
            s3gSimSlot = AUTO_SELECT_DISABLE;
            if (sUserType == sType1User || sUserType == sType2User) {
                setRatMode(SET_RAT_TO_AUTO, PhoneConstants.GEMINI_SIM_1);
            }
        }
        handle3gSwitched();
    }

    private void handleSwitchModem(int toModem) {
        if (toModem == ModemSwitchHandler.getModem()) {
            if (toModem == ModemSwitchHandler.MODEM_SWITCH_MODE_TDD) {
                logd("Already in TDD modem");
            } else {
                logd("Already in FDD modem");
            }
            return;
        } else {
            for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
                if (sGsmPhone[i].getState() != PhoneConstants.State.IDLE) {
                    logd("Phone" + i + " is not idle, modem switch not allowed");
                    return;
                }
            }
            removeModemStandByTimer();
            if (toModem == ModemSwitchHandler.MODEM_SWITCH_MODE_FDD) {
                logd("Switching to FDD modem");
                ModemSwitchHandler.switchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
            } else {
                logd("Switching to TDD modem");
                ModemSwitchHandler.switchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
            }
            resetNetworkProperties();
        }
    }

    private void resumeCampingProcedure(int slotId) {
        logd("Resume camping slot" + slotId + " sSuspendId:" + sSuspendId);
        if (isAllowCampOn(sGsmPlmnStrings[0], slotId)) {
            if (sGsmCi[slotId].getRadioState().isOn()) {
                sGsmCi[slotId].setResumeRegistration(sSuspendId, null);
            } else {
                logd("Radio off or unavailable, can not send EMSR");
            }
        } else {
            logd("Because: " + WorldPhoneUtil.denyReasonToString(sDenyReason));
            if (sDenyReason == DENY_CAMP_ON_REASON_NEED_SWITCH_TO_FDD) {
                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_FDD);
            } else if (sDenyReason == DENY_CAMP_ON_REASON_NEED_SWITCH_TO_TDD) {
                handleSwitchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
            }
        }
    }

    /* Might return -1 if 3G is off */
    private int get3gCapabilitySim() {
        int slot3g;
        int capability = SystemProperties.getInt("gsm.baseband.capability", 3);
        int capability2 = SystemProperties.getInt("gsm.baseband.capability2", 3);
        if (capability > 3 || capability2 > 3) {
            slot3g = sGsmPhone[0].get3GCapabilitySIM();
            logd("s3gSimSlot=" + slot3g);
            return slot3g;
        } else {
            logd("3G turn off");
            return -1;
        }
    }
        
    private void setRatMode(int ratMode, int slotId) {
        if (ratMode == SET_RAT_TO_AUTO) {
            logd("[setRatMode] Setting slot" + slotId + " RAT=auto");
            sGsmCi[slotId].setPreferredNetworkType(Phone.NT_MODE_WCDMA_PREF,
                obtainMessage(EVENT_SET_RAT_WCDMA_PREF));       
        } else {
            logd("[setRatMode] Setting slot" + slotId + " RAT=2G");
            sGsmCi[slotId].setPreferredNetworkType(Phone.NT_MODE_GSM_ONLY,
                obtainMessage(EVENT_SET_RAT_GSM_ONLY));
        }
    }

    private void resetAllProperties() {
        logd("Reseting all properties");
        sGsmPlmnStrings = null;
        for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
            sFirstSelect[i] = true;
        }
        sWaitForDesignateService = false;
        sDenyReason = DENY_CAMP_ON_REASON_UNKNOWN;
        resetSimProperties();
        resetNetworkProperties();
    }
    
    private void resetNetworkProperties() {
        logd("[resetNetworkProperties]");
        synchronized (sLock) {        
            sRegion = REGION_UNKNOWN;
            for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
                sSuspendWaitImsi[i] = false;
            }
        }
    }

    private void resetSimProperties() {
        logd("[resetSimProperties]");
        synchronized (sLock) {        
            for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
                sImsi[i] = "";
            }
            sUserType = sUnknownUser;
            if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                s3gSimSlot = UNKNOWN_3G_SLOT;
            } else {
                s3gSimSlot = DEFAULT_3G_SLOT;
            }
        }
    }

    private void restartSelection(String reason) {
        // clean all state, properties and restart from TD modem
        logd("[restartSelection] Restarting from TDD modem");
        logd("Reason:" + reason);
        resetAllProperties();
        logd("Switching to TDD modem");
        ModemSwitchHandler.switchModem(ModemSwitchHandler.MODEM_SWITCH_MODE_TDD);
    }

    private int getUserType(String simImsi) {
        if (simImsi != null && !simImsi.equals("")) {
            simImsi = simImsi.substring(0, 5);
            logd("[getUserType] simPlmn:" + simImsi);
            for (String mccmnc : MCCMNC_TABLE_TYPE1) {
                if (simImsi.equals(mccmnc)) {
                    logd("[getUserType] Type1 user");
                    return sType1User;
                }    
            }
            for (String mccmnc : MCCMNC_TABLE_TYPE3) {
                if (simImsi.equals(mccmnc)) {
                    logd("[getUserType] Type3 user");
                    return sType3User;
                }    
            }
            logd("[getUserType] Type2 user");
            return sType2User;
        } else {
            logd("[getUserType] null simImsi");
            return sUnknownUser;
        }
    }

    private int getRegion(String srcMccOrPlmn) {
        String currentMcc;
        if (srcMccOrPlmn == null) {
            logd("[getRegion] null source");
            return REGION_UNKNOWN;
        }
        // Lab test PLMN 46602 & 50270 are Type1 & Domestic region
        // Other real world PLMN 466xx & 502xx are Type2 & Foreign region
        currentMcc = srcMccOrPlmn.substring(0, 5);
        if (currentMcc.equals("46602") || currentMcc.equals("50270")) {
            return REGION_DOMESTIC;
        }
        currentMcc = srcMccOrPlmn.substring(0, 3);
        for (String mcc : MCC_TABLE_DOMESTIC) {
            if (currentMcc.equals(mcc)) {
                logd("[getRegion] REGION_DOMESTIC");
                return REGION_DOMESTIC;
            }    
        }
        logd("[getRegion] REGION_FOREIGN");
        return REGION_FOREIGN;
    }

    public void disposeWorldPhone() {
        sContext.unregisterReceiver(mReceiver);
        for (int i=0; i<PhoneConstants.GEMINI_SIM_NUM; i++) {
            sGsmCi[i].unSetOnPlmnChangeNotification(this);
            sGsmCi[i].unSetOnGSMSuspended(this);
            sGsmCi[i].unregisterForOn(this);
        }
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, "[WPOP01]" + msg);
    }
}
