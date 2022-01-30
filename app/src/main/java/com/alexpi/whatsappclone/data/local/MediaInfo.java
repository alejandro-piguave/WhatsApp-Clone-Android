package com.alexpi.whatsappclone.data.local;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MediaInfo implements Parcelable {
    private String mediaUri, senderName;
    private long  timestamp;

    public MediaInfo(String mediaUri, String senderName, long timestamp) {
        this.mediaUri = mediaUri;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    protected MediaInfo(Parcel in) {
        mediaUri = in.readString();
        senderName = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
        @Override
        public MediaInfo createFromParcel(Parcel in) {
            return new MediaInfo(in);
        }

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }
    };

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaUri);
        dest.writeString(senderName);
        dest.writeLong(timestamp);
    }

    public static int indexOf(List<MediaInfo> mediaInfoList, String media){
        for (MediaInfo mediaInfo: mediaInfoList)
            if(mediaInfo.getMediaUri().equals(media))
                return mediaInfoList.indexOf(mediaInfo);
        return -1;
    }
}
