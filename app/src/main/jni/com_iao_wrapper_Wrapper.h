/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_iao_wrapper_Wrapper */

#ifndef _Included_com_iao_wrapper_Wrapper
#define _Included_com_iao_wrapper_Wrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_iao_wrapper_Wrapper
 * Method:    open
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_iao_wrapper_Wrapper_open
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_iao_wrapper_Wrapper
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_iao_wrapper_Wrapper_close
  (JNIEnv *, jobject);

/*
 * Class:     com_iao_wrapper_Wrapper
 * Method:    setSegment
 * Signature: (CCCCCC)V
 */
JNIEXPORT void JNICALL Java_com_iao_wrapper_Wrapper_setSegment
  (JNIEnv *, jobject, jchar, jchar, jchar, jchar, jchar, jchar);

#ifdef __cplusplus
}
#endif
#endif
