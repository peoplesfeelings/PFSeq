package peoplesfeelingscode.com.pfseq;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PFSeqAudio {
    static final long TIMEOUTUS = 3000;

    static MediaFormat getMediaFormat(File file) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaFormat format;

        try {
            extractor.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            throw new Exception("Error: exception thrown when trying to extract data from file " + file.getName());
        }

        format = extractor.getTrackFormat(0);

        extractor.release();

        return format;
    }

    // adapted from here and other places - https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecoderTest.java
    static short[] getPcm(File file) throws Exception {
        MediaCodec decoder;
        MediaExtractor extractor = new MediaExtractor();
        MediaFormat sourceFormat;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            extractor.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            throw new Exception("Error: exception thrown when trying to extract data from file " + file.getName());
        }

        sourceFormat = extractor.getTrackFormat(0);

        extractor.selectTrack(0);

        try {
            decoder = MediaCodec.createDecoderByType(sourceFormat.getString(MediaFormat.KEY_MIME));
        } catch (Exception e) {
            throw new Exception("Failed to instantiate " + sourceFormat.getString(MediaFormat.KEY_MIME) + " decoder.");
        }

        decoder.configure(sourceFormat, null, null, 0);
        decoder.start();

        MediaCodec.BufferInfo outputBufferInfo = new MediaCodec.BufferInfo();
        boolean inputEOS = false;
        boolean outputEOS = false;

        while (!outputEOS) {
            if (!inputEOS) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUTUS);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufIndex);
                    int sampleSize = extractor.readSampleData(inputBuffer, 0 );
                    if (sampleSize < 0) {
                        inputEOS = true;
                        decoder.queueInputBuffer(
                                inputBufIndex,
                                0 ,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(
                                inputBufIndex,
                                0 ,
                                sampleSize,
                                presentationTimeUs,
                                0);
                        extractor.advance();
                    }
                }
            }

            int outputBufferIndex = decoder.dequeueOutputBuffer(outputBufferInfo, TIMEOUTUS);

            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferIndex);

                for (int i = 0; i < outputBufferInfo.size; i++) {
                    byteArrayOutputStream.write(outputBuffer.get(i));
                }

                decoder.releaseOutputBuffer(outputBufferIndex, false);

                if ((outputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputEOS = true;
                }
            }
        }

        extractor.release();
        decoder.stop();
        decoder.release();

        byte[] decodedBytes = byteArrayOutputStream.toByteArray();
        short[] shorts = byteArrayToShortArray(decodedBytes);

        return shorts;
    }

    static private short[] byteArrayToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];

        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < shortArray.length; i++) {
            bb.clear();
            bb.put(byteArray[i * 2]);
            bb.put(byteArray[(i * 2) + 1]);
            shortArray[i] = bb.getShort(0);
        }

        return shortArray;
    }
}
