package com.example.stjkagilonu.myapplication;


import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("channel")
    @Expose
    private Channel channel;
    @SerializedName("feeds")
    @Expose
    private List<Feed> feeds = new ArrayList<Feed>();

    /**
     *
     * @return
     * The channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     *
     * @param channel
     * The channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     *
     * @return
     * The feeds
     */
    public List<Feed> getFeeds() {
        return feeds;
    }

    /**
     *
     * @param feeds
     * The feeds
     */
    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

}