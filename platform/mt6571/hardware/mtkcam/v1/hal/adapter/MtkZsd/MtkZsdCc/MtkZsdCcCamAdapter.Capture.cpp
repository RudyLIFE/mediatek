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

#define LOG_TAG "MtkCam/CamAdapter"
//
#include <camera/MtkCamera.h>
//
#include <inc/CamUtils.h>
using namespace android;
using namespace MtkCamUtils;
//
#include <inc/ImgBufProvidersManager.h>
//
#include <mtkcam/v1/IParamsManager.h>
#include <mtkcam/v1/ICamAdapter.h>
#include <inc/BaseCamAdapter.h>
#include "inc/MtkZsdCcCamAdapter.h"
using namespace NSMtkZsdCcCamAdapter;
//
#include <mtkcam/imageio/ispio_stddef.h>
using namespace NSImageio::NSIspio;


/******************************************************************************
*
*******************************************************************************/
#define MY_LOGV(fmt, arg...)        CAM_LOGV("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGD(fmt, arg...)        CAM_LOGD("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGI(fmt, arg...)        CAM_LOGI("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGW(fmt, arg...)        CAM_LOGW("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGE(fmt, arg...)        CAM_LOGE("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGA(fmt, arg...)        CAM_LOGA("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
#define MY_LOGF(fmt, arg...)        CAM_LOGF("(%d)(%s)[%s] "fmt, ::gettid(), getName(), __FUNCTION__, ##arg)
//
#define MY_LOGV_IF(cond, ...)       do { if ( (cond) ) { MY_LOGV(__VA_ARGS__); } }while(0)
#define MY_LOGD_IF(cond, ...)       do { if ( (cond) ) { MY_LOGD(__VA_ARGS__); } }while(0)
#define MY_LOGI_IF(cond, ...)       do { if ( (cond) ) { MY_LOGI(__VA_ARGS__); } }while(0)
#define MY_LOGW_IF(cond, ...)       do { if ( (cond) ) { MY_LOGW(__VA_ARGS__); } }while(0)
#define MY_LOGE_IF(cond, ...)       do { if ( (cond) ) { MY_LOGE(__VA_ARGS__); } }while(0)
#define MY_LOGA_IF(cond, ...)       do { if ( (cond) ) { MY_LOGA(__VA_ARGS__); } }while(0)
#define MY_LOGF_IF(cond, ...)       do { if ( (cond) ) { MY_LOGF(__VA_ARGS__); } }while(0)


/******************************************************************************
*   Function Prototype.
*******************************************************************************/
bool
createShotInstance(
    sp<IShot>&          rpShot, 
    uint32_t const      u4ShotMode, 
    int32_t const       i4OpenId, 
    sp<IParamsManager>  pParamsMgr
);


/******************************************************************************
*
*******************************************************************************/
bool
CamAdapter::
updateShotInstance()
{
    MY_LOGI("<shot mode> %#x", mu4ShotMode);
    return  createShotInstance(mpShot, mu4ShotMode, getOpenId(), getParamsManager());
}


/******************************************************************************
*
*******************************************************************************/
bool
CamAdapter::
isTakingPicture() const
{
    bool ret =  mpStateManager->isState(IState::eState_Capture)
            ||  mpStateManager->isState(IState::eState_PreCapture)
            ||  mpStateManager->isState(IState::eState_PreviewCapture)
                ;
    if  ( ret )
    {
        MY_LOGD("isTakingPicture(1):%s", mpStateManager->getCurrentState()->getName());
    }
    //
    return  ret;
}


/******************************************************************************
*
*******************************************************************************/
status_t
CamAdapter::
takePicture()
{
    status_t status = OK;
    MINT32 bFlashOn = 0;

    status = mpStateManager->getCurrentState()->onPreCapture(this);
    if  ( OK != status ) {
        goto lbExit;
    }
    mu4ShotMode = mpPreviewCmdQueThread->getShotMode();
    if ( mu4ShotMode != eShotMode_ZsdShot && mu4ShotMode != eShotMode_ContinuousShotCc) {
        status = mpStateManager->getCurrentState()->onStopPreview(this);
        if  ( OK != status ) {
            goto lbExit;
        }
    }
    else
    {
        MY_LOGD("Take picture without stopping preview");
        mbTakePicPrvNotStop = true;
    }
    status = mpStateManager->getCurrentState()->onCapture(this);
    if  ( OK != status ) {
        goto lbExit;
    }
    goto lbExit;

    //
lbExit:
    return status;
}


/******************************************************************************
*
*******************************************************************************/
status_t
CamAdapter::
cancelPicture()
{
    mpStateManager->getCurrentState()->onCancelCapture(this);

    return OK;
}

/******************************************************************************
*
*******************************************************************************/
status_t
CamAdapter::
setCShotSpeed(int32_t i4CShotSpeeed)
{
    if(i4CShotSpeeed <= 0)
    {
        MY_LOGE("can not set continuous shot speed as %d fps)", i4CShotSpeeed);
        return BAD_VALUE;
    }
    
    sp<IShot> pShot = mpShot;
    if  ( pShot != 0 )
    {
        pShot->sendCommand(eCmd_setCShotSpeed, i4CShotSpeeed, 0);
    }
    
    return OK;
}

/******************************************************************************
*   CamAdapter::takePicture() -> IState::onCapture() ->
*   IStateHandler::onHandleCapture() -> CamAdapter::onHandleCapture()
*******************************************************************************/
status_t
CamAdapter::
onHandleCapture()
{
    status_t status = DEAD_OBJECT;
    //
    onCheckCapMemory();
    //
    sp<IPreviewCmdQueThread> pPreviewCmdQueThread = mpPreviewCmdQueThread;
    sp<ICaptureCmdQueThread> pCaptureCmdQueThread = mpCaptureCmdQueThread;

    if  ( pPreviewCmdQueThread != 0 ) {
        if ( ! pPreviewCmdQueThread->postCommand(PrvCmdCookie::eCaptureUpdate, PrvCmdCookie::eSemBefore) ) {
            MY_LOGE("onHandleCapture capture preview update: fail");
        }
    }

    if  ( pCaptureCmdQueThread != 0 ) {
        status = pCaptureCmdQueThread->onCapture();
    }
    //
    return  status;
}


/******************************************************************************
*
*******************************************************************************/
status_t
CamAdapter::
onHandleCaptureDone()
{
#if 0
    //  Message may disable before shutter/image callback if: DONE --> Image CB
    mNotifyCb(MTK_CAMERA_MSG_EXT_NOTIFY, MTK_CAMERA_MSG_EXT_NOTIFY_CAPTURE_DONE, 0, mCallbackCookie);
#endif
    //
    if ( mu4ShotMode == eShotMode_ZsdShot  || mu4ShotMode == eShotMode_ContinuousShotCc) {
        if( mbTakePicPrvNotStop &&
            mpPreviewCmdQueThread != 0)
        {
            if(!mpPreviewCmdQueThread->postCommand(PrvCmdCookie::eDisablePass2, PrvCmdCookie::eSemBefore))
            {
                MY_LOGE("eEnablePass2 fail");
            }
        }
        //
        mpStateManager->transitState(IState::eState_Preview);
        //if ( ! mpPreviewCmdQueThread->postCommand(PrvCmdCookie::eUpdate, PrvCmdCookie::eSemBefore) ){
        //    MY_LOGE("onHandleCaptureDone: restart preview fail");
        //}
    } else {
        mpStateManager->transitState(IState::eState_Idle);
    }
    //
    sp<ICaptureBufMgr> pCaptureBufMgr = mpCaptureBufMgr;
    if(pCaptureBufMgr != NULL)
    {
        pCaptureBufMgr->freeBuffer();
        pCaptureBufMgr = NULL;
    }
    //
    return  OK;
}

/******************************************************************************
*   CamAdapter::cancelPicture() -> IState::onCancelCapture() ->
*   IStateHandler::onHandleCancelCapture() -> CamAdapter::onHandleCancelCapture()
*******************************************************************************/
status_t
CamAdapter::
onHandleCancelCapture()
{
    sp<IShot> pShot = mpShot;
    if  ( pShot != 0 )
    {
        pShot->sendCommand(eCmd_cancel);
    }
    //
    return  OK;
}


/******************************************************************************
*
*******************************************************************************/
bool
CamAdapter::
onCaptureThreadLoop()
{
    bool ret = false;
    //
    //  [1] transit to "Capture" state.
    if ( mu4ShotMode == eShotMode_ZsdShot  || mu4ShotMode == eShotMode_ContinuousShotCc) {
        mpStateManager->transitState(IState::eState_PreviewCapture);
    } else {
        mpStateManager->transitState(IState::eState_Capture);
    }
    //  [2.1] update mpShot instance.
    ret = updateShotInstance();
    sp<IShot> pShot = mpShot;
    //
    //  [2.2] return if no shot instance.
    if  ( ! ret || pShot == 0 )
    {
#warning "[TODO] perform a dummy compressed-image callback or CAMERA_MSG_ERROR to inform app of end of capture?"
        MY_LOGE("updateShotInstance(%d), pShot.get(%p)", ret, pShot.get());
        goto lbExit;
    }
    else
    {
        //  [3.1] prepare parameters
        sp<IParamsManager> pParamsMgr = getParamsManager();
        int iPictureWidth = 0, iPictureHeight = 0;
        pParamsMgr->getPictureSize(&iPictureWidth, &iPictureHeight);
        int iPreviewWidth = 0, iPreviewHeight = 0;
        pParamsMgr->getPreviewSize(&iPreviewWidth, &iPreviewHeight);
        String8 s8DisplayFormat = mpImgBufProvidersMgr->queryFormat(IImgBufProvider::eID_DISPLAY);
        if  ( String8::empty() == s8DisplayFormat ) {
            MY_LOGW("Display Format is empty");
        }
        //
        //  [3.2] prepare parameters: ShotParam
        ShotParam shotParam;
        shotParam.ms8PictureFormat          = pParamsMgr->getStr(CameraParameters::KEY_PICTURE_FORMAT);
        shotParam.mi4PictureWidth           = iPictureWidth;
        shotParam.mi4PictureHeight          = iPictureHeight;
        shotParam.ms8PostviewDisplayFormat  = s8DisplayFormat;
        shotParam.ms8PostviewClientFormat   = pParamsMgr->getStr(CameraParameters::KEY_PREVIEW_FORMAT);
        shotParam.mi4PostviewWidth          = iPreviewWidth;
        shotParam.mi4PostviewHeight         = iPreviewHeight;
        shotParam.ms8ShotFileName           = pParamsMgr->getStr(MtkCameraParameters::KEY_CAPTURE_PATH);
        shotParam.mu4ZoomRatio              = pParamsMgr->getZoomRatio();
        shotParam.mu4ShotCount              = pParamsMgr->getInt(MtkCameraParameters::KEY_BURST_SHOT_NUM);
        shotParam.mi4Rotation               = pParamsMgr->getInt(CameraParameters::KEY_ROTATION);
        //
        //  [3.3] prepare parameters: JpegParam
        JpegParam jpegParam;
        jpegParam.mu4JpegQuality            = pParamsMgr->getInt(CameraParameters::KEY_JPEG_QUALITY);
        jpegParam.mu4JpegThumbQuality       = pParamsMgr->getInt(CameraParameters::KEY_JPEG_THUMBNAIL_QUALITY);
        jpegParam.mi4JpegThumbWidth         = pParamsMgr->getInt(CameraParameters::KEY_JPEG_THUMBNAIL_WIDTH);
        jpegParam.mi4JpegThumbHeight        = pParamsMgr->getInt(CameraParameters::KEY_JPEG_THUMBNAIL_HEIGHT);
        jpegParam.ms8GpsLatitude            = pParamsMgr->getStr(CameraParameters::KEY_GPS_LATITUDE);
        jpegParam.ms8GpsLongitude           = pParamsMgr->getStr(CameraParameters::KEY_GPS_LONGITUDE);
        jpegParam.ms8GpsAltitude            = pParamsMgr->getStr(CameraParameters::KEY_GPS_ALTITUDE);
        jpegParam.ms8GpsTimestamp           = pParamsMgr->getStr(CameraParameters::KEY_GPS_TIMESTAMP);
        jpegParam.ms8GpsMethod              = pParamsMgr->getStr(CameraParameters::KEY_GPS_PROCESSING_METHOD);
        //
        //  [4.1] perform Shot operations.
        if ( mu4ShotMode == eShotMode_ZsdShot  || mu4ShotMode == eShotMode_ContinuousShotCc) {
            MY_LOGD("mpCaptureBufMgr addr  %d 0x%x",  mpCaptureBufMgr->getStrongCount(),mpCaptureBufMgr.get() );
            ret =
                    pShot->sendCommand(eCmd_reset)
                &&  pShot->setCallback(this)
                &&  pShot->sendCommand(eCmd_setCaptureBufHandler, ((uint32_t)(static_cast<void*>(mpCaptureBufMgr.get()))), 0)
                &&  pShot->sendCommand(eCmd_setShotParam, (uint32_t)&shotParam, sizeof(ShotParam))
                &&  pShot->sendCommand(eCmd_setJpegParam, (uint32_t)&jpegParam, sizeof(JpegParam))
                &&  pShot->sendCommand(eCmd_capture)
                    ;

        } else {
            ret =
                    pShot->sendCommand(eCmd_reset)
                &&  pShot->setCallback(this)
                &&  pShot->sendCommand(eCmd_setShotParam, (uint32_t)&shotParam, sizeof(ShotParam))
                &&  pShot->sendCommand(eCmd_setJpegParam, (uint32_t)&jpegParam, sizeof(JpegParam))
                &&  pShot->sendCommand(eCmd_capture)
                    ;
        }

    }
    //
    //
lbExit:
    //
    //  [5.1] uninit shot instance.
    MY_LOGD("free shot instance: (mpShot/pShot)=(%p/%p)", mpShot.get(), pShot.get());
    mpShot = NULL;
    pShot  = NULL;
    //
    //MY_LOGD("mpCaptureBufMgr addr  %d 0x%x",  mpCaptureBufMgr->getStrongCount(),mpCaptureBufMgr.get() );
    //  [5.2] notify capture done.
    mpStateManager->getCurrentState()->onCaptureDone(this);
    //
    //
    return  true;
}


/******************************************************************************
*
*******************************************************************************/
bool
CamAdapter::
onInitCapMemory()
{
    int32_t rot, p2CapBufCnt;

    getParamsManager()->getPreviewSize(&mCurPrvW, &mCurPrvH);
    MY_LOGD("Preview size(%d %d)",mCurPrvW, mCurPrvH);

    getParamsManager()->getPictureSize(&mCurCapW, &mCurCapH);
    rot = getParamsManager()->getInt(CameraParameters::KEY_ROTATION);
    MY_LOGD("Capture size (%d,%d,%d)",mCurCapW, mCurCapH, rot);

    EImageRotation erot =  rot == 0 ? eImgRot_0
        : rot == 90 ? eImgRot_90
        : rot == 180 ? eImgRot_180 : eImgRot_270;
    if (rot == 90 || rot == 270) {
        int32_t temp = mCurCapW;
        mCurCapW = mCurCapH;
        mCurCapH = temp;
    }
    //
    if(mpPreviewCmdQueThread->getShotMode() == eShotMode_ZsdShot)
    {
        p2CapBufCnt = 1;
    }
    else
    {
        p2CapBufCnt = 3;
    }
    MY_LOGD("p2CapBufCnt(%d)", p2CapBufCnt);
    //
    mpCaptureBufMgr->allocBuffer(
            mCurCapW,
            mCurCapH,
            MtkCameraParameters::PIXEL_FORMAT_YUV422I,
            erot,
            mCurPrvW,
            mCurPrvH,
            MtkCameraParameters::PIXEL_FORMAT_YUV420P,
            p2CapBufCnt);
    mpCaptureBufMgr->setStoredBufferCnt(1);
    //
    return true;
}


/******************************************************************************
*
*******************************************************************************/
bool
CamAdapter::
onCheckCapMemory()
{
    int32_t rot, curPrvW, curPrvH, curCapW, curCapH, p2CapBufCnt;

    getParamsManager()->getPreviewSize(&curPrvW, &curPrvH);
    MY_LOGD("Preview size(%d %d)",curPrvW, curPrvH);

    getParamsManager()->getPictureSize(&curCapW, &curCapH);
    rot = getParamsManager()->getInt(CameraParameters::KEY_ROTATION);
    MY_LOGD("Capture size (%d,%d,%d)",curCapW, curCapH, rot);

    EImageRotation erot =  rot == 0 ? eImgRot_0
        : rot == 90 ? eImgRot_90
        : rot == 180 ? eImgRot_180 : eImgRot_270;
    if (rot == 90 || rot == 270) {
        int32_t temp = curCapW;
        curCapW = curCapH;
        curCapH = temp;
    }
    //
    if(mu4ShotMode == eShotMode_ZsdShot)
    {
        p2CapBufCnt = 1;
    }
    else
    {
        p2CapBufCnt = 3;
    }
    MY_LOGD("P2CapBufCnt(%d),Prv(%dx%d/%dx%d),Cap(%dx%d/%dx%d)",\
            p2CapBufCnt,
            mCurPrvW,
            mCurPrvH,
            curPrvW,
            curPrvH,
            mCurCapW,
            mCurCapH,
            curCapW,
            curCapH);
    //
    if( mCurPrvW != curPrvW ||
        mCurPrvH != curPrvH ||
        mCurCapW != curCapW ||
        mCurCapH != curCapH)
    {
        mCurPrvW = curPrvW;
        mCurPrvH = curPrvH;
        mCurCapW = curCapW;
        mCurCapH = curCapH;
        //
        mpCaptureBufMgr->reallocBuffer(
            mCurCapW,
            mCurCapH,
            MtkCameraParameters::PIXEL_FORMAT_YUV422I,
            mCurPrvW,
            mCurPrvH,
            MtkCameraParameters::PIXEL_FORMAT_YUV420P,
            p2CapBufCnt);
    }
    //
    return true;
}

