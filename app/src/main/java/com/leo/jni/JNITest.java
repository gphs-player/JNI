package com.leo.jni;

/**
 * <p>Date:2019/6/26.11:28 AM</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class JNITest {
    static {

//        System.load("/Users/lihua/Learn/Android/JNI/app/src/main/java/jni/libHelloWorld.so");
        System.loadLibrary("HelloWorld");
        ///Users/lihua/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
    }
    //注意  否则好烦
    // 1. javah 命令的时候要在包名上级目录
    //2.➜  java java -Djava.library.path=/Users/lihua/Learn/Android/JNI/app/src/main/java/jni com.leo.jni.JNITest
    ///Users/lihua/Learn/Android/JNI/app/src/main/java/jni
    //invoke get from C
    //Hello From JNI
    //invoke set from C
    //Hello world
    public static void main(String [] args){
        System.out.println(System.getProperty("java.library.path"));
        JNITest jniTest = new JNITest();
        String s = jniTest.get();
        System.out.println(s);
        jniTest.set("Hello world");
    }

    public native String get();
    public native void set(String str);
}
