package com.chengzhi.mybaits.code_gen.plugin;

public abstract class LavaExample {
    /**
     * 排序字段
     */
    protected String orderByClause;
    /**
     * 去重
     */
    protected boolean distinct;

    /**
     * 对应Do类
     *
     * @return
     */
    protected Class<? extends LavaDo> getDoClass() {
        return null;
    }

    /**
     * 设置排序字段
     *
     * @param orderByClause
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * 排序字段
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * 设置去重
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * 是否去重
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * 所属组织
     */
    @Deprecated
    public String getFullOrgPath() {
        return null;
    }

    /**
     * 所属用户
     */
    @Deprecated
    public String getOwner() {
        return null;
    }

}
