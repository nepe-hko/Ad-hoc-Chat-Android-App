package com.example.bachelorarbeit.types;

public class MACK extends PayloadType  {
    private String OriginalSourceID;
    private String OriginalUID;

    public MACK () {
        super.type = "MACK";
    }

    public String getOriginalSourceID() { return OriginalSourceID; }
    public void setOriginalSourceID(String originalSourceID) { OriginalSourceID = originalSourceID; }
    public String getOriginalUID() { return OriginalUID; }
    public void setOriginalUID(String originalUID) { OriginalUID = originalUID; }
}
