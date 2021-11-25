package com.chengzhi.mybaits.code_gen.plugin;

import java.util.Date;

public abstract class LavaDo extends LavaBaseModel {

    private Date gmtCreate;

    private String creator;

    private Date gmtModified;

    private String modifier;

    private String isDeleted;

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier == null ? null : modifier.trim();
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted == null ? null : isDeleted.trim();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder().append(getClass().getName()).append(",#").append(this.hashCode()).append(",id:").append(getId() == null ? "Null" : getId());
        return sb.toString();
    }

}
