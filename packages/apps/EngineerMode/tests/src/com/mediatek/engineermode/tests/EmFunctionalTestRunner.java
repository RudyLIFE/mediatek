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

package com.mediatek.engineermode.tests;

import junit.framework.TestSuite;

public class EmFunctionalTestRunner extends JUnitInstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite tests = new TestSuite();
        tests.addTestSuite(EngineerModeReceiverTest.class);
        tests.addTestSuite(EngineerModeTest.class);
        tests.addTestSuite(MemoryTest.class);
        tests.addTestSuite(WiFiTest.class);
        tests.addTestSuite(AutoAnswerTest.class);
        tests.addTestSuite(DeviceMgrTest.class);
        tests.addTestSuite(AudioTest.class);
        //tests.addTestSuite(DesenseTest.class);
        tests.addTestSuite(WifiCtiaTest.class);
        // tests.addTestSuite(CameraTest.class);

        tests.addTestSuite(ElogTest.class);
        tests.addTestSuite(ModemCategoryTest.class);
        // tests.addTestSuite(CfuActivityTest.class); // Phone process
        tests.addTestSuite(SDLogActivityTest.class);
        // tests.addTestSuite(FastDormancyTest.class); // Phone process
        tests.addTestSuite(SettingsFontSizeTest.class);
        tests.addTestSuite(BatteryLogTest.class);
        tests.addTestSuite(PowerTest.class);
        tests.addTestSuite(ChargeBatteryTest.class);

        tests.addTestSuite(VideoTeleTest.class);
        tests.addTestSuite(VtWorkModeTest.class);
        tests.addTestSuite(VtAutoAnswerTest.class);
        tests.addTestSuite(VtConfigTest.class);
        tests.addTestSuite(VtLogFliterTest.class);
        tests.addTestSuite(VtPeerAudioRecTest.class);
        tests.addTestSuite(VtPeerVideoRecTest.class);
        tests.addTestSuite(VtTagSelectTest.class);
        // tests.addTestSuite(BandModeSimSelectTest.class);
        // tests.addTestSuite(BandSelectTest.class);
        tests.addTestSuite(MsdcTest.class);
        // tests.addTestSuite(RatSimSelectTest.class);
        tests.addTestSuite(NetworkDualTest.class);
        tests.addTestSuite(BaseBandTest.class);
        // tests.addTestSuite(NetworkInfoTest.class);
        // tests.addTestSuite(NetworkInformationTest.class);

        tests.addTestSuite(UsbTest.class);
        tests.addTestSuite(TouchScreenTest.class);
        tests.addTestSuite(DisplayTest.class);
        tests.addTestSuite(IoListTest.class);
        //tests.addTestSuite(MobileDataPreferTest.class);
        tests.addTestSuite(NonSleepModeTest.class);

        tests.addTestSuite(TsSettingTest.class);
        tests.addTestSuite(TsMultiTouchTest.class);
        tests.addTestSuite(TsRateReportTest.class);
        tests.addTestSuite(TsVerifyLineTest.class);
        tests.addTestSuite(TsVerifyPointTest.class);
        tests.addTestSuite(TsVerifyShakingTest.class);
        tests.addTestSuite(TsHandWritingTest.class);

        tests.addTestSuite(BtChipInfoTest.class);
        tests.addTestSuite(BtBleTest.class);
        tests.addTestSuite(BtNoSignalRxTest.class);
        tests.addTestSuite(BtRelayerModeTest.class);

        tests.addTestSuite(BtTestModeTest.class);
        tests.addTestSuite(BtTxOnlyTest.class);
        tests.addTestSuite(BtSspDebugTest.class);

        tests.addTestSuite(BtListTest.class);
        tests.addTestSuite(CameraMclkTest.class);
        tests.addTestSuite(HqaNfcTest.class);
        tests.addTestSuite(DailyUseSettingTest.class);
        tests.addTestSuite(WiFiDisplayTest.class);
        tests.addTestSuite(CWTestTest.class);
        //tests.addTestSuite(CameraNewTest.class);
        tests.addTestSuite(CipTest.class);
        tests.addTestSuite(CpuStressTestTest.class);
        return tests;
    }
}
