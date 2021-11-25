package com.chengzhi.mybaits.code_gen.plugin.tran;

import com.chengzhi.mybaits.code_gen.plugin.LavaBaseModel;
import com.chengzhi.mybaits.code_gen.plugin.exception.LavaException;
import org.apache.commons.beanutils.BeanUtils;

public interface TransSupport {

    default <T extends LavaBaseModel> T trans(Class<T> clazz) {
        LavaBaseModel sourceModel = (LavaBaseModel) this;
        try {
            // 基础字段赋值
            T destModel = clazz.newInstance();
            BeanUtils.copyProperties(destModel, sourceModel);
            return destModel;
        } catch (Exception e) {
            throw new LavaException("Trans Model failed", e);
        }
    }

}