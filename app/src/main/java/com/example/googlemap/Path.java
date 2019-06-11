package com.example.googlemap;

public class Path {
    private String NUmName;
    private String LatName;
    private String LongName;
    private String Pnum;

    public Path(String num , String latname, String longname ,String pnum){
        NUmName = num;
        LatName = latname;
        LongName = longname;
        Pnum = pnum;
    }

    public String getNUmName() {
        return NUmName;
    }

    public String getLatName() {
        return LatName;
    }

    public String getLongName() {
        return LongName;
    }

    public String getpnum() {
        return Pnum;
    }


}
