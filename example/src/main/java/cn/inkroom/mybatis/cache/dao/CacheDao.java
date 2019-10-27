package cn.inkroom.mybatis.cache.dao;

import cn.inkroom.mybatis.cache.bean.Cache;
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
    @cn.inkroom.mybatis.cache.annotation.Cache(key = "'page='+page+' - '+#context")
    List<Cache> list(@Param("page") int page, @Param("size") int size) throws Exception;

}
