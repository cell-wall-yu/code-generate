package com.chengzhi.mybaits.code_gen.plugin;


import com.chengzhi.mybaits.code_gen.plugin.exception.LavaException;
import com.chengzhi.mybaits.code_gen.plugin.theaduser.ThreadUserGetter;
import com.chengzhi.page.PageList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 业务Bo默认实现增删改查方法，变更前后事件，Id加解密
 *
 * @param <D> 业务Do，与数据库打交道
 * @param <T> 业务Dto，接口传参使用
 * @param <M> 业务数据操作Mapper
 * @param <E> 业务数据操作Example
 */
public abstract class AbstractLavaBoImpl<D extends LavaDo, T extends LavaDto, M extends LavaMapper<D, E>, E extends LavaExample> implements LavaBo<D, T, E> {

    protected M mapper;

    protected Logger logger = LoggerFactory.getLogger(AbstractLavaBoImpl.class);

    public void setMapper(M mapper) {
        this.mapper = mapper;
    }


    private static ThreadUserGetter threadUserGetter;

    public static void setThreadUserGetter(ThreadUserGetter threadUserGetter) {
        AbstractLavaBoImpl.threadUserGetter = threadUserGetter;
    }

    private String getOperator() {
        if (threadUserGetter == null) {
            throw new LavaException("需要对 AbstractLavaBoImpl 设置 threadUserGetter");
        }
        if (threadUserGetter.getThreadLoginUser() == null) {
            return "system";
        } else {
            return threadUserGetter.getThreadLoginUser();
        }
    }

    public void insert(T dataObject) {
        D saveObject = dataObject.trans(getDoClass());
        long v;
        if (isValidDo(saveObject)) {
            String operator = getOperator();
            saveObject.setCreator(operator);
            saveObject.setGmtCreate(new Date(System.currentTimeMillis()));
            saveObject.setGmtModified(new Date(System.currentTimeMillis()));
            mapper.insertSelective(saveObject);

            // Id回写到Dto
            dataObject.setId(saveObject.getId());
        } else {
            throw new LavaException("Invalid do:" + dataObject.toString());
        }
    }

    @Override
    public int delete(Long id) {
        D record = null;
        try {
            record = getDoClass().newInstance();
            record.setId(id);
            record.setGmtModified(new Date(System.currentTimeMillis()));
            record.setModifier(getOperator());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapper.deleteByPrimaryKey(record);
    }

    protected int deleteByExample(E example) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("example", example);
        Map<String, Object> record = new HashMap<String, Object>();
        record.put("gmtModified", new Date(System.currentTimeMillis()));
        record.put("modifier", getOperator());
        record.put("isDeleted", "y");
        map.put("record", record);
        return mapper.updateByExampleSelective(map);
    }

    protected List<D> selectByExample(E example) {
        return mapper.selectByExample(example);
    }

    protected int countByExample(E example) {
        return mapper.countByExample(example);
    }

    @Override
    public T selectByPrimaryKey(Long id) {
        D data = mapper.selectByPrimaryKey(id);
        return convertToDto(data);
    }

    @Override
    public int update(T dataObject) {
        D saveObject = dataObject.trans(getDoClass());
        int v;
        if (isValidDo(saveObject)) {
            saveObject.setGmtModified(new Date(System.currentTimeMillis()));
            saveObject.setModifier(getOperator());
            v = mapper.updateByPrimaryKeySelective(saveObject);
        } else {
            throw new LavaException("Invalid do:" + dataObject.toString());
        }
        return v;
    }

    protected int updateByExample(D record, E example) {
        return mapper.updateByExampleSelective(record, example);
    }

    protected int updateByExample(Map<String, Object> map) {
        return mapper.updateByExampleSelective(map);
    }

    public boolean isValidDo(D dataObject) {
        return true;
    }

    protected T convertToDto(D data) {
        if (data == null) {
            return null;
        }
        return data.trans(getDtoClass());
    }

    protected List<T> convertToDtoList(List<? extends D> list) {

        if (list instanceof PageList) {
            PageList<?> pageList = (PageList<?>) list;

            PageList<T> result = new PageList<T>(list.size());
            result.setCurrentPage(pageList.getCurrentPage());
            result.setHasNext(pageList.getHasNext());
            result.setHasPre(pageList.getHasPre());
            result.setPageSize(pageList.getPageSize());
            result.setTotalPage(pageList.getTotalPage());
            result.setTotalSize(pageList.getTotalSize());

            for (D item : list) {
                result.add(convertToDto(item));
            }
            return result;
        }
        else {
            ArrayList<T> result = new ArrayList<T>(list.size());

            for (D item : list) {
                result.add(convertToDto(item));
            }
            return result;
        }
    }

    protected abstract Class<? extends D> getDoClass();

    protected abstract Class<T> getDtoClass();
}