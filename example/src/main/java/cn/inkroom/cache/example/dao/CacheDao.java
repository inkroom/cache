package cn.inkroom.cache.example.dao;

import cn.inkroom.cache.example.bean.Cache;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author 墨盒
 * @date 2019/10/26
 */
@Mapper
public interface CacheDao {


    @Select("select * from cache where id=#{id}")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "id", nullable = true)
    List<Cache> listById(@Param("id") int id);


    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "page", sync = true)
    List<Cache> list(@Param("page") int page, @Param("size") int size);

    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "'page_con_'+page", condition = "params.page==2")
    List<Cache> condition(@Param("page") int page, @Param("size") int size);

    @Select("select * from cache")
    @ResultType(Cache.class)
    @cn.inkroom.cache.core.annotation.Cache(key = "'page_conRv_'+page", condition = "params.page==32 && count(rv) != 0")
    List<Cache> conditionRv(@Param("page") int page, @Param("size") int size);


    @Insert({"insert into cache (id,name,type,age) values (#{c.id},#{c.name},#{c.type},#{c.age})"})
    @cn.inkroom.cache.core.annotation.Cache(key = "c.age", del = true)
    int insert(@Param("c") Cache cache);

    @Select("select * from cache where id=#{id}")
    @cn.inkroom.cache.core.annotation.Cache(key = "id", nullable = true)
    Cache detail(@Param("id") int id);

    @Select("select * from cache ")
    @cn.inkroom.cache.core.annotation.Cache(key = "id", nullable = true)
    Cache detailNotOnlyOne(@Param("id") int id);
}
