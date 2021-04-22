#include <jni.h>
#include "android/log.h"
#include "libmp3lame/lame.h"
#include "mp3-encoder.h"
#include <pthread.h>
#define Loge(...) __android_log_print(ANDROID_LOG_ERROR  , "zll", __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_demo_jni_Mp3Encoder_init(JNIEnv * env, jobject job,
                                   jstring pcmPath,
                                   jint channel,
                                   jint bitRate,
                                   jint sampleRate,
                                   jstring mp3Path){
    Loge("encoder...int..");
    int code = -1;
    const char * pcmPath_c = env->GetStringUTFChars(pcmPath, nullptr);
    const char * mp3Path_c = env->GetStringUTFChars(mp3Path, nullptr);
    //打开文件 读
    pcmFile = fopen(pcmPath_c, "rb");
    if(pcmFile){
        ///写
        mp3File = fopen(mp3Path_c, "wb");
        if(mp3File){
            lameClient = lame_init();
            lame_set_in_samplerate(lameClient, sampleRate);
            lame_set_out_samplerate(lameClient,sampleRate);
            lame_set_num_channels(lameClient,channel);
            lame_set_brate(lameClient,bitRate);
            lame_init_params(lameClient);
            code = 0;
        }
    }
    env->ReleaseStringUTFChars(pcmPath,pcmPath_c);
    env->ReleaseStringUTFChars(mp3Path,mp3Path_c);
    return code;
}

void * encode(void *){

    int bufferSize = 1024 * 256;
    auto * buffer = new short[bufferSize/2];
    auto * leftBuffer  = new short[bufferSize/4];
    auto * rightBuffer   = new short[bufferSize/4];
    auto* mp3_buffer = new unsigned char[bufferSize];
    size_t readBufferSize = 0;

    while ((readBufferSize=fread(buffer,2,bufferSize/2,pcmFile))){
        for (int i = 0; i < readBufferSize; ++i) {
            if(i % 2 == 0){
                leftBuffer[i/2] = buffer[i];
            }else{
                rightBuffer[i/2] = buffer[i];
            }
        }
        size_t wroteSize = lame_encode_buffer(lameClient,leftBuffer,rightBuffer,readBufferSize/2,mp3_buffer,bufferSize);
        fwrite(mp3_buffer,1,wroteSize,mp3File);
    }
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_demo_jni_Mp3Encoder_encode(JNIEnv * env, jobject job){
    Loge("encoder encode...");
//    pthread_t pid;
//    pthread_create(&pid,0,encode, nullptr);
//    pthread_join(pid, nullptr);

    int bufferSize = 1024 * 256;
    auto * buffer = new short[bufferSize/2];
    auto * leftBuffer  = new short[bufferSize/4];
    auto * rightBuffer   = new short[bufferSize/4];
    auto* mp3_buffer = new unsigned char[bufferSize];
    size_t readBufferSize = 0;

    while ((readBufferSize=fread(buffer,2,bufferSize/2,pcmFile))){
        for (int i = 0; i < readBufferSize; ++i) {
            if(i % 2 == 0){
                leftBuffer[i/2] = buffer[i];
            }else{
                rightBuffer[i/2] = buffer[i];
            }
        }
        size_t wroteSize = lame_encode_buffer(lameClient,leftBuffer,rightBuffer,readBufferSize/2,mp3_buffer,bufferSize);
        fwrite(mp3_buffer,1,wroteSize,mp3File);
    }
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;

}

extern "C" JNIEXPORT void JNICALL
Java_com_demo_jni_Mp3Encoder_destroy(JNIEnv * env, jobject job){
    Loge("encoder destroy....");
    if(pcmFile){
        fclose(pcmFile);
    }
    if(mp3File){
        fclose(mp3File);
        lame_close(lameClient);
    }
}