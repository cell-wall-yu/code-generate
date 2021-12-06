package com.chengzhi.mybaits.code_gen.plugin;

import com.chengzhi.page.PageList;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于实体与DTO之间的转换
 */
public interface LavaMapStruct<E, T> {

    @Mapping(target = "gmtCreate", source = "gmtCreate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "gmtModified", source = "gmtModified", dateFormat = "yyyy-MM-dd HH:mm:ss")
    T convertToDto(E entity);

    E convertToDo(T dto);

    List<E> convertToListDo(List<? extends T> dtoList);

    default List<T> convertToListDto(List<? extends E> dtoList) {
        if (dtoList instanceof PageList) {
            PageList<?> pageList = (PageList<?>) dtoList;

            PageList<T> result = new PageList<T>(dtoList.size());
            result.setCurrentPage(pageList.getCurrentPage());
            result.setHasNext(pageList.getHasNext());
            result.setHasPre(pageList.getHasPre());
            result.setPageSize(pageList.getPageSize());
            result.setTotalPage(pageList.getTotalPage());
            result.setTotalSize(pageList.getTotalSize());

            for (E item : dtoList) {
                result.add(convertToDto(item));
            }
            return result;
        } else {
            List<T> result = new ArrayList<T>(dtoList.size());

            for (E item : dtoList) {
                result.add(convertToDto(item));
            }
            return result;
        }
    }

}
