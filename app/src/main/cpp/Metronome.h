private:
    bool openStream();
    std::shared_ptr<AudioStream> mAudioStream;
    std::unique_ptr<Player> tick;
    std::unique_ptr<Player> tock;
    bool setupAudioSources();

public:
    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

class Metronome : public AudioStreamDataCallback {

}