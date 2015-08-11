#ifndef glbuffer_h_seen
#define glbuffer_h_seen

#include <jni.h>
#define UNUSED  __attribute__((unused))
void native_start(JNIEnv *env, jclass clazz);
void native_gl_resize(JNIEnv *env, jclass clazz, jint w, jint h);
void native_gl_render(JNIEnv *env, jclass clazz);
void native_key_event(JNIEnv *env,jclass clazz,jint key,jint status);
void native_touch_event(JNIEnv *env,jclass clazz,jfloat x,jfloat y,jint status);
int native_FindFeatures(JNIEnv *env,jclass clazz,jlong addrGray, jlong addrRgba);
#endif
