package com.chengzhi.mybaits.code_gen.plugin;

public interface LavaBo<D extends LavaDo, T extends LavaDto, E extends LavaExample> {

    void insert(T dataObject);

    int delete(Long id);

    int update(T dataObject);

    T selectByPrimaryKey(Long id);
}
