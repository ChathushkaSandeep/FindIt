package com.findit.app.models;

public class Claim {
    private String id;
    private String itemId;
    private String itemTitle;
    private String claimantId;
    private String claimantName;
    private String proofDescription;
    private String proofImageUri;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private String adminNotes;
    private String createdAt;

    public Claim() {
    }

    public Claim(String id, String itemId, String claimantId, String proofDescription, String proofImageUri,
            String status, String adminNotes, String createdAt) {
        this.id = id;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.proofDescription = proofDescription;
        this.proofImageUri = proofImageUri;
        this.status = status;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(String claimantId) {
        this.claimantId = claimantId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public void setProofDescription(String proofDescription) {
        this.proofDescription = proofDescription;
    }

    public String getProofImageUri() {
        return proofImageUri;
    }

    public void setProofImageUri(String proofImageUri) {
        this.proofImageUri = proofImageUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
