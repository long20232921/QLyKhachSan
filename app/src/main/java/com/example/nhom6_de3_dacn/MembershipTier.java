package com.example.nhom6_de3_dacn;

public class MembershipTier {
    private String name;
    private String requirement;
    private String benefits;
    private int colorCode;

    public MembershipTier(String name, String requirement, String benefits, int colorCode) {
        this.name = name;
        this.requirement = requirement;
        this.benefits = benefits;
        this.colorCode = colorCode;
    }

    public String getName() { return name; }
    public String getRequirement() { return requirement; }
    public String getBenefits() { return benefits; }
    public int getColorCode() { return colorCode; }
}