package com.chengzhi.mybaits.code_gen.plugin;

import com.chengzhi.utils.SecurityIDUtil;

import java.util.Date;

public abstract class LavaDto extends LavaBaseModel {

    private String securityId;

    private Date gmtCreate;

    private String creator;

    private Date gmtModified;

    private String modifier;

    public void setId(Long id) {
        if (id != null) {
            this.securityId = SecurityIDUtil.encryptId(id);
        }
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

}
