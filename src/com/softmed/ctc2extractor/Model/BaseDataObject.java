package com.softmed.ctc2extractor.Model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

public abstract class BaseDataObject extends MotechBaseDataObject {

    @JsonProperty
    private Date dateCreated;
    @JsonProperty
    private Date dateEdited;
    @JsonProperty
    private Boolean voided;
    @JsonProperty
    private Date dateVoided;
    @JsonProperty
    private String voidReason;
    @JsonProperty
    private Integer clientApplicationVersion;
    @JsonProperty
    private Integer clientDatabaseVersion;
    @JsonProperty
    private String clientApplicationVersionName;

    public BaseDataObject() {
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    public Date getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(Date dateEdited) {
        this.dateEdited = dateEdited;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public Date getDateVoided() {
        return dateVoided;
    }

    public void setDateVoided(Date dateVoided) {
        this.dateVoided = dateVoided;
    }


    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public Integer getClientApplicationVersion() {
        return clientApplicationVersion;
    }

    public void setClientApplicationVersion(Integer clientApplicationVersion) {
        this.clientApplicationVersion = clientApplicationVersion;
    }

    public String getClientApplicationVersionName() {
        return clientApplicationVersionName;
    }

    public void setClientApplicationVersionName(String clientApplicationVersionName) {
        this.clientApplicationVersionName = clientApplicationVersionName;
    }

    public Integer getClientDatabaseVersion() {
        return clientDatabaseVersion;
    }

    public void setClientDatabaseVersion(Integer clientDatabaseVersion) {
        this.clientDatabaseVersion = clientDatabaseVersion;
    }

    public BaseDataObject withDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public BaseDataObject withDateEdited(Date dateEdited) {
        this.dateEdited = dateEdited;
        return this;
    }

    public BaseDataObject withVoided(Boolean voided) {
        this.voided = voided;
        return this;
    }

    public BaseDataObject withDateVoided(Date dateVoided) {
        this.dateVoided = dateVoided;
        return this;
    }


    public BaseDataObject withVoidReason(String voidReason) {
        this.voidReason = voidReason;
        return this;
    }

    public BaseDataObject withClientApplicationVersion(Integer clientApplicationVersion) {
        this.clientApplicationVersion = clientApplicationVersion;
        return this;
    }

    public BaseDataObject withClientDatabaseVersion(Integer clientDatabaseVersion) {
        this.clientDatabaseVersion = clientDatabaseVersion;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

