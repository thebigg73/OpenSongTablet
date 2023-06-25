bool Metronome::openStream() {
   AudioStreamBuilder builder;
   builder.setFormat(AudioFormat::Float);
   builder.setFormatConversionAllowed(true);
   builder.setPerformanceMode(PerformanceMode::LowLatency);
   //builder.setSharingMode(SharingMode::Exclusive);
   builder.setSampleRate(48000);
   builder.setSampleRateConversionQuality(
      SampleRateConversionQuality::Medium);
   builder.setChannelCount(2);

   Result result = builder.openStream(mAudioStream);
       if (result != Result::OK){
           LOGE("Failed to open stream. Error: %s", convertToText(result));
           return false;
       }
       return true;
}

bool Metronome::setupAudioSources() {
   // Set the properties of our audio source(s) to match those of our audio stream.
   AudioProperties targetProperties {
            .channelCount = 2,
            .sampleRate = 48000
   };

   // Create a data source and player for the clap sound.
   std::shared_ptr<AAssetDataSource> mClapSource {
           AAssetDataSource::newFromCompressedAsset(mAssetManager, "CLAP.mp3", targetProperties)
   };
   if (mClapSource == nullptr){
       LOGE("Could not load source data for clap sound");
       return false;
   }
   mClap = std::make_unique<Player>(mClapSource);
   return true;
}