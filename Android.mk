LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_AAPT_FLAGS += -c mdpi

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 libjcifs2 android-tool
LOCAL_JNI_SHARED_LIBRARIES := libserial_port
LOCAL_PACKAGE_NAME := DeviceTest
LOCAL_CERTIFICATE := platform
LOCAL_REQUIRED_MODULES := libserial_port
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)


########################################
include $(CLEAR_VARS)
#libname必须与上面自己定义的名称一致,needimport.jar是你需要导入的第三方jar包.注意这里的修改！！   #加了libname2:lib/needimport2.jar  

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libjcifs2:libs/jcifs-1.3.16.jar \
					android-tool:libs/android-tools.jar


include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))
LOCAL_PRIVILEGED_MODULE := true
