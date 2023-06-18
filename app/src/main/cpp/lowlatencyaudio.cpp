/*
#include <jni.h>
#include <string>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include "AAssetDataSource.h"

using namespace oboe;

//    // Load the RAW PCM data files into memory
//std::shared_ptr<AAssetDataSource> soundSource(AAssetDataSource::newFromAssetManager(assetManager, "sound.raw", ChannelCount::Stereo));


std::shared_ptr<AudioStream> mAudioStream;
static const char* TAG = "lowlatencyaudio";

*/
//extern "C" JNIEXPORT jstring JNICALL
//Java_com_garethevans_church_opensongtablet_metronome_Metronome_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

/*
// JNI functions are "C" calling convention
extern "C" {


JNIEXPORT jboolean JNICALL
Java_com_garethevans_church_opensongtablet_metronome_Metronome_openStream(JNIEnv *env,jobject thiz) {
    AudioStreamBuilder builder;
    builder.setFormat(AudioFormat::Float);
    builder.setFormatConversionAllowed(true);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setSharingMode(SharingMode::Exclusive);
    builder.setSampleRate(48000);
    builder.setSampleRateConversionQuality(
            SampleRateConversionQuality::Medium);
    builder.setChannelCount(2);
    Result result = builder.openStream(mAudioStream);
    if (result != Result::OK){
        //LOGE("Failed to open stream. Error: %s", convertToText(result));
        return false;
    }
    return true;
}

JNIEXPORT void JNICALL
Java_com_garethevans_church_opensongtablet_metronome_Metronome_setDefaultStreamValues(JNIEnv *env,
                                                                                  jclass type,
                                                                                  jint sampleRate,
                                                                                  jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}


}
*/