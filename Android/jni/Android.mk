LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../sdk/native/jni/OpenCV.mk
LOCAL_CFLAGS := -DOPEL_ES_1 -DANDROID_NDK
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ObjRecog 
LOCAL_MODULE    := cvgl
LOCAL_SRC_FILES := ObjRecog/commonCvFunctions.cpp ObjRecog/orException.cpp ObjRecog/imageDB.cpp ObjRecog/visualWords.cpp ObjRecog/controlOR.cpp register_natives.cpp native_main.cpp 
LOCAL_LDLIBS +=  -L$(LOCAL_PATH)/lib -lGLESv1_CM -llog -ldl -lnonfree
include $(BUILD_SHARED_LIBRARY)
