package com.example.stjkagilonu.myapplication;

/**
 * Created by stj.kagilonu on 19.08.2016.
 */

        import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

public class Channel {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("field1")
    @Expose
    private String field1;
    @SerializedName("field2")
    @Expose
    private String field2;
    @SerializedName("field3")
    @Expose
    private String field3;
    @SerializedName("field4")
    @Expose
    private String field4;
    @SerializedName("field5")
    @Expose
    private String field5;
    @SerializedName("field6")
    @Expose
    private String field6;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("last_entry_id")
    @Expose
    private Integer lastEntryId;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     *
     * @param latitude
     * The latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @return
     * The longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     *
     * @param longitude
     * The longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     * The field1
     */
    public String getField1() {
        return field1;
    }

    /**
     *
     * @param field1
     * The field1
     */
    public void setField1(String field1) {
        this.field1 = field1;
    }

    /**
     *
     * @return
     * The field2
     */
    public String getField2() {
        return field2;
    }

    /**
     *
     * @param field2
     * The field2
     */
    public void setField2(String field2) {
        this.field2 = field2;
    }

    /**
     *
     * @return
     * The field3
     */
    public String getField3() {
        return field3;
    }

    /**
     *
     * @param field3
     * The field3
     */
    public void setField3(String field3) {
        this.field3 = field3;
    }

    /**
     *
     * @return
     * The field4
     */
    public String getField4() {
        return field4;
    }

    /**
     *
     * @param field4
     * The field4
     */
    public void setField4(String field4) {
        this.field4 = field4;
    }

    /**
     *
     * @return
     * The field5
     */
    public String getField5() {
        return field5;
    }

    /**
     *
     * @param field5
     * The field5
     */
    public void setField5(String field5) {
        this.field5 = field5;
    }

    /**
     *
     * @return
     * The field6
     */
    public String getField6() {
        return field6;
    }

    /**
     *
     * @param field6
     * The field6
     */
    public void setField6(String field6) {
        this.field6 = field6;
    }

    /**
     *
     * @return
     * The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     *
     * @param createdAt
     * The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     * @return
     * The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     *
     * @param updatedAt
     * The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     *
     * @return
     * The lastEntryId
     */
    public Integer getLastEntryId() {
        return lastEntryId;
    }

    /**
     *
     * @param lastEntryId
     * The last_entry_id
     */
    public void setLastEntryId(Integer lastEntryId) {
        this.lastEntryId = lastEntryId;
    }

}