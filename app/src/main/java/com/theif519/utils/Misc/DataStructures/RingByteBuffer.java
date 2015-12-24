package com.theif519.utils.Misc.DataStructures;

import java.nio.ByteBuffer;

/**
 * Created by theif519 on 12/23/2015.
 */
public class RingByteBuffer {
    private ByteBuffer mBufferWrapper;
    private byte[] mBuffer;
    private int mHead, mTail;
    private boolean mHeadPassedTail = true;

    public RingByteBuffer(int size){
        mBuffer = new byte[size];
        mBufferWrapper = ByteBuffer.wrap(mBuffer);
    }

    public void add(byte[] buf){
        for (byte b : buf) {
            mBuffer[mHead] = b;
            advanceHead();
        }
    }

    public byte[] get(int amount){
        byte[] contents = new byte[amount];
        for(int i = 0; i < amount; i++){
            contents[i] = mBuffer[mTail];
            advanceTail();
        }
        return contents;
    }

    private void advanceHead(){
        // Advance the head.
        mHead = (mHead + 1) % mBuffer.length;
        // Note that if the NEW head is the tail, then the buffer is full.
        if (mHead == mTail) {
            // Hence we must increment as such here.
            mTail = (mTail + 1) % mBuffer.length;
        }
    }

    private void advanceTail(){
        // Note that, if the tail before advancing was the head, it is actually empty, and hence is an illegal operation.
        if(mTail == mHead){
            throw new RuntimeException("An attempt to dequeue more than was available in the buffer was made!");
        }
        // Advance the tail.
        mTail = (mTail + 1) % mBuffer.length;
    }
}