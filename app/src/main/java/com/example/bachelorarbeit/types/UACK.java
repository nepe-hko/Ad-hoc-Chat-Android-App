package com.example.bachelorarbeit.types;

import java.util.UUID;

public class UACK extends PayloadType  {
    private String originalSourceID;
    private String originalUID;
    private String sourceID;

    public UACK () {
        super.type = "UACK";
    }

    public String getOriginalSourceID() { return originalSourceID; }
    public void setOriginalSourceID(String originalSourceID) { this.originalSourceID = originalSourceID; }
    public String getOriginalUID() { return originalUID; }
    public void setOriginalUID(String originalUID) { this.originalUID = originalUID; }
    public String getSourceID() { return sourceID; }
    public void setSourceID(String sourceID) { this.sourceID = sourceID; }
}
