package cn.inkroom.cache.example.dao;

import cn.inkroom.cache.example.bean.Cache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 墨盒
 * @date 2019/10/26
 */
@Mapper
public interface CacheDao {


    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "'page_'+#page", sync = false)
    List<Cache> list(@Param("page") int page, @Param("size") int size);

    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "'page_con_'+#page", condition = "#params['page']==2")
    List<Cache> condition(@Param("page") int page, @Param("size") int size);

    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "'page_conRv_'+#page", condition = "#params['page']==32 && #rv.size() == 6")
    List<Cache> conditionRv(@Param("page") int page, @Param("size") int size);
}
