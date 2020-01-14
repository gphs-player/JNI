//
// Created by 李华 on 2019/6/26.
//
#include <stdio.h>
#include "com_leo_jni_JNITest.h"

JNIEXPORT jstring JNICALL Java_com_leo_jni_JNITest_get(JNIEnv *env, jobject thiz){

    printf("invoke get from C\n");

    return (*env) -> NewStringUTF(env,"Hello From JNI");
}


JNIEXPORT void JNICALL Java_com_leo_jni_JNITest_set
  (JNIEnv *env, jobject thiz, jstring string){
  printf("invoke set from C\n");

  char * str = (char *)(*env)-> GetStringUTFChars(env,string,NULL);
  printf("%s\n",str);
  (*env) -> ReleaseStringUTFChars(env,string,str);

}
