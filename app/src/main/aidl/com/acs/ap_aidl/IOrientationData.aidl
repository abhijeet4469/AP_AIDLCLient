// IOrientationData.aidl
package com.acs.ap_aidl;

interface IOrientationData{
    void startOrientationReceiver();
    void stopOrientationReceiver();

    float[] orientationDataListener();

}